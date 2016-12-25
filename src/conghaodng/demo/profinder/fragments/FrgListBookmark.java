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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.ListBMAdapter;
import conghaodng.demo.profinder.utils.ListBMAdapter.ListBMData;

public class FrgListBookmark extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	private ListView lv_list_main;
	private TextView tvTitle;
	
	private static final String TAG = FrgListBookmark.class.getSimpleName();
	private ProgressDialog pDialog;
		
	private SwipeRefreshLayout swipeRefreshLayout;
	private List<ListBMData> bmList;
	private ListBMAdapter bmAdapter;
	
	private View v;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		
		v = inflater.inflate(R.layout.frg_list_bookmark, container, false);
		
		setInitView();
		setData();
		feedListBM();
	    return v;
	}
	
	public void setData() {
		// TODO Auto-generated method stub
		if (bmList == null)
			bmList = new ArrayList<ListBMData>();
		if (bmAdapter == null)
			bmAdapter = new ListBMAdapter(getActivity(), bmList);
		
		lv_list_main.setAdapter(bmAdapter);
		
	}

	private void setInitView() {
		// TODO Auto-generated method stub
		setCustomActionBar();
		lv_list_main = (ListView)v.findViewById(R.id.lv_list_main);
		swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                feedListBM();
            }
        });
		
		pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
	}
	
	@Override
    public void onRefresh() {
		feedListBM();
    }
	private void feedListBM() {
		// TODO Auto-generated method stub
        pDialog.setMessage("Đang tải dữ liệu...");
                
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_GET_BOOKMARK, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get List BM Response: " + response.toString());
                
                try {
                	// Reference 1: jSon sample in below
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {

                    	JSONArray jAList = jORoot.getJSONArray("bms");

                    	bmList.clear();
                    	for (int i = 0; i < jAList.length(); i++) {
                    		JSONObject jElm = jAList.getJSONObject(i);

                    		ListBMData bm = new ListBMData(jElm.optString("bm_FBID"), jElm.optString("bm_id"), jElm.optString("bm_email"),
                    				jElm.optString("bm_name"), jElm.optString("bm_avatar"), jElm.optString("bm_tel"), jElm.optString("bm_address"), 
                    				jElm.optString("bm_rating"), jElm.optInt("bm_sex"));
                    		bmList.add(bm);
                    	}
                    	bmAdapter.notifyDataSetChanged();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get List BM Error: " + errorMsg);
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
                Log.e(TAG, "Get List BM Error: " + error.getMessage());

                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                swipeRefreshLayout.setRefreshing(false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("sToken", Variables.userToken);
                params.put("userID", Variables.userID);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "");
	}
	
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        
        tvTitle.setText(getResources().getString(R.string.title_list_bm));
        
	}
}
