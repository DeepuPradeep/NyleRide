package com.nyletech.nyleride;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import com.nyletech.nyleride.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class EmpCheckActivity extends Activity {
	
	SharedPreferences sharedpreferences;
	SharedPreferences.Editor editor;
	
	EditText id;
	Button submit;
	
	String user_id;
	String login_api;
	String OTP_SEND;
	String jsonResult;
	
//	ImageView cover01, cover1, cover02, cover2, cover03, cover3, cover04, cover4;
	
	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emp_check);
		
		statusCheck();
		
		sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
		editor = sharedpreferences.edit();
		
		id = (EditText)findViewById(R.id.id);
		submit = (Button)findViewById(R.id.submit);
/*		
		cover01 = (ImageView)findViewById(R.id.cover01);
		cover1 = (ImageView)findViewById(R.id.cover1);
		cover02 = (ImageView)findViewById(R.id.cover02);
		cover2 = (ImageView)findViewById(R.id.cover2);
		cover03 = (ImageView)findViewById(R.id.cover03);
		cover3 = (ImageView)findViewById(R.id.cover3);
		cover04 = (ImageView)findViewById(R.id.cover04);
		cover4 = (ImageView)findViewById(R.id.cover4);
		
		Runnable runnable = new Runnable() {
            int i=1;
            public void run() {
            	if(i == 1)
            	{
            		cover01.setVisibility(View.GONE);
            		cover1.setVisibility(View.VISIBLE);
            		cover02.setVisibility(View.VISIBLE);
            		cover2.setVisibility(View.VISIBLE);
            		cover03.setVisibility(View.VISIBLE);
            		cover3.setVisibility(View.VISIBLE);
            		cover04.setVisibility(View.VISIBLE);
            		cover4.setVisibility(View.VISIBLE);
            	}
            	else if(i == 2)
            	{
            		cover1.setVisibility(View.GONE);
            	}
            	else if(i == 3)
            	{
            		cover02.setVisibility(View.GONE);
            	}
            	else if(i == 4)
            	{
            		cover2.setVisibility(View.GONE);
            	}
            	else if(i == 5)
            	{
            		cover03.setVisibility(View.GONE);
            	}
            	else if(i == 6)
            	{
            		cover3.setVisibility(View.GONE);
            	}
            	else if(i == 7)
            	{
            		cover04.setVisibility(View.GONE);
            	}
            	else if(i == 8)
            	{
            		cover4.setVisibility(View.GONE);
            		cover01.setVisibility(View.VISIBLE);
            		i = 0;
            	}
            	i++;
            	handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
*/		
		submit.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	user_id = id.getText().toString();
		    	try {
					login_api = "http://api.trackervigil.com/employee/auth?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+URLEncoder.encode(user_id,"UTF-8")+"&password=dee5ovojr94toj";
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    	OTP_SEND = "http://api.trackervigil.com/employee/otp?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+user_id+"&request=true";
		    	getJSON(login_api);
		    }
		});
		
	}
	
	public void statusCheck()
    {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();

        }


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
	
	private void getJSON(String url) {
        class GetJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(EmpCheckActivity.this, "Please Wait...",null,true,true);
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
            otpmsg(msg);

        } catch (Exception e) {
        	try {
        	JSONObject jsonObject = new JSONObject(jsonResult);
            JSONObject users = jsonObject.getJSONObject("error");
            String msg = users.getString("messsage");
            if(msg.equals("Login failed!"))
            	popupSucc();
            else
            	popup();
        	}
        	catch (Exception ex) {
        		net_error();
        	}
        }
    }
    
    public void otpmsg(String msg)
    {
    	AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
        dlgAlert.setTitle("Alert:");
        dlgAlert.setMessage(msg);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            	editor.putString("empid", user_id);
	    		editor.commit();
                Intent intent = new Intent(EmpCheckActivity.this, OtpActivity.class);
                startActivity(intent);
                finish();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
    
    public void popupSucc(){
    	getJSON(OTP_SEND);
    }
    
    public void popup(){
        AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
        dlgAlert.setTitle("Alert:");
        dlgAlert.setMessage("Invalid Employee Id");
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
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //onBackPressed();
        	Intent intent = new Intent(EmpCheckActivity.this, LoginActivity.class);
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
		getMenuInflater().inflate(R.menu.emp_check, menu);
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
