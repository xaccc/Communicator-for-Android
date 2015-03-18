package com.sightcall.Communicator;

/**
 * The Session Manager handles the login and logout of the user.  Because the App is
 * authenticated using an API, an HTTP method call is used to determine if the username/password
 * are accepted.  We choose to use the api.me method - which returns the name and UID of the user,
 * if the username/password are correct.
 * 
 *  If login is accepted, the Session Manager caches the username, password, name and UID of the 
 *  user.
 *  
 *  The Session Manager also provides slots for storing and retrieving a presence and a list of friends.
 *  
 */
 
import java.util.HashMap;
 
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;
     
    // Editor for Shared preferences
    Editor editor;
     
    // Context
    Context _context;
     
    // Shared pref mode
    int PRIVATE_MODE = 0;
     
    // Sharedpref file name
    private static final String PREF_NAME = "TutorialPref";
     
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
     
    // User name (make variable public to access from outside)
    public static final String KEY_USERNAME = "username";
     
    // Password
    public static final String KEY_PASSWORD = "password";
    
    // My displayname
    public static final String KEY_DISPLAYNAME = "displayname";
    
    // My UID
    public static final String KEY_UID = "uid";
    
    // My presence
    public static final String KEY_PRESENCE = "presence";
    
    // My friends, serialized JSON
    public static final String KEY_FRIENDS = "friends";
    

    
     
    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
     
    /**
     * Create login session
     * */
    public void createLoginSession(String username, String password, String displayname, String uid){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
         
        // Store items in pref
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_DISPLAYNAME, displayname);
        editor.putString(KEY_UID, uid);
         
        // commit changes
        editor.commit();
    }   
     
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             
            // Staring Login Activity
            _context.startActivity(i);
        }
         
    }
          
     
    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        
        user.put(KEY_USERNAME,  pref.getString(KEY_USERNAME, null));
        user.put(KEY_PASSWORD,  pref.getString(KEY_PASSWORD, null));
        user.put(KEY_DISPLAYNAME,  pref.getString(KEY_DISPLAYNAME, null));
        user.put(KEY_UID,  pref.getString(KEY_UID,  null));
        
        // return user
        return user;
    }
     
    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        // editor.clear();
    	editor.remove(KEY_DISPLAYNAME);
    	editor.remove(KEY_UID);
    	editor.remove(KEY_FRIENDS);
    	editor.remove(IS_LOGIN);
        editor.commit();
         
        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         
        // Staring Login Activity
        _context.startActivity(i);
    }
     
    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
    
    public String getUsername() {
    	return pref.getString(KEY_USERNAME, null);
    }
    
    public String getPassword() {
    	return pref.getString(KEY_PASSWORD, null);
    }
    
    public String getUid() {
    	return pref.getString(KEY_UID,  null);
    }
    
    /**
     * for saving/restoring my presence
     */
    public void setPresence(Integer value) {
    	editor.putInt(KEY_PRESENCE, value);
    	editor.commit();
    }
    
    public Integer getPresence() {
    	return pref.getInt(KEY_PRESENCE,  0);
    }
    
    /**
     * for saving/restoring friends and presences
     */
    public void setFriends(String f) {
    	editor.putString(KEY_FRIENDS, f);
        editor.commit();
    }
    
    public String getFriends() {
    	return pref.getString(KEY_FRIENDS, null);
    }
}