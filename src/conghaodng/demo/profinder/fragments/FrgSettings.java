package conghaodng.demo.profinder.fragments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.SQLiteHandler;

public class FrgSettings extends Fragment {
	
	protected static final String TAG = FrgSettings.class.getSimpleName();
	private TextView tvTitle, tv_st_name, tv_st_email, tv_st_bm, tv_st_chsg;
	private ImageView img_st_avatar, img_st_avatar_edt;
	private EditText edt_st_name, edt_st_email, edt_st_tel, edt_st_address;
	private Button bt_st_sl_avatar, bt_st_update;
	private SQLiteHandler db;
	private Bitmap bmAvatar;
	private ProgressDialog pDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frg_setting, container, false);
		
		getInfoIfSavedInSqlite();
		
		setInitView(v);
		fillData();
		
		return v;
	}
	
	private void fillData() {
		// TODO Auto-generated method stub
		getAvatar();
		tv_st_name.setText(Variables.userName);
		tv_st_email.setText(Variables.userEmail);
		edt_st_name.setText(Variables.userName);
		edt_st_email.setText(Variables.userEmail);
		edt_st_address.setText(Variables.userAddress);
		edt_st_tel.setText(Variables.userTel);
		if (!Variables.userEmail.equals("")) {
			edt_st_email.setEnabled(false);
		}
	}
	private void getAvatar() {
		// TODO Auto-generated method stub
		if (Variables.userType == 0) {
			String urlAvatar = "https://graph.facebook.com/" + Variables.userFBID + "/picture?type=large";
			new loadAvatar().execute(urlAvatar);
		} else if (Variables.userType == 1 && !Variables.userAvatar.equals("")) {
			String urlAvatar = Constants.URL_FILE_FOLDER + Variables.userAvatar;
			new loadAvatar().execute(urlAvatar);
		}
	}
	
	private void getInfoIfSavedInSqlite() {
		// TODO Auto-generated method stub
		db = new SQLiteHandler(getActivity());
	}
	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		tv_st_name = (TextView)v.findViewById(R.id.tv_st_name);
		tv_st_email = (TextView)v.findViewById(R.id.tv_st_email);
		img_st_avatar = (ImageView)v.findViewById(R.id.img_st_avatar);
		img_st_avatar_edt = (ImageView)v.findViewById(R.id.img_st_avatar_edt);
		edt_st_name = (EditText)v.findViewById(R.id.edt_st_name);
		edt_st_email = (EditText)v.findViewById(R.id.edt_st_email);
		edt_st_tel = (EditText)v.findViewById(R.id.edt_st_tel);
		edt_st_address = (EditText)v.findViewById(R.id.edt_st_address);
		bt_st_sl_avatar = (Button)v.findViewById(R.id.bt_st_sl_avatar);
		bt_st_update = (Button)v.findViewById(R.id.bt_st_update);
		tv_st_bm = (TextView)v.findViewById(R.id.tv_st_bm);
		tv_st_chsg = (TextView)v.findViewById(R.id.tv_st_chsg);
		SpannableString content = new SpannableString(getResources().getString(R.string.tv_st_bm));
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tv_st_bm.setText(content);
		SpannableString content2 = new SpannableString(getResources().getString(R.string.tv_st_chsg));
		content2.setSpan(new UnderlineSpan(), 0, content2.length(), 0);
		tv_st_chsg.setText(content2);
		
		if (Variables.userType == 0) {
			bt_st_sl_avatar.setVisibility(View.INVISIBLE);
		}
		bt_st_sl_avatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showFileChooser();
			}
		});
		bt_st_update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkAndUpdate();
			}
		});
		tv_st_bm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FrgListBookmark frgBM = new FrgListBookmark();
				
				getActivity().getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, frgBM)
			     .addToBackStack(null)
			     .commit();
			}
		});
		tv_st_chsg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FrgListChsg lstChsgFrg = new FrgListChsg();
			    getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, lstChsgFrg, Constants.TAG_FRG_LISTCHSG)
			     .addToBackStack(null)
			     .commit();
			}
		});
		// Progress dialog
		pDialog = new ProgressDialog(getActivity());
		pDialog.setCancelable(false);
	}
	
	protected void checkAndUpdate() {
		// TODO Auto-generated method stub
		String name = edt_st_name.getText().toString();
		String email = edt_st_email.getText().toString();
		String tel = edt_st_tel.getText().toString();
		String address = edt_st_address.getText().toString();
		if (name.equals("") || !Functions.isEmailValid(email) || !Functions.isPhoneValid(tel) || address.equals("")) {
			Functions.toastString("Bạn cần nhập đầy đủ và hợp lệ các thông tin để update", getActivity());
			return;
		}
		updateUser(name, email, tel, address);
	}
	private void updateUser(final String name, final String email, final String tel, final String address) {

        pDialog.setMessage("Đang đăng nhập ...");
        showDialog();
 
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_UPDATE_SETTING, new Response.Listener<String>() {
 
            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                    	Functions.toastString("Update thành công!", getActivity());
                    	Variables.userName = name;
                    	Variables.userEmail = email;
                    	Variables.userAddress = address;
                    	Variables.userTel = tel;
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("msg_err");
                        Functions.toastString("Lỗi đăng nhập: "+errorMsg, getActivity());
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Functions.toastString(getResources().getString(R.string.error_json), getActivity());
                    Log.d(TAG, e.getMessage()+"\\"+response.toString());
                }
 
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Functions.toastString(getResources().getString(R.string.error_server_connect), getActivity());
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tel", tel);
                params.put("name", name);
				params.put("email", email);
				params.put("userid", Variables.userID);
				params.put("address", address);
				params.put("token", Variables.userToken);
 
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "");
    }
	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}
	
	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
	private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), Constants.PICK_FILE_REQUEST);
    }
	@SuppressWarnings("static-access")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.PICK_FILE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
        	Uri uriFileToUpload = data.getData();
        	String temPath = Functions.getRealPathFromURI(getActivity(), uriFileToUpload); // For image (and check file type)
        	if (temPath != null) {// Not video file
        		if (temPath.equals("")) // Not image file
        			temPath = uriFileToUpload.getPath(); // For document only
        		File f = new File(temPath);
            	if (Functions.isImage(f)) {
            		
            		if (Functions.getFileLength(temPath) > Constants.MAX_IMGSIZE_UPLOAD)
            			uploadFile(temPath, Functions.getBitmapFromPath(temPath, 3));
            		else
            			uploadFile(temPath, Functions.getBitmapFromPath(temPath, 1));
            		
            	} else {
            		Functions.toastString("Xin lỗi, bạn phải chọn file ảnh",  getActivity());
            	}
        	} else { // Video file
        		Functions.toastString("Xin lỗi, bạn phải chọn file ảnh",  getActivity());
        	}	
        }
	}

	public void uploadFile(final String path, final Bitmap mBM) {
		if (mBM == null) {
			Functions.toastString("Có vấn đề với file của bạn, vui lòng kiểm tra lại.", getActivity());
		} else {
			final ProgressDialog loading = ProgressDialog.show(getActivity(),"Uploading...","Please wait...",false,false);
			StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_UPDATE_AVATAR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        
                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            // Check for error node in json
                            if (!error) {

        	                    JSONObject info = jObj.getJSONObject("info");
        	                    String nameOnServer = info.getString("nameOnServer"); // Name on Server (file name)
        	                    Variables.userAvatar = nameOnServer;
        	                    if (Variables.userType == 1 && !Variables.userAvatar.equals("")) {
        	            			String urlAvatar = Constants.URL_FILE_FOLDER + Variables.userAvatar;
        	            			new loadAvatar().execute(urlAvatar);
        	            		}
                                
                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("msg_err");
                                Functions.toastString("Không thể upload avatar: "+errorMsg, getActivity());
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Functions.toastString(getResources().getString(R.string.error_json), getActivity());
                            Log.d(TAG, e.getMessage()+": "+response.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        //Showing toast
                        Functions.toastString("error: "+volleyError.getMessage(), getActivity());
                    }
                }){
	            @SuppressWarnings("static-access")
				@Override
	            protected Map<String, String> getParams() throws AuthFailureError {
	                
	            	String sBase64 = null; // holding base64 string of file
	            	String name = "";
	            	sBase64 = Functions.getBase64Image(mBM);
	                name = Functions.getFileName(path);
	 
	                //Creating parameters
	                Map<String,String> params = new Hashtable<String, String>();
	 
	                //Adding parameters
	                params.put("userid", Variables.userID);
	                params.put("sBase64", sBase64);
	                params.put("name", name);
	                
	                //returning parameters
	                return params;
	            }
	        };
	        //Creating a Request Queue
	        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
	        //Adding request to the queue
	        requestQueue.add(stringRequest);
		}
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
            catch (IOException e) {  
                e.printStackTrace();
            }
            return null;
        }
        @Override       
        protected void onPostExecute(String args) {
        	if (bmAvatar != null) {
	            img_st_avatar.setImageBitmap(bmAvatar);
	            img_st_avatar_edt.setImageBitmap(bmAvatar);
	        }
        }
    }
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_settings));
	}
}
