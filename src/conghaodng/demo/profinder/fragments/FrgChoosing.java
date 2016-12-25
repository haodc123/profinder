package conghaodng.demo.profinder.fragments;

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
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.services.NetworkStateReceiver;
import conghaodng.demo.profinder.services.NetworkStateReceiver.NetworkStateListener;
import conghaodng.demo.profinder.utils.MyInstantAutoComplete;
import conghaodng.demo.profinder.utils.MyLinkedHashMap;
import conghaodng.demo.profinder.utils.NetworkUtil;
import conghaodng.demo.profinder.utils.SQLiteHandler;

public class FrgChoosing extends Fragment {
	
	// Upload
	private String arrPathFile[] = new String[]{"",""}; // array of File path
	private int arrIsImg[] = new int[] {-1, -1}; // array indicate file in arrPathFile is image or not
	private Bitmap arrBitmap[] = new Bitmap[Constants.MAX_NUM_FILE_UPLOAD];
	
	// Capture
	private Uri capturedUri = null;
	
	/**
	 * statusInfo:
	 * 0 - not upload, 
	 * 1 - only tv_chsg_ath_info_1, 
	 * 2 - only tv_chsg_ath_info_2, 
	 * 3 - both
	 */
    private int statusInfo = 0; // 0 - not upload, 1 - only tv_chsg_ath_info_1, 2 - only tv_chsg_ath_info_2, 3 - both
	
	private TextView tvTitle, tv_chsg_intro, 
					tv_chsg_con_label,
					tv_chsg_ath_doc, tv_chsg_ath_capture, tv_chsg_ath_info_1, tv_chsg_ath_info_2, tv_chsg_ath_info;
	private Spinner sp_chsg_cat;
	private Button bt_chsg_go;
	private Button bt_chsg_add;
	private EditText edt_con, edt_time, edt_fee;
	private ImageView img_chsg_ath_del_1, img_chsg_ath_del_2;
	private MyInstantAutoComplete atv_chsg_tag;
	
	// Only NTG
	private LinearLayout ll_chsg_attach;
	private FrameLayout fr_chsg_attach;
	// Only GS
	private LinearLayout ll_chsg_time, ll_chsg_fee;
	
	private static final String TAG = FrgChoosing.class.getSimpleName();
	private ProgressDialog pDialog;
	
	private int iLearn, iCatInput, iCatSelect;
	private int iField, iSbj;
	private String sContent, sTag, sTime = "", sFee = "";
	
	private MyLinkedHashMap<Integer, String> hmTag; // Tags
	private ArrayAdapter<String> tagAdapter;
	
	private int firstTimeListSelection = 0; // check is already loaded list selection (order to load if has network during app runtime not initial)
	
	private NetworkStateReceiver mNetworkStateCallback;
	
	private SQLiteHandler db;
	
	private Bundle b;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		getBundle();
		View v = inflater.inflate(R.layout.frg_choosing0, container, false);
		setInitView(v);
		getSavedInstanceState(savedInstanceState);
		
		// Init UploadVisibility
		statusInfo = 0;
		setInfoUploadVisibility(statusInfo);
		
		prepareVariable();
		
		setInitData();
		
	    return v;
	}
	
	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null) {
			iCatInput = b.getInt("iCat");
			iLearn = b.getInt("iLearn");
			iSbj = b.getInt("iSbj");
			iField = b.getInt("iField");
		}
	}
	
	private void setInitData() {
		// TODO Auto-generated method stub
		tv_chsg_intro.setText(getActivity().getResources().getString(R.string.title_hello)+" "+Variables.userName);
		
		setDataForCat();
		
        prepareListSelection();
        feedListTag();
	}
	
	private void setDataForCat() {
		// TODO Auto-generated method stub
		List<String> catArray =  new ArrayList<String>();
		if (iCatInput == Constants.CATEGORY_GS) { // only GS
			catArray.add(getActivity().getResources().getString(R.string.label_chsg_0));
		} else if (iCatInput == Constants.CATEGORY_NTG) { // only NTG
			catArray.add(getActivity().getResources().getString(R.string.label_chsg_1));
		} else { // both
			catArray.add(getActivity().getResources().getString(R.string.label_chsg_1));
			catArray.add(getActivity().getResources().getString(R.string.label_chsg_0));
		}
        ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(
            getActivity(), android.R.layout.simple_spinner_item, catArray);
        catAdapter.setDropDownViewResource(R.layout.spinner_item);
        sp_chsg_cat.setAdapter(catAdapter);
	}

	private void prepareListSelection() {
		// TODO Auto-generated method stub
		hmTag = new MyLinkedHashMap<>();
	}
	private void resetListSelection(String need) {
		// TODO Auto-generated method stub
		switch (need) {
		case "tags":
			hmTag.clear();
			break;
		default:
			break;
		}
	}

	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		
		ll_chsg_attach = (LinearLayout)v.findViewById(R.id.ll_chsg_attach);
		fr_chsg_attach = (FrameLayout)v.findViewById(R.id.fr_chsg_attach);
		ll_chsg_time = (LinearLayout)v.findViewById(R.id.ll_chsg_time);
		ll_chsg_fee = (LinearLayout)v.findViewById(R.id.ll_chsg_fee);
		
		tv_chsg_intro = (TextView)v.findViewById(R.id.tv_chsg_intro);
		tv_chsg_con_label = (TextView)v.findViewById(R.id.tv_chsg_con_label);
		atv_chsg_tag = (MyInstantAutoComplete)v.findViewById(R.id.atv_chsg_tag);
		
		bt_chsg_add = (Button)v.findViewById(R.id.bt_chsg_add);
		bt_chsg_go = (Button)v.findViewById(R.id.bt_chsg_go);
		edt_con = (EditText)v.findViewById(R.id.edt_con);
		sp_chsg_cat = (Spinner)v.findViewById(R.id.sp_chsg_cat);
		
		pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        
		bt_chsg_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onAdd();
			}
		});
		bt_chsg_go.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onGo();
			}
		});
		sp_chsg_cat.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, 
                    int pos, long id) {
				onCatChange(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		atv_chsg_tag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // If wanna filter only the texts are present in TextView, update adapter here
            	
                String text = String.valueOf(s);
                String[] values = text.split(",");
                List<String> tagArray =  new ArrayList<String>();
    			for (int i = 0; i <hmTag.size(); i++) {
    				tagArray.add(hmTag.getValue(i));
    			}
                for (String value : values) {
                    for (String original : tagArray) {
                        if (original.equals(value.trim())) {
                        	tagAdapter.remove(original);
                        }
                    }
                }
                tagAdapter.notifyDataSetChanged();
                // delay execution to avoid "android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid"
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    	atv_chsg_tag.showDropDown();
                    }
                }, 500);  
                
            }
        });
		
			edt_time = (EditText)v.findViewById(R.id.edt_time);
			edt_fee = (EditText)v.findViewById(R.id.edt_fee);
			tv_chsg_ath_doc = (TextView)v.findViewById(R.id.tv_chsg_ath_doc);
			tv_chsg_ath_capture = (TextView)v.findViewById(R.id.tv_chsg_ath_capture);
			tv_chsg_ath_info_1 = (TextView)v.findViewById(R.id.tv_chsg_ath_info_1);
			tv_chsg_ath_info_2 = (TextView)v.findViewById(R.id.tv_chsg_ath_info_2);
			tv_chsg_ath_info = (TextView)v.findViewById(R.id.tv_chsg_ath_info);
			img_chsg_ath_del_1 = (ImageView)v.findViewById(R.id.img_chsg_ath_del_1);
			img_chsg_ath_del_2 = (ImageView)v.findViewById(R.id.img_chsg_ath_del_2);
			tv_chsg_ath_doc.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (statusInfo == 3)
						Functions.toastString(getActivity().getResources().getString(R.string.alert_chsg_ath_info), getActivity());
					else
						onUpFile();
				}
			});
			tv_chsg_ath_capture.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (statusInfo == 3)
						Functions.toastString(getActivity().getResources().getString(R.string.alert_chsg_ath_info), getActivity());
					else
						onCapture();
				}
			});
			
			img_chsg_ath_del_1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onInfoDel(1);
				}
			});
			img_chsg_ath_del_2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onInfoDel(2);
				}
			});

			if (iCatInput == 0) { // GS
				ll_chsg_attach.setVisibility(View.GONE);
				fr_chsg_attach.setVisibility(View.GONE);
				ll_chsg_time.setVisibility(View.VISIBLE);
				ll_chsg_fee.setVisibility(View.VISIBLE);
			} else { // NTG or BOTH
				ll_chsg_attach.setVisibility(View.VISIBLE);
				fr_chsg_attach.setVisibility(View.VISIBLE);
				ll_chsg_time.setVisibility(View.GONE);
				ll_chsg_fee.setVisibility(View.GONE);
			}
	}
	
	protected void onCapture() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    capturedUri = Functions.getOutputMediaFileUri(Constants.MEDIA_TYPE_IMAGE);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri);
	    // start the image capture Intent
	    startActivityForResult(intent, Constants.CAPTURE_IMAGE_REQUEST);
	}
	protected void onCatChange(int pos) {
		// TODO Auto-generated method stub
		String s = sp_chsg_cat.getSelectedItem().toString();
		if (s.equals(getActivity().getResources().getString(R.string.label_chsg_0))) {
			iCatSelect = 0;
		} else {
			iCatSelect = 1;
		}
		if (iCatSelect == 0) { // GS
			ll_chsg_attach.setVisibility(View.GONE);
			fr_chsg_attach.setVisibility(View.GONE);
			ll_chsg_time.setVisibility(View.VISIBLE);
			ll_chsg_fee.setVisibility(View.VISIBLE);
		} else { // NTG or BOTH
			ll_chsg_attach.setVisibility(View.VISIBLE);
			fr_chsg_attach.setVisibility(View.VISIBLE);
			ll_chsg_time.setVisibility(View.GONE);
			ll_chsg_fee.setVisibility(View.GONE);
		}
		//feedListTag();
	}
	
	private void feedListTag() {
		// TODO Auto-generated method stub
		String tag_string_feed = "feed_list_selection";
        pDialog.setMessage("Đang tải dữ liệu...");
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_LIST_SELECTION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get List selection Response: " + response.toString());
                
                try {
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                    	JSONArray jAList = jORoot.getJSONArray("tags");
                    	resetListSelection("tags");
                    	
                    	for (int i = 0; i < jAList.length(); i++) {
                    		JSONObject jElm = jAList.getJSONObject(i);
                    		Integer id = Integer.parseInt(jElm.optString("id"));
                            String name = jElm.optString("name");

								hmTag.put(id, name);

                    	}
                    	putListDataToView("tags");
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get List selection Error: " + errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Get List selection Error: " + error.getMessage());
                if (Variables.isAlreadyAlertConnection == 0) {
                	Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                	Variables.isAlreadyAlertConnection = 1;
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "sbj");
                params.put("sbj", String.valueOf(iSbj));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_feed);
	}
	protected void putListDataToView(String need) {
		// TODO Auto-generated method stub
		switch (need) {
		case "tags":
			List<String> tagArray =  new ArrayList<String>();
			for (int i = 0; i <hmTag.size(); i++) {
				tagArray.add(hmTag.getValue(i));
			}
			if (getActivity() != null) {
				tagAdapter = new ArrayAdapter<String>(
		            getActivity(), android.R.layout.simple_dropdown_item_1line, tagArray);
				tagAdapter.setDropDownViewResource(R.layout.spinner_item);
		        atv_chsg_tag.setAdapter(tagAdapter);
		        atv_chsg_tag.setTokenizer(new MyInstantAutoComplete.CommaTokenizer());
		        atv_chsg_tag.showDropDown();
			}
	        
	        firstTimeListSelection = 1;
			break;
		default:
			break;
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
	
	protected void onUpFile() {
		// TODO Auto-generated method stub
		showFileChooser(Constants.PICK_FILE_REQUEST);
	}
	private void showFileChooser(int code) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), code);
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
            		
            		putExistedImage(temPath);
            		
            	} else {
            		if (Functions.getFileLength(temPath) > Constants.MAX_DOCSIZE_UPLOAD)
                    	Functions.toastString("File không được lớn hơn "+String.valueOf(Constants.MAX_DOCSIZE_UPLOAD/1000)+"MB", getActivity());
                    else {
                    	sendFileToArr(0, temPath, null);
                    	setAttachInfo(Functions.getFileName(temPath));
                    }
            	}
        	} else { // Video file
        		Functions.toastString("Xin lỗi, bạn chỉ được gửi file ảnh hoặc tài liệu",  getActivity());
        	}
        	
        } else if (requestCode == Constants.CAPTURE_IMAGE_REQUEST) {
        	if (resultCode == getActivity().RESULT_OK) {
        		if (capturedUri != null) {
            		getFile();
            	}
        		putCapturedImage();
        	} else if (resultCode == getActivity().RESULT_CANCELED) {
                // user cancelled recording
            } else {
            	Functions.toastString("Có vấn đề khi chụp ảnh, vui lòng thử lại", getActivity());
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
                                                                            // only
                                                                            // mini's
                    MediaStore.Images.Thumbnails.MINI_KIND;

            String sort = MediaStore.Images.Thumbnails._ID + " DESC";

            // At the moment, this is a bit of a hack, as I'm returning ALL
            // images, and just taking the latest one. There is a better way
            // to
            // narrow this down I think with a WHERE clause which is
            // currently
            // the selection variable
            Cursor myCursor = getActivity().managedQuery(
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
                    MediaStore.Images.ImageColumns.DATA };

            String largeFileSort = MediaStore.Images.ImageColumns._ID
                    + " DESC";
            myCursor = getActivity().managedQuery(
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
                capturedUri = Uri.fromFile(new File(
                        largeImagePath));

            } finally {
                // myCursor.close();
            }
            // These are the two URI's you'll be interested in. They give
            // you a
            // handle to the actual images
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
            Log.i("TAG", "URI Normal:" + capturedUri.getPath());
        } catch (Exception e) {
            Log.i("TAG", "Excfeption inside Normal URI :" + e.toString());
        }
	}

	private void putExistedImage(String temPath) {
		// TODO Auto-generated method stub
		if (Functions.getFileLength(temPath) > Constants.MAX_IMGSIZE_UPLOAD)
			sendFileToArr(1, temPath, Functions.getBitmapFromPath(temPath, Constants.RESIZE_SampleSize));
		else
			sendFileToArr(1, temPath, Functions.getBitmapFromPath(temPath, 1));
		setAttachInfo(Functions.getFileName(temPath));
	}

	private void putCapturedImage() {
		// TODO Auto-generated method stub
		if (capturedUri != null) {
			Bitmap mBM = Functions.getBitmapFromPath(capturedUri.getPath().toString(), Constants.RESIZE_SampleSize);
			mBM = Functions.rotateBitmapIfNeeded(getActivity(), capturedUri.getPath().toString(), mBM);
			sendFileToArr(1, capturedUri.getPath().toString(), mBM);
			setAttachInfo(Functions.getFileName(capturedUri.getPath().toString()));
		}
	}
	
	protected void onAdd() {
		// TODO Auto-generated method stub
		getInput();
		checkFillingAndSend(0);
	}
	protected void onGo() {
		// TODO Auto-generated method stub
		getInput();
		checkFillingAndSend(1);
	}
	private void getInput() {
		// TODO Auto-generated method stub
		sContent = edt_con.getText().toString();
		sTag = atv_chsg_tag.getText().toString();
		String s = sp_chsg_cat.getSelectedItem().toString();
		if (s.equals(getActivity().getResources().getString(R.string.label_chsg_0))) {
			iCatSelect = Constants.CATEGORY_GS;
		} else {
			iCatSelect = Constants.CATEGORY_NTG;
		}
		if (iCatSelect == Constants.CATEGORY_GS) {
			sTime = edt_time.getText().toString();
			sFee = edt_fee.getText().toString();
		}
	}
	private void checkFillingAndSend(int isGo) {
		// TODO Auto-generated method stub
		sendChoosingToServer(isGo, iLearn, iCatSelect, iField, iSbj, sContent, sTag, Variables.userTel, sTime, sFee, Constants.SEX_NO_CHOICE, Variables.userAddress);
	}
	private void sendChoosingToServer(final int aisGo, final int aiLearn, final int aiCat,
			final int aiFie, final int aiSbj, final String asContent, final String asTag, final String asTel,
			final String asTime, final String asFee, final int aiSex, final String asLocation) {

		// TODO Auto-generated method stub
		String tag_string_choosing = "send_choosing";
		pDialog.setMessage("Đang gửi dữ liệu ...");
		showDialog();
		StringRequest strReq = new StringRequest(Method.POST,
		    Constants.URL_SET_CHOOSING, new Response.Listener<String>() {
		        	
			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Send Choosing Response: " + response.toString());
			    hideDialog();
			                
			    try {        	
			    	JSONObject jORoot = new JSONObject(response);
			        boolean error = jORoot.getBoolean("error");
			 
			        // Check for error node in json
			        if (!error) {
			        	if (aisGo == 0) {
			        		Functions.toastString("Thêm yêu cầu thành công, xin mời bạn tiếp tục chọn yêu cầu khác", getActivity());
			        	} else {
			        		Functions.toastString("Gửi yêu cầu thành công, chúng tôi đang tìm kiếm người phù hợp cho bạn", getActivity());
			        	}
			        	int idChoosing = jORoot.getInt("id_choosing");
			        	
			        	String imgToSqlite = "";
			        	String docToSqlite = "";
			        	
			        	if (iCatSelect == 1) {
				        	switch (statusInfo) {
							case 1:
								uploadFile(idChoosing, arrIsImg[0], arrPathFile[0], arrBitmap[0]);
								if (arrIsImg[0] == 1)
									imgToSqlite = arrPathFile[0];
								else
									docToSqlite = arrPathFile[0];
								break;
							case 2:
								uploadFile(idChoosing, arrIsImg[1], arrPathFile[1], arrBitmap[1]);
								if (arrIsImg[1] == 1)
									imgToSqlite = arrPathFile[1];
								else
									docToSqlite = arrPathFile[1];
								break;
							case 3:
								uploadFile(idChoosing, arrIsImg[0], arrPathFile[0], arrBitmap[0]);
								uploadFile(idChoosing, arrIsImg[1], arrPathFile[1], arrBitmap[1]);
								if (arrIsImg[0] == 0 && arrIsImg[1] == 0)
									docToSqlite = arrPathFile[0]+Constants.DELIMITER+arrPathFile[1];
								else if (arrIsImg[0] == 1 && arrIsImg[1] == 1)
									imgToSqlite = arrPathFile[0]+Constants.DELIMITER+arrPathFile[1];
								else if (arrIsImg[0] == 1 && arrIsImg[1] == 0) {
									imgToSqlite = arrPathFile[0];
									docToSqlite = arrPathFile[1];
								} else if (arrIsImg[0] == 0 && arrIsImg[1] == 1) {
									imgToSqlite = arrPathFile[1];
									docToSqlite = arrPathFile[0];
								}
								break;
							default:
								break;
							}
			        	}
			        	
			        	// Store last choosing to local database to reuse faster
			        	db.deleteChoosing(); // Delete the older
			        	db.addChoosing(idChoosing, aiLearn, aiCat, aiFie, aiSbj, asContent, asTag, 
			        			docToSqlite, imgToSqlite, asTel, asLocation, asFee, asTime, aiSex, 
			        			Functions.getCurrentTimestamp(), Variables.userID);
			        	
			        	if (aisGo == 0) {
			        		FrgField frgField = new FrgField();
			        		
			        		getActivity().getFragmentManager().beginTransaction()
			        	     .replace(R.id.fragment_container, frgField)
			        	     .addToBackStack(null)
			        	     .commit();
			        	} else {
						    FrgListMeet frgLMeet = new FrgListMeet();
							
							Bundle b = new Bundle();
							b.putInt("cId", 0);
							frgLMeet.setArguments(b);
							
							getActivity().getFragmentManager().beginTransaction()
						     .replace(R.id.fragment_container, frgLMeet)
						     .addToBackStack(null)
						     .commit();
			        	}
			        } else {
			        	// Error in login. Get the error message
			            String errorMsg = jORoot.getString("msg_err");
			            Functions.toastString(errorMsg, getActivity());
			            Log.e(TAG, "Send Choosing Error: " + errorMsg);
			        }
			    } catch (JSONException e) {
			    	// JSON error
			        e.printStackTrace();
			    }
			}
		}, new Response.ErrorListener() {
			@Override
		    public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Send Choosing Error: " + error.getMessage());
				if (Variables.isAlreadyAlertConnection == 0) {
                	Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                	Variables.isAlreadyAlertConnection = 1;
                }
		        hideDialog();
		    }
		}) {
			@Override
		    protected Map<String, String> getParams() {
				// Posting parameters to login url
		        Map<String, String> params = new HashMap<String, String>();
		        params.put("isGo", String.valueOf(aisGo));
		        params.put("iLearn", String.valueOf(aiLearn));
		        params.put("iCat", String.valueOf(aiCat));
		        params.put("iFie", String.valueOf(aiFie));
		        params.put("iSbj", String.valueOf(aiSbj));
		        params.put("sContent", asContent);
		        params.put("sTag", asTag);
		        params.put("sTel", asTel);
		        params.put("sTime", asTime);
		        params.put("sFee", asFee);
		        params.put("iSex", String.valueOf(aiSex));
		        params.put("sLocation", asLocation);
		        params.put("sidUser", Variables.userID);
		        params.put("iUserType", String.valueOf(Variables.userType));
		        params.put("sToken", Variables.userToken);
		 
		        return params;
		    }
		};
		AppController.getInstance().addToRequestQueue(strReq, tag_string_choosing);
	}
	public void uploadFile(final int idChoosing, final int isImg, final String path, final Bitmap mBM) {
		if (isImg == 1 && mBM == null) {
			Functions.toastString("Có vấn đề với file đính kèm của bạn, vui lòng kiểm tra lại.", getActivity());
		} else {
			final ProgressDialog loading = ProgressDialog.show(getActivity(),"Uploading...","Please wait...",false,false);
			StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_UPLOAD,
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
        	                    int isImg = info.getInt("isImg");
        	                    String path = info.getString("path"); // Path on Server (full as stored on db, include all file)
        	                    //Showing toast message of the response
                                //Functions.toastString("Gửi đi thành công", getActivity());
                                
                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("msg_err");
                                Functions.toastString("Không thể upload tài liệu: "+errorMsg, getActivity());
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
                        if (Variables.isAlreadyAlertConnection == 0) {
                        	Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                        	Variables.isAlreadyAlertConnection = 1;
                        }
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
	            		sBase64 = Functions.getBase64Image(mBM);
	                    name = Functions.getFileName(path);
	            	}
	 
	                //Creating parameters
	                Map<String,String> params = new Hashtable<String, String>();
	 
	                //Adding parameters
	                params.put("idChoosing", String.valueOf(idChoosing));
	                params.put("isImg", String.valueOf(isImg));
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
	protected void onAddTag() {
		// TODO Auto-generated method stub
		sContent = edt_con.getText().toString();
		sContent += " #";
		edt_con.setText(sContent);
		edt_con.setSelection(sContent.length());
	}
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        String sTitle = getActivity().getResources().getString(R.string.title_chsg);
        tvTitle.setText(sTitle);
	}
	protected void onInfoDel(int position) {
		// TODO Auto-generated method stub
		if (position == 1) {
			arrPathFile[0] = "";
			arrBitmap[0] = null;
			arrIsImg[0] = -1;
			switch (statusInfo) {
			case 1:
				statusInfo = 0;
				setInfoUploadVisibility(statusInfo);
				break;
			case 3:
				statusInfo = 2;
				setInfoUploadVisibility(statusInfo);
				break;
			default:
				break;
			}
		} else if (position == 2) {
			arrPathFile[1] = "";
			arrBitmap[1] = null;
			arrIsImg[1] = -1;
			switch (statusInfo) {
			case 2:
				statusInfo = 0;
				setInfoUploadVisibility(statusInfo);
				break;
			case 3:
				statusInfo = 1;
				setInfoUploadVisibility(statusInfo);
				break;
			default:
				break;
			}
		}
	}
	private void sendFileToArr(int isImg, String path, Bitmap mBM) {
		// TODO Auto-generated method stub
		switch (statusInfo) { // Check for previous status of statusInfo
		case 0: // will add to first position
			arrPathFile[0] = path;
			arrBitmap[0] = mBM;
			arrIsImg[0] = isImg;
			break;
		case 1: // will add to second position
			arrPathFile[1] = path;
			arrBitmap[1] = mBM;
			arrIsImg[1] = isImg;
			break;
		case 2: // will add to first position
			arrPathFile[0] = path;
			arrBitmap[0] = mBM;
			arrIsImg[0] = isImg;
			break;
		default:
			break;
		}
	}
	private void setAttachInfo(String path) {
		// TODO Auto-generated method stub
		switch (statusInfo) { // Check for previous status of statusInfo
		case 0:
			tv_chsg_ath_info_1.setText(path);
			statusInfo = 1;
			setInfoUploadVisibility(statusInfo);
			break;
		case 1:
			tv_chsg_ath_info_2.setText(path);
			statusInfo = 3;
			setInfoUploadVisibility(statusInfo);
			break;
		case 2:
			tv_chsg_ath_info_1.setText(path);
			statusInfo = 3;
			setInfoUploadVisibility(statusInfo);
			break;
		default:
			break;
		}
	}
	private void setInfoUploadVisibility(int aCase) {
		// TODO Auto-generated method stub
		if (aCase == 0) { // Not upload
			tv_chsg_ath_info.setVisibility(View.VISIBLE);
			tv_chsg_ath_info_1.setVisibility(View.INVISIBLE);
			tv_chsg_ath_info_2.setVisibility(View.INVISIBLE);
			img_chsg_ath_del_1.setVisibility(View.INVISIBLE);
			img_chsg_ath_del_2.setVisibility(View.INVISIBLE);
		} else if (aCase == 1) { // Only file 1 uploaded
			tv_chsg_ath_info.setVisibility(View.INVISIBLE);
			tv_chsg_ath_info_1.setVisibility(View.VISIBLE);
			img_chsg_ath_del_1.setVisibility(View.VISIBLE);
			tv_chsg_ath_info_2.setVisibility(View.INVISIBLE);
			img_chsg_ath_del_2.setVisibility(View.INVISIBLE);
		} else if (aCase == 2) { // Only file 2 uploaded
			tv_chsg_ath_info.setVisibility(View.INVISIBLE);
			tv_chsg_ath_info_1.setVisibility(View.INVISIBLE);
			img_chsg_ath_del_1.setVisibility(View.INVISIBLE);
			tv_chsg_ath_info_2.setVisibility(View.VISIBLE);
			img_chsg_ath_del_2.setVisibility(View.VISIBLE);
		} else { // Both file 1,2 uploaded
			tv_chsg_ath_info.setVisibility(View.INVISIBLE);
			tv_chsg_ath_info_1.setVisibility(View.VISIBLE);
			img_chsg_ath_del_1.setVisibility(View.VISIBLE);
			tv_chsg_ath_info_2.setVisibility(View.VISIBLE);
			img_chsg_ath_del_2.setVisibility(View.VISIBLE);
		}
	}
	
	private void initNetworkChecking() {
		// TODO Auto-generated method stub
		mNetworkStateCallback = new NetworkStateReceiver();
		mNetworkStateCallback.setNetworkStateListener(new NetworkStateListener() {		
			@Override
			public void onNetChange(int status) {
				// TODO Auto-generated method stub
				switch (status) {
				case NetworkUtil.TYPE_WIFI:
					Variables.isAlreadyAlertConnection = 0;
					if (firstTimeListSelection == 0)
						feedListTag();
					break;
				case NetworkUtil.TYPE_MOBILE:
					Variables.isAlreadyAlertConnection = 0;
					if (firstTimeListSelection == 0)
						feedListTag();
					break;
				case NetworkUtil.TYPE_NOT_CONNECTED:
					if (Variables.isAlreadyAlertConnection == 0) {
						Functions.toastString(getActivity().getResources().getString(R.string.alert_no_internet), getActivity());
						Variables.isAlreadyAlertConnection = 1;
					}
					break;
				default:
					break;
				}
			}
		});
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
		getActivity().registerReceiver(mNetworkStateCallback, filter);
	}
	private void prepareVariable() {
		// TODO Auto-generated method stub
		// SQLite database handler
        db = new SQLiteHandler(getActivity());
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putParcelable("capturedUri", capturedUri);
	}
	private void getSavedInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (savedInstanceState != null) {
			capturedUri = savedInstanceState.getParcelable("capturedUri");
		}
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		getActivity().unregisterReceiver(mNetworkStateCallback);
		super.onStop();
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initNetworkChecking();
	}
	/* Reference 1
	{
	"error": false
	"subjects": [
		{
			"id": "1"
			"name": "Lớp 1"
		},
		{
			"id": "2"
			"name": "Lớp 2"
		}]
	}*/
}
