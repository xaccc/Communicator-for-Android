package com.sightcall.api;

/**
 *  Simple extension to Volley's JsonObjectRequest to provide an HTTP Basic Authentication
 *  header on a request.
 */

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public class JsonObjectRequestBasicAuth extends JsonObjectRequest {
	
	String username = null;
	String password = null;

	public JsonObjectRequestBasicAuth(String user, String pass,
			int method, String url, JSONObject jsonRequest, 
			Listener<JSONObject> listener, ErrorListener errorListener) {
		
		super(method, url, jsonRequest, listener, errorListener);

		// Simply store these values for use by getHeaders
		username = user;
		password = pass;

	}

	@Override

	public Map<String, String> getHeaders() throws AuthFailureError {

		// Provide an authentication header
		HashMap<String, String> headers = new HashMap<String, String>();
		String creds = String.format("%s:%s", username, password);
	    String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
        headers.put("Authorization", auth);
        return headers;
	}
}