package com.sightcall.Communicator;

import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.sightcall.api.Api;
import com.sightcall.api.JsonObjectRequestBasicAuth;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

    // Email, Password text fields
    EditText txtUsername, txtPassword;
     
    // login button
    Button btnLogin;

	// Request queue containing the token request
	RequestQueue mRequestQueue;
     
    // Session Manager Class
	SessionManager session;
	
	public static final String TAG = "LoginActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        Log.d(TAG, "onCreate");
         
        // Session Manager
        session = new SessionManager(getApplicationContext());

        // HTTP request manager for Volley
		mRequestQueue = Volley.newRequestQueue(this);
         
        // Email, Password input text
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword); 
        
        // Prefill fields if previously set
        if (session.getUsername() != null) {
        	txtUsername.setText(session.getUsername());
        }
        
        if (session.getPassword() != null) {
        	txtPassword.setText(session.getPassword());
        }
         
        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), "User Login Status: ", Toast.LENGTH_LONG).show();         
         
        // Login button
        btnLogin = (Button) findViewById(R.id.btnLogin);         
         
        // Login button click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {
                // Get username, password from EditText
                final String username = txtUsername.getText().toString();
                final String password = txtPassword.getText().toString();
                 
                // Check if username, password is filled                
                if(username.trim().length() > 0 && password.trim().length() > 0){                    
                	
                    Api api = new Api(username, password);
                    
                    // retrieve my displayname and UID with the /me method
               		JsonObjectRequestBasicAuth jsonObjReq = api.me(new Response.Listener<JSONObject>() {

            			@Override
            			public void onResponse(JSONObject response) {
            				Log.d(TAG, response.toString());
            				JSONObject jme = response.optJSONObject("me");
            				if (jme != null) {
            					String displayname = jme.optString("name", null);
            					String uid = jme.optString("rtcc_uid", null);
            				
            					session.createLoginSession(username, password, displayname, uid);
                            
            					// Staring MainActivity
            					Intent i = new Intent(getApplicationContext(), MainActivity.class);
            					startActivity(i);
            					finish();
            				}
            				else {
            					showAlertDialog(LoginActivity.this, "Login failed..", "Could not retrieve identity", false);
            				}
            			}
            		}, new Response.ErrorListener() {

            			@Override
            			public void onErrorResponse(VolleyError error) {
            				VolleyLog.d(TAG, "Error: " + error.getMessage());
            				// username / password doesn't match
            				showAlertDialog(LoginActivity.this, "Login failed..", "Username/Password is incorrect", false);
            			}
            		});
            		jsonObjReq.setTag(Api.REQUEST_ME);
            		mRequestQueue.add(jsonObjReq);
                }else{
                    // user didn't entered username or password
                    // Show alert asking him to enter the details
                	showAlertDialog(LoginActivity.this, "Login failed..", "Please enter username and password", false);
                }
                 
            }
        });
    }        
    
    public void showAlertDialog(Context ctx, String title, String message, boolean cancelable)
    {
    	new AlertDialog.Builder(ctx)
    	.setTitle(title)
        .setMessage(message)
        .setCancelable(cancelable)
        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
        })
        .show();
    }
}
