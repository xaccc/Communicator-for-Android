package com.sightcall.Communicator;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.sightcall.Communicator.UserCustomArrayAdapter;
import com.sightcall.api.Api;
import com.sightcall.api.JsonObjectRequestBasicAuth;
import com.sightcall.api.User;
import com.sightcall.Communicator.R;

import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.Logger;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.DataChannel;
import net.rtccloud.sdk.RtccEngine;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.FloorListEvent;
import net.rtccloud.sdk.event.call.StatusEvent;
import net.rtccloud.sdk.event.datachannel.DataChannelOutOfBandEvent;
import net.rtccloud.sdk.event.global.AuthenticatedEvent;
import net.rtccloud.sdk.event.global.ConnectedEvent;
import net.rtccloud.sdk.view.VideoInFrame;
import net.rtccloud.sdk.view.VideoOutPreviewFrame;
import net.rtccloud.sdk.event.meetingpoint.RequestEvent;
import net.rtccloud.sdk.event.presence.PresenceRequestEvent;
import net.rtccloud.sdk.event.presence.PresenceUpdateEvent;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity {
    	
	Context ctx = null;
	
	public static final String TAG = "MainActivity";

	// My friends from the API
	ArrayList<User> mFriends = null;
	
	// Request queue containing the API calls
	RequestQueue mRequestQueue;
	
	// My credentials - from the login session
	String mUsername = null;
	String mPassword = null;	
	String mDisplayname = null;
	String mUid = null;
	Integer mPresence = 0;
     
    // Session Manager Class
    SessionManager session;
    
    // Labels on the screen
    View mViewCircle;
    TextView mLblName;
    TextView mLblStatus;
    
    // Layout manager for the Video Elements
    AspectLockedFrameLayout mLayoutFrame;
    
    // Video Elements on the Screen
    VideoOutPreviewFrame mVideoOutPreviewFrame;
	VideoInFrame mVideoInFrame;
     
    // Friends selection list
    Spinner mSpinnerFriends;
    
    // Call button
    Button mBtnCall;
    
    // Button Hangup
    Button mBtnHangup;
    
    // Track previous call state
    CallStatus mPrevStatus = null;
    

    /**
     * onCreate is called every time Android needs to begin the Activity.  Android
     * may choose to do this when the device is rotated, the App resumes, or some
     * other event happens.  In any case, the Activity may be restarted many times
     * during the lifetime of the Rtcc instance.  Because of this, we check if
     * a call is in progress and re-attach its video elements if needed.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d(TAG, "onCreate");
        
        ctx = this;
                
        // Toast.makeText(getApplicationContext(), "Redirecting...", Toast.LENGTH_SHORT).show();
        
        // Session class instance
        session = new SessionManager(getApplicationContext());
        
        // HTTP request manager for Volley
		mRequestQueue = Volley.newRequestQueue(this);
        
		// Text labels
		mViewCircle = (View) findViewById(R.id.viewCircle);
        mLblName = (TextView) findViewById(R.id.lblName);
        mLblStatus = (TextView) findViewById(R.id.lblStatus);
        
        // Layout manager for the Incoming Call
       	mLayoutFrame = (AspectLockedFrameLayout) findViewById(R.id.aspectLockedFrameLayout);
    	mLayoutFrame.setAspectRatio(4.0 / 3.0);
    	
    	// Video elements
        mVideoOutPreviewFrame = (VideoOutPreviewFrame) findViewById(R.id.video_out);
    	mVideoInFrame = (VideoInFrame) findViewById(R.id.video_in);
        
        // Friends spinner (selection list)
        mSpinnerFriends = (Spinner) findViewById(R.id.spinner1);
        
        // Call button
        mBtnCall = (Button) findViewById(R.id.btnCall);
         
        // Button hangup
        mBtnHangup = (Button) findViewById(R.id.btnHangup);
        
        // Keep screen on as long as we are displayed
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                 
        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in.  If we get past this, we are logged in.
         **/
        session.checkLogin();
        
        // get user data from session
        HashMap<String, String> userdetails = session.getUserDetails();
        
        // username, password (for API access)
        mUsername = userdetails.get(SessionManager.KEY_USERNAME);
        mPassword = userdetails.get(SessionManager.KEY_PASSWORD);
        
        // displayname, uid (from Login API Access)
        mDisplayname = userdetails.get(SessionManager.KEY_DISPLAYNAME);
        mUid = userdetails.get(SessionManager.KEY_UID);
                 
        // display user name and connection status
        mLblName.setText(Html.fromHtml("<b>" + mDisplayname + "</b>"));
        mLblStatus.setText("Status:");
        updateStatus();        

            
        /**
         * Spinner selection
         */
        mSpinnerFriends.setOnItemSelectedListener(new OnItemSelectedListener() { 
            
        	@Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
        		// No behavior for now
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
 
            }
        });
        
        /**
         * Call button click event
         */
        mBtnCall.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
            	User friend = (User) mSpinnerFriends.getSelectedItem();
            	Log.v(TAG, "Calling:" + friend.uid);
            	RtccEngine rtcc = Rtcc.instance();
            	if (rtcc != null) {
            		rtcc.createCall(friend.uid);
            	}
            }
        });
        
        
        /**
         * Hangup button click event
         */
        mBtnHangup.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {
            	Log.d(TAG, "btnHangup");
            	RtccEngine rtcc = Rtcc.instance();
            	Call currentCall = rtcc.getCurrentCall();
            	if (currentCall != null) {
            		currentCall.hangup();
            	}
            }
        });
        
    }
    
    /**
     * onStart is called after onCreate.  This method starts or maintains the RTCC Engine using a logged-in Session
     * (via getAppidThenInitialize).
     */
    @Override
    public void onStart() {
    	Log.d(TAG, "onStart");
    	super.onStart();
    	Rtcc.eventBus().register(this);
    	
    	RtccEngine rtcc = Rtcc.instance();

    	Logger.setGlobalLevel(Logger.LoggerLevel.VERBOSE);
    	if (rtcc == null) {
			Log.d(TAG, "onStart: rtcc initializing");
			getAppidThenInitialize();
		} else {
			Log.d(TAG, "onStart: rtcc resuming");
 		}
    	
 		if (rtcc != null && rtcc.isInBackground()) {
 			rtcc.goToForeground();
 		}
 		
 		// Restore session state if set
 		restoreSessionState();

 		// Display the call if there is a current RtccCall
 		if (rtcc != null && rtcc.getCurrentCall() != null) {
 			Call call = rtcc.getCurrentCall();
 			attachVideoElements(call);
 		}
    	
    }
    
    /**
     * Create the main menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    /**
     * Define the behavior of the Logout menu item.
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_logout:
        	Log.d(TAG, "actionLogout");
        	RtccEngine rtcc = Rtcc.instance();
        	if (rtcc != null) {
        		rtcc.disconnect();
        	}
    		mFriends = null;
            session.logoutUser();
            return true;
    	}
    	return super.onMenuItemSelected(featureId,  item);
    }
    
    /**
     * unused - but included to log activity lifecycle
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	
    	Log.d(TAG, "onSaveInstanceState");
    }
    
    /**
     * unused - but included to log activity lifecycle
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	
    	Log.d(TAG, "onRestoreInstanceState");
    }
    
    /**
     * Activity is stopped (this can happen even if the device is simply rotated).
     */
	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();

		RtccEngine rtcc = Rtcc.instance();
    	if (rtcc != null) {
    		rtcc.goToBackground();
    	}
		Rtcc.eventBus().unregister(this);
		
		// Save the session state
		saveSessionState();
	}
	
	
   /**
     * Utility: Save friends, presences in the session.
     */
    public void saveSessionState() {
    	
    	Log.d(TAG, "saveSessionState");

    	// serialize the friends' presences and store in the session
    	JSONArray jFriends;
		try {
			jFriends = User.toJson(mFriends);
		   	String f = jFriends.toString();
	    	session.setFriends(f);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// save my presence in the session
		session.setPresence(mPresence);
    }
    
    /**
     * Utility: Restore the list of friends, and my own presence from the session.
     */
    public void restoreSessionState() {
    	
    	Log.d(TAG, "restoreSessionState");
    	
        // Session class instance
        session = new SessionManager(getApplicationContext());
        String f = session.getFriends();

        // restore the friends' presences from the session
        if (f != null) {
        	try {
				JSONArray jFriends = new JSONArray(f);
				mFriends = User.fromJson(jFriends);
				
				ArrayAdapter<User> adapter = new UserCustomArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, (List<User>) mFriends);;
        		mSpinnerFriends.setAdapter(adapter);
        		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        // restore my presence from the session
        updateMyPresence(session.getUid(), session.getPresence());
    }

	/**
	 * Attach our UI elements to a Rtcc Call.
	 */
	public void attachVideoElements(final Call call) {
	
    	call.setVideoOut(mVideoOutPreviewFrame);
    	
     	if (call.isConference()) {
     		// Set to ActiveSpeaker if there is one
            int[] floor = call.getFloorList();
            if (floor != null && floor.length > 0) {
                Contact mainSpeaker = call.getContact(floor[0]);
                if (mainSpeaker != null) {
                    mainSpeaker.setView(mVideoInFrame);
                }
            }
     	}
     	else {
     		// Set to current call
     		call.setVideoIn(mVideoInFrame, Contact.DEFAULT_CONTACT_ID);
     	}
    	
	}
	
	/**
	 * Utility: Pop up an alert.
	 */
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
	
	/**
	 * API Access and continuation.
	 * Access the API to get the AppID.
	 * Upon success, initialize Rtcc.
	 */
	public void getAppidThenInitialize() {
		Log.d(TAG, "getAppidThenInitialize");

		Api api = new Api(mUsername, mPassword);

		JsonObjectRequestBasicAuth jsonObjReq = api.appid(new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, response.toString());
				String appid = null;
				try {
					appid = response.getString("appid");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// Successfully obtained appid, continue with Rtcc.initialize (which triggers onConnected)
				Rtcc.initialize(appid, ctx.getApplicationContext());
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				VolleyLog.d(TAG, "API Error: " + error.getMessage());
				showAlertDialog(MainActivity.this, "API Access Failed", "Username/Password is incorrect", false);
			}
		});

		jsonObjReq.setTag(Api.REQUEST_APPID);
		mRequestQueue.add(jsonObjReq);
	}
	
	/**
	 * API Access and continuation.
	 * Access the API to get a fresh token.
	 * Upon success, authenticate RTCC.
	 */
	public void getTokenThenAuthenticate() {
		Log.d(TAG, "getTokenThenAuthenticate");

		Api api = new Api(mUsername, mPassword);

		JsonObjectRequestBasicAuth jsonObjReq = api.token(new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, response.toString());
				String token = null;
				try {
					token = response.getString("token");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			   	RtccEngine rtcc = Rtcc.instance();
				rtcc.authenticate(ctx, token, RtccEngine.UserType.INTERNAL);			
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				VolleyLog.d(TAG, "API Error: " + error.getMessage());
				showAlertDialog(MainActivity.this, "API Access Failed", "Username/Password is incorrect", false);
			}
		});

		jsonObjReq.setTag(Api.REQUEST_ME);
		mRequestQueue.add(jsonObjReq);
	}
    
 
    /**
     * API Access and continuation.
     * Access the API to get our list of friends.
     * Upon success, populate the spinner and request RTCC to begin updating presences.
     */
    public void getFriendsThenSetPresences() {
    	Log.d(TAG, "getFriends");    	

        Api api = new Api(mUsername, mPassword);
        
        JsonObjectRequestBasicAuth jsonObjReq = api.friends(new Response.Listener<JSONObject>() {

        	@Override
        	public void onResponse(JSONObject response) {
        		Log.d(TAG, response.toString());

        		JSONArray jfriends = null;
        		try {
        			jfriends = response.getJSONArray("friends");
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}

        		mFriends = User.fromJson(jfriends);
        		ArrayAdapter<User> adapter = new UserCustomArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, (List<User>) mFriends);;
        		mSpinnerFriends.setAdapter(adapter);
        		
        		startPresences();
        	}
        }, new Response.ErrorListener() {

        	@Override
        	public void onErrorResponse(VolleyError error) {
        		VolleyLog.d(TAG, "API Error: " + error.getMessage());
        		showAlertDialog(MainActivity.this, "API Access Failed", "Username/Password is incorrect", false);
        	}
        });
        jsonObjReq.setTag(Api.REQUEST_FRIENDS);
        mRequestQueue.add(jsonObjReq);
    }
    
    /*
     * Set our own presence and register for updates of the presences of our friends.
     */
    public void startPresences() {
    	
	   	RtccEngine rtcc = Rtcc.instance();

		// PRESENCE
		rtcc.presence().set(1);
		
   		for (User friend : mFriends) {
			Log.d(TAG, "Roster.add:" + friend.toString() + " " + friend.uid);
			Rtcc.instance().roster().add(friend.uid);        			
		}
		Log.d(TAG, "Roster.presece.request");
		Rtcc.instance().presence().request();
    }

    
    /*
     * Ask whether the current call should be answered or rejected.
     */
    public void confirmCallAccept(final Call call) {
    	Log.d(TAG, "confirmCallAccept");
    	String dn = call.getContactDisplayName(0); // TOM: ContactID=0
    	new AlertDialog.Builder(ctx)
    	.setMessage("Accept call from " + dn + "?")
    	.setCancelable(false)
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			call.resume();
    		}
    	})
    	.setNegativeButton("No", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			call.hangup();
    		}
    	})
    	.show();
    }
    
    /**
     * Ask whether the conference call should be answered or rejected.
     */
    public void confirmConfCallAccept(final RequestEvent event) {
    	Log.d(TAG, "confirmCallAccept");
    	
    	Rtcc.instance().getMeetingPoint(event.getMeetingPoint().getId()); // TOM: workaround - to stash

    	String dn = event.getDisplayName();
    	
    	new AlertDialog.Builder(ctx)
    	.setMessage("Accept conference invitation from " + dn + "?")
    	.setCancelable(false)
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
            	event.getMeetingPoint().accept(event.getUid());
            	event.getMeetingPoint().call();
    		}
    	})
    	.setNegativeButton("No", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			event.getMeetingPoint().deny(event.getUid());
    		}
    	})
    	.show();
    }

    
    /**
     * Update the status textfield with information about the Rtcc engine.
     */
    public void updateStatus() {
        switch (Rtcc.getEngineStatus()) {
        case UNDEFINED:
			mLblStatus.setText(Html.fromHtml("Status: <b> Undefined </b>"));
			break;
        case INITIALIZING:
 			mLblStatus.setText(Html.fromHtml("Status: <b> Initializing </b>"));
 			break;
        case CONNECTED:
 			mLblStatus.setText(Html.fromHtml("Status: <b> Connected </b>"));
 			break;
        case AUTHENTICATING:
 			mLblStatus.setText(Html.fromHtml("Status: <b> Authenticating </b>"));
 			break;
        case AUTHENTICATED:
  			mLblStatus.setText(Html.fromHtml("Status: <b> Authenticated </b>"));
  			break;
        case NETWORK_LOST:
 			mLblStatus.setText(Html.fromHtml("Status: <b> Network Lost </b>"));
 			break;
        case DISCONNECTING:
        	mLblStatus.setText(Html.fromHtml("Status: <b> Disconnecting </b>"));
 			break;
 		default:
 			mLblStatus.setText(Html.fromHtml("Status: <b> Unknown </b>"));
        }
    }
    
    /**
     * The completion of the Rtcc.initialize method call is signaled with the onConnected event.  The RTCC
     * singleton is running and is ready to be authenticated to the cloud.  The next step is to retrieve
     * an authentication token through the API.
     */
    @RtccEventListener
	public void onConnected(final ConnectedEvent event) {
    	updateStatus();    	
    	Log.d(TAG, "onConnected:" + event.toString());

    	if (event.isSuccess()) {
			Toast.makeText(getApplicationContext(), "Authenticating...", Toast.LENGTH_SHORT).show();
			getTokenThenAuthenticate();			

		} else {
			Toast.makeText(getApplicationContext(), "Connect Failed.", Toast.LENGTH_SHORT).show();
		}
	}
    
    /**
     * When we have received the authenticated event, we know that our token has authenticated us
     * to the RTCC cloud.  Proceed by getting the list of friends through the API, and then get
     * their presences.
     */
    @RtccEventListener
    public void onAuthenticated(final AuthenticatedEvent event) {
    	updateStatus();
  	  	Log.d(TAG, "onAuthenticated:" + event.toString());

    	if (event.isSuccess()) {
			Toast.makeText(getApplicationContext(), "Authenticated.", Toast.LENGTH_SHORT).show();
    		RtccEngine rtcc = Rtcc.instance();
    		rtcc.setDisplayName(mDisplayname);
    		
    		getFriendsThenSetPresences();
    	}
    	else {
  			mLblStatus.setText(Html.fromHtml("Status: <b> Authentication Failed </b>"));
			Toast.makeText(getApplicationContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
    	}
    }
    

    /**
     * on a CallStatus changed event, update the display of the call status
     */
    @RtccEventListener
    public void onCallStatusChanged(final StatusEvent event) {
  	  Log.d(TAG, "onCallStatusChanged:" + event.toString() + " " + event.getCall().getStatus().toString());
  	  
  	  Call mCall = event.getCall();
  	  
  	  switch(event.getStatus()) {
      case ACTIVE:
    	  Toast.makeText(getApplicationContext(), "Call Active", Toast.LENGTH_SHORT).show();
    	  mPrevStatus = CallStatus.ACTIVE;
    	  attachVideoElements(mCall);
    	  break;
    	  
      case PROCEEDING:
    	  Toast.makeText(getApplicationContext(), "Call proceeding ...", Toast.LENGTH_SHORT).show();    	  
    	  mPrevStatus = CallStatus.PROCEEDING;
    	  break;
    	  
      case CREATED:
    	  Toast.makeText(getApplicationContext(), "Call created ...", Toast.LENGTH_SHORT).show();
    	  mPrevStatus = CallStatus.CREATED;
    	  break;
         
      case RINGING:
    	  Toast.makeText(getApplicationContext(), "Call ringing ...", Toast.LENGTH_SHORT).show();
          mPrevStatus = CallStatus.RINGING;
          confirmCallAccept(mCall);
          break;
          
      case ENDED:
    	  // deduce the reason ended based on the previous state
    	  if (mPrevStatus == CallStatus.PROCEEDING){ 
    		  Toast.makeText(getApplicationContext(), "Call Rejected", Toast.LENGTH_SHORT).show();
    	  }
    	  else if (mPrevStatus == CallStatus.ACTIVE) {
    		  Toast.makeText(getApplicationContext(), "Call Ended", Toast.LENGTH_SHORT).show();    		  
    	  }
    	  else {
    		  Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();    		      		  
    	  }
    	  Log.d(TAG, "case");
    	  mPrevStatus = CallStatus.ENDED;
    	  break;
      }
      
    }
    
    ////////////////////////////////////////////////////////////////
    //
    // MULTI-PARTY
    
    /**
     * Respond to an invitation to join a Conference Call
     */
    @RtccEventListener
    public void onRequestEvent(final RequestEvent event) {
    	Log.d(TAG, "onRequestEvent:" + event.toString());

        switch (event.getType()) {
        case INVITED:
        	confirmConfCallAccept(event);
            break;
        default:
        	break;
        }
    }
    
    /**
	 * Attach our Video View for Active Speaker in a Conference Call
	 */
    @RtccEventListener
    public void onFloorListEvent(final FloorListEvent event) {
    	Log.d(TAG, "onFloorListEvent");
        Call call = event.getCall();
        int[] floor = call.getFloorList();
        if (floor != null && floor.length > 0) {
            Contact mainSpeaker = call.getContact(floor[0]);
            if (mainSpeaker != null) {
                mainSpeaker.setView(mVideoInFrame);
            }
        }
    }
 
    
    ////////////////////////////////////////////////////////////////
    //
    // PRESENCE
    
    /**
     * if the Uid matches me, then update my presence value display
     */
    public void updateMyPresence(String uid, Integer value) {
       	// Check if it is myself and draw
    	if (uid.equals(mUid)) {
    		mPresence = value;
    		if (value == 0) {
    			mViewCircle.setBackground( getResources().getDrawable(R.drawable.redcircle));
    		}
    		else if (value == 1) {
    			mViewCircle.setBackground( getResources().getDrawable(R.drawable.greencircle));
    		}
    		else {
    			mViewCircle.setBackground( getResources().getDrawable(R.drawable.circle));
    		}
    	}
    }

    /**
     * Search the list of friends for the uid and update the corresponding presence value
     */
    public void updateFriendPresence(String uid, Integer value) {
    	 
    	// Check the list of friends
        User u = User.findByUid(mFriends, uid);
        if (u != null) {
        	u.presence = value;
        }
        ArrayAdapter<User> adapter = (ArrayAdapter<User>)mSpinnerFriends.getAdapter();
        adapter.notifyDataSetChanged();
    }
    
    /**
     * on a Presence request event, get the uid and value and distribute to the update functions
     */
    @RtccEventListener
    public void onPresenceRequestEvent(PresenceRequestEvent event) {
    	Log.d(TAG, "PresenceRequest:" + event.toString());
    	Map<String, Integer> presences = event.get();
    	    	
    	for (Map.Entry<String, Integer> p: presences.entrySet()) {
    		String uid = p.getKey();
    		Integer value = p.getValue();
    		
    		// If it is me, update that
        	updateMyPresence(uid, value);

        	// If it is a friend, update that
    		updateFriendPresence(uid, value);  
    	}
    }

    /**
     * on a Presence update event, get the uid and value and distribute to the update functions
     */
    @RtccEventListener
    public void onPresenceUpdateEvent(PresenceUpdateEvent event) {
    	Log.d(TAG, "Presence:" + event.toString());
    	String uid = event.getUid();
        int value = event.getValue();
 
		// If it is me, update that
    	updateMyPresence(uid, value);

    	// If it is a friend, update that
    	updateFriendPresence(uid, value);
    }
    
    ////////////////////////////////////////////////////////////////
    //
    // TEXT MESSAGES
    
    /**
     * On a data channel event, display the message in a dismissable alert.
     * Give the user three pre-defined responses.
     */
    
    public void utilSendTextMessage(String uid, String textmsg) {
        final RtccEngine rtcc = Rtcc.instance();
        final byte[] bytes = textmsg.getBytes();
		if (rtcc != null) {
			rtcc.dataChannel().send(bytes, 0, uid);
		}
    }
    
    @RtccEventListener
    public void onDataChannelOutOfBandEvent(DataChannelOutOfBandEvent event) throws UnsupportedEncodingException {
        final String uid = event.getUid();
        User u = User.findByUid(mFriends, uid);
        String title;
        if (u != null) {
        	title= "From: " + u.name;
        }
        else {
        	title = "Text Message";
        }
        String message = new String(event.getPayload());
        
         
        // Display an alert with the message.  Offer three canned responses
		new AlertDialog.Builder(ctx)
	  	.setTitle(title)
	    .setMessage(message)
	    .setCancelable(false)
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		utilSendTextMessage(uid, "Yes");
	    	}
	     })
	     .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		utilSendTextMessage(uid, "No");
		    	}
	     })
	    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		utilSendTextMessage(uid, "Ok");
	    	}
	     })
	    .show();
    }

    
}
