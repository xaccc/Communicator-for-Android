package com.sightcall.api;

/**
 * A Very simple User model.  The API returns a list of friends - including their name and UID.
 * Here, we also define a slot for the user's presence.  When presence values are received from the
 * RTCC cloud, we search the mFriends list and update the appropriate friend.
 * 
 * The list of friends (mFriends) is converted to JSON and serialized to save/restore the main
 * activity.  Class methods are defined here to aid in the conversions.
 * 
 */


import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class User{        

	public String name;
	public String uid;
	public int presence;
	
	public User(String name, String uid) {
		this.name = name;
		this.uid = uid;
		this.presence = 0;
	}
	
	// Create a User from a Json object.  Fields 'name' and 'rtcc_uid' are required.
	// The 'presence' field is not returned by the Communicator HTTP API, but when a User
	// is serialized to JSON for save/restore, it is included.	
	public User(JSONObject object){
        try {
        	// objects from the API have these two fields
            this.name = object.getString("name");
            this.uid = object.getString("rtcc_uid");
            
            // objects serialized from Main activity also have this field
            if (object.has("presence")) {
            	this.presence = object.getInt("presence");
            }
       } catch (JSONException e) {
            e.printStackTrace();
       }
    }
	
	// Create a JSON object from a User
	public JSONObject toJson() throws JSONException {
		JSONObject j = new JSONObject();
		j.put("name", this.name);
		j.put("rtcc_uid", this.uid);
		j.put("presence", this.presence);
		return j;
	}
	
	/**
	 * 
	 * What follows are utilities for operating on Lists of User objects.
	 * 
     */

    // User.fromJson(jsonArray);
    public static ArrayList<User> fromJson(JSONArray jsonObjects) {
           ArrayList<User> users = new ArrayList<User>();
           for (int i = 0; i < jsonObjects.length(); i++) {
               try {
                  users.add(new User(jsonObjects.getJSONObject(i)));
               } catch (JSONException e) {
                  e.printStackTrace();
               }
          }
          return users;
    }
    
    // User.toJson(ArrayList<User>)
    public static JSONArray toJson(ArrayList<User> users) throws JSONException {
    	JSONArray jsonObjects = new JSONArray();
    	if (users == null) {
    		return jsonObjects;
    	}
    	for (User u: users) {
    		JSONObject j = u.toJson();
    		jsonObjects.put(j); // TOM: puts at end of list
    	}
    	return jsonObjects;
    }
    
    // User.findByUid(ArrayList<User> users)
    public static User findByUid(ArrayList<User> users, String uid) {
    	if (users == null) {
    		return null;
    	}
    	for (User u: users) {
    		// Log.d("U", "LOOKING UID:" + u.uid);
    		if (u.uid.equals(uid)) {
    			// Log.d("U", "Found UID:" + uid);
    			return u;
    		}
    	}
    	// Log.d("U",  "Not Found UID" + uid);
    	return null;
    }
    

	
}
	

	