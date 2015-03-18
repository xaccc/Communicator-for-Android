
Implementation Notes - Android
==============================



## Necessary Libraries and Permissions

1. Clone the RTCC [Android-SDK](https://github.com/weemo/Android-SDK) from github.
   Copy the directory `armeabi-v7a`, and the file
   `WeemoAndroidSDK.jar` into the `libs` directory of your project.


2. This project uses
   [Volley](https://github.com/mcxiaoke/android-volley), a library
   that makes it easier to make HTTP requests on Android.  If you do
   not already have it, you will need to build it.

   Here are some instructions on how to build
   it. [Volley-Howto](http://karanbalkar.com/2014/05/tutorial-81-using-volley-networking-library-in-android/).
   Once you have the `volley.jar` file, simply copy it into the `libs` directory.

3. Include the javadoc directory from the RTCC
   [Android SDK-Helper](https://github.com/weemo/Android-SDK-Helper)
   project and put it in the `libs` directory.  Create a file there
   called `WeemoAndroidSDK.jar.properties` with the following line in
   it.

   ```text
    doc=javadoc/html/
   ```

   This line associates the .jar file with its javadoc.

4. Add the necessary permissions to the `AndroidManifest.xml` file.
   These permissions allow the RTCC SDK to access critical functions
   of the phone.

   ```xml
    <!-- Internet access -->
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- PSTN calls -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <!-- Video capture -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <!-- Audio capture -->
    <uses-permission android:name="android.permission.CAMERA" />
    <!-- Change audio route -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    
   ```


#### Short Explanation of the API 

For this project, we chose to use Volley to handle the HTTP requests.
Volley provides a `JsonObjectRequest` that we extend to HTTP Basic
Authentication.  Volley allows you to write HTTP requests using
anonymous inner classes to handle success and failure.  With Volley, requests
look like the following.

```java
JsonObjectRequest req = new JsonObjectRequest(Request.GET, "http://api/foo", null, new Response.Listener<JSONObject>() {
    @Override
    public void onResponse(JSONObject response) {
        // success
}, new Response.ErrorListener() {
    @Override
    public void onErrorResponse(VolleyError error) {
        // failure
});
req.setTag(...)
mRequestQueue.add(req)

```

This instantates a new GET request to the URL specified.  The instance
of the `Response.Listener` class defines the `onResponse` method,
where the response is delivered directly as a JSON object.  The
instance of the `Response.ErrorListener` class defines the
`onErrorResponse` method

For our Rails-based API we need an authenticated JSON request that
provides HTTP Basic Authentication.  This is implemented in file
`JsonObjectRequestBasicAuth.java`.  Its initializer is extended with
two parameters, `username` and `password`.  The class overriders the
`getHeaders` method so that it provides the necessary Basic
Authentication header.

```java
// JsonObjectRequestBasicAuth.java
@Override
public Map<String, String> getHeaders() throws AuthFailureError {

	// Provide an authentication header
	HashMap<String, String> headers = new HashMap<String, String>();
	String creds = String.format("%s:%s", username, password);
    String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
    headers.put("Authorization", auth);
    return headers;
}
```

The Rails-API returns a list of users as the result of the `friends`
method, and it is easier to render an Android "Spinner" widget if the
list items are objects.  For this, we have the `User` class, defined
in `User.java`.  This class can construct a User object from a JSON
object, and can also construct an ArrayList of User objects from a
JSON array.  Here is an excerpt of the code that constructs a User
object from JSON.

```java
// User.java
public User(JSONObject object){
  try {
        this.name = object.getString("name");
        this.uid = object.getString("rtcc_uid");
   } catch (JSONException e) {
        e.printStackTrace();
   }
}
```

The User class determines how each friend is rendered in the Spinner
widget by the way it defines the `toString` method.  Ours simply
returns the name of the user.

```java
// User.java
public String toString() {	
    return this.name;            // What to display in the Spinner list.	   
}
```

The top-level file of the API is defined in `Api.java.`  An instance
of the API can be created with the parameters `username` and
`password`.  The body of the API class is straightforward.  It simply maps a
`JsonObjectRequest` with Basic Authentication to each of the API endpoints listed below.


    /api/token
    /api/appid
    /api/me
    /api/friends


## Short Explanation of the Session Manager

The Login and Session Manager of this simple App is based on
the ideas presented in this excellent
[Session Management Tutorial](http://www.androidhive.info/2012/08/android-session-management-using-shared-preferences/).

The Session Manager stores key/value pairs in an Android shared
preferences.  The method `createLoginSession` stores the Username,
Password, RTCC UID and RTCC Displayname in the preferences, and sets
the flag `IS_LOGIN` to `true`.  

The Session Manager provides another method called `checkLogin` that
may be called from any activity to verify login status.  If the user
is not marked as logged-in, the user is redirected to the Login
activity.

```java
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
```

The UI for the login screen is simple.

<img src="screenshot_login.png" height="360px">

The Login Activity implements the method that is invoked when the
"Login" button is pressed.  It uses the API to verify the
Username/Password against the Rails App and stores the credentials in
the Session Manager.

## Short Explanation of the Main Activity

Android calls the `onCreate` method whenever it needs to rebuild the
UI.  This can happen very often: for instance, every time the phone is
rotated.  It is during this method that we verify the logged-in status of the user.


The `onStart` method is called after `onCreate` when a user can begin
interacting with the activity on-screen.  It is important to note that
the RTCC object has a lifetime that will span many invocations of
`onStart`.  Thus, the `onStart` message needs to check whether there
is a RTCC instance already running, and if there is one running that
is already involved in a call.  If a call is on-going, the UI elements
are re-attached to the video.  (The RTCC object iself manages the
microphone and speaker.)

If there is no RTCC instance in the `onStart` method, we know that
the user has provided his Username/Password and that he is considered
"logged-in."  In this case, we proceed with RTCC authentication.  The
first method, `getAppidThenInitialize` kicks off the process.  This
method launches an API call to retrieve the AppID for this App.  Upon
success, this calls the `RTCC.initialize` method.

When RTCC initialization is complete, it uses a `@RTCCEventListener`
to signal the `onConnected` method.  The `onConnected` method kicks
off the next step by calling `getTokenThenAuthenticate`.  This method
launches an API call to retrieve a token with which to authenticate
this RTCC connection.  Upon success of this API call, we call the
RTCC `authenticate` method.


```java
    token = response.getString("token");
    ...
    WeemoEngine weemo = Weemo.instance();
    weemo.authenticate(ctx, token, WeemoEngine.UserType.INTERNAL);	   
```

Once RTCC authentication is complete, it uses a `@WeemoEventListener`
to signal the `onAuthenticated` method.  At this point, we can finally
set the Display Name of this endpoint, and the initialization process
is complete.

## Short Explanation of the Video Elements in the Main View

Everything in `activity_main.xml` is vanilla Android with the
exception of the Video In view and the Video Out view.  These objects
are defined by custom classes in the RTCC SDK.  In the tutorial here,
these two views are instantiated by the following code.

```xml
<!-- activity_main.xml -->
	<com.weemo.sdk.view.WeemoVideoInFrame
	    android:id="@+id/video_in" 
        android:layout_width="240dp" 
        android:layout_height="180dp" 
        android:singleLine = "true"
        android:layout_gravity = "center"
        android:layout_marginBottom="10dip"
        />
            
	<com.weemo.sdk.view.WeemoVideoOutPreviewFrame
    	android:id="@+id/video_out"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:singleLine = "true"
        android:layout_gravity="center"
        android:layout_marginBottom="10dip"
        />
```

In the App, an in-progress call is attached to these UI elements with the following method defined in `MainActivity.java`.

```java
// MainActivity.java
public void attachVideoElements(final WeemoCall call) {
    WeemoVideoOutPreviewFrame mVideoOutPreviewFrame = (WeemoVideoOutPreviewFrame) findViewById(R.id.video_out);
   	WeemoVideoInFrame mVideoInFrame = (WeemoVideoInFrame) findViewById(R.id.video_in);
   	call.setVideoOut(mVideoOutPreviewFrame);
   	call.setVideoIn(mVideoInFrame);
}
```


