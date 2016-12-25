package conghaodng.demo.profinder.fragments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.login.LoginManager;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.LoginActivity;
import conghaodng.demo.profinder.MainActivity;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.MyChat;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.MyZoomableImageView;

public class FrgPerson extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
	
	private ImageView img_ps_avatar, img_ps_fb, img_ps_ath_del_1, img_ps_ath_del_2, img_ps_bmk, img_ps_rating;
	private MyZoomableImageView img_dlf;
	private Button bt_ps_call, bt_ps_chat, bt_dlf_close;
	private TextView tvTitle, tv_ps_learn, tv_ps_name, tv_ps_email, tv_ps_sbj, tv_ps_content, tv_ps_file_label,
		tv_ps_ath_info_1, tv_ps_ath_info_2, tv_ps_ath_info, tv_ps_address, tv_ps_note_label, tv_ps_note, tv_dlf;
	
	private TextView tv_ps_request_status, tv_ps_do_confirm, tv_ps_do_ignore, tv_ps_do_cancelrequest;
	private LinearLayout ll_ps_do_rate, ll_ps_request_do, ll_ps_request_do2;
	private ImageView[] img_ps_dorate = new ImageView[5];
	
	private SwipeRefreshLayout swipeRefreshLayout;
	private FrameLayout fl_ps_file;
	private Bundle b;
	private int id_chsg, from_chsg; 
	private static final String TAG = FrgListMeet.class.getSimpleName();
	private ProgressDialog pDialog;
	private String p_name, p_email, p_avatar, p_fbid, p_tel = "", c_doc, c_img = "", p_id = "", p_bmed;
	private int p_rating;
	private int statusInfo = 0;
	private int co_id, co_isIamSender, co_status, co_rate; // connect
	private Dialog mDialogFile;
	// For download file IMG
	private Bitmap bmImg = null, bmAvatar = null;
	private int[] arrIsImg = new int[] {-1, -1};
	// For download file DOC
	private Uri pathDocOnDevice;
	private int downloadedSize = 0, totalsize;
	float per = 0;
	private int isBmk = 0; // is current person is bookmarked
	private int doRate = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		getActivity().getActionBar().show();
		View v = inflater.inflate(R.layout.frg_person, container, false);
		getBundle();
		setInitView(v);
		
		// Init UploadVisibility
		statusInfo = 0;
		setInfoUploadVisibility(statusInfo);
		
		getInfo();
		return v;
	}
	private void getAvatar() {
		// TODO Auto-generated method stub
		if (!p_fbid.equals("")) {
			String urlAvatar = "https://graph.facebook.com/" + p_fbid + "/picture?type=large";
			new loadAvatar().execute(urlAvatar);
		} else if (p_fbid.equals("") && !p_avatar.equals("")) {
			String urlAvatar = Constants.URL_FILE_FOLDER + p_avatar;
			new loadAvatar().execute(urlAvatar);
		}
	}
	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		
		swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getInfo();
            }
        });
		
		tv_ps_learn = (TextView)v.findViewById(R.id.tv_ps_learn);
		tv_ps_name = (TextView)v.findViewById(R.id.tv_ps_name);
		tv_ps_email = (TextView)v.findViewById(R.id.tv_ps_email);
		tv_ps_sbj = (TextView)v.findViewById(R.id.tv_ps_sbj);
		tv_ps_content = (TextView)v.findViewById(R.id.tv_ps_content);
		tv_ps_file_label = (TextView)v.findViewById(R.id.tv_ps_file_label);
		tv_ps_ath_info_1 = (TextView)v.findViewById(R.id.tv_ps_ath_info_1);
		tv_ps_ath_info_2 = (TextView)v.findViewById(R.id.tv_ps_ath_info_2);
		tv_ps_ath_info = (TextView)v.findViewById(R.id.tv_ps_ath_info);
		tv_ps_note_label = (TextView)v.findViewById(R.id.tv_ps_note_label);
		tv_ps_note = (TextView)v.findViewById(R.id.tv_ps_note);
		tv_ps_address = (TextView)v.findViewById(R.id.tv_ps_address);
		fl_ps_file = (FrameLayout)v.findViewById(R.id.fl_ps_file);
		
		tv_ps_request_status = (TextView)v.findViewById(R.id.tv_ps_request_status);
		tv_ps_do_confirm = (TextView)v.findViewById(R.id.tv_ps_do_confirm);
		tv_ps_do_ignore = (TextView)v.findViewById(R.id.tv_ps_do_ignore);
		tv_ps_do_cancelrequest = (TextView)v.findViewById(R.id.tv_ps_do_cancelrequest);
		ll_ps_do_rate = (LinearLayout)v.findViewById(R.id.ll_ps_do_rate);
		ll_ps_request_do = (LinearLayout)v.findViewById(R.id.ll_ps_request_do);
		ll_ps_request_do2 = (LinearLayout)v.findViewById(R.id.ll_ps_request_do2);
		tv_ps_request_status.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onRequestStatus();
			}
		});
		tv_ps_do_confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onRequestConfirm();
			}
		});
		tv_ps_do_ignore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onRequestIgnore();
			}
		});
		tv_ps_do_cancelrequest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onRequestCancel();
			}
		});
		
		img_ps_fb = (ImageView)v.findViewById(R.id.img_ps_fb);
		img_ps_dorate[0] = (ImageView)v.findViewById(R.id.img_ps_dorate0);
		img_ps_dorate[1] = (ImageView)v.findViewById(R.id.img_ps_dorate1);
		img_ps_dorate[2] = (ImageView)v.findViewById(R.id.img_ps_dorate2);
		img_ps_dorate[3] = (ImageView)v.findViewById(R.id.img_ps_dorate3);
		img_ps_dorate[4] = (ImageView)v.findViewById(R.id.img_ps_dorate4);
		img_ps_avatar = (ImageView)v.findViewById(R.id.img_ps_avatar);
		img_ps_ath_del_1 = (ImageView)v.findViewById(R.id.img_ps_ath_del_1);
		img_ps_ath_del_2 = (ImageView)v.findViewById(R.id.img_ps_ath_del_2);
		img_ps_bmk = (ImageView)v.findViewById(R.id.img_ps_bmk);
		img_ps_rating = (ImageView)v.findViewById(R.id.img_ps_rating);
		bt_ps_call = (Button)v.findViewById(R.id.bt_ps_call);
		bt_ps_chat = (Button)v.findViewById(R.id.bt_ps_chat);
		
		img_ps_fb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!p_fbid.equals("")) {
					Intent i = Functions.newFacebookIntent(getActivity().getPackageManager(), "https://www.facebook.com/"+p_fbid);
					startActivity(i);
				}
			}
		});
		img_ps_dorate[0].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 1;
				displayDoRate(doRate);
				doRequestToServer("rate", doRate);
			}
		});
		img_ps_dorate[1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 2;
				displayDoRate(doRate);
				doRequestToServer("rate", doRate);
			}
		});
		img_ps_dorate[2].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 3;
				displayDoRate(doRate);
				doRequestToServer("rate", doRate);
			}
		});
		img_ps_dorate[3].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 4;
				displayDoRate(doRate);
				doRequestToServer("rate", doRate);
			}
		});
		img_ps_dorate[4].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doRate = 5;
				displayDoRate(doRate);
				doRequestToServer("rate", doRate);
			}
		});
		img_ps_bmk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isBmk == 0) {
					isBmk = 1;
					img_ps_bmk.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark1));
				} else {
					isBmk = 0;
					img_ps_bmk.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark0));
				}
				setBmk(isBmk, p_id);
			}
		});
		tv_ps_name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!p_fbid.equals("")) {
					Functions.openFacebookIntent(p_fbid, getActivity());
				}
			}
		});
		bt_ps_call.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Uri number = Uri.parse("tel:" + p_tel);
				Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
				startActivity(callIntent);
			}
		});
		bt_ps_chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				goToMyChat();
			}
		});
		tv_ps_ath_info_1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onDisplay(tv_ps_ath_info_1.getText().toString(), arrIsImg[0]);
			}
		});
		tv_ps_ath_info_2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onDisplay(tv_ps_ath_info_2.getText().toString(), arrIsImg[1]);
			}
		});
		img_ps_ath_del_1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onDisplay(tv_ps_ath_info_1.getText().toString(), arrIsImg[0]);
			}
		});
		img_ps_ath_del_2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onDisplay(tv_ps_ath_info_2.getText().toString(), arrIsImg[1]);
			}
		});
		
		pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(true);
        tv_ps_ath_info.setText(getActivity().getResources().getString(R.string.label_ps_ath_info));
        
        setInfoUploadVisibility(statusInfo);
	}
	protected void updateRateForPerson(final int doRate) {
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
                    	Functions.toastString("Bạn đã gửi xếp hạng thành công", getActivity());
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Functions.toastString(errorMsg, getActivity());
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
                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", Variables.userToken);
                params.put("user_id", Variables.userID);
                params.put("rate", String.valueOf(doRate));
                params.put("person", p_id);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "");
	}
	protected void goToMyChat() {
		// TODO Auto-generated method stub
		String crName = "";
		try {
			crName = URLEncoder.encode(Variables.userName+" - "+p_name, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String endPoint = Constants.CHAT_ROOM_NEW.replace("_ID1_", Variables.userID).replace("_ID2_", p_id).replace("_NAME_", crName);

		StringRequest strReq = new StringRequest(Request.Method.GET,
				endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);
 
                    if (obj.getBoolean("error") == false) {
                        
                        Intent i = new Intent(getActivity(), MyChat.class);
        				i.putExtra("chat_room_id", obj.getString("chat_room_id"));
        				i.putExtra("chat_room_name", p_name);
        				getActivity().startActivity(i);
                    } else {
                        Log.e(TAG, "get chatroom error: " + response);
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
            }
        });
        AppController.getInstance().addToRequestQueue(strReq);
	}
	protected void onDisplay(String filename, int isImg) {
		// TODO Auto-generated method stub  
		mDialogFile = new Dialog(getActivity());
		mDialogFile.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		mDialogFile.setContentView(R.layout.dialog_display_file);
		mDialogFile.setTitle("");

		tv_dlf = (TextView) mDialogFile.findViewById(R.id.tv_dlf);
		img_dlf = (MyZoomableImageView) mDialogFile.findViewById(R.id.img_dlf);
		
		bt_dlf_close = (Button) mDialogFile.findViewById(R.id.bt_dlf_close);
		bt_dlf_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialogFile.dismiss();
			}
		});
		
		String url = Constants.URL_FILE_FOLDER+filename;
		if (isImg == 1) {
			new loadRemoteIMG().execute(url);
		} else {
			new loadRemoteDOC().execute(url, filename);
		}

	}
	/*protected void onDownloadAndDisplay(String filename) {
		FrgDialogFile frgDLF = new FrgDialogFile();
		
		Bundle b = new Bundle();
		b.putString("filename", filename);
		b.putInt("isImg", 1);
		frgDLF.setArguments(b);
		
	    getActivity().getFragmentManager().beginTransaction()
	     .replace(R.id.fragment_container, frgDLF)
	     .addToBackStack(null)
	     .commit();
	}*/
	public class loadRemoteIMG extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Đang tải file...");
            showDialog();
        }
        @SuppressWarnings("null")
		@Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
        	final String imageKey = Functions.getFileName(String.valueOf(args[0]));
        	bmImg = Variables.mDCache.getBitmap(imageKey); // get bitmap from cache
        	if (bmImg == null) { // no have specified bitmap in cache
        		if (!Functions.hasConnection(getActivity())) {
        			return null;
        		}
	            try {  
	            	URL mUrl = new URL(args[0]);
	                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
	                conn.setDoInput(true);
	                conn.connect();
	                InputStream is = conn.getInputStream();
	                if (is != null) {
	                	bmImg = BitmapFactory.decodeStream(is); 
	                	if (bmImg != null)
	                	Variables.mDCache.put(imageKey, bmImg); // put bitmap to cache
	                } 
	            }
	            catch (IOException e) {
	                e.printStackTrace();
	            }
        	}

            return null;
        }
        @Override       
        protected void onPostExecute(String args) {
        	hideDialog();
        	if (bmImg != null) {
	            img_dlf.setImageBitmap(bmImg);
	            mDialogFile.show();
	            Window window = mDialogFile.getWindow();
	            window.setLayout(LayoutParams.MATCH_PARENT, 1400);
        	} else {
        		Functions.toastString("Không thể tải file", getActivity());
        	}
        }

    }
	public class loadRemoteDOC extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Đang tải file...");
            
            showDialog();
        }
        @Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
        	if (!Functions.hasConnection(getActivity())) {
    			return null;
    		}
        	File f = downloadDoc(args[0], args[1]);
        	if (f != null)
        		pathDocOnDevice = Uri.fromFile(f);
            
            return null;
        }
        @Override       
        protected void onPostExecute(String args) {
        	hideDialog();
        	if (pathDocOnDevice != null || !pathDocOnDevice.toString().equals("")) {
	        	try {
	                Intent intent = new Intent(Intent.ACTION_VIEW);
	                Functions.findDataType(intent, pathDocOnDevice);
	                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(intent);
	
	            } catch (ActivityNotFoundException e) {
	            	e.printStackTrace();
	            }
        	} else {
        		Functions.toastString("Không thể tải file", getActivity());
        	}
        }
    }
	File downloadDoc(String urlOnServer, String filenamOnServer) {
        File file = null;
        try {
            URL url = new URL(urlOnServer);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
 
            // connect
            urlConnection.connect();
 
            // set the path where we want to save the file
            File SDCardRoot = Environment.getExternalStorageDirectory();
            // create a new file, to save the downloaded file
            file = new File(SDCardRoot, filenamOnServer);
 
            FileOutputStream fileOutput = new FileOutputStream(file);
 
            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];  
            int bufferLength = 0;
 
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                per = ((float) downloadedSize / totalsize) * 100;
            }
            // close the output stream when complete //
            fileOutput.close();
 
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
        	e.printStackTrace();
        } catch (final Exception e) {
        	e.printStackTrace();
        }
        return file;
    }
	public class loadAvatar extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @SuppressWarnings("null")
		@Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
            try {  
            	URL mUrl = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();   
                HttpURLConnection.setFollowRedirects(true);
                conn.setDoInput(true);   
                conn.connect();     
                InputStream is = conn.getInputStream();                
                if (is != null) {
                	bmAvatar = BitmapFactory.decodeStream(is); 
                } 
            }
            catch (IOException e)
            {  
                e.printStackTrace();
            }

            return null;
        }
        @Override       
        protected void onPostExecute(String args) {
        	if (bmAvatar != null) {
        		//bmAvatar = Functions.getCircleBitmap(bmAvatar, 160, 150);
	            img_ps_avatar.setImageBitmap(bmAvatar);
	        }
        }

    }
	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null) {
			id_chsg = b.getInt("id_chsg");
			from_chsg = b.getInt("from_chsg");
		}
	}
	private void getInfo() {
		// TODO Auto-generated method stub		
		String tag_string_feed = "feed_info_person";
        pDialog.setMessage("Đang tải dữ liệu...");
        
        showDialog();
        
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_INFO_PERSON, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get info person Response: " + response.toString());
                
                hideDialog();
                
                try {
                	// Reference 1: jSon sample in below
                	
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {

                    	p_name = jORoot.getString("p_name");
                    	p_email = jORoot.getString("p_email");
                    	p_avatar = jORoot.getString("p_avatar");
                    	p_bmed = jORoot.getString("p_bmed");
                    	p_id = jORoot.getString("p_id");
                    	p_fbid = jORoot.getString("p_fbid");
                    	p_rating = jORoot.getInt("p_rating");
                    	p_tel = jORoot.getString("c_tel");
                    	c_doc = jORoot.getString("c_doc");
                    	c_img = jORoot.getString("c_img");
                    	int c_cat = jORoot.getInt("c_cat");
                    	int c_learn = jORoot.getInt("c_learn");
                    	co_id = jORoot.getInt("co_id");
                    	co_isIamSender = jORoot.getInt("co_isIamSender");
                    	co_status = jORoot.getInt("co_status");
                    	co_rate = jORoot.getInt("co_rate");
                    	
                    	tv_ps_name.setText(p_name);
                    	tv_ps_email.setText(p_email.equals("") ? p_tel : p_email);
                    	tv_ps_sbj.setText(jORoot.getString("c_field")+" > "+jORoot.getString("c_subject"));
                    	tv_ps_content.setText(!jORoot.getString("c_content").equals("") ? jORoot.getString("c_content") : jORoot.getString("c_tag"));
                    	tv_ps_address.setText(jORoot.getString("p_address"));
                    	String s = "";
                    	s += c_learn == 1 ? "Cần " : "Muốn làm ";
                    	s += c_cat == 0 ? "Gia sư " : "Người trợ giúp ";
                    	s += "trong lĩnh vực:";
                    	tv_ps_learn.setText(s);
                    	
						if (c_cat == 0) {
							String c_fee = jORoot.getString("c_fee");
							String c_time = jORoot.getString("c_time");
							String s1 = !c_fee.equals("") ? "Chi phí: "+c_fee+" - " : "";
							s1 += !c_time.equals("") ? "Thời gian: "+c_time : "";
                    		tv_ps_note.setText(s1);
                    		tv_ps_file_label.setVisibility(View.INVISIBLE);
                    		fl_ps_file.setVisibility(View.INVISIBLE);
                    		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, 0);
                    		tv_ps_file_label.setLayoutParams(param);
                    		fl_ps_file.setLayoutParams(param);
                    	} else {
                    		tv_ps_note.setVisibility(View.INVISIBLE);
                    		tv_ps_note_label.setVisibility(View.INVISIBLE);
                    	}
                    	
						setDisplayFB();
						setDisplayRequest();
                    	setFileUploaded(c_doc, c_img);
                    	getAvatar();
                    	setBoomark();
                    	displayRating(p_rating);
                    	//Functions.setRatingDisplay(getActivity(), p_rating, img_ps_rating);
                    	//getDoRatingOfUser(p_rating);
                    	
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get info person Error: " + errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Get info person Error: " + error.getMessage());

                if (Variables.isAlreadyAlertConnection == 0) {
                	Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                	Variables.isAlreadyAlertConnection = 1;
                }
                
                hideDialog();
                swipeRefreshLayout.setRefreshing(false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("sToken", Variables.userToken);
                params.put("userID", Variables.userID);
                params.put("iChsg", String.valueOf(id_chsg));
                params.put("fromChsg", String.valueOf(from_chsg));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_feed);
	}
	
	protected void setDisplayRequest() {
		// TODO Auto-generated method stub
		if (co_id == 0) {
			tv_ps_request_status.setVisibility(View.GONE);
			return;
		}
		switch (co_status) {
		case Constants.CONNECT_STATUS_NOT_CONNECT:
			tv_ps_request_status.setText(getActivity().getResources().getString(R.string.label_ps_request_notconnect));
			ll_ps_request_do.setVisibility(View.GONE);
			ll_ps_request_do2.setVisibility(View.GONE);
			ll_ps_do_rate.setVisibility(View.GONE);
			break;
		case Constants.CONNECT_STATUS_REQUESTING:
			if (co_isIamSender == 1) {
				tv_ps_request_status.setText(getActivity().getResources().getString(R.string.label_ps_request_requesting));
				ll_ps_request_do.setVisibility(View.GONE);
				ll_ps_request_do2.setVisibility(View.VISIBLE);
				ll_ps_do_rate.setVisibility(View.GONE);
			} else {
				tv_ps_request_status.setText(getActivity().getResources().getString(R.string.label_ps_request_requesting2));
				ll_ps_request_do.setVisibility(View.VISIBLE);
				ll_ps_request_do2.setVisibility(View.GONE);
				ll_ps_do_rate.setVisibility(View.GONE);
			}
			break;
		case Constants.CONNECT_STATUS_CONFIRMED:
			tv_ps_request_status.setText(getActivity().getResources().getString(R.string.label_ps_request_confirmed));
			ll_ps_do_rate.setVisibility(View.VISIBLE);
			ll_ps_request_do.setVisibility(View.GONE);
			ll_ps_request_do2.setVisibility(View.GONE);
			displayDoRate(co_rate);
			break;
		default:
			break;
		}
	}
	
	private void onRequestStatus() {
		if (co_status == Constants.CONNECT_STATUS_NOT_CONNECT) {
			doRequestToServer("send", 0);
		} else if (co_status == Constants.CONNECT_STATUS_CONFIRMED) {
			lauchAlertDeleteDialog();
		}
	}
	private void onRequestConfirm() {
		if (co_status == Constants.CONNECT_STATUS_REQUESTING) {
			if (co_isIamSender == 0) {
				doRequestToServer("confirm", 0);
			}
		}	
	}
	private void onRequestIgnore() {
		if (co_status == Constants.CONNECT_STATUS_REQUESTING) {
			if (co_isIamSender == 0) {
				doRequestToServer("ignore", 0);
			}
		}	
	}
	private void onRequestCancel() {
		if (co_status == Constants.CONNECT_STATUS_REQUESTING) {
			if (co_isIamSender == 1) {
				doRequestToServer("cancel", 0);
			}
		}	
	}
	private void lauchAlertDeleteDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dl_send = new AlertDialog.Builder(getActivity());
        dl_send.setTitle(getResources().getString(R.string.app_name));
        	dl_send.setMessage(getResources().getString(R.string.label_ps_request_delete_dialog))
            .setPositiveButton(getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	
                	doRequestToServer("delete", 0);
                }
            })
            .setNegativeButton(getResources().getString(R.string.bt_no), null);
           
        AlertDialog dialog = dl_send.create();
        dialog.show();
	}
	/**
	 * When take an action, send the sender, receiver,...
	 * In server-side, check current status:
	 * - If ok, get effect and response sender, receiver, status,... result
	 *    Note that, in db, who is sender is NOT fixed, base on data from mobile.
	 *    Ex: If A send request, A is sender and action=send, B confirm, B is sender and action=confirm
	 * - If not, do nothing and response sender, receiver, status,...on current db and in mobile-side, roll display to correct status
	 */
	private void doRequestToServer(final String action, final int rate) {
		// TODO Auto-generated method stub		
				pDialog.setMessage("Đang gửi dữ liệu...");
		        
		        showDialog();
		        
		        StringRequest strReq = new StringRequest(Method.POST,
		                Constants.URL_DO_REQUEST, new Response.Listener<String>() {
		        	
		            @Override
		            public void onResponse(String response) {
		                Log.d(TAG, "Get send request Response: " + response.toString());
		                
		                hideDialog();
		                
		                try {
		                	// Reference 1: jSon sample in below
		                	
		                    JSONObject jORoot = new JSONObject(response);
		                    boolean error = jORoot.getBoolean("error");
		 
		                    // Check for error node in json
		                    if (!error) {
		                    	if (action.equals("rate"))
		                    		Functions.toastString("Gửi xếp hạng thành công", getActivity());
		                    } else {
		                        // Error in login. Get the error message
		                        String errorMsg = jORoot.getString("msg_err");
		                        Log.e(TAG, "Get send request Error: " + errorMsg);
		                    }
		                    
		                    co_id = jORoot.getInt("co_id");
	                    	co_isIamSender = jORoot.getInt("co_isIamSender");
	                    	co_status = jORoot.getInt("co_status");
	                    	co_rate = jORoot.getInt("co_rate");
	                    	
	                    	setDisplayRequest();
	                    	
		                } catch (JSONException e) {
		                    // JSON error
		                    e.printStackTrace();
		                }
		            }
		        }, new Response.ErrorListener() {
		            @Override
		            public void onErrorResponse(VolleyError error) {
		                Log.e(TAG, "Get send request Error: " + error.getMessage());

		                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
		                
		                hideDialog();
		            }
		        }) {
		            @Override
		            protected Map<String, String> getParams() {
		                // Posting parameters to login url
		                Map<String, String> params = new HashMap<String, String>();
		                params.put("token", Variables.userToken);
		                params.put("action", action);
		                params.put("co_id", String.valueOf(co_id));
		                params.put("uSend", Variables.userID);
		                params.put("chSend", String.valueOf(from_chsg));
		                params.put("uReceive", p_id);
		                params.put("chReceive", String.valueOf(id_chsg));
		                params.put("rate", String.valueOf(rate));
		                return params;
		            }
		        };
		        AppController.getInstance().addToRequestQueue(strReq, "");
	}
	
	protected void setDisplayFB() {
		// TODO Auto-generated method stub
		if (!p_fbid.equals("")) {
			img_ps_fb.setVisibility(View.VISIBLE);
		}
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
	private void displayRating(int rating) {
		switch (rating) {
        case 0:
        	img_ps_rating.setBackground(getActivity().getResources().getDrawable((R.drawable.ic_rat_0)));
        	break;
        case 1:
        	img_ps_rating.setBackground(getActivity().getResources().getDrawable((R.drawable.ic_rat_1)));
        	break;
        case 2:
        	img_ps_rating.setBackground(getActivity().getResources().getDrawable((R.drawable.ic_rat_2)));
        	break;
        case 3:
        	img_ps_rating.setBackground(getActivity().getResources().getDrawable((R.drawable.ic_rat_3)));
        	break;
        case 4:
        	img_ps_rating.setBackground(getActivity().getResources().getDrawable((R.drawable.ic_rat_4)));
        	break;
        case 5:
        	img_ps_rating.setBackground(getActivity().getResources().getDrawable((R.drawable.ic_rat_5)));
        	break;
        }
	}
	protected void setBoomark() {
		// TODO Auto-generated method stub
		if (p_bmed.contains(Variables.userID)) { // already bookmarked this person
			isBmk = 1;
			img_ps_bmk.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark1));
		}
	}
	private void setBmk(final int isBookmark, final String person) {
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_SET_BOOKMARK, new Response.Listener<String>() {	
            @Override
            public void onResponse(String response) {                
                try {
                	// Reference 1: jSon sample in below
                	
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                    	if (isBookmark == 0)
                    		Functions.toastString("Bạn đã bỏ đánh dấu người này", getActivity());
                    	else
                    		Functions.toastString("Bạn đã đánh dấu người này", getActivity());
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
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
                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", Variables.userToken);
                params.put("user_id", Variables.userID);
                params.put("isBmk", String.valueOf(isBookmark));
                params.put("person", person);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "");
	}
	
	@Override
    public void onRefresh() {
		Variables.isAlreadyAlertConnection = 0;
		getInfo();
    }
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		Variables.isAlreadyAlertConnection = 0;
		super.onStop();
	}
	
	protected void setFileUploaded(String ac_doc, String ac_img) {
		// TODO Auto-generated method stub
		String[] tempDocName = ac_doc.split(Constants.DELIMITER);
		String[] tempImgName = ac_img.split(Constants.DELIMITER);
		 
		if (!tempDocName[0].toString().equals("")) {
        	setAttachInfo(tempDocName[0].toString(), 0);
		}
		if (tempDocName.length == 2)
			if (!tempDocName[1].toString().equals("")) {
				setAttachInfo(tempDocName[1].toString(), 0);
			}
		if (!tempImgName[0].toString().equals("")) {
        	setAttachInfo(tempImgName[0].toString(), 1);
		}
		if (tempImgName.length == 2)
			if (!tempImgName[1].toString().equals("")) {
				setAttachInfo(tempImgName[1].toString(), 1);
			}
	}
	private void setAttachInfo(String filename, int isImg) {
		// TODO Auto-generated method stub
		switch (statusInfo) { // Check for previous status of statusInfo
		case 0:
			tv_ps_ath_info_1.setText(filename);
			statusInfo = 1;
			arrIsImg[0] = isImg;
			setInfoUploadVisibility(statusInfo);
			break;
		case 1:
			tv_ps_ath_info_2.setText(filename);
			statusInfo = 3;
			arrIsImg[1] = isImg;
			setInfoUploadVisibility(statusInfo);
			break;
		default:
			break;
		}
	}
	private void setInfoUploadVisibility(int aCase) {
		// TODO Auto-generated method stub
		if (aCase == 0) { // Not upload
			tv_ps_ath_info.setVisibility(View.VISIBLE);
			tv_ps_ath_info_1.setVisibility(View.INVISIBLE);
			tv_ps_ath_info_2.setVisibility(View.INVISIBLE);
			img_ps_ath_del_1.setVisibility(View.INVISIBLE);
			img_ps_ath_del_2.setVisibility(View.INVISIBLE);
		} else if (aCase == 1) { // Only file 1 uploaded
			tv_ps_ath_info.setVisibility(View.INVISIBLE);
			tv_ps_ath_info_1.setVisibility(View.VISIBLE);
			tv_ps_ath_info_2.setVisibility(View.INVISIBLE);
			img_ps_ath_del_1.setVisibility(View.VISIBLE);
			img_ps_ath_del_2.setVisibility(View.INVISIBLE);
		} else if (aCase == 2) { // Only file 2 uploaded
			tv_ps_ath_info.setVisibility(View.INVISIBLE);
			tv_ps_ath_info_1.setVisibility(View.INVISIBLE);
			tv_ps_ath_info_2.setVisibility(View.VISIBLE);
			img_ps_ath_del_1.setVisibility(View.INVISIBLE);
			img_ps_ath_del_2.setVisibility(View.VISIBLE);
		} else { // Both file 1,2 uploaded
			tv_ps_ath_info.setVisibility(View.INVISIBLE);
			tv_ps_ath_info_1.setVisibility(View.VISIBLE);
			tv_ps_ath_info_2.setVisibility(View.VISIBLE);
			img_ps_ath_del_1.setVisibility(View.VISIBLE);
			img_ps_ath_del_2.setVisibility(View.VISIBLE);
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
	
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_person));
	}
}
