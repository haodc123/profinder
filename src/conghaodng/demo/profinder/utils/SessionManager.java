package conghaodng.demo.profinder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import conghaodng.demo.profinder.global.Constants;

public class SessionManager {
	// LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();
 
    // Shared Preferences
    SharedPreferences pref;
 
    Editor editor;
    Context _context;
 
    // Shared pref mode
    int PRIVATE_MODE = 0;
 
    // Shared preferences file name
    private static final String PREF_LOGIN = Constants.APP_NAME+"_Login";
    private static final String KEY_USER_TYPE = "uType";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
 
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_LOGIN, PRIVATE_MODE);
        editor = pref.edit();
    }
 
    public void setLogin(boolean isLoggedIn, int userType) {
 
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putInt(KEY_USER_TYPE, userType);
        
        // commit changes
        editor.commit();
 
        Log.d(TAG, "User login session modified!");
    }
     
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public int getUserType(){
        return pref.getInt(KEY_USER_TYPE, 0);
    }
}
