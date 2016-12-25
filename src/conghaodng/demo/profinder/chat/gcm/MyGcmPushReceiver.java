package conghaodng.demo.profinder.chat.gcm;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GcmListenerService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import conghaodng.demo.profinder.InfoDialog;
import conghaodng.demo.profinder.LoginActivity;
import conghaodng.demo.profinder.MainActivity;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.Config;
import conghaodng.demo.profinder.chat.ListChat;
import conghaodng.demo.profinder.chat.MyChat;
import conghaodng.demo.profinder.chat.utils.ChatNotificationUtils;
import conghaodng.demo.profinder.chat.utils.MessageData;
import conghaodng.demo.profinder.chat.utils.Sender;
import conghaodng.demo.profinder.fragments.FrgPerson;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.SQLiteHandler;
import conghaodng.demo.profinder.utils.SessionManager;

public class MyGcmPushReceiver extends GcmListenerService {
	private static final String TAG = MyGcmPushReceiver.class.getSimpleName();
    private ChatNotificationUtils chatNotiUtils;
    private SessionManager session;
    @Override
    public void onMessageReceived(String from, Bundle bundle) {
    	// TODO Auto-generated method stub
    	String title = bundle.getString("title");
    	Boolean isBackground = Boolean.valueOf(bundle.getString("is_background"));
    	String flag = bundle.getString("flag");
        String data = bundle.getString("data");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "isBackground: " + isBackground);
        Log.d(TAG, "flag: " + flag);
        Log.d(TAG, "data: " + data);
        
        if (flag == null)
        	return;
        
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) { // not log in
        	return;
        }
    	
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }
        
        switch (Integer.parseInt(flag)) {
        case Config.PUSH_TYPE_CHATROOM:
            // push notification belongs to a chat room
            processChatRoomPush(title, isBackground, data);
            break;
        case Config.PUSH_TYPE_USER:
            // push notification is specific to user
            processUserMessage(title, isBackground, data);
            break;
        case Config.PUSH_INFO:
            // push notification is announcement
            processInfoMessage(title, isBackground, data);
            break;
        case Config.PUSH_CHOOSING:
            // push notification is announcement
            processChoosingMessage(title, isBackground, data);
            break;    
        }
    }

	private void processChatRoomPush(String title, Boolean isBackground, String data) {
		// TODO Auto-generated method stub
		if (!isBackground) {
            try {
                JSONObject dataObj = new JSONObject(data);
 
                JSONObject mObj = dataObj.getJSONObject("message");
                MessageData message = new MessageData();
                message.setMessage(mObj.getString("message"));
                message.setId(mObj.getString("message_id"));
                message.setCreatedAt(mObj.getString("created_at"));
                String chatRoomId = mObj.getString("chat_room_id");
 
                JSONObject uObj = dataObj.getJSONObject("user");
 
                // skip the message if the message belongs to same user as
                // the user would be having the same message when he was sending
                // but it might differs in your scenario
                if (uObj.getString("uId").equals(Variables.userID)) {
                    Log.e(TAG, "Skipping the push message as it belongs to same user");
                    return;
                }
 
                Sender sender = new Sender();
                sender.setId(uObj.getString("uId"));
                sender.setEmail(uObj.getString("uEmail"));
                sender.setName(uObj.getString("uName"));
                message.setSender(sender);
 
                // verifying whether the app is in background or foreground
                if (!Functions.isAppIsInBackground(getApplicationContext())) {
 
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_CHAT_LIVE);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_CHATROOM);
                    pushNotification.putExtra("message", message); // 1 message
                    pushNotification.putExtra("chat_room_id", chatRoomId);
                    pushNotification.putExtra("chat_room_name", message.getSender().getName());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
 
                    // play notification sound
                    Functions.playNotificationSound(getApplicationContext());
                } else {
 
                    // app is in background. show the message in notification try
                    Intent resultIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    resultIntent.putExtra("what_is_going_to", Constants.GO_TO_MYCHAT);
                    resultIntent.putExtra("chat_room_id", chatRoomId);
                    resultIntent.putExtra("chat_room_name", message.getSender().getName());
                    generateTextNoti(getApplicationContext(), title, sender.getName() + " : " + message.getMessage(), message.getCreatedAt(), resultIntent);
                }
 
            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
            }
        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
	}

	private void processUserMessage(String title, Boolean isBackground, String data) {
		// TODO Auto-generated method stub
		if (!isBackground) {
			 
            try {
                JSONObject dataObj = new JSONObject(data);
 
                JSONObject mObj = dataObj.getJSONObject("message");
                MessageData message = new MessageData();
                message.setMessage(mObj.getString("message"));
                message.setId(mObj.getString("message_id"));
                message.setCreatedAt(mObj.getString("created_at"));
                message.setFileType(mObj.getInt("fileType"));
                String chatRoomId = mObj.getString("chat_room_id");
 
                JSONObject uObj = dataObj.getJSONObject("user");
                Sender sender = new Sender();
                sender.setId(uObj.getString("uId"));
                sender.setEmail(uObj.getString("uEmail"));
                sender.setName(uObj.getString("uName"));
                message.setSender(sender);
                
                String imageUrl = "";
                if (mObj.getInt("fileType") == 1) {
                	imageUrl = mObj.getString("message");
                }
 
                // verifying whether the app is in background or foreground
                if (!Functions.isAppIsInBackground(getApplicationContext())) {
 
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_CHAT_LIVE);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_USER);
                    pushNotification.putExtra("message", message);
                    pushNotification.putExtra("chat_room_id", chatRoomId);
                    pushNotification.putExtra("chat_room_name", message.getSender().getName());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
 
                    // play notification sound
                    Functions.playNotificationSound(getApplicationContext());
                } else {
 
                    // app is in background. show the message in notification try
                	Intent resultIntent = new Intent(getApplicationContext(), LoginActivity.class);
                	if (chatRoomId.equals("0") || chatRoomId.equals("1")) { // Just announcement from Admin, go Main Activity
                		resultIntent.putExtra("what_is_going_to", "");
                	} else { // go MyChat
                		resultIntent.putExtra("what_is_going_to", Constants.GO_TO_MYCHAT);
                		resultIntent.putExtra("chat_room_id", chatRoomId);
                        resultIntent.putExtra("chat_room_name", message.getSender().getName());
                	}
                    // check for push notification image attachment
                    if (TextUtils.isEmpty(imageUrl)) {
                    	generateTextNoti(getApplicationContext(), title, sender.getName() + " : " + message.getMessage(), message.getCreatedAt(), resultIntent);
                    } else {
                        // push notification contains image
                        // show it with the image
                    	generateBigNoti(getApplicationContext(), title, message.getMessage(), message.getCreatedAt(), resultIntent, imageUrl);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
            }
 
        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
	}
	
	private void processInfoMessage(String title, Boolean isBackground, String data) {
		// TODO Auto-generated method stub
		if (!isBackground) {
			 
			try {
                JSONObject dataObj = new JSONObject(data);
 
                int user = dataObj.getInt("user");
                int type = dataObj.getInt("type");
                String jMSG = dataObj.getString("message");
                JSONObject msgObj = new JSONObject(jMSG);
                String mTitle = msgObj.getString("title");
                
                if (Variables.userID.equals("")) {
                	// get current user id logged in in db
                	SQLiteHandler db = new SQLiteHandler(getBaseContext());
                	String curUserID = db.getUserID();
                	if (user == 0 || curUserID.equals(String.valueOf(user))) {
                        if (!Functions.isAppIsInBackground(getApplicationContext())) { // foreground
                        	Intent resultIntent = new Intent(getApplicationContext(), InfoDialog.class);
	    	                resultIntent.putExtra("msg", jMSG.toString());
	    	                resultIntent.putExtra("isBackgroud", 0);
	    	                generateTextNoti(getApplicationContext(), title, mTitle, "", resultIntent);
                        } else { // background
	    	                Intent resultIntent = new Intent(getApplicationContext(), InfoDialog.class);
	    	                resultIntent.putExtra("msg", jMSG.toString());
	    	                resultIntent.putExtra("isBackgroud", 1);
	    	                generateTextNoti(getApplicationContext(), title, mTitle, "", resultIntent);
                        }
                    }
                } else {
	                if (user == 0 || Variables.userID.equals(String.valueOf(user))) {
	                	if (!Functions.isAppIsInBackground(getApplicationContext())) { // foreground
                        	Intent resultIntent = new Intent(getApplicationContext(), InfoDialog.class);
	    	                resultIntent.putExtra("msg", jMSG.toString());
	    	                resultIntent.putExtra("isBackgroud", 0);
	    	                generateTextNoti(getApplicationContext(), title, mTitle, "", resultIntent);
                        } else { // background
	    	                Intent resultIntent = new Intent(getApplicationContext(), InfoDialog.class);
	    	                resultIntent.putExtra("msg", jMSG.toString());
	    	                resultIntent.putExtra("isBackgroud", 1);
	    	                generateTextNoti(getApplicationContext(), title, mTitle, "", resultIntent);
                        }
	                }
                }
            
			} catch (JSONException e) {
				Log.e(TAG, "json parsing error: " + e.getMessage());
			}
 
        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
	}
	private void processChoosingMessage(String title, Boolean isBackground, String data) {
		// TODO Auto-generated method stub
		if (!isBackground) {
			 
			try {
                JSONObject dataObj = new JSONObject(data);
 
                String users = dataObj.getString("users");
                String[] usersArr = users.split(",");
                int id_chsg = dataObj.getInt("id_chsg");
                String fromChsg = dataObj.getString("from_chsg");
                String[] fromChsgArr = fromChsg.split(",");
                String jMSG = dataObj.getString("message");
                JSONObject msgObj = new JSONObject(jMSG);
                String mTitle = msgObj.getString("title");
                
                if (Variables.userID.equals("")) {
                	// get current user id logged in in db
                	SQLiteHandler db = new SQLiteHandler(getBaseContext());
                	String curUserID = db.getUserID();
                	int index = Arrays.asList(usersArr).indexOf(curUserID);
                	if (index != -1) {
                		if (!Functions.isAppIsInBackground(getApplicationContext())) { // foreground
                			
	    	                Intent pushNotification = new Intent(Config.PUSH_CHOOSING_LIVE);
	                        pushNotification.putExtra("id_chsg", id_chsg);
	                        pushNotification.putExtra("from_chsg", Integer.parseInt(fromChsgArr[index]));
	                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
	     
	                        // play notification sound
	                        Functions.playNotificationSound(getApplicationContext());
                		} else { // background
                			Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
			                resultIntent.putExtra("action", Constants.ACTION_PERSON);
			                resultIntent.putExtra("id_chsg", id_chsg);
			                resultIntent.putExtra("from_chsg", Integer.parseInt(fromChsgArr[index]));
	    	                resultIntent.putExtra("isBackgroud", 1);
	    	                generateTextNoti(getApplicationContext(), title, mTitle, "", resultIntent);
                		}
                    }
                } else {
                	int index = users.indexOf(Variables.userID);
                	if (index != -1) {
	                	if (!Functions.isAppIsInBackground(getApplicationContext())) { // foreground
	                		Intent pushNotification = new Intent(Config.PUSH_CHOOSING_LIVE);
	                		pushNotification.putExtra("id_chsg", id_chsg);
	                        pushNotification.putExtra("from_chsg", Integer.parseInt(fromChsgArr[index]));
	                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
	     
	                        // play notification sound
	                        Functions.playNotificationSound(getApplicationContext());
                		} else { // background
                			Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
			                resultIntent.putExtra("action", Constants.ACTION_PERSON);
			                resultIntent.putExtra("id_chsg", id_chsg);
			                resultIntent.putExtra("from_chsg", Integer.parseInt(fromChsgArr[index]));
	    	                resultIntent.putExtra("isBackgroud", 1);
	    	                generateTextNoti(getApplicationContext(), title, mTitle, "", resultIntent);
                		}
	                }
                }
            
			} catch (JSONException e) {
				Log.e(TAG, "json parsing error: " + e.getMessage());
			}
 
        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
	}

	private void generateTextNoti(Context context, String title, String message, String timestamp,
			Intent resultIntent) {
		// TODO Auto-generated method stub
		chatNotiUtils = new ChatNotificationUtils(context);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        chatNotiUtils.showNotificationMessage(title, message, timestamp, resultIntent);
	}

	private void generateBigNoti(Context context, String title, String message, String timestamp,
			Intent resultIntent, String image) {
		// TODO Auto-generated method stub
		chatNotiUtils = new ChatNotificationUtils(context);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        chatNotiUtils.showNotificationMessage(title, message, timestamp, resultIntent, image);
	}
}
