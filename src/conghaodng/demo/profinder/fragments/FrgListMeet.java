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
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.ListMeetAdapter;
import conghaodng.demo.profinder.utils.ListMeetData;

public class FrgListMeet extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	private ListView lv_list_main;
	private LinearLayout ll_tab_learn, ll_tab_teach;
	private TextView tvTitle, tv_lm_alert;
	private Button bt_lm_add;
	private int isGUITab;
	
	private static final String TAG = FrgListMeet.class.getSimpleName();
	private ProgressDialog pDialog;
		
	private SwipeRefreshLayout swipeRefreshLayout;
	private List<ListMeetData> meetList;
	private ListMeetAdapter meetAdapter;
	
	private Bundle b;
	private int iChsg = 0, iLearn = 0;
	private String sField = "", sSubject = "";
	/**
	 * in case user want both to learn and teach, using tab layout
	 * curLearn: is corresponds with tab
	 * -1: default
	 * 1: tab Learn
	 * 0: tab Teach
	 * furthermore, be static to remain value over many fragments
	 */
	public static int curLearn = -1;
	private View v;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		
		getBundle();
		
		if (iChsg != 0 && (iLearn == 0 || iLearn == 1)) {
			curLearn = iLearn;
			isGUITab = 0;
			v = inflater.inflate(R.layout.frg_list_meet, container, false);
		} else {
			if (curLearn == -1)
				curLearn = 1; // default
			isGUITab = 1;
			v = inflater.inflate(R.layout.frg_list_meet_tab, container, false);
		}
		
		setInitView();
		
		setData();
		
		setGUITab();
		feedListMeet();
	    return v;
	}
	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null) {
			iChsg = b.getInt("cId");
			if (iChsg != 0) {
				iLearn = b.getInt("iLearn");
				sField = b.getString("sField");
				sSubject = b.getString("sSubject");
			}
		}
	}
	public void setData() {
		// TODO Auto-generated method stub
		if (meetList == null)
			meetList = new ArrayList<ListMeetData>();
		if (meetAdapter == null)
			meetAdapter = new ListMeetAdapter(getActivity(), meetList);
		
		lv_list_main.setAdapter(meetAdapter);
		
	}

	private void setInitView() {
		// TODO Auto-generated method stub
		setCustomActionBar();
		lv_list_main = (ListView)v.findViewById(R.id.lv_list_main);
		bt_lm_add = (Button)v.findViewById(R.id.bt_lm_add);
		tv_lm_alert = (TextView)v.findViewById(R.id.tv_lm_alert);
		swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                feedListMeet();
            }
        });
		bt_lm_add.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onAdd();
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
	private void setGUITab() {
		// TODO Auto-generated method stub
		if (isGUITab == 1) {
			ll_tab_learn = (LinearLayout)v.findViewById(R.id.ll_tab_learn);
			ll_tab_teach = (LinearLayout)v.findViewById(R.id.ll_tab_teach);
			
			ll_tab_learn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (curLearn == 0) {
						curLearn = 1;
						feedListMeet();
						ll_tab_learn.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_row_gray));
						ll_tab_teach.setBackgroundColor(getActivity().getResources().getColor(R.color.label_global_blur));
					}
				}
			});
			ll_tab_teach.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (curLearn == 1) {
						curLearn = 0;
						feedListMeet();
						ll_tab_teach.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_row_gray));
						ll_tab_learn.setBackgroundColor(getActivity().getResources().getColor(R.color.label_global_blur));
					}
				}
			});
			
			if (curLearn == 0) {
				ll_tab_teach.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_row_gray));
				ll_tab_learn.setBackgroundColor(getActivity().getResources().getColor(R.color.label_global_blur));
			} else {
				ll_tab_learn.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_row_gray));
				ll_tab_teach.setBackgroundColor(getActivity().getResources().getColor(R.color.label_global_blur));
			}
		}
	}
	private void feedListMeet() {
		// TODO Auto-generated method stub
		String tag_string_feed = "feed_list_meet";
        pDialog.setMessage("Đang tải dữ liệu...");
                
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_LIST_MEET, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get List meet Response: " + response.toString());
                
                try {
                	// Reference 1: jSon sample in below
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {

                    	JSONArray jAList = jORoot.getJSONArray("meets");

                    	meetList.clear();
                    	for (int i = 0; i < jAList.length(); i++) {
                    		JSONObject jElm = jAList.getJSONObject(i);

                    		ListMeetData meet = new ListMeetData(jElm.optString("person_Avatar"), jElm.optString("person_FBID"), jElm.optString("person_email"), jElm.optString("person_name"),
                    				jElm.optString("content"), jElm.optString("tag"), jElm.optString("doc"), jElm.optString("img"), 
                    				jElm.optString("location"), jElm.optString("date"), jElm.optString("tel"), jElm.optString("time"),
                    				jElm.optInt("person_id"), jElm.optString("person_rating"), jElm.optInt("iField"), jElm.optInt("iSubject"), 
                    				jElm.optInt("iSex"), jElm.optInt("id_chsg"), jElm.optInt("from_chsg"));

                    		meetList.add(meet);
                    	}
                    	meetAdapter.notifyDataSetChanged();
                    	if (meetList.size() > 0) {
                    		tv_lm_alert.setVisibility(View.GONE);
                    	} else {
                    		if (curLearn == 1)
                    			tv_lm_alert.setText(getActivity().getResources().getString(R.string.alert_no_meet_learn));
                    		else
                    			tv_lm_alert.setText(getActivity().getResources().getString(R.string.alert_no_meet_teach));
                    		tv_lm_alert.setVisibility(View.VISIBLE);
                    	}
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                        Log.e(TAG, "Get List meet Error: " + errorMsg);
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
                Log.e(TAG, "Get List meet Error: " + error.getMessage());

                if (Variables.isAlreadyAlertConnection == 0) {
                	Functions.toastString(getActivity().getResources().getString(R.string.error_server_connect), getActivity());
                	Variables.isAlreadyAlertConnection = 1;
                }
                
                swipeRefreshLayout.setRefreshing(false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("sToken", Variables.userToken);
                params.put("userID", Variables.userID);
                params.put("iChsg", String.valueOf(iChsg));
                params.put("iLearn", String.valueOf(curLearn));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_feed);
	}
	
	@Override
    public void onRefresh() {
		Variables.isAlreadyAlertConnection = 0;
		feedListMeet();
    }
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        if (iChsg == 0)
        	tvTitle.setText(getResources().getString(R.string.title_list_meet_all));
        else
        	tvTitle.setText(getResources().getString(R.string.title_list_meet) + " " + sSubject);
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		Variables.isAlreadyAlertConnection = 0;
		super.onStop();
	}
}
