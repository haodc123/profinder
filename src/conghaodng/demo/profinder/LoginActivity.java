package conghaodng.demo.profinder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import conghaodng.demo.profinder.chat.MyChat;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.MyDiskCache;
import conghaodng.demo.profinder.utils.SQLiteHandler;
import conghaodng.demo.profinder.utils.SessionManager;

public class LoginActivity extends Activity {
	// FB
	private LoginButton bt_fb_login;
	private CallbackManager callbackManager;
	
	private EditText edt_login_tel, edt_login_pass;
	private Button bt_login_login;
	private TextView tvTitle, tv_login_forgotpass, tv_login_reg, tv_login_guide;
	
	private static final String TAG = LoginActivity.class.getSimpleName();
	private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private HashMap<String, String> userSaved;
    
    private String wigt, chatRoomId, chatRoomName;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		setInitCache();
		
		getBundle();
		FacebookSdk.sdkInitialize(getApplicationContext());
		
		setContentView(R.layout.activity_login);
		
		setInitView(); // initial view object
		
		initFacebook();
		
		prepareVariable();
		
		goIfLoggedIn();
		
	}
	
	private void setInitCache() {
		// TODO Auto-generated method stub
		Variables.mDCache = new MyDiskCache(this, Constants.APP_NAME, Constants.DISK_CACHE_SIZE);
	}

	private void getBundle() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
        wigt = intent.getStringExtra("what_is_going_to");
        if (wigt.equals(Constants.GO_TO_MYCHAT)) {
        	chatRoomId = intent.getStringExtra("chat_room_id");
            chatRoomName = intent.getStringExtra("chat_room_name");
        } 
	}

	private void initFacebook() {
		// TODO Auto-generated method stub	
		bt_fb_login.setReadPermissions(Arrays.asList("email"));
		bt_fb_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!Functions.hasConnection(getBaseContext())) {
					Functions.toastString(getResources().getString(R.string.alert_no_internet), getBaseContext());
				}
			}
		});
		
		callbackManager = CallbackManager.Factory.create();
		
		bt_fb_login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
		    @Override
		    public void onSuccess(LoginResult loginResult) {

		    	getFacebookProfile(loginResult);
		    	
		    }
		    @Override
		    public void onCancel() {}
		    @Override
		    public void onError(FacebookException e) {
		    	Log.d(TAG, e.getMessage());
		    }
		});
	}

	protected void onLogIn() {
		// TODO Auto-generated method stub
		String email = edt_login_tel.getText().toString().trim();
	    String password = edt_login_pass.getText().toString().trim();
	
	    int isEmail = 1;
	    if (Functions.isEmailValid(email) != true && Functions.isPhoneValid(email) != true) {
	    	Functions.toastString(getResources().getString(R.string.alert_input_email), getBaseContext());
	    } else {
	    	if (Functions.isPhoneValid(email) == true) {
	    		isEmail = 0;
	    	}
	    	if (!email.isEmpty() && !password.isEmpty()) {
		    	if (!Functions.hasConnection(this)) {
					Functions.toastString(getResources().getString(R.string.alert_no_internet), this);
				} else {
					// login user
		            requestServer("login", "", email, "", "", password, isEmail);
				}
		    } else {
		        // Prompt user to enter credentials
		        Functions.toastString("Vui lòng điền đầy đủ email, password", this);
		    }
	    }
	}
	protected void getFacebookProfile(LoginResult loginResult) {
		// TODO Auto-generated method stub
		Variables.userFBID = loginResult.getAccessToken().getUserId();
        Variables.userType = 0;
        Variables.userToken = loginResult.getAccessToken().getToken();
        
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v(TAG, response.toString());

                        // Application code
                        int isEmail = 1;
                        try {
                        	Variables.userName = object.getString("name");
                        	if (object.has("telephone")) {
                        		Variables.userTel = object.getString("telephone");
                        		isEmail = 0;
                        	} else if (object.has("email")) {
                        		Variables.userEmail = object.getString("email");
                        	}
                        	String gender = "";
                        	if (object.has("gender"))
                        		gender = object.getString("gender");
                        	if (gender.equalsIgnoreCase("male"))
                        		Variables.userSex = 1;
                        	else
                        		Variables.userSex = 2;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                        // Send info to server
                        if (isEmail == 0)
                        	requestServer("fb-login", Variables.userName, Variables.userTel, Variables.userFBID, Variables.userToken, "", 0);
                        else
                        	requestServer("fb-login", Variables.userName, Variables.userEmail, Variables.userFBID, Variables.userToken, "", 1);
                       
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
        
	}
	/**
     * function to verify login details in mysql db
     * */
    private void requestServer(final String type, final String name, final String email,
    		final String id, final String token, final String password, final int isEmail) {

        pDialog.setMessage("Đang đăng nhập ...");
        showDialog();
 
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_LOGIN, new Response.Listener<String>() {
 
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();
 
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in

                    	if (type.equals("login")) {
	                        JSONObject user = jObj.getJSONObject("user");
	                        Variables.userID = user.getString("userid");
	                        Variables.userType = 1;
	                        Variables.userName = user.getString("name");
	                        Variables.userAvatar = user.getString("avatar");
	                        Variables.userEmail = user.getString("email");
	                        Variables.userTel = user.getString("tel");
	                        Variables.userAddress = user.getString("address");
	                        Variables.userSex = user.getInt("sex");
	                        Variables.userToken = user.getString("token");
	
	                        // Create login session for reg user
	                        session.setLogin(true, 1);
	                        
	                        // Inserting row in users table: reg user
	                        db.addUser("", Variables.userName, Variables.userAvatar, Variables.userEmail, Variables.userTel, Variables.userAddress, Variables.userSex, Variables.userID, Variables.userToken, 1);
	                        goSomeWhere();
                    	} else if (type.equals("fb-login")) {
                    		int isFirstTime = jObj.getInt("isFirstTime");
                    		JSONObject user = jObj.getJSONObject("user");
	                        Variables.userID = user.getString("userid");
	                        Variables.userType = 0;
	                        if (isFirstTime == 0) {
		                        Variables.userName = user.getString("name");
		                        Variables.userEmail = user.getString("email");
		                        Variables.userTel = user.getString("tel");
		                        Variables.userAddress = user.getString("address");
		                        Variables.userSex = user.getInt("sex");
		                        Variables.userToken = user.getString("token");
	                        }
	                        
                    		// Create login session for facebook user
                            session.setLogin(true, 0);
                            db.addUser(Variables.userFBID, Variables.userName, "", Variables.userEmail, Variables.userTel, Variables.userAddress, Variables.userSex, Variables.userID, Variables.userToken, 0);
                            
                            if (isFirstTime == 0) {	
                            	goSomeWhere();
                            } else { // This is first-time-user, so need to update some info beyong of facebook
                            	Intent i = new Intent(LoginActivity.this, RegFBActivity.class);
                            	startActivity(i);
                            	finish();
                            }
                    	}
                    	
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("msg_err");
                        Functions.toastString("Lỗi đăng nhập: "+errorMsg, getApplicationContext());
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Functions.toastString(getResources().getString(R.string.error_json), getApplicationContext());
                    Log.d(TAG, e.getMessage()+"\\"+response.toString());
                    if (Variables.userType == 0) {
                		/**
                		 * If this is FB user and has error when try to connect to our server,
                		 * roll back facebook login status
                		 */
                		LoginManager.getInstance().logOut();
                	}
                }
 
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());

                Functions.toastString(getResources().getString(R.string.error_server_connect), getApplicationContext());
                hideDialog();
                if (Variables.userType == 0) {
            		/**
            		 * If this is FB user and has error when try to connect to our server,
            		 * roll back facebook login status
            		 */
            		LoginManager.getInstance().logOut();
            	}
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", type);
                params.put("name", name);
                params.put("email", email);
                params.put("fbid", id);
                params.put("token", token);
                params.put("password", password);
                params.put("isEmail", String.valueOf(isEmail));
 
                return params;
            }
 
        };
 
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, type);
    }
    protected void goSomeWhere() {
		// TODO Auto-generated method stub
    	Intent intent = null;
    	if (wigt.equals(Constants.GO_TO_MYCHAT)) {
    		intent = new Intent(LoginActivity.this, MyChat.class);
    		intent.putExtra("chat_room_id", chatRoomId);
    		intent.putExtra("chat_room_name", chatRoomName);
    	} else {
	    	// Launch main activity
	        intent = new Intent(LoginActivity.this, MainActivity.class);
	        intent.putExtra("action", Constants.ACTION_LISTCONNECT);
    	}
    	startActivity(intent);
        finish();
	}

	private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
 
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    callbackManager.onActivityResult(requestCode, resultCode, data);
	}
	
	private void goIfLoggedIn() {
		// TODO Auto-generated method stub
		if (session.isLoggedIn()) {
			// Assign logged in info to global variables
			Log.d(TAG, "logged in !!!");
			if (session.getUserType() == 1) {
				userSaved = db.getUserDetails();
				Variables.userID = userSaved.get(SQLiteHandler.KEY_UID);
				Variables.userType = 1;
				Variables.userName = userSaved.get(SQLiteHandler.KEY_NAME);
				Variables.userAvatar = userSaved.get(SQLiteHandler.KEY_AVATAR);
				Variables.userEmail = userSaved.get(SQLiteHandler.KEY_EMAIL);
				Variables.userTel = userSaved.get(SQLiteHandler.KEY_TELEPHONE);
				Variables.userAddress= userSaved.get(SQLiteHandler.KEY_ADDRESS);
				Variables.userSex= Integer.parseInt(userSaved.get(SQLiteHandler.KEY_USER_SEX));
				Variables.userToken = userSaved.get(SQLiteHandler.KEY_TOKEN);
			} else {
				userSaved = db.getUserDetails();
				Variables.userFBID = userSaved.get(SQLiteHandler.KEY_FBID);
				Variables.userID = userSaved.get(SQLiteHandler.KEY_UID);
				Variables.userType = 0;
				Variables.userName = userSaved.get(SQLiteHandler.KEY_NAME);
				Variables.userEmail = userSaved.get(SQLiteHandler.KEY_EMAIL);
				Variables.userTel = userSaved.get(SQLiteHandler.KEY_TELEPHONE);
				Variables.userAddress= userSaved.get(SQLiteHandler.KEY_ADDRESS);
				Variables.userSex= Integer.parseInt(userSaved.get(SQLiteHandler.KEY_USER_SEX));
				Variables.userToken = userSaved.get(SQLiteHandler.KEY_TOKEN);
			}
			
			if (Variables.userID != null) {
				if (!Variables.userID.equals("")) {

					goSomeWhere();

				} else {
					if (Variables.userType == 0) {
                		// FB user
                		LoginManager.getInstance().logOut();
                	}
                	db.deleteUser();
                	db.deleteChoosing();
					session.setLogin(false, 0);
				}
			} else {
				// Variables.userToken = null => Variables.userType = null => no check
            	db.deleteUser();
            	db.deleteChoosing();
				session.setLogin(false, 0);
			}
        }
	}
	
	private void prepareVariable() {
		// TODO Auto-generated method stub
		// SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Session manager
        session = new SessionManager(getApplicationContext());
        userSaved = new HashMap<String, String>();
	}

	private void setInitView() {
		// TODO Auto-generated method stub
		//setCustomActionBar();
		
		bt_fb_login = (LoginButton)findViewById(R.id.bt_fb_login);
		
		edt_login_tel = (EditText)findViewById(R.id.edt_login_tel);
		edt_login_pass = (EditText)findViewById(R.id.edt_login_pass);
		bt_login_login = (Button)findViewById(R.id.bt_login_login);
		tv_login_forgotpass = (TextView)findViewById(R.id.tv_login_forgotpass);
		tv_login_reg = (TextView)findViewById(R.id.tv_login_reg);
		tv_login_guide = (TextView)findViewById(R.id.tv_login_guide);
		
		// Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        
		bt_login_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onLogIn();
			}
		});
		tv_login_forgotpass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onForgotPass();
			}
		});
		tv_login_reg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onReg();
			}
		});
	}

	protected void onReg() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(LoginActivity.this, RegActivity.class);
        startActivity(intent);
        finish();
	}

	protected void onForgotPass() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(LoginActivity.this, FGPActivity.class);
        startActivity(intent);
	}
    @Override
    protected void onResume() {
      super.onResume();

      // Logs 'install' and 'app activate' App Events.
      AppEventsLogger.activateApp(this);
    }
    
    @Override
    protected void onPause() {
      super.onPause();

      // Logs 'app deactivate' App Event.
      AppEventsLogger.deactivateApp(this);
    }
	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }
	
}
