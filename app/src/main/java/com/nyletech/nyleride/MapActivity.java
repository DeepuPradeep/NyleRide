package com.nyletech.nyleride;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nyletech.nyleride.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
	
	TextView Distance, Time;
	
	SharedPreferences sharedpreferences;
	
	String employeeId, employeeName;
	String CAB_API, HOME_API;
	Location myLocation;
	
	AppLocationService appLocationService;
	
	GoogleMap map;
	MapView mapview;
	Marker v, e, h;
	Double Vlat, Vlng;
	LatLngBounds.Builder builder;
	
	String myJSONString;
	JSONObject jsonObject;
	JSONArray cab_location;
	JSONObject emp_home;
	
	int firsttime = 0;
	int rc = 1;
	
	JSONArray routeDT, legDT, panic;
	
	Button panicbtn;
	
	Boolean panicPressed = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		Distance = (TextView)findViewById(R.id.dis);
		Time = (TextView)findViewById(R.id.time);
		panicbtn = (Button)findViewById(R.id.panic);
		
		sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
		employeeId = sharedpreferences.getString("empid", "");
		employeeName = sharedpreferences.getString("empname", "");
		
		CAB_API = "http://api.trackervigil.com/employee/cablocation?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+employeeId;
		HOME_API = "http://api.trackervigil.com/employee/getcoords?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+employeeId;
		
		SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        builder = new LatLngBounds.Builder();
        
        panicbtn.setOnTouchListener(new View.OnTouchListener(){
			
			private final Handler handler = new Handler();
            private final Runnable runnable = new Runnable() {
                public void run() {
                     if(panicPressed)
                     {
                    	 String PANIC_API = "http://api.trackervigil.com/employee/panic?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+employeeId;
                    	 getJSON(PANIC_API);
                     }
                }
            };
            
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_DOWN) {
		        	handler.postDelayed(runnable, 2000);    
                    panicPressed = true;
		        }
		        if(event.getAction()==MotionEvent.ACTION_UP){
		        	if(panicPressed) {
                        panicPressed = false;
                        handler.removeCallbacks(runnable);
                    }
		        }
		        return false;
		    }
		});
	}
	
	@Override
	public void onMapReady(GoogleMap googleMap) {
		// TODO Auto-generated method stub
		map = googleMap;
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(12.956524, 77.611670), 7));
        
        getJSON(CAB_API);
	}
	
	private void getJSON(String url) {
        class GetJSON extends AsyncTask<String, Void, String>{
/*        	
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                if(lf == 1){
                loading = ProgressDialog.show(MapsDistanceActivity.this, "Please Wait...",null,true,true);
//                lf = 0; }
            }
*/            
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }
                    return sb.toString().trim();

                }catch(Exception e){
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                loading.dismiss();
                // already there txtviewEmpid.setText(s);
                
                    myJSONString=s;
                    if(s == null)
                		net_error();
                    else
                    	extractJSON();
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
    }
	
	public void net_error(){
		AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
        dlgAlert.setTitle("Error:");
        dlgAlert.setMessage("Please check your Internet connection");
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
                //startActivity(intent);
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
	}
    
    public void popup(){
        AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
        dlgAlert.setTitle("Alert:");
        dlgAlert.setMessage("Data not Available");
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                //startActivity(intent);
            }
        });
        
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
    
    private void extractJSON(){
        try {
        	jsonObject = new JSONObject(myJSONString);
            
            try {
            	cab_location = jsonObject.getJSONArray("cab_location");
            	cab_location();
            }
            catch(Exception ex){
            	try {
            		emp_home = jsonObject.getJSONObject("coordinates");
                	emp_home();
            	}
            	catch(Exception e) {
            		try {
                    	JSONObject jsonObject = new JSONObject(myJSONString);
                        panic = jsonObject.getJSONArray("soft_panic");
                        panic();
                    	} catch (Exception exc) {
                    		popup();
                    	}
            	}
            	
            }
            
        } catch (Exception e) {
        	popup();
        }
    }
    
    public void panic(){
    	JSONObject jsonObject_Panic;
    	if(panic != null)
    	{
		try {
			jsonObject_Panic = panic.getJSONObject(0);
			String msg = jsonObject_Panic.getString("message");
			AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
	        dlgAlert.setTitle("Alert:");
	        dlgAlert.setMessage(msg);
	        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // TODO Auto-generated method stub
	                //Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
	                //startActivity(intent);
	            }
	        });
	        dlgAlert.setCancelable(true);
	        dlgAlert.create().show();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
	        dlgAlert.setTitle("Alert:");
	        dlgAlert.setMessage("You have already pressed Panic button");
	        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // TODO Auto-generated method stub
	                //Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
	                //startActivity(intent);
	            }
	        });
	        dlgAlert.setCancelable(true);
	        dlgAlert.create().show();
		}
    	}
    	else
    	{
			AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
	        dlgAlert.setTitle("Alert:");
	        dlgAlert.setMessage("You have already pressed Panic button");
	        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // TODO Auto-generated method stub
	                //Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
	                //startActivity(intent);
	            }
	        });
	        dlgAlert.setCancelable(true);
	        dlgAlert.create().show();
    	}
    }
    
    public void cab_location()
    {
    	try {
            JSONObject jsonObject = cab_location.getJSONObject(0);

            try{
            	Vlat = Double.valueOf(jsonObject.getString("lat"));
                Vlng = Double.valueOf(jsonObject.getString("lng"));
                String v_name = jsonObject.getString("vehicle_no");
                String speed = jsonObject.getString("speed");
/*  
                if(Double.compare(Vlat, CVlat) == 1 || Double.compare(Vlng, CVlng) == 1)
                {
                	CVlat = Vlat;
                	CVlng = Vlng;
                }
            	VehicleMarker.position(new LatLng(Vlat, Vlng)).title("Your Vehicle!");
            	mMap.addMarker(VehicleMarker);
*/            	if(v != null)
					v.remove();
				v = map.addMarker(new MarkerOptions().position(new LatLng(Vlat, Vlng)).title(v_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapcar)));
				if(firsttime == 0)
					builder.include(v.getPosition());
				getJSON(HOME_API);
            }catch(Exception e){
            	Toast.makeText(this, "Can't show the vehicle position at this moment.", 1000*10).show();
            	getJSON(HOME_API);
            }
            
        } catch (JSONException e) {
        	Toast.makeText(this, "Can't show the vehicle position at this moment.", 1000*10).show();
        	getJSON(HOME_API);
        }
    }
    
    public void emp_home()
    {
    	try{
        	Double Vlat = Double.valueOf(emp_home.getString("lat"));
            Double Vlng = Double.valueOf(emp_home.getString("lng"));
            
            if(h != null)
				h.remove();
			h = map.addMarker(new MarkerOptions().position(new LatLng(Vlat, Vlng)).title("Home").icon(BitmapDescriptorFactory.fromResource(R.drawable.maphome)));
			if(firsttime == 0)
				builder.include(h.getPosition());
			emp_location();
        }catch(Exception e){
        	Toast.makeText(this, "Please set your home location to display in the map", 1000*10).show();
        	emp_location();
        }
    }
    
    public void emp_location()
    {
    	Double latitude = null, longitude = null;
    	try {
        	appLocationService = new AppLocationService(MapActivity.this);
        	Location gpsLocation = appLocationService.getLocation(LocationManager.GPS_PROVIDER);

			if (gpsLocation != null) {
				latitude = gpsLocation.getLatitude();
				longitude = gpsLocation.getLongitude();
				myLocation = gpsLocation;
				if(e != null)
					e.remove();
				e = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(employeeName).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapperson)));
				if(firsttime == 0)
					builder.include(e.getPosition());
			}
			else {
				try {
		        	Location nwLocation = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);

					if (nwLocation != null) {
						latitude = nwLocation.getLatitude();
						longitude = nwLocation.getLongitude();
						myLocation = nwLocation;
						if(e != null)
							e.remove();
						e = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.mapperson)));
						if(firsttime == 0)
							builder.include(e.getPosition());
					} else {
						showSettingsAlert();
					}
		        	}
		        	catch(Exception ex) {
		        		showSettingsAlert();
		        	}
			}
        }
        catch(Exception ex) {
        	try {
        	Location nwLocation = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);

			if (nwLocation != null) {
				latitude = nwLocation.getLatitude();
				longitude = nwLocation.getLongitude();
				myLocation = nwLocation;
				if(e != null)
					e.remove();
				e = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.mapperson)));
				if(firsttime == 0)
					builder.include(e.getPosition());
			} else {
				showSettingsAlert();
			}
        	}
        	catch(Exception exc) {
        		showSettingsAlert();
        	}
        }
    	if(firsttime == 0)
    	{
    		LatLngBounds bounds = builder.build();
    		CameraPosition camPos = new CameraPosition.Builder().target(bounds.getCenter()).zoom(11).build();
    		CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
    		map.animateCamera(camUpd3);
    		firsttime = 1;
    	}
    	DownloadTask dt = new DownloadTask();
        dt.execute("https://maps.googleapis.com/maps/api/directions/json?origin="+latitude+","+longitude+"&destination="+Vlat+","+Vlng+"&sensor=false");
    }
    
    public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapActivity.this);

		alertDialog.setTitle("GPS SETTINGS");

		alertDialog.setMessage("GPS is not enabled! Want to go to settings menu?");

		alertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				});

		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		alertDialog.show();
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //onBackPressed();
        	Intent intent = new Intent(MapActivity.this, TripSheetActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	
	
	
	
	private String getDirectionsUrl(){
   	 
        // Origin of route
        String str_origin = "origin="+myLocation.getLatitude()+","+myLocation.getLongitude();
 
        // Destination of route
        String str_dest = "destination=" + Vlat + "," + Vlng;
 
        // Sensor enabled
        String sensor = "sensor=false";
 
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
 
        // Output format
        String output = "json";
 
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
 
        return url;
    }
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
 
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
 
            // Connecting to url
            urlConnection.connect();
 
            // Reading data from url
            iStream = urlConnection.getInputStream();
 
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
 
            StringBuffer sb = new StringBuffer();
 
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
 
            data = sb.toString();

            br.close();
 
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
 
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{
 
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
 
            // For storing data from web service
            String data = "";
 
            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
 
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
 
            myJSONString = result;
            extractJSONDT();
            
            if(rc == 1){
            	ParserTask parserTask = new ParserTask();
            	parserTask.execute(result);
            	rc = 0;
            }
            else {

            	final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    	
                		getJSON(CAB_API);
                    }
                }, 1000*60);
            }
 
            // Invokes the thread for parsing the JSON data
            
        }
        
        
        private void extractJSONDT(){
            try {
                jsonObject = new JSONObject(myJSONString);
                
                try {
                	routeDT = jsonObject.getJSONArray("routes");
                	route_DT();
                }
                catch(Exception ex){
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        private void route_DT(){
            try {
                JSONObject jsonObject = routeDT.getJSONObject(0);
                legDT = jsonObject.getJSONArray("legs");
                JSONObject jsonObject1 = legDT.getJSONObject(0);
                JSONObject distance = jsonObject1.getJSONObject("distance");
                //JSONObject jsonD = distance.getJSONObject(0);
                JSONObject time = jsonObject1.getJSONObject("duration");
                //JSONObject jsonT = time.getJSONObject(0);

                try{
                	String Rdistance = distance.getString("text");
                	Distance.setText("Distance: " + Rdistance);
                	String Rtime = time.getString("text");
                	Time.setText("Est Time: " + Rtime);
                }catch(Exception e){}
                
            } catch (JSONException e) {}
            
             
        }
        
    }
 
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
 
        List<HashMap<String, String>> path = null;
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
 
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
 
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
 
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
 
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
 
                // Fetching i-th route
                if(path != null)
                	path.clear();
                path = result.get(i);
 
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
 
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
 
                    points.add(position);
                }
 
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(3);
                lineOptions.color(Color.BLUE);
            }
 
            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
            
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                	String jsonUrl_vehicle = "http://api.trackervigil.com/employee/cablocation?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+employeeId;
            		getJSON(jsonUrl_vehicle);
                }
            }, 1000*60);
            
            
        }
    }
	
}

