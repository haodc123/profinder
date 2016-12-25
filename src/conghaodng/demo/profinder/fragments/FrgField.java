package conghaodng.demo.profinder.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.ListFieldAdapter;
import conghaodng.demo.profinder.utils.ListFieldData;
import conghaodng.demo.profinder.utils.ListMeetData;
import conghaodng.demo.profinder.view.MyGridView;

public class FrgField extends Fragment {
	
	private TextView tvTitle;
	private MyGridView grd_row_field;
	private List<ListFieldData> fieldList;
	private ListFieldAdapter fieldAdapter;
	
	private static final String TAG = FrgField.class.getSimpleName();
	private ProgressDialog pDialog;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frg_field, container, false);
		
		setInitView(v);
		setData();
		
		return v;
	}
	
	public void setData() {
		// TODO Auto-generated method stub
		if (fieldList == null)
			fieldList = new ArrayList<ListFieldData>();
		if (fieldAdapter == null)
			fieldAdapter = new ListFieldAdapter(getActivity(), fieldList);
		
		grd_row_field.setAdapter(fieldAdapter);
		feedListField();
	}
	
	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		grd_row_field = (MyGridView)v.findViewById(R.id.grd_row_field);
		pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
	}

	private void feedListField() {
		// TODO Auto-generated method stub
        pDialog.setMessage("Đang tải dữ liệu...");
        showDialog();
        
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_LIST_SELECTION, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get List field Response: " + response.toString());
                hideDialog();
                try {
                	// Reference 1: jSon sample in below
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                    	fieldList.clear();
                    	JSONArray jAList = jORoot.getJSONArray("fields");
                    	for (int i = 0; i < jAList.length(); i++) {
                    		JSONObject jElm = jAList.getJSONObject(i);
                    		Integer id = Integer.parseInt(jElm.optString("id"));
                            String name = jElm.optString("name");
                            String img = jElm.optString("img");
                            
                            ListFieldData data = new ListFieldData(name, img, id);
                            fieldList.add(data);
                    	}
                    	fieldAdapter.notifyDataSetChanged();
                    	
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get List field Error: " + errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Get List field Error: " + error.getMessage());
                hideDialog();
                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
               
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "cat");
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
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_field));
	}
}
