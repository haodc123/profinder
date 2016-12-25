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
import conghaodng.demo.profinder.utils.SessionManager;
/**
 * 
 * @author UserPC
 * Convention:
 * sex: 0 - not set
 * 1 - male
 * 2 - female
 *
 */
public class RegActivity extends Activity {
	
	private EditText edt_reg_name, edt_reg_tel, edt_reg_pass, edt_reg_address;
	private Button bt_reg_enter;
	private TextView tvTitle;
	private Spinner sp_reg_sex;
	
	private static final String TAG = RegActivity.class.getSimpleName();
	private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private HashMap<String, String> userSaved;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_reg);
		
		setInitView(); // initial view object
		
		prepareVariable();
		
		goMainActivityIfLoggedIn();
	
	}
	
	private void setInitView() {
		// TODO Auto-generated method stub
		setCustomActionBar(); 
		
		edt_reg_name = (EditText)findViewById(R.id.edt_reg_name);
		edt_reg_tel = (EditText)findViewById(R.id.edt_reg_tel);
		edt_reg_pass = (EditText)findViewById(R.id.edt_reg_pass);
		edt_reg_address = (EditText)findViewById(R.id.edt_reg_address);
		bt_reg_enter = (Button)findViewById(R.id.bt_reg_enter);
		sp_reg_sex = (Spinner)findViewById(R.id.sp_reg_sex);
		// Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        List<String> sexArray =  new ArrayList<String>();
        sexArray.add(getResources().getString(R.string.spinner_chsg_male));
        sexArray.add(getResources().getString(R.string.spinner_chsg_female));
        ArrayAdapter<String> sexAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sexArray);
        sexAdapter.setDropDownViewResource(R.layout.spinner_item);
        sp_reg_sex.setAdapter(sexAdapter);
        
		bt_reg_enter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onReg();
			}
		});
	}
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_reg));
	}
	private void goMainActivityIfLoggedIn() {
		// TODO Auto-generated method stub
		if (session.isLoggedIn()) {
			// Assign logged in info to global variables
			if (session.getUserType() == 1) {
				userSaved = db.getUserDetails();
				Variables.userID = userSaved.get(SQLiteHandler.KEY_UID);
				Variables.userType = 1;
				Variables.userName = userSaved.get(SQLiteHandler.KEY_NAME);
				Variables.userEmail = userSaved.get(SQLiteHandler.KEY_EMAIL);
				Variables.userToken = userSaved.get(SQLiteHandler.KEY_TOKEN);
			} else {
				userSaved = db.getUserDetails();
				Variables.userID = userSaved.get(SQLiteHandler.KEY_UID);
				Variables.userType = 0;
				Variables.userName = userSaved.get(SQLiteHandler.KEY_NAME);
				Variables.userEmail = userSaved.get(SQLiteHandler.KEY_EMAIL);
				Variables.userToken = userSaved.get(SQLiteHandler.KEY_TOKEN);
			}
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegActivity.this, MainActivity.class);
            intent.putExtra("action", Constants.ACTION_LISTMEET);
            startActivity(intent);
            finish();
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
	protected void onReg() {
		// TODO Auto-generated method stub
		String name = edt_reg_name.getText().toString().trim();
        String email = edt_reg_tel.getText().toString().trim();
        String password = edt_reg_pass.getText().toString().trim();
        String address = edt_reg_address.getText().toString();
        int sex = sp_reg_sex.getSelectedItemPosition() + 1;

        int isEmail = 1;
	    if (Functions.isEmailValid(email) != true && Functions.isPhoneValid(email) != true) {
	    	Functions.toastString(getResources().getString(R.string.alert_input_email), getBaseContext());
	    } else {
	    	if (Functions.isPhoneValid(email) == true) {
	    		isEmail = 0;
	    	}
	    	if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
	            Functions.toastString("Vui lòng nhập đày đủ thông tin.", this);
	        } else {
	        	if (!Functions.hasConnection(this)) {
					Functions.toastString(getResources().getString(R.string.alert_no_internet), this);
				} else {
					registerUser(name, email, password, address, sex, isEmail);
				}
	        }
	    }
	}

	private void registerUser(final String name, final String email, final String password, final String address, final int sex, final int isEmail) {
		// TODO Auto-generated method stub
		// Tag used to cancel the request
        String tag_string_req = "req_register";
        final String token = Functions.genRandomString(20);
 
        pDialog.setMessage("Đang đăng ký ...");
        showDialog();
 
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_REG, new Response.Listener<String>() {
 
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();
 
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        JSONObject user = jObj.getJSONObject("user");
                        Variables.userID = user.getString("userid");
                        Variables.userType = 1;
                        Variables.userName = user.getString("name");
                        Variables.userEmail = user.getString("email");
                        Variables.userAddress = user.getString("address");
                        Variables.userSex = user.getInt("sex");
                        Variables.userTel = user.getString("tel");
                        Variables.userToken = token;
                        
                        // Create login session
                        session.setLogin(true, 1);
                        // Inserting row in users table
                        db.addUser("", Variables.userName, Variables.userAvatar, Variables.userEmail, Variables.userTel, Variables.userAddress, Variables.userSex, Variables.userID, Variables.userToken, Variables.userType);
 
                        Functions.toastString("Đăng ký thành công!", getBaseContext());
 
                        // Launch main activity
                        Intent intent = new Intent(RegActivity.this,MainActivity.class);
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
                    Log.d(TAG, e.getMessage()+"\\"+response.toString());
                }
 
            }
        }, new Response.ErrorListener() {
 
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Functions.toastString(getResources().getString(R.string.error_server_connect), getBaseContext());
                hideDialog();
            }
        }) {
 
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("address", address);
                params.put("sex", String.valueOf(sex));
                params.put("token", token);
                params.put("isEmail", String.valueOf(isEmail));
                params.put("IMEI", Functions.getIMEI(getBaseContext()));
 
                return params;
            }
 
        };
 
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
