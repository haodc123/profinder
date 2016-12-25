package conghaodng.demo.profinder.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.utils.ChatNotificationUtils;
import conghaodng.demo.profinder.chat.utils.MessageAdapter;
import conghaodng.demo.profinder.chat.utils.MessageData;
import conghaodng.demo.profinder.chat.utils.Sender;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;

public class MyChat extends Activity {
	private RecyclerView recyclerView;
	private EditText edt_message;
    private Button bt_send, bt_file, bt_capture, bt_smile, bt_rate;
	private TextView tvTitle;
	private static final String TAG = MyChat.class.getSimpleName();
	private ProgressDialog pDialog;
	private int msgIdTemp = -1; // use to mark message while send to server and wait for response from server  to update
	
	private List<MessageData> msgList;
	private MessageAdapter msgAdapter;
	
	private String chatRoomId = "";
	private String chatRoomName = "";
	
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    
    private Uri fileUri = null;
    
    // Icon
    private LinearLayout ll_smile, ll_rate;
    private ImageView[] img_smile;
    private ImageView[] img_ps_dorate = new ImageView[5];
    private int isDisplaySmileSet = 0;
    private int isDisplayRate = 0;
    private int refreshLocked = 0;
    private int doRate = 0;
    private String partner_rating;
    private String partner_id;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getBundle();
		setInitView();
		initVariables();
		
		setData();
		setBroadcastReceiver();
		
	}
	private void getBundle() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        chatRoomName = intent.getStringExtra("chat_room_name");
        if (chatRoomId == null) {
            Functions.toastString("Không tim thấy cuộc trò chuyện nào!", this);
            finish();
        }
	}
	private void setBroadcastReceiver() {
		// TODO Auto-generated method stub
		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Config.PUSH_CHAT_LIVE)) {
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
		String crID = intent.getStringExtra("chat_room_id");
        if (crID.equals(chatRoomId) && chatRoomId != null && message != null) {
        	if (!message.getSender().getId().equals(Variables.userID)) {
	            msgList.add(message);
	            msgAdapter.notifyDataSetChanged();
        	}
            if (msgAdapter.getItemCount() > 1) {
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, msgAdapter.getItemCount() - 1);
            }
        } else if (!crID.equals(chatRoomId) && chatRoomId != null && message != null) {
        	// other chat room
        	Functions.toastString("Tin nhắn mới từ " + message.getSender().getName()+ ": " + message.getMessage(), this);
        }
	}
	protected void handlePushChoosing(Intent intent) {
		// TODO Auto-generated method stub
		Functions.toastString(getResources().getString(R.string.ann_have_partner), this);
	}
	
	public void setData() {
		// TODO Auto-generated method stub
		msgList = new ArrayList<>();
		msgAdapter = new MessageAdapter(this, msgList, Variables.userID);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(msgAdapter);
	}
	private void getListMessage() {
		// TODO Auto-generated method stub
		if (!Functions.hasConnection(this)) {
			return;
		}
			// get list chat_room
			pDialog.setMessage("Đang tải dữ liệu...");
	        showDialog();
	        String endPoint = Constants.CHAT_THREAD.replace("_ID_", chatRoomId).replace("_ID2_", Variables.userID);
			StringRequest strReq = new StringRequest(Request.Method.GET,
					endPoint, new Response.Listener<String>() {
	            @Override
	            public void onResponse(String response) {
	                Log.e(TAG, "response: " + response);
	                hideDialog();
	                try {
	                    JSONObject obj = new JSONObject(response);
	 
	                    msgList.clear();
	                    if (obj.getBoolean("error") == false) {
	                        JSONArray messagesObj = obj.getJSONArray("messages");
	 
	                        for (int i = 0; i < messagesObj.length(); i++) {
	                            JSONObject messageObj = (JSONObject) messagesObj.get(i);
	 
	                            String commentId = messageObj.getString("message_id");
	                            String commentText = messageObj.getString("message");
	                            String createdAt = messageObj.getString("created_at");
	                            int fileType = messageObj.getInt("fileType");
	 
	                            JSONObject userObj = messageObj.getJSONObject("user");
	                            String userId = userObj.getString("user_id");
	                            String userName = userObj.getString("username");
	                            Sender user = new Sender(userId, userName, null);
	 
	                            MessageData message = new MessageData();
	                            message.setId(commentId);
	                            message.setMessage(commentText);
	                            message.setCreatedAt(createdAt);
	                            message.setFileType(fileType);
	                            message.setSender(user);
	 
	                            msgList.add(message);
	                        }
	 
	                        refreshRecyclerview();
	                        
	                        JSONObject psObj = obj.getJSONObject("person");
	                        partner_id = psObj.getString("person_id");
	                        partner_rating = psObj.getString("person_rating");
	                        getDoRatingOfUser(partner_rating);
	 
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
	
	protected void sendMessage(String msg) {
		final String msgContent = msg;
        if (TextUtils.isEmpty(msgContent)) {
            return;
        }
        // Update to List msg first:
        MessageData message = new MessageData();
        message.setId(String.valueOf(msgIdTemp));
        message.setMessage(msgContent);
        message.setCreatedAt("sending...");
        message.setFileType(0);
        Sender user = new Sender(Variables.userID, Variables.userName, null);
        message.setSender(user);
        msgList.add(message);
        msgAdapter.notifyDataSetChanged();
        if (msgAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, msgAdapter.getItemCount() - 1);
        }
        // Send to server and wait for response to update
        String endPoint = Constants.CHAT_SEND_MESSAGE.replace("_ID_", chatRoomId).replace("_ISGROUP_", "0");
        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);
 
                    // check for error
                    if (obj.getBoolean("error") == false) {
                        JSONObject msgObj = obj.getJSONObject("message");
 
                        String msgId = msgObj.getString("message_id");
                        String msgText = msgObj.getString("message");
                        String msgCreatedAt = msgObj.getString("created_at");
                        int fileType = msgObj.getInt("fileType");
 
                        JSONObject userObj = obj.getJSONObject("user");
                        String userId = userObj.getString("uId");
                        String userName = userObj.getString("uName");
                        Sender user = new Sender(userId, userName, null);
                        
                        String msgIdTempFromSV = obj.getString("msgIdTemp");
                        
                        // Replace temp message already existed with info updated from server
                        for (MessageData msg : msgList) {
                            if (msg.getId().equals(msgIdTempFromSV) || msg.getId().equals(msgId)) {
                                int index = msgList.indexOf(msg);
                                msg.setId(msgId);
                                msg.setMessage(msgText);
                                msg.setCreatedAt(msgCreatedAt);
                                msg.setSender(user);
                                msg.setFileType(fileType);
                                msgList.remove(index);
                                msgList.add(index, msg);
                                break;
                            }
                        }
                        refreshRecyclerview();
 
                    } else {
                        Functions.toastString(obj.getString("message").toString(), getApplicationContext());
                    }
                    Log.d(TAG, response.toString());
                } catch (JSONException e) {
                	Log.d(TAG, e.getMessage()+"\\"+response.toString());
                }
            }
        }, new Response.ErrorListener() {
 
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                edt_message.setText(msgContent);
            }
        }) {
 
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", Variables.userID);
                params.put("message", msgContent);
                params.put("fileType", "0");
                params.put("msgIdTemp", String.valueOf(msgIdTemp));
 
                Log.e(TAG, "Params: " + params.toString());
                
                msgIdTemp--; // we need msgIdTemp not duplicate with other msg
                return params;
            };
        };
 
 
        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
 
        strReq.setRetryPolicy(policy);
        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
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
	}
	private void setInitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_mychat);
		setCustomActionBar();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		edt_message = (EditText) findViewById(R.id.edt_message);
        bt_send = (Button) findViewById(R.id.bt_send);
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(edt_message.getText().toString().trim());
                edt_message.setText("");
            }
        });
        bt_file = (Button) findViewById(R.id.bt_file);
        bt_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeFile();
            }
        });
        bt_capture = (Button) findViewById(R.id.bt_capture);
        bt_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeCapture();
            }
        });
        bt_smile = (Button) findViewById(R.id.bt_smile);
        bt_smile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (isDisplaySmileSet == 0)
            		showSmileSet();
            	else 
            		hideSmileSet();
            }
        });
        bt_rate = (Button) findViewById(R.id.bt_rate);
        bt_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (isDisplayRate == 0)
            		showRate();
            	else 
            		hideRate();
            }
        });
		pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
       // Icon
        ll_smile = (LinearLayout)findViewById(R.id.ll_smile);
        hideSmileSet();
        ll_rate = (LinearLayout)findViewById(R.id.ll_rate);
        hideRate();
       
        img_smile = new ImageView[Constants.NUM_ICON];
        img_smile[0] = (ImageView)findViewById(R.id.img_smile0);
        img_smile[1] = (ImageView)findViewById(R.id.img_smile1);
        img_smile[2] = (ImageView)findViewById(R.id.img_smile2);
        img_smile[3] = (ImageView)findViewById(R.id.img_smile3);
        img_smile[4] = (ImageView)findViewById(R.id.img_smile4);
        img_smile[5] = (ImageView)findViewById(R.id.img_smile5);
        img_smile[6] = (ImageView)findViewById(R.id.img_smile6);
        img_smile[7] = (ImageView)findViewById(R.id.img_smile7);
        img_smile[8] = (ImageView)findViewById(R.id.img_smile8);
        img_smile[9] = (ImageView)findViewById(R.id.img_smile9);
        img_smile[10] = (ImageView)findViewById(R.id.img_smile10);
        img_smile[11] = (ImageView)findViewById(R.id.img_smile11);
        img_smile[0].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(0);
			}
		});
        img_smile[1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(1);
			}
		});
        img_smile[2].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(2);
			}
		});
        img_smile[3].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(3);
			}
		});
        img_smile[4].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(4);
			}
		});
        img_smile[5].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(5);
			}
		});
        img_smile[6].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(6);
			}
		});
        img_smile[7].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(7);
			}
		});
        img_smile[8].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(8);
			}
		});
        img_smile[9].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(9);
			}
		});
        img_smile[10].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(10);
			}
		});
        img_smile[11].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSelectSmile(11);
			}
		});
        
        img_ps_dorate[0] = (ImageView)findViewById(R.id.img_ps_dorate0);
		img_ps_dorate[1] = (ImageView)findViewById(R.id.img_ps_dorate1);
		img_ps_dorate[2] = (ImageView)findViewById(R.id.img_ps_dorate2);
		img_ps_dorate[3] = (ImageView)findViewById(R.id.img_ps_dorate3);
		img_ps_dorate[4] = (ImageView)findViewById(R.id.img_ps_dorate4);
		img_ps_dorate[0].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 1;
				displayDoRate(doRate);
				updateRate(doRate);
			}
		});
		img_ps_dorate[1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 2;
				displayDoRate(doRate);
				updateRate(doRate);
			}
		});
		img_ps_dorate[2].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 3;
				displayDoRate(doRate);
				updateRate(doRate);
			}
		});
		img_ps_dorate[3].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 4;
				displayDoRate(doRate);
				updateRate(doRate);
			}
		});
		img_ps_dorate[4].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 5;
				displayDoRate(doRate);
				updateRate(doRate);
			}
		});
	}
	protected void updateRate(final int doRate) {
		// TODO Auto-generated method stub
		StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_UPDATE_RATING, new Response.Listener<String>() {	
            @Override
            public void onResponse(String response) {                
                try {
                	// Reference 1: jSon sample in below
                	
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                    	Functions.toastString("Bạn đã gửi xếp hạng thành công", getBaseContext());
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Functions.toastString(errorMsg, getBaseContext());
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.d(TAG, response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Functions.toastString(getResources().getString(R.string.error_server_connect), getBaseContext());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", Variables.userToken);
                params.put("user_id", Variables.userID);
                params.put("rate", String.valueOf(doRate));
                params.put("person", partner_id);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "");
	}
	// get Rating info of current user for this person
		protected void getDoRatingOfUser(String rating) {
			// TODO Auto-generated method stub
			int curRate = 0;
			if (rating.equals("")) {
				doRate = curRate;
				displayDoRate(curRate);
				return;
			}
			String[] listRating = rating.split("-")[1].split(",");
			for (int i = 0; i < listRating.length; i++) {
				String idUser = listRating[i].split(":")[0];
				if (idUser.equals(Variables.userID)) {
					curRate = Integer.parseInt(listRating[i].split(":")[1]);
					break;
				}
			}
			doRate = curRate;
			displayDoRate(curRate);
			
		}
		private void displayDoRate(int curDoRate) {
			// TODO Auto-generated method stub
			for (int i = 0; i < 5; i++) {
				if (i < curDoRate)
					img_ps_dorate[i].setBackground(getResources().getDrawable((R.drawable.ic_do_rate1)));
				else
					img_ps_dorate[i].setBackground(getResources().getDrawable((R.drawable.ic_do_rate0)));
			}
		}
		
	private void hideSmileSet() {
		// TODO Auto-generated method stub
		 ll_smile.setVisibility(View.INVISIBLE);
	     RelativeLayout.LayoutParams params = null;
	     params = new RelativeLayout.LayoutParams(0, 0);
	     ll_smile.setLayoutParams(params);
	     isDisplaySmileSet = 0;
	}
	private void showSmileSet() {
		// TODO Auto-generated method stub
		 ll_smile.setVisibility(View.VISIBLE);
	     RelativeLayout.LayoutParams params = null;
	     params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
	    		 RelativeLayout.LayoutParams.WRAP_CONTENT);
	     params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	     params.setMargins(0, 0, 0, Functions.dpToPx(70));
	     ll_smile.setLayoutParams(params);
	     isDisplaySmileSet = 1;
	}
	private void hideRate() {
		// TODO Auto-generated method stub
		 ll_rate.setVisibility(View.INVISIBLE);
	     RelativeLayout.LayoutParams params = null;
	     params = new RelativeLayout.LayoutParams(0, 0);
	     ll_rate.setLayoutParams(params);
	     isDisplayRate = 0;
	}
	private void showRate() {
		// TODO Auto-generated method stub
		ll_rate.setVisibility(View.VISIBLE);
	     RelativeLayout.LayoutParams params = null;
	     params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
	    		 RelativeLayout.LayoutParams.WRAP_CONTENT);
	     params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	     params.setMargins(Functions.dpToPx(25), 0, 0, Functions.dpToPx(75));
	     ll_rate.setLayoutParams(params);
	     isDisplayRate = 1;
	}
	protected void onSelectSmile(int i) {
		// TODO Auto-generated method stub
		String ic_string = "";
		switch (i) {
		case 0:
			ic_string = Constants.ICON0;
			break;
		case 1:
			ic_string = Constants.ICON1;
			break;
		case 2:
			ic_string = Constants.ICON2;
			break;
		case 3:
			ic_string = Constants.ICON3;
			break;
		case 4:
			ic_string = Constants.ICON4;
			break;
		case 5:
			ic_string = Constants.ICON5;
			break;
		case 6:
			ic_string = Constants.ICON6;
			break;
		case 7:
			ic_string = Constants.ICON7;
			break;
		case 8:
			ic_string = Constants.ICON8;
			break;
		case 9:
			ic_string = Constants.ICON9;
			break;
		case 10:
			ic_string = Constants.ICON10;
			break;
		case 11:
			ic_string = Constants.ICON11;
			break;
		default:
			break;
		}
		sendMessage(ic_string);
		hideSmileSet();
	}
	protected void takeCapture() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = Functions.getOutputMediaFileUri(Constants.MEDIA_TYPE_IMAGE);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	    startActivityForResult(intent, Constants.CAPTURE_IMAGE_REQUEST);
	}
	protected void takeFile() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FILE_REQUEST);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		refreshLocked = 1;
		if (requestCode == Constants.CAPTURE_IMAGE_REQUEST) {
        	if (resultCode == RESULT_OK) {
        		if (fileUri == null) {
        			getFile();
        		}
        		resizeAndSendImg(fileUri.getPath().toString());
        	} else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
            } else {
            	Functions.toastString("Có vấn đề khi chụp ảnh, vui lòng thử lại", this);
            }
        } else if (requestCode == Constants.PICK_FILE_REQUEST) {
        	if (resultCode == RESULT_OK) {
        		checkFileType(data);
        	} else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
            } else {
            	Functions.toastString("Có vấn đề khi chọn file, vui lòng thử lại", this);
            }
        }
	}
	private void getFile() {
		// TODO Auto-generated method stub
		try {
            Log.i("TAG", "inside Samsung Phones");
            String[] projection = {
                    MediaStore.Images.Thumbnails._ID, // The columns we want
                    MediaStore.Images.Thumbnails.IMAGE_ID,
                    MediaStore.Images.Thumbnails.KIND,
                    MediaStore.Images.Thumbnails.DATA };
            String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select
                    MediaStore.Images.Thumbnails.MINI_KIND;

            String sort = MediaStore.Images.Thumbnails._ID + " DESC";

            // At the moment, this is a bit of a hack, as I'm returning ALL
            // images, and just taking the latest one. There is a better way
            // to
            // narrow this down I think with a WHERE clause which is
            // currently
            // the selection variable
            Cursor myCursor = this.managedQuery(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    projection, selection, null, sort);

            long imageId = 0l;
            long thumbnailImageId = 0l;
            String thumbnailPath = "";

            try {
                myCursor.moveToFirst();
                imageId = myCursor
                        .getLong(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
                thumbnailImageId = myCursor
                        .getLong(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
                thumbnailPath = myCursor
                        .getString(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            } finally {
                // myCursor.close();
            }

            // Create new Cursor to obtain the file Path for the large image
            String[] largeFileProjection = {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA};

            String largeFileSort = MediaStore.Images.ImageColumns._ID
                    + " DESC";
            myCursor = this.managedQuery(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    largeFileProjection, null, null, largeFileSort);
            String largeImagePath = "";

            try {
                myCursor.moveToFirst();

                // This will actually give yo uthe file path location of the
                // image.
                largeImagePath = myCursor
                        .getString(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                fileUri = Uri.fromFile(new File(
                        largeImagePath));

            } finally {
                // myCursor.close();
            }
            // These are the two URI's you'll be interested in. They give
            // you a handle to the actual images
            Uri uriLargeImage = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    String.valueOf(imageId));
            Uri uriThumbnailImage = Uri.withAppendedPath(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    String.valueOf(thumbnailImageId));

            // I've left out the remaining code, as all I do is assign the
            // URI's
            // to my own objects anyways...
        } catch (Exception e) {

            Log.i("TAG",
                    "inside catch Samsung Phones exception " + e.toString());

        }
        try {
            Log.i("TAG", "URI Normal:" + fileUri.getPath());
        } catch (Exception e) {
            Log.i("TAG", "Excfeption inside Normal URI :" + e.toString());
        }
	}
	private void checkFileType(Intent data) {
		// TODO Auto-generated method stub
		Uri uriFileToUpload = data.getData();
    	String temPath = Functions.getRealPathFromURI(this, uriFileToUpload); // For image (and check file type)
    	if (temPath != null) {// Not video file
    		if (temPath.equals("")) // Not image file
    			temPath = uriFileToUpload.getPath(); // For document only
    		File f = new File(temPath);
        	if (Functions.isImage(f)) {
        		// Image
        		resizeAndSendImg(temPath);
        		
        	} else {
        		// Document
                checkSizeAndSendDoc(temPath);
                
        	}
    	} else { // Video file
    		Functions.toastString("Xin lỗi, bạn chỉ được gửi file ảnh hoặc tài liệu",  this);
    		Functions.toastString(temPath, getBaseContext());
    	}
	}
	private void checkSizeAndSendDoc(String temPath) {
		// TODO Auto-generated method stub
		if (Functions.getFileLength(temPath) <= Constants.MAX_DOCSIZE_UPLOAD)
        	sendFile(0, temPath, null);
        else {
        	Functions.toastString("File không được lớn hơn "+String.valueOf(Constants.MAX_DOCSIZE_UPLOAD/1000)+"MB", this);
        }
	}
	
	private void resizeAndSendImg(String sPath) {
		// TODO Auto-generated method stub
		Bitmap bm = null;
		if (Functions.getFileLength(sPath) > Constants.MAX_IMGSIZE_UPLOAD)
			bm = Functions.getBitmapFromPath(sPath, 3);
		else
			bm = Functions.getBitmapFromPath(sPath, 1);
		
		bm = Functions.rotateBitmapIfNeeded(getBaseContext(), sPath, bm);
		sendFile(1, sPath, bm);
	}
	
	private void sendFile(final int isImg, final String path, final Bitmap bm) {
		// TODO Auto-generated method stub
		if (isImg == 1 && bm == null) {
			Functions.toastString("Có vấn đề với file của bạn, vui lòng kiểm tra lại.", getBaseContext());
			return;
		}
		// Update to List msg first:
        MessageData message = new MessageData();
        message.setId(String.valueOf(msgIdTemp));
        message.setMessage(path); // path local
        message.setCreatedAt("sending...");
        message.setFileType(isImg == 1?1:2);
        Sender user = new Sender(Variables.userID, Variables.userName, null);
        message.setSender(user);
        msgList.add(message);
        msgAdapter.notifyDataSetChanged();
        if (msgAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, msgAdapter.getItemCount() - 1);
        }
        
		String endPoint = Constants.CHAT_SEND_FILE.replace("_ID_", chatRoomId).replace("_ISGROUP_", "0");
		StringRequest stringRequest = new StringRequest(Request.Method.POST, endPoint,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                    	JSONObject obj = new JSONObject(response);
                        // check for error
                        if (obj.getBoolean("error") == false) {
                            JSONObject msgObj = obj.getJSONObject("message");
     
                            String msgId = msgObj.getString("message_id");
                            String msgText = msgObj.getString("message");
                            String msgCreatedAt = msgObj.getString("created_at");
                            int fileType = msgObj.getInt("fileType");
     
                            JSONObject userObj = obj.getJSONObject("user");
                            String userId = userObj.getString("uId");
                            String userName = userObj.getString("uName");
                            Sender user = new Sender(userId, userName, null);
                            
                            String msgIdTempFromSV = obj.getString("msgIdTemp");
                            
                            // Replace temp message already existed with info updated from server
                            for (MessageData msg : msgList) {
                                if (msg.getId().equals(msgIdTempFromSV) || msg.getId().equals(msgId)) {
                                    int index = msgList.indexOf(msg);
                                    msg.setId(msgId);
                                    msg.setMessage(msgText);
                                    msg.setCreatedAt(msgCreatedAt);
                                    msg.setSender(user);
                                    msg.setFileType(fileType);
                                    msgList.remove(index);
                                    msgList.add(index, msg);
                                    break;
                                }
                            }
                            refreshRecyclerview();
     
                        } else {
                            Functions.toastString(obj.getString("message").toString(), getApplicationContext());
                        }
                        Log.d(TAG, response.toString());
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Functions.toastString(getResources().getString(R.string.error_json), getBaseContext());
                        Log.d(TAG, e.getMessage()+": "+response.toString());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Functions.toastString("error send file", getBaseContext());
                }
            }){
            @SuppressWarnings("static-access")
			@Override
            protected Map<String, String> getParams() throws AuthFailureError {
                
            	String sBase64 = null; // holding base64 string of file
            	String name = "";
            	if (isImg == 0) { // Doc
					try {
						sBase64 = Functions.getBase64Doc(path);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	name = Functions.getFileName(path);
            	} else if (isImg == 1) {
            		sBase64 = Functions.getBase64Image(bm);
                    name = Functions.getFileName(path);
            	}
 
                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();
                //Adding parameters
                params.put("isImg", String.valueOf(isImg));
                params.put("sBase64", sBase64);
                params.put("filename", name);
                params.put("user_id", Variables.userID);
                params.put("msgIdTemp", String.valueOf(msgIdTemp)); // --------------- repair
                
                msgIdTemp--;
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
	}
	protected void delMessage(final String id, final int position) {
		// TODO Auto-generated method stub
        StringRequest strReq = new StringRequest(Request.Method.POST,
        		Constants.CHAT_MESSAGE_DEL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);
 
                    // check for error
                    if (obj.getBoolean("error") == false) {
                        msgList.remove(position);
                        
                        refreshRecyclerview();
 
                    } else {
                        Functions.toastString("Có lỗi xảy ra, không thể xóa tin nhắn", getApplicationContext());
                    }
                    Log.d(TAG, response.toString());
                } catch (JSONException e) {
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
                params.put("msg_id", id);
 
                Log.e(TAG, "Params: " + params.toString());
                return params;
            };
        };
        AppController.getInstance().addToRequestQueue(strReq);
	}
	protected void refreshRecyclerview() {
		// TODO Auto-generated method stub
		msgAdapter.notifyDataSetChanged();
        if (msgAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, msgAdapter.getItemCount() - 1);
        }
	}
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        if (chatRoomName.equals(""))
        	tvTitle.setText(getResources().getString(R.string.title_mychat));
        else
        	tvTitle.setText(chatRoomName);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (refreshLocked == 0) {
			getListMessage();
		}
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_CHAT_LIVE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_CHOOSING_LIVE));
		
		ChatNotificationUtils.clearNotifications();
	}
	@Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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

/*public class MyChat extends Activity {
	private ListView lv_mychat_contain;
	private EditText edt_message;
    private Button bt_send, bt_img, bt_capture;
	private TextView tvTitle;
	private static final String TAG = MyChat.class.getSimpleName();
	private ProgressDialog pDialog;
	private int msgIdTemp = 0; // use to mark message while send to server and wait for response from server  to update
	
	private List<MessageData> msgList;
	private MessageAdapter msgAdapter;
	
	private String chatRoomId = "";
	private String chatRoomName = "";
	private int stateIndex, stateTop;
	
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    
    private Uri fileUri = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getBundle();
		setInitView();
		initVariables();
		
		setData();
		setBroadcastReceiver();
	}
	private void getBundle() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        chatRoomName = intent.getStringExtra("chat_room_name");
        if (chatRoomId == null) {
            Functions.toastString("Không tim thấy cuộc trò chuyện nào!", this);
            finish();
        }
	}
	private void setBroadcastReceiver() {
		// TODO Auto-generated method stub
		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                	handlePushNotification(intent);
                }
            }
        };
	}
	protected void handlePushNotification(Intent intent) {
		// TODO Auto-generated method stub
		MessageData message = (MessageData) intent.getSerializableExtra("message");
		String crID = intent.getStringExtra("chat_room_id");
        if (crID.equals(chatRoomId) && chatRoomId != null && message != null && msgList != null && msgAdapter != null) {
            msgList.add(message);
            msgAdapter.notifyDataSetChanged();
        } else if (!crID.equals(chatRoomId) && chatRoomId != null && message != null && msgList != null && msgAdapter != null) {
        	// other chat room
        	Functions.toastString("Tin nhắn mới từ " + message.getSender().getName()+ ": " + message.getMessage(), this);
        }
	}
	private void refreshListview() {
		stateIndex = lv_mychat_contain.getFirstVisiblePosition();
		View v = lv_mychat_contain.getChildAt(0);
		stateTop = (v == null) ? 0 : (v.getTop() - lv_mychat_contain.getPaddingTop());

        msgAdapter.notifyDataSetChanged();
        // restore index and position
        lv_mychat_contain.setSelectionFromTop(stateIndex, stateTop);
	}
	public void setData() {
		// TODO Auto-generated method stub
		if (msgList == null)
			msgList = new ArrayList<MessageData>();
		if (msgAdapter == null)
			msgAdapter = new MessageAdapter(this, msgList);
		
		lv_mychat_contain.setAdapter(msgAdapter);
		
	}
	private void getListMessage() {
		// TODO Auto-generated method stub
		if (Functions.hasConnection(this)) {
			// get list chat_room
			pDialog.setMessage("Đang tải dữ liệu...");
	        showDialog();
	        String endPoint = Constants.CHAT_THREAD.replace("_ID_", chatRoomId);
			StringRequest strReq = new StringRequest(Request.Method.GET,
					endPoint, new Response.Listener<String>() {
	            @Override
	            public void onResponse(String response) {
	                Log.e(TAG, "response: " + response);
	                hideDialog();
	                try {
	                    JSONObject obj = new JSONObject(response);
	 
	                    msgList.clear();
	                    if (obj.getBoolean("error") == false) {
	                        JSONArray messagesObj = obj.getJSONArray("messages");
	 
	                        for (int i = 0; i < messagesObj.length(); i++) {
	                            JSONObject messageObj = (JSONObject) messagesObj.get(i);
	 
	                            String commentId = messageObj.getString("message_id");
	                            String commentText = messageObj.getString("message");
	                            String createdAt = messageObj.getString("created_at");
	                            int fileType = messageObj.getInt("fileType");
	 
	                            JSONObject userObj = messageObj.getJSONObject("user");
	                            String userId = userObj.getString("user_id");
	                            String userName = userObj.getString("username");
	                            Sender user = new Sender(userId, userName, null);
	 
	                            MessageData message = new MessageData();
	                            message.setId(commentId);
	                            message.setMessage(commentText);
	                            message.setCreatedAt(createdAt);
	                            message.setFileType(fileType);
	                            message.setSender(user);
	 
	                            msgList.add(message);
	                        }
	 
	                        msgAdapter.notifyDataSetChanged();
	                        Log.e(TAG, response);
	 
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
	
	
	protected void sendMessage() {
		final String msgContent = this.edt_message.getText().toString().trim();
        if (TextUtils.isEmpty(msgContent)) {
            Functions.toastString(getResources().getString(R.string.alert_fill_content), this);
            return;
        }
        // Update to List msg first:
        MessageData message = new MessageData();
        message.setId(String.valueOf(msgIdTemp));
        message.setMessage(msgContent);
        message.setCreatedAt("sending...");
        message.setFileType(0);
        Sender user = new Sender(Variables.userID, Variables.userName, null);
        message.setSender(user);
        msgList.add(message);
        msgAdapter.notifyDataSetChanged();
        
        // Send to server and wait for response to update
        String endPoint = Constants.CHAT_ROOM_MESSAGE.replace("_ID_", chatRoomId).replace("_ISGROUP_", "0");
        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);
 
                    // check for error
                    if (obj.getBoolean("error") == false) {
                        JSONObject msgObj = obj.getJSONObject("message");
 
                        String msgId = msgObj.getString("message_id");
                        String msgText = msgObj.getString("message");
                        String msgCreatedAt = msgObj.getString("created_at");
                        int fileType = msgObj.getInt("fileType");
 
                        JSONObject userObj = obj.getJSONObject("user");
                        String userId = userObj.getString("uId");
                        String userName = userObj.getString("uName");
                        Sender user = new Sender(userId, userName, null);
                        
                        String msgIdTempFromSV = obj.getString("msgIdTemp");
                        
                        // Replace temp message already existed with info updated from server
                        for (MessageData msg : msgList) {
                            if (msg.getId().equals(msgIdTempFromSV)) {
                                int index = msgList.indexOf(msg);
                                msg.setId(msgId);
                                msg.setMessage(msgText);
                                msg.setCreatedAt(msgCreatedAt);
                                msg.setSender(user);
                                msg.setFileType(fileType);
                                msgList.remove(index);
                                msgList.add(index, msg);
                                break;
                            }
                        }
                        msgAdapter.notifyDataSetChanged();
 
                    } else {
                        Functions.toastString(obj.getString("message").toString(), getApplicationContext());
                    }
                    Log.d(TAG, response.toString());
                } catch (JSONException e) {
                	Log.d(TAG, e.getMessage()+"\\"+response.toString());
                }
            }
        }, new Response.ErrorListener() {
 
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                edt_message.setText(msgContent);
            }
        }) {
 
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", Variables.userID);
                params.put("message", msgContent);
                params.put("fileType", "0");
                params.put("msgIdTemp", String.valueOf(msgIdTemp));
 
                Log.e(TAG, "Params: " + params.toString());
                msgIdTemp--; // we need msgIdTemp not duplicate with id of other msg
                return params;
            };
        };
 
 
        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
 
        strReq.setRetryPolicy(policy);
        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
        
        this.edt_message.setText("");
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
	}
	private void setInitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_mychat);
		setCustomActionBar();
		lv_mychat_contain = (ListView)findViewById(R.id.lv_mychat_contain);
		edt_message = (EditText) findViewById(R.id.edt_message);
        bt_send = (Button) findViewById(R.id.bt_send);
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        bt_img = (Button) findViewById(R.id.bt_img);
        bt_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeImg();
            }
        });
        bt_capture = (Button) findViewById(R.id.bt_capture);
        bt_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeCapture();
            }
        });
		pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        
        registerForContextMenu(lv_mychat_contain);
       
	}
	
	protected void takeCapture() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = Functions.getOutputMediaFileUri(Constants.MEDIA_TYPE_IMAGE);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	    startActivityForResult(intent, Constants.CAPTURE_IMAGE_REQUEST);
	}
	protected void takeImg() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FILE_REQUEST);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.CAPTURE_IMAGE_REQUEST) {
        	if (resultCode == RESULT_OK) {
        		sendImg(fileUri.getPath().toString());
        	} else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
            } else {
            	Functions.toastString("Có vấn đề khi chụp ảnh, vui lòng thử lại", this);
            }
        } else if (requestCode == Constants.PICK_FILE_REQUEST ) {
        	if (resultCode == RESULT_OK) {
        		checkImg(data);
        	} else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
            } else {
            	Functions.toastString("Có vấn đề khi chọn ảnh, vui lòng thử lại", this);
            }
        }
	}
	private void checkImg(Intent data) {
		// TODO Auto-generated method stub
		Uri uriFileToUpload = data.getData();
    	String temPath = Functions.getRealPathFromURI(this, uriFileToUpload); // For image (and check file type)
    	if (temPath != null) {// Not video file
    		File f = new File(temPath);
        	if (Functions.isImage(f)) {
        		
        		sendImg(temPath);
        		
        	} else { // Doc file
        		Functions.toastString("Xin lỗi, bạn chỉ được gửi file ảnh",  this);
        	}
    	} else { // Video file
    		Functions.toastString("Xin lỗi, bạn chỉ được gửi file ảnh",  this);
    	}
	}
	private void sendImg(String sPath) {
		// TODO Auto-generated method stub
		Functions.toastString(sPath, getApplicationContext());
	}
	protected void delMessage(final String id, final int position) {
		// TODO Auto-generated method stub
        StringRequest strReq = new StringRequest(Request.Method.POST,
        		Constants.CHAT_MESSAGE_DEL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);
 
                    // check for error
                    if (obj.getBoolean("error") == false) {
                        msgList.remove(position);
                        
                        refreshListview();
 
                    } else {
                        Functions.toastString("Có lỗi xảy ra, không thể xóa tin nhắn", getApplicationContext());
                    }
                    Log.d(TAG, response.toString());
                } catch (JSONException e) {
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
                params.put("msg_id", id);
 
                Log.e(TAG, "Params: " + params.toString());
                return params;
            };
        };
        AppController.getInstance().addToRequestQueue(strReq);
	}
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        if (chatRoomName.equals(""))
        	tvTitle.setText(getResources().getString(R.string.title_mychat));
        else
        	tvTitle.setText(chatRoomName);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getListMessage();
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
		
		ChatNotificationUtils.clearNotifications();
	}
	@Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    String[] menuItems = getResources().getStringArray(R.array.menu_mychat);
	    for (int i = 0; i<menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  MessageData msg = msgList.get(info.position);
	  if (menuItemIndex == 1) {
		  if (msg.getSender().getId().equals(Variables.userID)) {
		  		delMessage(msg.getId(), info.position);
		  } else {
		  		Functions.toastString("Bạn không thể xóa tin nhắn của người khác", getApplicationContext());
		  }
	  } else if (menuItemIndex == 0) {
		  ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		  ClipData clip = ClipData.newPlainText("", msg.getMessage());
		  clipboard.setPrimaryClip(clip);
	  }
	  return true;
	}
}*/