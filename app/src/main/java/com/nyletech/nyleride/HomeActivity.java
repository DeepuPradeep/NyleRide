package com.nyletech.nyleride;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nyletech.nyleride.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {
	
	SharedPreferences sharedpreferences;
	SharedPreferences.Editor editor;
	
	String user_id, page;
	
	Location myLocation;
	AppLocationService appLocationService;
	LatLngBounds.Builder builder;
	
	GoogleMap map;
	MapView mapview;
//	Marker m;
	
	Double latitude = null, longitude = null, hlat, hlng;
	
	String Home_Api;
	String jsonResult;
	
	Button sethome;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		
		sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
		editor = sharedpreferences.edit();
		
		user_id = sharedpreferences.getString("empid", "");
		page = sharedpreferences.getString("page", "");
		
		sethome = (Button)findViewById(R.id.sethome);
		builder = new LatLngBounds.Builder();
		sethome.setVisibility(View.GONE);
		SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.homemap);
        mapFragment.getMapAsync(this);
        
        sethome.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		    	AlertDialog.Builder dlgAlert=new AlertDialog.Builder(HomeActivity.this);
        		dlgAlert.setTitle("Are you sure want to set this location as your HomeLocation?");
        		dlgAlert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				// TODO Auto-generated method stub
        				Home_Api = "http://api.trackervigil.com/employee/setcoords?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+user_id+"&lat="+hlat+"&lng="+hlng;
        		    	getJSON(Home_Api);
        			}
        		});
        		dlgAlert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				// TODO Auto-generated method stub
        			}
        		});
        		dlgAlert.setCancelable(true);
        		dlgAlert.create().show();
        		
		    }
		});
	}
	
	@Override
	public void onMapReady(GoogleMap googleMap) {
		// TODO Auto-generated method stub
		map = googleMap;
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        emp_location();
        
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                // gpsPosition contains the last position obtained
            	hlat = map.getCameraPosition().target.latitude;
            	hlng = map.getCameraPosition().target.longitude;
            	map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        });
	}
	
	private void getJSON(String url) {
        class GetJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(HomeActivity.this, "Please Wait...",null,true,true);
				loading.setCancelable(false);
            }
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
                loading.dismiss();
                try
                {
                    jsonResult=s;
                    extractJSON();
                }catch(NullPointerException e)
                {
                    e.printStackTrace();
                }
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
    }
	
    private void extractJSON(){
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONObject users = jsonObject.getJSONObject("success");
            String msg = users.getString("messsage");
            popupSucc(msg);

        } catch (Exception e) {
        	try {
        	JSONObject jsonObject = new JSONObject(jsonResult);
            JSONObject users = jsonObject.getJSONObject("error");
            String msg = users.getString("messsage");
            popup(msg);
        	}
        	catch (Exception ex) {
        		net_error();
        	}
        }
    }
    
    public void popupSucc(String msg){
    	AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
		dlgAlert.setTitle("Alert:");
		dlgAlert.setMessage(msg);
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent;
            	if(page.equals("pass"))
            	{
            		editor.putString("page", "");
            		editor.commit();
            		intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
            	}
            	else if(page.equals("trip"))
            	{
            		editor.putString("page", "");
            		editor.commit();
            		intent = new Intent(HomeActivity.this, TripSheetActivity.class);
            		startActivity(intent);
            		finish();
            	}
            	else if(page.equals("map"))
            	{
            		editor.putString("page", "");
            		editor.commit();
            		intent = new Intent(HomeActivity.this, MapActivity.class);
            		startActivity(intent);
            		finish();
            	}
			}
		});
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
    }
    
    public void popup(String msg){
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
    }
    
    public void net_error(){
    	AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
        dlgAlert.setTitle("Alert:");
        dlgAlert.setMessage("Check your internet connection");
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
	
	public void emp_location()
    {
    	try {
        	appLocationService = new AppLocationService(HomeActivity.this);
        	Location gpsLocation = appLocationService.getLocation(LocationManager.GPS_PROVIDER);

			if (gpsLocation != null) {
				latitude = gpsLocation.getLatitude();
				longitude = gpsLocation.getLongitude();
				myLocation = gpsLocation;
				/*
				if(m != null)
					m.remove();
				m = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.mapperson)));
				*/
				
				builder.include(new LatLng(latitude, longitude));
				sethome.setVisibility(View.VISIBLE);
				CameraPosition camPos = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(13).build();
	    		CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
	    		map.moveCamera(camUpd3);
			}
			else {
				try {
		        	Location nwLocation = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);

					if (nwLocation != null) {
						latitude = nwLocation.getLatitude();
						longitude = nwLocation.getLongitude();
						myLocation = nwLocation;
						/*
						if(m != null)
							m.remove();
						m = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.mapperson)));
						*/
						builder.include(new LatLng(latitude, longitude));
						sethome.setVisibility(View.VISIBLE);
						CameraPosition camPos = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(13).build();
			    		CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
			    		map.moveCamera(camUpd3);
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
				/*
				if(m != null)
					m.remove();
				m = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.mapperson)));
				builder.include(m.getPosition());
				LatLngBounds bounds = builder.build();
				*/
				sethome.setVisibility(View.VISIBLE);
				builder.include(new LatLng(latitude, longitude));
	    		CameraPosition camPos = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(13).build();
	    		CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
	    		map.moveCamera(camUpd3);
			} else {
				showSettingsAlert();
			}
        	}
        	catch(Exception exc) {
        		showSettingsAlert();
        	}
        }
    }
    
    public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);

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
        	Intent intent = null;
        	if(page.equals("pass"))
        	{
        		intent = new Intent(HomeActivity.this, LoginActivity.class);
        		startActivity(intent);
        		finish();
        	}
        	else if(page.equals("trip"))
        	{
        		intent = new Intent(HomeActivity.this, TripSheetActivity.class);
        		startActivity(intent);
        		finish();
        	}
        	else if(page.equals("map"))
        	{
        		intent = new Intent(HomeActivity.this, MapActivity.class);
        		startActivity(intent);
        		finish();
        	}
        	else
        	{
        		intent = new Intent(HomeActivity.this, LoginActivity.class);
        		startActivity(intent);
        		finish();
        	}
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
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
	
}
