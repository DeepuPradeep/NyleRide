package com.nyletech.nyleride;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nyletech.nyleride.location.GooglePlayServiceLocation;

public class TripSheetActivity extends Activity {
	
	// menu variables
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	int isdata = 0;
	// menu variables

	SharedPreferences sharedpreferences;
	SharedPreferences.Editor editor;

	public String employeeId, gender, belongsto, page;
	private String JSON_URL;

	String message;

	Button menu, panicbutton, map, safehome, emergencycall;
	TextView ts_verify_msg, shortlink;
	LinearLayout driverNoLayout;
	EditText id, name, date, rtono, routeno, pdtime, triptype, driverno, etcno;
	GridLayout menugrid, buttons, ts_verify, hidebtns, hide2;
	RelativeLayout maingrid;

	Boolean panicPressed = false;
	Boolean safePressed = false;
	Boolean callPressed = false;

	public String myJSONString;


	private JSONArray users = null;
	private JSONObject error=null;
	private JSONArray safe = null;
	private JSONArray panic = null;

	//private static final String Message_Error="messsage";

	//Panic
	private String Emp_Id_Panic;
	private String Emp_trip_id;
	private String Emp_Name_Panic;
	private String Date_Panic;
	private String RTO_No_Panic;
	private String Route_No_Panic;
	private String PD_Time_Panic;
	private String Trip_Type_Panic;
	private String Driver_No_Panic;
	private String ETC_No_Panic;
	private String Driver_Id;
	private String Emergency_Panic;
	private String Short_Link_Panic;

	String call;

	public static Location location;

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {

			case 123:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					Intent my_callIntent = new Intent(Intent.ACTION_CALL);
					my_callIntent.setData(Uri.parse("tel:" + call));
					//here the word 'tel' is important for making a call...
					startActivity(my_callIntent);
				} else {
					Toast.makeText(this, "Need Call Permitission!", Toast.LENGTH_SHORT).show();
					if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
					{
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 123);
					}
				}
				break;

			default:
				break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip_sheet);
		
		menuready(savedInstanceState);
		statusCheck();

		new GooglePlayServiceLocation(TripSheetActivity.this).connect();
		
		sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
		editor = sharedpreferences.edit();
		employeeId = sharedpreferences.getString("empid", "");
		gender = sharedpreferences.getString("empGender", "");
		belongsto = sharedpreferences.getString("empBelongsto", "");
		page = sharedpreferences.getString("page", "");
		
		JSON_URL = "http://api.trackervigil.com/employee/trip?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+employeeId;
		message="No trip data available for Student Id: " + employeeId;
		
		menu = (Button)findViewById(R.id.menu);
		panicbutton = (Button)findViewById(R.id.panicbutton);
		map = (Button)findViewById(R.id.mapbutton);
		safehome = (Button)findViewById(R.id.safehome);
		emergencycall = (Button)findViewById(R.id.emergencycontact);
		id = (EditText)findViewById(R.id.id);
		name = (EditText)findViewById(R.id.name);
		date = (EditText)findViewById(R.id.date);
		rtono = (EditText)findViewById(R.id.rtoNo);
		routeno = (EditText)findViewById(R.id.routeNo);
		pdtime = (EditText)findViewById(R.id.pdTime);
		triptype = (EditText)findViewById(R.id.tripType);
		driverno = (EditText)findViewById(R.id.driverNo);
		etcno = (EditText)findViewById(R.id.etc);
		shortlink = (TextView)findViewById(R.id.shortlink);
		ts_verify_msg = (TextView)findViewById(R.id.ts_verify_msg);

		driverNoLayout = (LinearLayout) findViewById(R.id.driverNoLayout);
		
		menugrid = (GridLayout)findViewById(R.id.menugrid);
		buttons = (GridLayout)findViewById(R.id.buttons);
		ts_verify = (GridLayout)findViewById(R.id.ts_verify);
		hidebtns = (GridLayout)findViewById(R.id.hidebtns);
		hide2 = (GridLayout)findViewById(R.id.hide2);
		maingrid = (RelativeLayout)findViewById(R.id.maingrid);

		if(gender.toLowerCase().equals("f") && belongsto.toLowerCase().equals("mcafee") || belongsto.toLowerCase().equals("caterpillar")) {
			driverNoLayout.setVisibility(View.GONE);
		}
		if(gender.toLowerCase().equals("m")) {
			safehome.setEnabled(false);
			safehome.setAlpha(0.15f);
		}
		
		getJSON(JSON_URL);
		
		menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menugrid.setVisibility(View.VISIBLE);
		        mDrawerLayout.openDrawer(mDrawerList);
		        if(isdata == 1)
            		buttons.setVisibility(View.GONE);
			}
		});
		
		shortlink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				String smsText =
						"Track "+Emp_Name_Panic+"\n"+
						"Emp ID: "+employeeId+"\n"+
						"Trip Date: "+Date_Panic+"\n"+
						"Trip Time: "+PD_Time_Panic+"\n"+
						"Vehicle No: "+RTO_No_Panic;

				if((!gender.toLowerCase().equals("f") || !belongsto.toLowerCase().equals("mcafee")) && !belongsto.toLowerCase().equals("caterpillar")) {
					smsText = smsText + "\n" + "Driver Mobile: "+Driver_No_Panic;
				}

				if(!Short_Link_Panic.equals("") && !Short_Link_Panic.equals("null"))
				{
					smsText = smsText + "\n" + "ShortLink to track "+Emp_Name_Panic+": "+Short_Link_Panic;
				}

				sendIntent.putExtra("sms_body", smsText);

				sendIntent.setType("vnd.android-dir/mms-sms");
				startActivity(sendIntent);
			}
		});		
		
		map.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isdata == 1)
				{
		        Intent intent = new Intent(TripSheetActivity.this, MapActivity.class);
		        startActivity(intent);
		        finish();
				}
				else
				{
					AlertDialog.Builder dlgAlert=new AlertDialog.Builder(TripSheetActivity.this);
			        dlgAlert.setTitle("Alert:");
			        dlgAlert.setMessage("Trip Sheet not available");
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
		});
		/*
		safehome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        String PANIC_API = "http://api.trackervigil.com/employee/safehome?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+employeeId;
		        getJSON(PANIC_API);
			}
		});
		*/
		/*
		panicbutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        String PANIC_API = "http://api.trackervigil.com/employee/panic?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+employeeId;
		        getJSON(PANIC_API);
			}
		});
		*/
		
		emergencycall.setOnTouchListener(new View.OnTouchListener(){
			
			private final Handler handler = new Handler();
            private final Runnable runnable = new Runnable() {
                public void run() {
                	if(callPressed)
                    {
                   	 String calls[] = Emergency_Panic.split(",");
                   	 if(calls.length > 1)
                   	 {
                   		 AlertDialog.Builder dlgAlert=new AlertDialog.Builder(TripSheetActivity.this);
                     		dlgAlert.setTitle("whom do you want to call?");
                     		dlgAlert.setPositiveButton("HelpDesk", new DialogInterface.OnClickListener() {
                     			@Override
                     			public void onClick(DialogInterface dialog, int which) {
                     				// TODO Auto-generated method stub
                     				String calls[] = Emergency_Panic.split(",");
                     				call = calls[0];
                     				try
                     				{
                     					
                     					int permissionCheck = ContextCompat.checkSelfPermission(TripSheetActivity.this, Manifest.permission.CALL_PHONE);

                     			        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                     			            ActivityCompat.requestPermissions(
                     			                    TripSheetActivity.this,
                     			                    new String[]{Manifest.permission.CALL_PHONE},
                     			                    123);
                     			        }
                     			        else
                     			        {
                     			        	Intent my_callIntent = new Intent(Intent.ACTION_CALL);
                                 		    my_callIntent.setData(Uri.parse("tel:" + calls[0]));
                                 		    //here the word 'tel' is important for making a call...
                                 		    startActivity(my_callIntent);
                     			        }
                     			        
                             		 } catch (ActivityNotFoundException e) {
                             		    Toast.makeText(getApplicationContext(), "Error in your phone call"+e.getMessage(), Toast.LENGTH_LONG).show();
                             		 }
                     			}
                     		});
                     		dlgAlert.setNegativeButton("Admin", new DialogInterface.OnClickListener() {
                     			@Override
                     			public void onClick(DialogInterface dialog, int which) {
                     				// TODO Auto-generated method stub
                     				String calls[] = Emergency_Panic.split(",");
                     				call = calls[1];
                     				try {
                     					
                     					int permissionCheck = ContextCompat.checkSelfPermission(TripSheetActivity.this, Manifest.permission.CALL_PHONE);

                     			        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                     			            ActivityCompat.requestPermissions(
                     			                    TripSheetActivity.this,
                     			                    new String[]{Manifest.permission.CALL_PHONE},
                     			                    123);
                     			        }
                     			        else
                     			        {
                     			        	Intent my_callIntent = new Intent(Intent.ACTION_CALL);
                                 		    my_callIntent.setData(Uri.parse("tel:" + calls[1]));
                                 		    //here the word 'tel' is important for making a call...
                                 		    startActivity(my_callIntent);
                     			        }
                     			        
                             		    
                             		 } catch (ActivityNotFoundException e) {
                             		    Toast.makeText(getApplicationContext(), "Error in your phone call"+e.getMessage(), Toast.LENGTH_LONG).show();
                             		 }
                     			}
                     		});
                     		dlgAlert.setCancelable(true);
                     		dlgAlert.create().show();
                   	 }
                   	 else
                   	 {
                   		 Intent my_callIntent = new Intent(Intent.ACTION_CALL);
                 		    my_callIntent.setData(Uri.parse("tel:" + calls[0]));
                 		    //here the word 'tel' is important for making a call...
                 		    startActivity(my_callIntent);
                   	 }
                     }
                }
            };
            
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_DOWN) {
		        	handler.postDelayed(runnable, 2000);    
                    callPressed = true;
		        }
		        if(event.getAction()==MotionEvent.ACTION_UP){
		        	if(callPressed) {
                        callPressed = false;
                        handler.removeCallbacks(runnable);
                    }
		        }
		        return false;
		    }
		});
		
		safehome.setOnTouchListener(new View.OnTouchListener() {
			
			private final Handler handler = new Handler();
            private final Runnable runnable = new Runnable() {
                public void run() {
                     if(safePressed)
                     {
						 location = new GooglePlayServiceLocation(TripSheetActivity.this).getLocation();
						 if(location != null) {
							 //Toast.makeText(TripSheetActivity.this, "lat: " + location.getLatitude() + ", lng: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
							 String SAFE_API = null;
							 try {
								 SAFE_API = "http://api.trackervigil.com/employee/safehomefortrip?api_key=d807527d60c7e620e968aef5f39599a3&emp_id=" + URLEncoder.encode(employeeId, "UTF-8") + "&trip_id=" + URLEncoder.encode(Emp_trip_id, "UTF-8") + "&lat=" + URLEncoder.encode(location.getLatitude()+"", "UTF-8") + "&lng=" + URLEncoder.encode(location.getLongitude()+"", "UTF-8");
							 } catch (UnsupportedEncodingException e) {
								 e.printStackTrace();
								 Toast.makeText(TripSheetActivity.this, "Encoding of parameter values failed..", Toast.LENGTH_SHORT).show();
							 }
							 //String SAFE_API = "http://api.trackervigil.com/employee/safehomefortrip?api_key=d807527d60c7e620e968aef5f39599a3&emp_id=v001&trip_id=NL-07-10-2017-LI-04:00-MTP-0011d&lat=17.416076&lng=78.448070;
							 getJSON(SAFE_API);
						 } else {
							 Toast.makeText(TripSheetActivity.this, "Unable to get your location. Enable your location and try again.", Toast.LENGTH_SHORT).show();
						 }
                     }
                }
            };
            
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_DOWN) {
		        	handler.postDelayed(runnable, 2000);    
                    safePressed = true;
		        }
		        if(event.getAction()==MotionEvent.ACTION_UP){
		        	if(safePressed) {
                        safePressed = false;
                        handler.removeCallbacks(runnable);
                    }
		        }
		        return false;
		    }
		});
		
		panicbutton.setOnTouchListener(new View.OnTouchListener(){
			
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
		
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
		

        version_check();
		
	}
	
	public void version_check()
	{
		new AsyncTask<Integer, Void, String>(){
			
			AlertDialog.Builder builder;
			
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				builder = new AlertDialog.Builder(TripSheetActivity.this);
			}
			
	        @Override
	        protected String doInBackground(Integer... params) {
	        	try {
	            String currentVersion = getCurrentVersion();
	        	String latestVersion = "";
	            
	          //It retrieves the latest version by scraping the content of current version from play store at runtime
		        String urlOfAppFromPlayStore = "https://play.google.com/store/apps/details?id=com.nyletech.nyleride";
		        org.jsoup.nodes.Document  doc = Jsoup.connect(urlOfAppFromPlayStore).get();
		        latestVersion = doc.getElementsByAttributeValue("itemprop","softwareVersion").first().text();
	        	
		      //If the versions are not the same
		        if(!currentVersion.equals(latestVersion))
		        {
		            return "update";
		        }
	        	} catch (Exception e) {
	        		
	        	}
	            return "no";
	        }
	        
	        @Override
	        protected void onPostExecute(String result) {
	               super.onPostExecute(result);
	               if(result.equals("update"))
	               {
	            	   builder.setTitle("An Update is Available");
			            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			                @Override
			                public void onClick(DialogInterface dialog, int which) {
			                    //Click button action
			                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nyletech.nyleride")));
			                    dialog.dismiss();
			                }
			            });

			            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			                @Override
			                public void onClick(DialogInterface dialog, int which) {
			                    //Cancel button action
			                }
			            });

			            builder.setCancelable(false);
			            builder.show();
	               }
	        }
	    }.execute(1);
	}
	
	public void statusCheck()
    {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();

        }
    }
	
	private String getCurrentVersion(){
		PackageManager pm = this.getPackageManager();
		PackageInfo pInfo = null;

		        try {
		            pInfo =  pm.getPackageInfo(this.getPackageName(),0);

		        } catch (PackageManager.NameNotFoundException e1) {
		            e1.printStackTrace();
		        }
		        String currentVersion = pInfo.versionName;

		        return currentVersion;
		    }
	
     private void buildAlertMessageNoGps() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, please enable it?")
                   .setCancelable(false)
                   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       public void onClick(final DialogInterface dialog,  final int id) {
                           startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                       }
                   })
                   .setNegativeButton("No", new DialogInterface.OnClickListener() {
                       public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                       }
                   });
            final AlertDialog alert = builder.create();
            alert.show();

        }
	
	private void extractJSON(){
        try {
            JSONObject jsonObject = new JSONObject(myJSONString);
            users = jsonObject.getJSONArray("trip");
            showData();
        } catch (JSONException e) {
        	try {
        		JSONObject jsonObject = new JSONObject(myJSONString);
            	error = jsonObject.getJSONObject("error");
				String msg = error.getString("messsage");
				if(msg.contains("safe home")) {
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
				} else {
					popup();
				}
        	} catch (JSONException ex) {
        		try {
                	JSONObject jsonObject = new JSONObject(myJSONString);
                    safe = jsonObject.getJSONArray("safehome");
                    safe();
                	} catch (JSONException exc) {
                		try {
                        	JSONObject jsonObject = new JSONObject(myJSONString);
                            panic = jsonObject.getJSONArray("soft_panic");
                            panic();
                        	} catch (JSONException exce) {
                        		net_error();
                        	}
                	}
        	}
        }
    }

    private void getJSON(String url) {
        class GetJSON extends AsyncTask<String, Void, String>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TripSheetActivity.this, "Please Wait...",null,true,true);
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
                
                // already there txtviewEmpid.setText(s);
                myJSONString=s;
                extractJSON();
                loading.dismiss();
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
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
    
    public void safe(){
    	JSONObject jsonObject_Panic;
    	if(safe != null)
    	{
		try {
			jsonObject_Panic = safe.getJSONObject(0);
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
	        dlgAlert.setTitle("Alert:");
	        dlgAlert.setMessage("You have already pressed SafeHome button");
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
	        dlgAlert.setMessage("You have already pressed SafeHome button");
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
    
    public void popup(){
    	isdata = 0;
    	buttons.setVisibility(View.GONE);
    	hidebtns.setVisibility(View.VISIBLE);
    	ts_verify.setVisibility(View.VISIBLE);
    	ts_verify_msg.setText(message);
    	panicbutton.setClickable(false);
    	map.setClickable(false);
    	safehome.setClickable(false);
    	emergencycall.setClickable(false);
    	/*
        AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
        dlgAlert.setTitle("Alert:");
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        */
    }
    
    private void showData(){
    	isdata = 1;
    	buttons.setVisibility(View.VISIBLE);
    	hidebtns.setVisibility(View.INVISIBLE);
    	ts_verify.setVisibility(View.GONE);
    	panicbutton.setClickable(true);
    	map.setClickable(true);
    	safehome.setClickable(true);
    	emergencycall.setClickable(true);
        try {
            JSONObject jsonObject = users.getJSONObject(0);

            try{
				Emp_trip_id = jsonObject.getString("trip_id");
            	Emp_Name_Panic = jsonObject.getString("emp_name");
            	Date_Panic =jsonObject.getString("trip_date");
            	RTO_No_Panic =jsonObject.getString("vehicle_no");
            	Route_No_Panic = jsonObject.getString("cab_id");
            	PD_Time_Panic = jsonObject.getString("trip_time");
            	Driver_No_Panic = jsonObject.getString("driver_mob_no");
            	Driver_Id = jsonObject.getString("driver_id");
            	ETC_No_Panic = jsonObject.getString("ern_no");
            	Trip_Type_Panic = jsonObject.getString("trip_type");
            	Short_Link_Panic = jsonObject.getString("shortlink");
            	Emergency_Panic = jsonObject.getString("emergency");
            	Short_Link_Panic = Short_Link_Panic.replace("\\", "");
           	
            	if(Trip_Type_Panic.equals("P") || Trip_Type_Panic.equals("p"))
            	{
            		safehome.setEnabled(false);
            		safehome.setAlpha(0.15f);
            	}
            	
            	if(Emergency_Panic.equals("") || Emergency_Panic.equals("null"))
            	{
            		emergencycall.setEnabled(false);
            		emergencycall.setAlpha(0.15f);
            	}
            	
            	editor.putString("empname", Emp_Name_Panic);
            	editor.putString("driver", Driver_Id);
        		editor.commit();
           	
            	String[] d = Date_Panic.split("-");
            	Date_Panic = d[2]+"-"+d[1]+"-"+d[0];
            	
            	id.setText(employeeId);
            	name.setText(Emp_Name_Panic);
            	date.setText(Date_Panic);
                rtono.setText(RTO_No_Panic);
                routeno.setText(Route_No_Panic);
                pdtime.setText(PD_Time_Panic);
                driverno.setText(Driver_No_Panic);
                etcno.setText(ETC_No_Panic);
                triptype.setText(Trip_Type_Panic);
            }
            catch(Exception e){}
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //onBackPressed();
        	Intent intent;
        	if(page.equals("map"))
        	{
        		intent = new Intent(TripSheetActivity.this, MapActivity.class);
                startActivity(intent);
                finish();
        	}
        	else
        	{
        		AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
        		dlgAlert.setTitle("Are you sure to exit?");
        		dlgAlert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				// TODO Auto-generated method stub
        				moveTaskToBack(false);
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
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    
    public void menuready(Bundle savedInstanceState)
	{
		mTitle = mDrawerTitle = getTitle();
		 
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
 
        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
 
        navDrawerItems = new ArrayList<NavDrawerItem>();
 
        // adding nav drawer items to array
        // Menu Title
        navDrawerItems.add(new NavDrawerItem(navMenuIcons.getResourceId(0, -1)));
        // Trip Sheet
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(Integer.parseInt("1"), -1)));
        // Map
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(Integer.parseInt("2"), -1)));
        // Change Password
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(Integer.parseInt("3"), -1)));
        // Change Home Location
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(Integer.parseInt("4"), -1)));
        // Driver Rating
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(Integer.parseInt("5"), -1)));
        // Logout
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(Integer.parseInt("6"), -1)));

        // Recycle the typed array
        navMenuIcons.recycle();
 
        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
 
        // enabling action bar app icon and behaving it as toggle button
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);
 
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.axa, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
            	menugrid.setVisibility(View.GONE);
            	if(isdata == 1)
            		buttons.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
            	if(isdata == 1)
            		buttons.setVisibility(View.GONE);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
 
        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
 
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
 
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    private class SlideMenuClickListener implements
    ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}
	
	private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        Intent intent;
        switch (position) {
        case 1:
            intent = new Intent(TripSheetActivity.this, TripSheetActivity.class);
        	startActivity(intent);
        	finish();
            break;
        case 2: // Map
        	if(isdata == 1)
        	{
        	editor.putString("page", "trip");
        	editor.commit();
            intent = new Intent(TripSheetActivity.this, MapActivity.class);
        	startActivity(intent);
        	finish();
        	}
        	else
        	{
        		AlertDialog.Builder dlgAlert=new AlertDialog.Builder(TripSheetActivity.this);
		        dlgAlert.setTitle("Alert:");
		        dlgAlert.setMessage("Trip Sheet not available");
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
            break;
        case 3: // Change Password
        	editor.putString("page", "trip");
        	editor.commit();
            intent = new Intent(TripSheetActivity.this, PasswordActivity.class);
        	startActivity(intent);
        	finish();
            break;
        case 4: // Change Home Location
        	editor.putString("page", "trip");
        	editor.commit();
            intent = new Intent(TripSheetActivity.this, HomeActivity.class);
        	startActivity(intent);
        	finish();
            break;
            
        case 5: // Driver Rating
        	String driverId = sharedpreferences.getString("driver", "");
        	if(driverId.equals("") || driverId.equals("null"))
        	{
        		AlertDialog.Builder dlgAlert=new AlertDialog.Builder(TripSheetActivity.this);
		        dlgAlert.setTitle("Alert:");
		        dlgAlert.setMessage("No Driver Data Available");
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
        	else
        	{
        		editor.putString("page", "rate");
            	editor.commit();
                intent = new Intent(TripSheetActivity.this, DriverRatingActivity.class);
            	startActivity(intent);
            	finish();
        	}
            break;
            
        case 6: // Logout
        	editor.putString("empid", "");
    		editor.putString("emppass", "");
    		editor.putString("page", "");
    		editor.commit();
            intent = new Intent(TripSheetActivity.this, LoginActivity.class);
        	startActivity(intent);
        	finish();
            break;
        default:
            break;
        }
 
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();
 
            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            //setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trip_sheet, menu);
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
