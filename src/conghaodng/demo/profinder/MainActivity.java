package conghaodng.demo.profinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.login.LoginManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import conghaodng.demo.profinder.chat.Config;
import conghaodng.demo.profinder.chat.ListChat;
import conghaodng.demo.profinder.chat.MyChat;
import conghaodng.demo.profinder.chat.gcm.GcmIntentService;
import conghaodng.demo.profinder.chat.utils.ChatNotificationUtils;
import conghaodng.demo.profinder.chat.utils.ChatRoomData;
import conghaodng.demo.profinder.chat.utils.MessageData;
import conghaodng.demo.profinder.fragments.FrgCat;
import conghaodng.demo.profinder.fragments.FrgField;
import conghaodng.demo.profinder.fragments.FrgLearn.FrgListener;
import conghaodng.demo.profinder.fragments.FrgLearn;
import conghaodng.demo.profinder.fragments.FrgListChsg;
import conghaodng.demo.profinder.fragments.FrgListConnect;
import conghaodng.demo.profinder.fragments.FrgListMeet;
import conghaodng.demo.profinder.fragments.FrgPerson;
import conghaodng.demo.profinder.fragments.FrgSettings;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.SQLiteHandler;
import conghaodng.demo.profinder.utils.SessionManager;

public class MainActivity extends Activity implements FrgListener {
	private SessionManager session;
	private SQLiteHandler db;
	
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private List<ChatRoomData> chatRoomList;
    
    private Bundle savedInstanceState, b;
    private String action;
    private int id_chsg, from_chsg;
    
    // Announcement line
    private LinearLayout ll_ann_cover;
    private TextView tv_ann_content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;
		setInitView();
		initVariables();
		getBundle();
		
		if (Variables.isUpdatedInfo == 0 && Functions.hasConnection(this))
			getUpdateInfo();
		
		setBroadcastReceiver();
		
		if (Functions.checkPlayServices(this)) {
            registerGCM();
        }
		
		setFragments(savedInstanceState);
	}
	private void getBundle() {
		// TODO Auto-generated method stub
		b = getIntent().getExtras();
		action = b.getString("action");
		if (action.equals(Constants.ACTION_PERSON)) {
			id_chsg = b.getInt("id_chsg");
			from_chsg = b.getInt("from_chsg");
		}
	}
	private void setBroadcastReceiver() {
		// TODO Auto-generated method stub
		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    String token = intent.getStringExtra("token");
                    Variables.userGCMRegId = token;
                    subscrible();
                    subscribleGlobal();
                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                	Log.d(Constants.APP_NAME, "token has been sent to srver");
                } else if (intent.getAction().equals(Config.PUSH_CHAT_LIVE)) {
                	handlePushChat(intent);
                } else if (intent.getAction().equals(Config.PUSH_CHOOSING_LIVE)) {
                	handlePushChoosing(intent);
                }
            }
        };
	}
	
	protected void handlePushChat(Intent intent) {
		// TODO Auto-generated method stub
        MessageData message = (MessageData) intent.getSerializableExtra("message");
        final String crID = intent.getStringExtra("chat_room_id");
        final String crName = intent.getStringExtra("chat_room_name");
        ll_ann_cover.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        		FrameLayout.LayoutParams.MATCH_PARENT,      
        		FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(Functions.dpToPx(15), 0, Functions.dpToPx(15), Functions.dpToPx(80));
        params.gravity = Gravity.BOTTOM;
        ll_ann_cover.setLayoutParams(params);
        
        tv_ann_content.setText("Tin nhắn mới từ " + message.getSender().getName()+ ": " + message.getMessage());
        findViewById(R.id.tv_ann_link).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ll_ann_cover.setVisibility(View.GONE);
				Intent i = new Intent(getBaseContext(), MyChat.class);
				i.putExtra("chat_room_id", crID);
				i.putExtra("chat_room_name", crName);
				startActivity(i);
			}
		});
	}
	protected void handlePushChoosing(Intent intent) {
		// TODO Auto-generated method stub
		id_chsg = intent.getIntExtra("id_chsg", 0);
		from_chsg = intent.getIntExtra("from_chsg", 0);
        ll_ann_cover.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        		FrameLayout.LayoutParams.MATCH_PARENT,      
        		FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(Functions.dpToPx(15), 0, Functions.dpToPx(15), Functions.dpToPx(80));
        params.gravity = Gravity.BOTTOM;
        ll_ann_cover.setLayoutParams(params);
        
        tv_ann_content.setText(getResources().getString(R.string.ann_have_partner));
        findViewById(R.id.tv_ann_link).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ll_ann_cover.setVisibility(View.GONE);
				final FrgPerson f = (FrgPerson) getFragmentManager().findFragmentByTag(Constants.TAG_FRG_PERSON);
		    	if (f != null && f.isVisible()) {
		    		getFragmentManager().popBackStack();
		    	}
				FrgPerson pFrg = new FrgPerson();
				Bundle b = new Bundle();
				b.putInt("id_chsg", id_chsg);
				b.putInt("from_chsg", from_chsg);
				pFrg.setArguments(b);
			    getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, pFrg, Constants.TAG_FRG_PERSON)
			     .addToBackStack(null)
			     .commit();
			}
		});
	}
	private void subscrible() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(MainActivity.this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
        intent.putExtra(GcmIntentService.TOPIC, "user_" + Variables.userID);
        startService(intent);
	}
	private void subscribleGlobal() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
        intent.putExtra(GcmIntentService.TOPIC, Config.TOPIC_GLOBAL);
        startService(intent);
    }
	private void subscribeToAllTopics() {
		// get List chatroom
		chatRoomList = new ArrayList<ChatRoomData>();
		
		if (Functions.hasConnection(this)) {
			// get list chat_room
			String endPoint = Constants.CHAT_ROOMS_LIST.replace("_ID_", Variables.userID);
			StringRequest strReq = new StringRequest(Request.Method.GET,
					endPoint, new Response.Listener<String>() {
	            @Override
	            public void onResponse(String response) {
	                Log.e(Constants.APP_NAME, "response: " + response);
	 
	                try {
	                    JSONObject obj = new JSONObject(response);
	 
	                    if (obj.getBoolean("error") == false) {
	                        JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
	                        for (int i = 0; i < chatRoomsArray.length(); i++) {
	                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
	                            ChatRoomData cr = new ChatRoomData();
	                            cr.setId(chatRoomsObj.getString("chat_room_id"));
	 
	                            chatRoomList.add(cr);
	                            
	                            for (ChatRoomData crd : chatRoomList) {
	                                Intent intent = new Intent(MainActivity.this, GcmIntentService.class);
	                                intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
	                                intent.putExtra(GcmIntentService.TOPIC, "topic_" + crd.getId());
	                                startService(intent);
	                            }
	                            
	                        }
	                    } else {
	                        Log.e(Constants.APP_NAME, "get list chatroom error: " + response);
	                    }
	                } catch (JSONException e) {
	                    Log.e(Constants.APP_NAME, "json parsing error: " + e.getMessage());
	                }
	            }
	        }, new Response.ErrorListener() {
	            @Override
	            public void onErrorResponse(VolleyError error) {
	                NetworkResponse networkResponse = error.networkResponse;
	                Log.e(Constants.APP_NAME, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
	            }
	        });
	        AppController.getInstance().addToRequestQueue(strReq);
		}
    }
	private void registerGCM() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
	}
	private void setFragments(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            if (action.equals(Constants.ACTION_CATEGORIES)) {
	            FrgCat fragment = new FrgCat();
	            fragment.setArguments(getIntent().getExtras());
	            getFragmentManager().beginTransaction()
	                    .add(R.id.fragment_container, fragment).commit();
            } else if (action.equals(Constants.ACTION_LEARN)) {
	            FrgLearn fragment = new FrgLearn();
	            fragment.setArguments(getIntent().getExtras());
	            getFragmentManager().beginTransaction()
	                    .add(R.id.fragment_container, fragment).commit();
            } else if (action.equals(Constants.ACTION_FIELD)) {
	            FrgField fragment = new FrgField();
	            fragment.setArguments(getIntent().getExtras());
	            getFragmentManager().beginTransaction()
	                    .add(R.id.fragment_container, fragment).commit();
            } else if (action.equals(Constants.ACTION_LISTMEET)) {
            	FrgListMeet fragment = new FrgListMeet();
            	Bundle b = new Bundle();
				b.putInt("cId", 0);
				fragment.setArguments(b);
            	//listMeetFragment.setArguments(getIntent().getExtras());
	            getFragmentManager().beginTransaction()
	                    .add(R.id.fragment_container, fragment).commit();
            } else if (action.equals(Constants.ACTION_LISTCONNECT)) {
            	FrgListConnect fragment = new FrgListConnect();
            	fragment.setArguments(getIntent().getExtras());
	            getFragmentManager().beginTransaction()
	                    .add(R.id.fragment_container, fragment).commit();
            } else if (action.equals(Constants.ACTION_PERSON)) {
	            	FrgPerson fragment = new FrgPerson();
	            	Bundle b = new Bundle();
	            	b.putInt("id_chsg", id_chsg);
	            	b.putInt("from_chsg", from_chsg);
	            	fragment.setArguments(b);
	            	//listMeetFragment.setArguments(getIntent().getExtras());
		            getFragmentManager().beginTransaction()
		                    .add(R.id.fragment_container, fragment).commit();
            }
        }
	}
	private void initVariables() {
		// TODO Auto-generated method stub
		session = new SessionManager(getApplicationContext());
		db = new SQLiteHandler(getApplicationContext());
	}
	private void setInitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_main);
		ll_ann_cover = (LinearLayout)findViewById(R.id.ll_ann_cover);
		tv_ann_content = (TextView)findViewById(R.id.tv_ann_content);
		findViewById(R.id.bt_ann_close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ll_ann_cover.setVisibility(View.GONE);
			}
		});
	}
	
	@Override
	public void onFrgEvent(int value) {
		// TODO Auto-generated method stub
		
	}
	private void getUpdateInfo() {
		String tag = "Update info";
 
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_REFRESH_INFO, new Response.Listener<String>() {
 
            @Override
            public void onResponse(String response) {
                Log.d(Constants.APP_NAME, "UpdateInfo Response: " + response.toString());
 
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
	                    JSONObject user = jObj.getJSONObject("user");
	                    Variables.userName = user.getString("new_name");
	                    Variables.userAvatar = user.getString("new_avatar");
	                    Variables.userEmail = user.getString("new_email");
	                    Variables.userTel = user.getString("new_tel");
	                    Variables.userToken = user.getString("new_token");
	                        
	                    // Update row in users table:
	                    db.updateUser(Variables.userName, Variables.userAvatar, Variables.userEmail, Variables.userTel, Variables.userToken, Variables.userID);
	                    
	                    Variables.isUpdatedInfo = 1;
	                    
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("msg_err");
                        Functions.toastString("Lỗi khi cập nhật thông tin mới: "+errorMsg, getBaseContext());
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Functions.toastString(getResources().getString(R.string.error_json), getBaseContext());
                    Log.d(Constants.APP_NAME, e.getMessage()+"\\"+response.toString());
                }
 
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.APP_NAME, "UpdateInfo Error: " + error.getMessage());
                Functions.toastString(getResources().getString(R.string.error_server_connect), getBaseContext());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("sidUser", Variables.userID);
 
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag);
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

	@SuppressWarnings("static-access")
	@Override
	public void onBackPressed() {
	    if (getFragmentManager().getBackStackEntryCount() > 0) {
	    	// get Back
	    	try {
				Thread.currentThread().sleep(100);
				getFragmentManager().popBackStack();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	/*final FrgMain frgMain = (FrgMain) getFragmentManager().findFragmentByTag(Constants.TAG_FRG_MAIN);
	    	if (frgMain != null && frgMain.isVisible()) {
		    	// if press back from Main, exit app
		    	finishAffinity();
		    } else {
		    	getFragmentManager().popBackStack();
		    }*/
	    } else {
	        super.onBackPressed();
	    }
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu); 
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.mn_setting:
	    	final FrgSettings frst = (FrgSettings) getFragmentManager().findFragmentByTag(Constants.TAG_FRG_SETTING);
	    	if (frst != null && frst.isVisible()) {
		    	// if current fragment is FrgSetting, do nothing
		    } else {
		    	FrgSettings stFrg = new FrgSettings();
			    getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, stFrg, Constants.TAG_FRG_SETTING)
			     .addToBackStack(null)
			     .commit();
		    }
	        return true;
	    case R.id.mn_history:
	    	final FrgListChsg frlc = (FrgListChsg) getFragmentManager().findFragmentByTag(Constants.TAG_FRG_LISTCHSG);
	    	if (frlc != null && frlc.isVisible()) {
		    	// if current fragment is FrgListChsg, do nothing
		    } else {
		    	FrgListChsg lstChsgFrg = new FrgListChsg();
			    getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, lstChsgFrg, Constants.TAG_FRG_LISTCHSG)
			     .addToBackStack(null)
			     .commit();
		    }
	        return true;
	    case R.id.mn_listchat:
	    	Intent i = new Intent(this, ListChat.class);
	    	startActivity(i);
	        return true;
	    case R.id.mn_logout:
	    	lauchAlertLogoutDialog();
	        return true;
	    case R.id.mn_exit:
	    	lauchAlertExitDialog();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	private void lauchAlertExitDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dl_send = new AlertDialog.Builder(this);
        dl_send.setTitle(getResources().getString(R.string.app_name));
        	dl_send.setMessage(getResources().getString(R.string.alert_exit_title))
            .setPositiveButton(getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	
                	finishAffinity();
                }
            })
            .setNegativeButton(getResources().getString(R.string.bt_no), null);
           
        AlertDialog dialog = dl_send.create();
        dialog.show();
	}
	private void lauchAlertLogoutDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dl_send = new AlertDialog.Builder(this);
        dl_send.setTitle(getResources().getString(R.string.app_name));
        	dl_send.setMessage(getResources().getString(R.string.alert_logout_title))
            .setPositiveButton(getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	
                	if (Variables.userType == 0) {
                		// FB user
                		LoginManager.getInstance().logOut();
                	}
                	db.deleteUser();
                	db.deleteChoosing();
                	session.setLogin(false, 0);
                	
                	Intent i = new Intent(MainActivity.this, LoginActivity.class);
                	// set the new task and clear flags
                	i.putExtra("what_is_going_to", "");
                	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                	startActivity(i);
                }
            })
            .setNegativeButton(getResources().getString(R.string.bt_no), null);
           
        AlertDialog dialog = dl_send.create();
        dialog.show();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Variables.isUpdatedInfo = 0;
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_CHAT_LIVE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_CHOOSING_LIVE));
		
		ChatNotificationUtils.clearNotifications();
	}
	@Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}