package com.nyletech.nyleride;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONObject;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class LoginActivity extends Activity implements OnLoadCompleteListener {
	
	SharedPreferences sharedpreferences;
	SharedPreferences.Editor editor;
	RelativeLayout privacyPolicy;
	PDFView pdfView;
	CheckBox check;
	Button agree;
	LinearLayout loginLayout;
	String user_id, user_pass, gender, belongsto;
	EditText id, pass;
	Button login;
	TextView new_employee, forgot_password;
	String login_api;
	String jsonResult;
	
	int auto_login = 0;

	Integer pageNumber = 0;
	String pdfFileName;
	public static final String SAMPLE_FILE = "Privacy_Policy - Nyletech.pdf";
	
//	ImageView cover01, cover1, cover02, cover2, cover03, cover3, cover04, cover4;
	
	Handler handler = new Handler();
	
	static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
	static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	    switch (requestCode) {
	        case PERMISSION_ACCESS_COARSE_LOCATION:
	            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	                // All good!
	            	if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
	        		{
	        	        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 123);
	        		}
	            } else {
	                Toast.makeText(this, "please accept location permission to use the App!", Toast.LENGTH_SHORT).show();
	                if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
	        		        != PackageManager.PERMISSION_GRANTED) {
	        		    ActivityCompat.requestPermissions(this, new String[] { ACCESS_COARSE_LOCATION },
	        		            PERMISSION_ACCESS_COARSE_LOCATION);
	        		}
	            }
	            break;
	            
	        case 123:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                	// All good!
                } else {
                	Toast.makeText(this, "please accept Telephone permission to use the App!", Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.activity_login);
		
		if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
		    ActivityCompat.requestPermissions(this, new String[] { ACCESS_COARSE_LOCATION }, PERMISSION_ACCESS_COARSE_LOCATION);
		}
		//int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
		else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
		{
	        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 123);
		}

		privacyPolicy = (RelativeLayout) findViewById(R.id.privacyPolicyLayout);
		pdfView = (PDFView) findViewById(R.id.pdfView);
		check = (CheckBox) findViewById(R.id.checked);
		agree = (Button) findViewById(R.id.agreed);
		loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
		id = (EditText)findViewById(R.id.id);
		pass = (EditText)findViewById(R.id.pass);
		login = (Button)findViewById(R.id.login);
		new_employee = (TextView)findViewById(R.id.new_employee);
		forgot_password = (TextView)findViewById(R.id.forgot_password);

		displayFromAsset(SAMPLE_FILE);
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
		sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
		editor = sharedpreferences.edit();
		editor.putString("page", "");

		if(sharedpreferences.getBoolean("agreed", false)) {
			privacyPolicy.setVisibility(View.GONE);
			loginLayout.setVisibility(View.VISIBLE);
		}
		
		if(sharedpreferences.getString("empid", "").equals("") || sharedpreferences.getString("emppass", "").equals(""))
		{
			// DO NOTHING
		}
		else
		{
			auto_login = 1;
			String auto_login_api;
			try {
				auto_login_api = "http://api.trackervigil.com/employee/auth?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+sharedpreferences.getString("empid", "")+"&password="+URLEncoder.encode(sharedpreferences.getString("emppass", ""),"UTF-8");
				getJSON(auto_login_api);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		login.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	user_id = id.getText().toString();
		    	user_pass = pass.getText().toString();
		    	try {
					login_api = "http://api.trackervigil.com/employee/auth?api_key=d807527d60c7e620e968aef5f39599a3&emp_id="+URLEncoder.encode(user_id,"UTF-8")+"&password="+URLEncoder.encode(user_pass,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	auto_login = 0;
		    	
		    	
		    	login_api = login_api.replace("<id>", user_id);
		    	login_api = login_api.replace("<pass>", user_pass);
		    	getJSON(login_api);
		    }
		});
		
		new_employee.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
	    		editor.commit();
		    	Intent intent = new Intent(LoginActivity.this, EmpCheckActivity.class);
		        startActivity(intent);
		        finish();
		    }
		});
		
		forgot_password.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
	    		editor.commit();
		    	Intent intent = new Intent(LoginActivity.this, EmpCheckActivity.class);
		        startActivity(intent);
		        finish();
		    }
		});

		check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(isChecked) {
					agree.setEnabled(true);
				} else {
					agree.setEnabled(false);
				}
			}
		});

		agree.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				privacyPolicy.setVisibility(View.GONE);
				loginLayout.setVisibility(View.VISIBLE);
				editor.putBoolean("agreed", true);
				editor.commit();
			}
		});

	}
	
	private void getJSON(String url) {
        class GetJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "Please Wait...",null,true,true);
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
			gender = users.getString("gender");
			belongsto = users.getString("belongsto");
            popupSucc();

        } catch (Exception e) {
        	try {
        	JSONObject jsonObject = new JSONObject(jsonResult);
            JSONObject users = jsonObject.getJSONObject("error");
            popup();
        	}
        	catch (Exception ex) {
        		net_error();
        	}
        }
    }
    
    public void popupSucc(){
		editor.putString("empGender", gender);
		editor.putString("empBelongsto", belongsto);
    	if(auto_login == 0)
    	{
			editor.putString("empid", user_id);
			editor.putString("emppass", user_pass);
    	}
		editor.commit();
        Intent intent = new Intent(LoginActivity.this, TripSheetActivity.class);
        startActivity(intent);
        finish();
    }
    
    public void popup(){
    	if(auto_login == 0)
    	{
    		AlertDialog.Builder dlgAlert=new AlertDialog.Builder(this);
    		dlgAlert.setTitle("Alert:");
    		dlgAlert.setMessage("Invalid Employee Id or Password");
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
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

	private void displayFromAsset(String assetFileName) {
		pdfFileName = assetFileName;

		pdfView.fromAsset(SAMPLE_FILE)
				.defaultPage(pageNumber)
				.enableSwipe(true)
				.swipeHorizontal(false)
				.enableAnnotationRendering(true)
				.onLoad(this)
				.scrollHandle(new DefaultScrollHandle(this))
				.load();
	}


	@Override
	public void loadComplete(int nbPages) {
		PdfDocument.Meta meta = pdfView.getDocumentMeta();
		printBookmarksTree(pdfView.getTableOfContents(), "-");

	}

	public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
		for (PdfDocument.Bookmark b : tree) {

			Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

			if (b.hasChildren()) {
				printBookmarksTree(b.getChildren(), sep + "-");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
