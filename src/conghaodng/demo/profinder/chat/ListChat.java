package conghaodng.demo.profinder.chat;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.MainActivity;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.utils.ChatNotificationUtils;
import conghaodng.demo.profinder.chat.utils.ChatRoomAdapter;
import conghaodng.demo.profinder.chat.utils.ChatRoomData;
import conghaodng.demo.profinder.chat.utils.MessageData;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.SQLiteHandler;
import conghaodng.demo.profinder.utils.SessionManager;

public class ListChat extends Activity {
	private SessionManager session;
	private SQLiteHandler db;
	private ListView lv_listchat_contain;
	private TextView tvTitle;
	private static final String TAG = ListChat.class.getSimpleName();
	private ProgressDialog pDialog;
	
	private List<ChatRoomData> crList;
	private ChatRoomAdapter crAdapter;
	
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private int iChsg;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setInitView();
		initVariables();
		setData();
		setBroadcastReceiver();
	}
	private void setBroadcastReceiver() {
		// TODO Auto-generated method stub
		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
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
            String chatRoomId = intent.getStringExtra("chat_room_id");
 
            if (message != null && chatRoomId != null) {
                updateRow(chatRoomId, message);
            }
	}
	protected void handlePushChoosing(Intent intent) {
		// TODO Auto-generated method stub
		iChsg = intent.getIntExtra("chsgId", 0);
		Functions.toastString(getResources().getString(R.string.ann_have_partner), this);
	}
	
	private void updateRow(String chatRoomId, MessageData message) {
		// TODO Auto-generated method stub
		for (ChatRoomData cr : crList) {
            if (cr.getId().equals(chatRoomId)) {
                int index = crList.indexOf(cr);
                cr.setLastMessage(message.getMessage());
                cr.setUnreadCount(cr.getUnreadCount() + 1);
                crList.remove(index);
                crList.add(index, cr);
                break;
            }
        }
        crAdapter.notifyDataSetChanged();
	}
	public void setData() {
		// TODO Auto-generated method stub
		if (crList == null)
			crList = new ArrayList<ChatRoomData>();
		if (crAdapter == null)
			crAdapter = new ChatRoomAdapter(this, crList);
		
		lv_listchat_contain.setAdapter(crAdapter);
		
	}
	private void getListChatroom() {
		// TODO Auto-generated method stub
		if (Functions.hasConnection(this)) {
			// get list chat_room
			pDialog.setMessage("Đang tải dữ liệu...");
	        showDialog();
	        String endPoint = Constants.CHAT_ROOMS_LIST.replace("_ID_", Variables.userID);
			StringRequest strReq = new StringRequest(Request.Method.GET,
					endPoint, new Response.Listener<String>() {
	            @Override
	            public void onResponse(String response) {
	                Log.e(TAG, "response: " + response);
	                hideDialog();
	                try {
	                    JSONObject obj = new JSONObject(response);
	 
	                    if (obj.getBoolean("error") == false) {
	                        JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
	                        
	                        crList.clear();
	                        for (int i = 0; i < chatRoomsArray.length(); i++) {
	                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
	                            ChatRoomData cr = new ChatRoomData();
	                            cr.setId(chatRoomsObj.getString("chat_room_id"));
	                            cr.setName(chatRoomsObj.getString("name"));
	                            cr.setTimestamp(chatRoomsObj.getString("created_at"));
	                            cr.setLastMessage(chatRoomsObj.getString("lastMsg"));
	 
	                            crList.add(cr);
	                    		
	                        }
	                        crAdapter.notifyDataSetChanged();
	                    } else {
	                        Log.e(TAG, "get list chatroom error: " + response);
	                    }
	                } catch (JSONException e) {
	                    Log.e(TAG, "json parsing error: " + response);
	                }
	            }
	        }, new Response.ErrorListener() {
	            @Override
	            public void onErrorResponse(VolleyError error) {
	                NetworkResponse networkResponse = error.networkResponse;
	                Log.e(Constants.APP_NAME, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
	                hideDialog();
	            }
	        });
	        AppController.getInstance().addToRequestQueue(strReq);
		}
	}
	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}
	 
	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
	private void initVariables() {
		// TODO Auto-generated method stub
		session = new SessionManager(getApplicationContext());
		db = new SQLiteHandler(getApplicationContext());
	}
	private void setInitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_listchat);
		setCustomActionBar();
		lv_listchat_contain = (ListView)findViewById(R.id.lv_listchat_contain);
		pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
	}
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_listchat));
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getListChatroom();
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