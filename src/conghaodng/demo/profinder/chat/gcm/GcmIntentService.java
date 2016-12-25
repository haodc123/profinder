package conghaodng.demo.profinder.chat.gcm;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.Config;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;

public class GcmIntentService extends IntentService {
	private static final String TAG = GcmIntentService.class.getSimpleName();
    public static final String KEY = "key";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    
    public GcmIntentService() {
        super(TAG);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
    	// TODO Auto-generated method stub
    	String value = intent.getStringExtra(KEY);
    	switch (value) {
		case SUBSCRIBE:
			String topic = intent.getStringExtra(TOPIC);
            subscribeToTopic(topic);
			break;
		case UNSUBSCRIBE:
			String topic1 = intent.getStringExtra(TOPIC);
            unsubscribeToTopic(topic1);
			break;
		default:
			registerGCM();
			break;
		}
    }

	private void registerGCM() {
		// TODO Auto-generated method stub
		SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String token = null;
		try {
			InstanceID mInstanceID = InstanceID.getInstance(this);
			token = mInstanceID.getToken(Constants.GG_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			Log.d(TAG, "GCM Registration Token: " + token);
			
			sendRegistrationIDToServer(token);
			mSharedPref.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, true);
		} catch(Exception e) {
			Log.e(TAG, "Failed to complete token refresh", e);
			mSharedPref.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, false);
		}
		
		// Notify UI that registration has completed, so the progress indicator can be hidden.
		Intent itRegCompleted = new Intent(Config.REGISTRATION_COMPLETE);
		itRegCompleted.putExtra("token", token);
		LocalBroadcastManager.getInstance(this).sendBroadcast(itRegCompleted);
	}

	private void sendRegistrationIDToServer(final String token) {
		// TODO Auto-generated method stub
		String endPoint = Constants.CHAT_UPDATE_USER_GCMREGID.replace("_ID_", Variables.userID);
		StringRequest strReq = new StringRequest(Request.Method.PUT,
                endPoint, new Response.Listener<String>() {
 
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
 
                try {
                    JSONObject obj = new JSONObject(response);
                    // check for error
                    if (obj.getBoolean("error") == false) {
                        // broadcasting token sent to server
                        Intent registrationComplete = new Intent(Config.SENT_TOKEN_TO_SERVER);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(registrationComplete);
                    } else {
                        Functions.toastString("Unable to send gcm registration id to our sever. " + obj.getJSONObject("error").getString("message"), getApplicationContext());
                    }
 
                } catch (JSONException e) {
                	Functions.toastString(getResources().getString(R.string.error_json), getBaseContext());
                    Log.d(TAG, e.getMessage()+"\\"+response.toString());
                }
            }
        }, new Response.ErrorListener() {
 
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        }) {
 
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("gcm_registration_id", token);
 
                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
	}

	private void unsubscribeToTopic(String topic) {
		// TODO Auto-generated method stub
		GcmPubSub mPubSub = GcmPubSub.getInstance(getApplicationContext());
		InstanceID mInstanceID = InstanceID.getInstance(getApplicationContext());
		String token = null;
		try {
			token = mInstanceID.getToken(Constants.GG_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			if (token != null) {
				mPubSub.unsubscribe(token, "");
				Log.d(TAG, "Unsubscribed to topic: " + topic);
			} else {
				Log.e(TAG, "error: gcm registration id is null");
			}
		} catch(Exception e) {
			Log.e(TAG, "Unsubscribe error. Topic: " + topic + ", error: " + e.getMessage());
		}
	}

	private void subscribeToTopic(String topic) {
		// TODO Auto-generated method stub
		GcmPubSub mPubSub = GcmPubSub.getInstance(getApplicationContext());
		InstanceID mInstanceID = InstanceID.getInstance(getApplicationContext());
		String token = null;
		try {
			token = mInstanceID.getToken(Constants.GG_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			if (token != null) {
				mPubSub.subscribe(token, "/topics/" + topic, null);
				Log.d(TAG, "Subscribed to topic: " + topic);
			} else {
				Log.e(TAG, "error: gcm registration id is null");
			}
		} catch(Exception e) {
			Log.e(TAG, "Topic subscribe error. Topic: " + topic + ", error: " + e.getMessage());
		}
	}
}
