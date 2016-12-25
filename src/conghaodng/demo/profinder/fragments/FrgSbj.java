package conghaodng.demo.profinder.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;

import android.app.ActionBar;
import android.app.Activity;
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
import conghaodng.demo.profinder.utils.ListSbjAdapter;
import conghaodng.demo.profinder.utils.ListSbjData;
import conghaodng.demo.profinder.view.MyGridView;

public class FrgSbj extends Fragment {
	
	private TextView tvTitle;
	private MyGridView grd_row_sbj;
	private List<ListSbjData> sbjList;
	private ListSbjAdapter sbjAdapter;
	
	private static final String TAG = FrgSbj.class.getSimpleName();
	private ProgressDialog pDialog;
	
	private Bundle b;
	private int iField;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frg_sbj, container, false);
		
		setInitView(v);
		getBundle();
		setData();
		return v;
	}
	
	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null) {
			iField = b.getInt("iField");
		}
	}
	public void setData() {
		// TODO Auto-generated method stub
		if (sbjList == null)
			sbjList = new ArrayList<ListSbjData>();
		if (sbjAdapter == null)
			sbjAdapter = new ListSbjAdapter(getActivity(), sbjList);
		
		grd_row_sbj.setAdapter(sbjAdapter);
		feedListSbj();
	}
	
	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		grd_row_sbj = (MyGridView)v.findViewById(R.id.grd_row_sbj);
		pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
	}

	private void feedListSbj() {
		// TODO Auto-generated method stub
        pDialog.setMessage("Đang tải dữ liệu...");
        showDialog();
                
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_LIST_SELECTION, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get List sbj Response: " + response.toString());
                hideDialog();
                
                try {
                	// Reference 1: jSon sample in below
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                    	sbjList.clear();
                    	JSONArray jAList = jORoot.getJSONArray("subjects");
                    	for (int i = 0; i < jAList.length(); i++) {
                    		JSONObject jElm = jAList.getJSONObject(i);
                    		Integer id = Integer.parseInt(jElm.optString("id"));
                            String name = jElm.optString("name");
                            Integer cID = Integer.parseInt(jElm.optString("cID"));
                            
                            ListSbjData data = new ListSbjData(name, "", id, cID, iField);
                            sbjList.add(data);
                    	}
                    	sbjAdapter.notifyDataSetChanged();
                    	
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get List sbj Error: " + errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Get List sbj Error: " + error.getMessage());
                hideDialog();
                Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
               
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "fie");
                params.put("fie", String.valueOf(iField));
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
