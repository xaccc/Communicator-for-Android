package com.sightcall.api;

/**
 * The SightCall Communicator API is protected by HTTP Basic Authentication.
 * This package provides a thin wrapper around a Volley authenticated request manager.
 */

import org.json.JSONObject;

import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public class Api {
	
	// The Base URL of the API
	// final String baseurl = "http://192.168.1.233:3000/api";
	final String baseurl = "https://sightcall-communicator.herokuapp.com/api";
	// final String baseurl = "https://thawing-harbor-4536.herokuapp.com/api";
	
	/** 
	 * Tags to identify the request to request queue
	 */
	public static final String REQUEST_TOKEN = "token";
	public static final String REQUEST_APPID = "appid";
	public static final String REQUEST_ME = "me";
	public static final String REQUEST_FRIENDS = "friends";
 
	/**
	 * This simple API is authenticated by username/password.
	 */
	String username = null;
	String password = null;
	
	public Api(String username, String password) {
		this.username = username;
		this.password = password;
		Log.d("API", "new:" + username + ":" + password + ":");
	}
	

	public JsonObjectRequestBasicAuth token(Listener<JSONObject> listener, ErrorListener errorListener) {
		String url = this.baseurl + "/token";
		
		JsonObjectRequestBasicAuth jsonObjReq = new JsonObjectRequestBasicAuth(username, password,
				Method.GET, url, null, listener, errorListener);
		
		return jsonObjReq;
	}
	
	public JsonObjectRequestBasicAuth appid(Listener<JSONObject> listener, ErrorListener errorListener) {
		String url = this.baseurl + "/appid";
		
		JsonObjectRequestBasicAuth jsonObjReq = new JsonObjectRequestBasicAuth(username, password,
				Method.GET, url, null, listener, errorListener);
		
		return jsonObjReq;
	}
	
	public JsonObjectRequestBasicAuth friends(Listener<JSONObject> listener, ErrorListener errorListener) {
		String url = this.baseurl + "/friends";
		
		JsonObjectRequestBasicAuth jsonObjReq = new JsonObjectRequestBasicAuth(username, password,
				Method.GET, url, null, listener, errorListener);
		
		return jsonObjReq;
	}
	
	public JsonObjectRequestBasicAuth me(Listener<JSONObject> listener, ErrorListener errorListener) {
		String url = this.baseurl + "/me";
		
		JsonObjectRequestBasicAuth jsonObjReq = new JsonObjectRequestBasicAuth(username, password,
				Method.GET, url, null, listener, errorListener);
		
		return jsonObjReq;

	}
			
}