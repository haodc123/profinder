package conghaodng.demo.profinder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import conghaodng.demo.profinder.chat.helper.ChatPreferenceManager;
import conghaodng.demo.profinder.global.Variables;
 
public class AppController extends Application {
 
    public static final String TAG = AppController.class.getSimpleName();
 
    private RequestQueue mRequestQueue;
 
    private static AppController mInstance;
    
    private ChatPreferenceManager chatPref;
 
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //printFBKeyHash();
        setSomeGlobalVariables();
    }
 
    public ChatPreferenceManager getPrefManager() {
    	if (chatPref == null)
    		return new ChatPreferenceManager(this);
    	return chatPref;
    }
    
    private void setSomeGlobalVariables() {
		// TODO Auto-generated method stub
    	Variables.curApiVersion = android.os.Build.VERSION.SDK_INT;
	}

	private void printFBKeyHash() {
		// TODO Auto-generated method stub
    	// Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "conghaodng.demo.profinder", 
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
            
        } catch (NoSuchAlgorithmException e) {
            
        }
	}

	public static synchronized AppController getInstance() {
        return mInstance;
    }
 
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
 
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
 
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
 
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}