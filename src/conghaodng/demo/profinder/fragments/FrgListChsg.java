package conghaodng.demo.profinder.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
import conghaodng.demo.profinder.utils.ListChsgAdapter;
import conghaodng.demo.profinder.utils.ListChsgData;

public class FrgListChsg extends Fragment {

	private ListView lv_list_main;
	private TextView tvTitle;
	
	private static final String TAG = FrgListChsg.class.getSimpleName();
	private ProgressDialog pDialog;
		
	private List<ListChsgData> chsgList;
	private ListChsgAdapter chsgAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		getActivity().getActionBar().show();
		View v = inflater.inflate(R.layout.frg_list_chsg, container, false);
		setInitView(v);
		setData();
		
	    return v;
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		feedListChoosing();
	}
	public void setData() {
		// TODO Auto-generated method stub
		if (chsgList == null)
			chsgList = new ArrayList<ListChsgData>();
		if (chsgAdapter == null)
			chsgAdapter = new ListChsgAdapter(getActivity(), chsgList);
		
		lv_list_main.setAdapter(chsgAdapter);
		
	}

	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		
		lv_list_main = (ListView)v.findViewById(R.id.lv_list_main);
		
		pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
	}
	
	private void feedListChoosing() {
		// TODO Auto-generated method stub
		String tag_string_feed = "feed_list_chsg";
        pDialog.setMessage("Đang tải dữ liệu...");
        
        showDialog();
        
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_LIST_CHOOSING, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get List choosing Response: " + response.toString());
                
                hideDialog();
                
                try {
                	// Reference 1: jSon sample in below
                	
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {

                    	JSONArray jAList = jORoot.getJSONArray("choosings");
                    	
                    	chsgList.clear();
                    	for (int i = 0; i < jAList.length(); i++) {
                    		JSONObject jElm = jAList.getJSONObject(i);

                    		ListChsgData chsg = new ListChsgData(jElm.getString("Field"), jElm.getString("Subject"), jElm.getString("Tag"), 
                    				jElm.getInt("id"), jElm.getInt("iLearn"), jElm.getInt("iCat"));
                    		chsgList.add(chsg);
                    	}
                    	chsgAdapter.notifyDataSetChanged();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get List choosing Error: " + errorMsg);
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
                
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", Variables.userToken);
                params.put("uID", Variables.userID);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_feed);
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
        tvTitle.setText(getResources().getString(R.string.title_list_chsg));
	}
}
