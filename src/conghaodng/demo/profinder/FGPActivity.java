package conghaodng.demo.profinder;

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
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.SQLiteHandler;
import conghaodng.demo.profinder.utils.SessionManager;

public class FGPActivity extends Activity {
	
	private EditText edt_fgp_email;
	private Button bt_fgp_enter;
	private TextView tvTitle, tv_fgp_gologin;
	
	private static final String TAG = FGPActivity.class.getSimpleName();
	private ProgressDialog pDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_fgp);
		
		setInitView(); // initial view object
	}
	
	private void setInitView() {
		// TODO Auto-generated method stub
		setCustomActionBar();
		
		edt_fgp_email = (EditText)findViewById(R.id.edt_fgp_email);
		bt_fgp_enter = (Button)findViewById(R.id.bt_fgp_enter);
		tv_fgp_gologin = (TextView)findViewById(R.id.tv_fgp_gologin);
		// Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        
        bt_fgp_enter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSend();
			}
		});
        tv_fgp_gologin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_fgp));
	}
	protected void onSend() {
		// TODO Auto-generated method stub
        String email = edt_fgp_email.getText().toString().trim();

        if (email.isEmpty()) {
            Functions.toastString("Vui lòng nhập email.", this);
        } else {
        	if (!Functions.hasConnection(this)) {
				Functions.toastString(getResources().getString(R.string.alert_no_internet), this);
			} else {
				resetPass(email);
			}
        }
	}

	private void resetPass(final String email) {
		// TODO Auto-generated method stub
		// Tag used to cancel the request
        String tag_string_send = "send_register";
 
        pDialog.setMessage("Đang xử lý ...");
        showDialog();
 
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_FORGOTPASS, new Response.Listener<String>() {
 
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "FGP Response: " + response.toString());
                hideDialog();
 
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
 
                        Functions.toastString("Gửi thành công! Chúng tôi đã gửi thư đến email bạn vừa nhập. Vui lòng kiểm tra email để thay đổi mât khẩu.", getBaseContext());
 
                    } else {
 
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("msg_err");
                        Functions.toastString(errorMsg + " Gửi đi thất bại, xin vui lòng thử lại.", getBaseContext());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.toastString(getResources().getString(R.string.error_json), getApplicationContext());
                    Log.d(TAG, e.getMessage()+"\\"+response.toString());
                }
 
            }
        }, new Response.ErrorListener() {
 
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "FGP Error: " + error.getMessage());
                Functions.toastString(getResources().getString(R.string.error_server_connect), getBaseContext());
                hideDialog();
            }
        }) {
 
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
 
                return params;
            }
 
        };
 
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_send);
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
