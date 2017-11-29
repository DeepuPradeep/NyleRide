package com.nyletech.nyleride;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

public class DriverRatingActivity extends Activity {
	
	String myJSONString;
	String rating_api;
	JSONArray rate;
	Button submit;
	RatingBar vcondition, dconcern, rconcern;
	EditText oissues;
	
	String driverid;
	String empid;
	
	SharedPreferences sharedpreferences;
	SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driver_rating);
		
		submit = (Button)findViewById(R.id.submit);
		vcondition = (RatingBar)findViewById(R.id.vcondition);
		dconcern = (RatingBar)findViewById(R.id.dconcern);
		rconcern = (RatingBar)findViewById(R.id.rconcern);
		oissues = (EditText)findViewById(R.id.oissues);
		
		sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
		empid = sharedpreferences.getString("empid", "");
		driverid = sharedpreferences.getString("driver", "");
		
		oissues.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        @Override
	        public void onFocusChange(View v, boolean hasFocus) {
	            if (!hasFocus) {
	                hideKeyboard(v);
	            }
	        }
	    });
		
		submit.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	String dc = (int)dconcern.getRating()+"";
		    	String vc = (int)vcondition.getRating()+"";;
		    	String rc = (int)rconcern.getRating()+"";
		    	if(dc.equals("0") || vc.equals("0") || rc.equals("0"))
		    	{
		    		AlertDialog.Builder dlgAlert=new AlertDialog.Builder(DriverRatingActivity.this);
	    	        dlgAlert.setTitle("Alert:");
	    	        dlgAlert.setMessage("Please rate at the fields.");
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
		    		String oi = oissues.getText().toString();
		    		oi.replaceAll(" ", "%20");
		    		try {
						rating_api = "http://api.trackervigil.com/employee/rating?api_key=d807527d60c7e620e968aef5f39599a3&emp_id=" + empid + "&drv_id=" + driverid + "&driver=" + dc + "&vehicle=" + vc + "&route=" + rc + "&other=" + URLEncoder.encode(oi,"UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		String a = rating_api;
			    	getJSON(rating_api);
		    	}
		    }
		});
	}
	
	public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
	
	private void extractJSON(){
        try {
            JSONObject jsonObject = new JSONObject(myJSONString);
            rate = jsonObject.getJSONArray("ratedriver");
            rate();
        } catch (Exception e) {
        	net_error();
        }
    }

    private void getJSON(String url) {
        class GetJSON extends AsyncTask<String, Void, String>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(DriverRatingActivity.this, "Please Wait...",null,true,true);
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
                // already there txtviewEmpid.setText(s);
                myJSONString=s;
                extractJSON();
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
    }
    
    public void rate()
    {
    	try
    	{
    		JSONObject jsonObject = rate.getJSONObject(0);
    		try
    		{
    			String msg = jsonObject.getString("message");
    			AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
    	        dlgAlert.setTitle("Alert:");
    	        dlgAlert.setMessage(msg);
    	        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	            @Override
    	            public void onClick(DialogInterface dialog, int which) {
    	                // TODO Auto-generated method stub
    	                Intent intent = new Intent(DriverRatingActivity.this, TripSheetActivity.class);
    	                startActivity(intent);
    	            }
    	        });
    	        dlgAlert.setCancelable(true);
    	        dlgAlert.create().show();
    		}
    		catch(Exception e)
    		{
    			
    		}
    	}
    	catch(Exception ex)
    	{
    		
    	}
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
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //onBackPressed();
        	Intent intent = new Intent(DriverRatingActivity.this, TripSheetActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.driver_rating, menu);
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
