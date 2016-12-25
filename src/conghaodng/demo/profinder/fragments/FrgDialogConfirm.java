package conghaodng.demo.profinder.fragments;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;

public class FrgDialogConfirm extends Fragment {

	private Button dg_Yes, dg_No;
	private Bundle b;
	private int cId;
	private static final String TAG = FrgDialogConfirm.class.getSimpleName();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		//getActivity().getActionBar().hide();
		View v = inflater.inflate(R.layout.dialog_confirm, container, false);
		setInitView(v);
		getBundle();

	    return v;
	}

	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null)
			cId = b.getInt("cId");
	}

	private void setInitView(View v) {
		// TODO Auto-generated method stub
		dg_Yes = (Button)v.findViewById(R.id.dg_yes);
		dg_No = (Button)v.findViewById(R.id.dg_no);
		
		dg_Yes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				requestDelChsg(cId);
			}
		});
		dg_No.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().getFragmentManager().popBackStack();
			}
		});
	}
	private void requestDelChsg(final int cId) {
		// TODO Auto-generated method stub
		String tag_del_chsg = "del_chsg";
        
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_DEL_CHOOSING, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Del choosing Response: " + response.toString());
                
                try {
                	// Reference 1: jSon sample in below
                	
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                    	Functions.toastString("Xóa thành công.", getActivity());
                    	getActivity().getFragmentManager().popBackStack();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Del choosing choosing Error: " + errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Get List choosing Error: " + error.getMessage());

                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("cId", String.valueOf(cId));
                params.put("sToken", Variables.userToken);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_del_chsg);
	}
}
