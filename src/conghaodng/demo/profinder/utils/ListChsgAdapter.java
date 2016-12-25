package conghaodng.demo.profinder.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.fragments.FrgListMeet;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;

public class ListChsgAdapter extends BaseAdapter {
	private Context context;
    private List<ListChsgData> chsgItems;
 
    public ListChsgAdapter(Context context, List<ListChsgData> items) {
        this.context = context;
        this.chsgItems = items;
    }
 
    @Override
    public int getCount() {
        return chsgItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return chsgItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint({ "InflateParams", "ViewHolder" }) @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        /**
         * The following list not implemented reusable list items as list items
         * are showing incorrect data Add the solution if you have one
         * */
    	
        final ListChsgData m = chsgItems.get(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.row_chsg, null);
 
        TextView row_title = (TextView) convertView.findViewById(R.id.row_title);
        ImageView row_del = (ImageView) convertView.findViewById(R.id.row_del);
        
        if (m.getILearn() == 0) {
        	convertView.setBackgroundColor(context.getResources().getColor(R.color.bg_row_blue));
        } else if (m.getILearn() == 1) {
        	convertView.setBackgroundColor(context.getResources().getColor(R.color.bg_row_gray));
        } else if (m.getILearn() == 2) {
        	convertView.setBackgroundColor(context.getResources().getColor(R.color.bg_row_yellow));
        }
        row_title.setText(m.getSubject()+": "+m.getTag());
        
        row_del.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				lauchAlertDialog(m.getId());
				/*FrgDialogConfirm frgDialog = new FrgDialogConfirm();
				
				Bundle b = new Bundle();
				b.putInt("cId", m.getId());
				frgDialog.setArguments(b);
				
			    ((Activity) context).getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, frgDialog)
			     .addToBackStack(null)
			     .commit();*/
			}
		});
        convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FrgListMeet frgLMeet = new FrgListMeet();
				
				Bundle b = new Bundle();
				b.putInt("cId", m.getId());
				b.putInt("iLearn", m.getILearn());
				b.putString("sField", m.getField());
				b.putString("sSubject", m.getSubject());
				frgLMeet.setArguments(b);
				
			    ((Activity) context).getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, frgLMeet)
			     .addToBackStack(null)
			     .commit();
			}
		});
        
        return convertView;
    }
    private void lauchAlertDialog(final int cId) {
		// TODO Auto-generated method stub
		AlertDialog.Builder dl_send = new AlertDialog.Builder(context);
        dl_send.setTitle(context.getResources().getString(R.string.app_name));
        	dl_send.setMessage(context.getResources().getString(R.string.alert_del_chsg_title))
            .setPositiveButton(context.getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	// Delete
                	requestDelChsg(cId);
                }
            })
            .setNegativeButton(context.getResources().getString(R.string.bt_no), null);
           
        AlertDialog dialog = dl_send.create();
        dialog.setOnShowListener(new OnShowListener() {
        	@Override
            public void onShow(DialogInterface dialog) {
                
            }
		});
        dialog.show();
	}
    
    private void requestDelChsg(final int cId) {
		// TODO Auto-generated method stub
		String tag_del_chsg = "del_chsg";
        
        StringRequest strReq = new StringRequest(Method.POST,
                Constants.URL_DEL_CHOOSING, new Response.Listener<String>() {
        	
            @Override
            public void onResponse(String response) {
                try {
                	
                    JSONObject jORoot = new JSONObject(response);
                    boolean error = jORoot.getBoolean("error");
 
                    // Check for error node in json
                    if (!error) {
                    	Functions.toastString("Xóa thành công.", context);
                    	
                    	// Delete item with cId from chsgItems
                    	for (ListChsgData chsg : chsgItems) {
                            if (chsg.getId() == cId) {
                                int index = chsgItems.indexOf(chsg);
                                chsgItems.remove(index);
                                break;
                            }
                        }
                    	notifyDataSetChanged();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jORoot.getString("msg_err");
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Functions.toastString(context.getResources().getString(R.string.error_server_connect), context);
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
