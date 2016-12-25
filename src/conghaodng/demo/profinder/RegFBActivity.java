package conghaodng.demo.profinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.SQLiteHandler;

public class RegFBActivity extends Activity {

	private EditText edt_regfb_tel, edt_regfb_email, edt_regfb_address;
	private Button bt_regfb_enter;
	private TextView tvTitle, tv_regfb_intro;

	private static final String TAG = RegFBActivity.class.getSimpleName();
	private ProgressDialog pDialog;
	private SQLiteHandler db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_regfb);

		setInitView(); // initial view object

		prepareVariable();

	}

	private void setInitView() {
		// TODO Auto-generated method stub
		setCustomActionBar();

		edt_regfb_tel = (EditText) findViewById(R.id.edt_regfb_tel);
		edt_regfb_email = (EditText) findViewById(R.id.edt_regfb_email);
		edt_regfb_address = (EditText) findViewById(R.id.edt_regfb_address);
		bt_regfb_enter = (Button) findViewById(R.id.bt_regfb_enter);
		tv_regfb_intro = (TextView) findViewById(R.id.tv_regfb_intro);

		tv_regfb_intro.setText("Chào "+Variables.userName+", vui lòng cung cấp thêm thông tin để chúng tôi tìm người phù hợp với bạn.");
		edt_regfb_tel.setText(Variables.userTel);
		edt_regfb_email.setText(Variables.userEmail);
		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		bt_regfb_enter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onRegFB();
			}
		});
	}

	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.actionbar_simple);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getResources().getString(R.string.title_reg));
	}

		private void prepareVariable() {
		// TODO Auto-generated method stub
		// SQLite database handler
		db = new SQLiteHandler(getApplicationContext());
	}

	protected void onRegFB() {
		// TODO Auto-generated method stub
		String email = edt_regfb_email.getText().toString().trim();
		String tel = edt_regfb_tel.getText().toString().trim();
		String address = edt_regfb_address.getText().toString();

		if (Functions.isEmailValid(email) != true || Functions.isPhoneValid(tel) != true) {
			Functions.toastString(getResources().getString(R.string.alert_input_email), getBaseContext());
		} else {

			if (address.isEmpty() || email.isEmpty() || tel.isEmpty()) {
				Functions.toastString("Vui lòng nhập đày đủ thông tin.", this);
			} else {
				if (!Functions.hasConnection(this)) {
					Functions.toastString(getResources().getString(R.string.alert_no_internet), this);
				} else {
					updateFBUser(email, tel, address, Variables.userID);
				}
			}
		}
	}

	private void updateFBUser(final String email, final String tel, final String address, final String userid) {
		// TODO Auto-generated method stub
		// Tag used to cancel the request
		
		pDialog.setMessage("Đang đăng ký ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST, Constants.URL_UPDATE_FBUSER, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Update FBUser Response: " + response.toString());
				hideDialog();

				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean("error");
					if (!error) {
						// User successfully stored in MySQL
						JSONObject user = jObj.getJSONObject("user");
						Variables.userID = user.getString("userid");
						Variables.userType = 0;
						Variables.userName = user.getString("name");
						Variables.userEmail = user.getString("email");
						Variables.userAddress = user.getString("address");
						Variables.userSex = user.getInt("sex");
						Variables.userTel = user.getString("tel");
						Variables.userToken = user.getString("token");

						// Inserting row in users table
						db.deleteUser();
						db.addUser(Variables.userFBID, Variables.userName, Variables
								.userAvatar, Variables.userEmail, Variables.userTel,
								Variables.userAddress, Variables.userSex, Variables.userID, Variables.userToken,
								Variables.userType);

						Functions.toastString("Đăng ký thành công!", getBaseContext());

						// Launch main activity
						Intent intent = new Intent(RegFBActivity.this, MainActivity.class);
						intent.putExtra("action", Constants.ACTION_FIELD);
						startActivity(intent);
						finish();
					} else {

						// Error occurred in registration. Get the error
						// message
						String errorMsg = jObj.getString("msg_err");
						Functions.toastString(errorMsg + " Đăng ký thất bại, xin vui lòng thử lại.", getBaseContext());
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Functions.toastString(getResources().getString(R.string.error_json), getBaseContext());
					Log.d(TAG, e.getMessage() + "\\" + response.toString());
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Update FBUser Error: " + error.getMessage());
				Functions.toastString(getResources().getString(R.string.error_server_connect), getBaseContext());
				hideDialog();
			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tel", tel);
				params.put("email", email);
				params.put("userid", userid);
				params.put("address", address);
				params.put("token", Variables.userToken);

				return params;
			}

		};

		// Adding request to request queue
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
					&& (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
			}
		}
		return ret;
	}
}
