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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class PasswordActivity extends Activity {
	
	SharedPreferences sharedpreferences;
	SharedPreferences.Editor editor;
	
	String user_id, page;
	
	EditText pass1, pass2;
	Button submit;
	
	String myJSONString;
	JSONObject jsonObject;
	
//	ImageView cover01, cover1, cover02, cover2, cover03, cover3, cover04, cover4;
	
	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);
		
		sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
		editor = sharedpreferences.edit();
		
		user_id = sharedpreferences.getString("empid", "");
		page = sharedpreferences.getString("page", "");
		
		pass1 = (EditText)findViewById(R.id.pass1);
		pass2 = (EditText)findViewById(R.id.pass2);
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
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String empPass1 = pass1.getText().toString();
				String empPass2 = pass2.getText().toString();
				
				if(!(empPass1.equals(empPass2)))
		        {
		        	pass1.setError(Html.fromHtml("<font color='red'>Mismatch of password</font>"));
		            pass2.setError(Html.fromHtml("<font color='red'>Mismatch of password</font>"));
		        }
		        else if(pass1.length()<8 || pass2.length()<8 ){
		        	pass1.setError(Html.fromHtml("<font color='red'>Password length should be atleast 8</font>")); 
		            pass2.setError(Html.fromHtml("<font color='red'>Password length should be atleast 8</font>"));
		        }
		        else
		        {
		        	String jsonUrl;
					try {
						jsonUrl = "http://api.trackervigil.com/employee/setpassword?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+user_id+"&password="+URLEncoder.encode(empPass1,"UTF-8");
						getJSON(jsonUrl);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
			}
		});
		
	}
	
	private void getJSON(String url) {
        class GetJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(PasswordActivity.this, "Please Wait...",null,true,true);
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
                // already there txtviewEmpid.setText(s);
                try
                {
                    myJSONString=s;
                    extractJSON();
                    //showData();
                    //popupSucc();
                }catch(NullPointerException e)
                {
                    e.printStackTrace();
                    //popup();
                }// edttxtRouteNo.setText(s);
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute(url);
    }
    
    private void extractJSON(){
        try {
            jsonObject = new JSONObject(myJSONString);

            //users = jsonObject.getJSONArray(Pwd_Results);
            //JSONObject jsonObject1 = users.getJSONObject(TRACK);
            //message=jsonObject1.getString(msg);
            JSONObject users = jsonObject.getJSONObject("success");
            String message = users.getString("messsage");
            //code1 = users.getString(code);
            popupSucc(message);
            //JSONObject parentObject = new JSONObject(json);
            //JSONObject userDetails = parentObject.getJSONObject("user_details");
        } catch (Exception e) {
            e.printStackTrace();
			try {
				JSONObject users = jsonObject.getJSONObject("error");
				String message = users.getString("messsage");
				popup(message);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
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
            	if(page.equals("otp"))
            	{
            		editor.putString("page", "pass");
            		editor.commit();
            		intent = new Intent(PasswordActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
            	}
            	else if(page.equals("trip"))
            	{
            		editor.putString("page", "");
            		editor.commit();
            		intent = new Intent(PasswordActivity.this, TripSheetActivity.class);
            		startActivity(intent);
            		finish();
            	}
            	else if(page.equals("map"))
            	{
            		editor.putString("page", "");
            		editor.commit();
            		intent = new Intent(PasswordActivity.this, MapActivity.class);
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
        	Intent intent = null;
        	if(page.equals("otp"))
        	{
        		intent = new Intent(PasswordActivity.this, LoginActivity.class);
        		startActivity(intent);
        		finish();
        	}
        	else if(page.equals("trip"))
        	{
        		intent = new Intent(PasswordActivity.this, TripSheetActivity.class);
        		startActivity(intent);
        		finish();
        	}
        	else if(page.equals("map"))
        	{
        		intent = new Intent(PasswordActivity.this, MapActivity.class);
        		startActivity(intent);
        		finish();
        	}
        	else
        	{
        		intent = new Intent(PasswordActivity.this, LoginActivity.class);
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
		getMenuInflater().inflate(R.menu.password, menu);
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
