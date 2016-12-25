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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import conghaodng.demo.profinder.utils.ListConnectAdapter;
import conghaodng.demo.profinder.utils.ListConnectData;

public class FrgListConnect extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	private ListView lv_list_main;
	private TextView tvTitle;
	private Button bt_lc_add, bt_lc_search;
	
	private static final String TAG = FrgListConnect.class.getSimpleName();
	private ProgressDialog pDialog;
		
	private List<ListConnectData> connList;
	private ListConnectAdapter connAdapter;
	private SwipeRefreshLayout swipeRefreshLayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		getActivity().getActionBar().show();
		View v = inflater.inflate(R.layout.frg_list_connect, container, false);
		setInitView(v);
		setData();
		
	    return v;
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		feedListConnect();
	}
	public void setData() {
		// TODO Auto-generated method stub
		if (connList == null)
			connList = new ArrayList<ListConnectData>();
		if (connAdapter == null)
			connAdapter = new ListConnectAdapter(getActivity(), connList);
		
		lv_list_main.setAdapter(connAdapter);
		
	}

	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		
		lv_list_main = (ListView)v.findViewById(R.id.lv_list_main);
		swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                feedListConnect();
            }
        });
		
		bt_lc_add = (Button)v.findViewById(R.id.bt_lc_add);
		bt_lc_search = (Button)v.findViewById(R.id.bt_lc_search);
		bt_lc_add.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onAdd();
			}
		});
		bt_lc_search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSearch();
			}
		});

		pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
	}
	protected void onAdd() {
		
		FrgField frgField = new FrgField();
		
		getActivity().getFragmentManager().beginTransaction()
	     .replace(R.id.fragment_container, frgField)
	     .addToBackStack(null)
	     .commit();
		
	}
	protected void onSearch() {
		
		FrgListMeet fragment = new FrgListMeet();
		
		Bundle b = new Bundle();
		b.putInt("cId", 0);
		fragment.setArguments(b);
		
		getActivity().getFragmentManager().beginTransaction()
	     .replace(R.id.fragment_container, fragment)
	     .addToBackStack(null)
	     .commit();
    	
	}

	@Override
    public void onRefresh() {
		feedListConnect();
    }
	private void feedListConnect() {
		// TODO Auto-generated method stub
		String tag_string_feed = "feed_list_connect";
        pDialog.setMessage("Đang tải dữ liệu...");
        
        showDialog();
        
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_LIST_CONNECT, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get List connect Response: " + response.toString());
                
                hideDialog();
                
                try {
                	// Reference 1: jSon sample in below
                	
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {

                    	JSONArray jAList = jORoot.getJSONArray("connect");
                    	
                    	connList.clear();
                    	for (int i = 0; i < jAList.length(); i++) {
                    		JSONObject jElm = jAList.getJSONObject(i);

                    		ListConnectData conn = new ListConnectData(
                    				jElm.getInt("idConnect"), 
                    				jElm.getString("username1"), 
                    				jElm.getString("username2"), 
                    				jElm.getString("field"), 
                    				jElm.getString("subject"), 
                    				jElm.getString("content"),
                    				jElm.getString("date"));
                    		connList.add(conn);
                    	}
                    	connAdapter.notifyDataSetChanged();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get List connect Error: " + errorMsg);
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
                Log.e(TAG, "Get List connect Error: " + error.getMessage());

                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                
                hideDialog();
                swipeRefreshLayout.setRefreshing(false);
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
        tvTitle.setText(getResources().getString(R.string.title_list_connect));
	}
}
