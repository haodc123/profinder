package conghaodng.demo.profinder.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.MyChat;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.ListMeetAdapter.loadRemoteIMG;

public class ListBMAdapter extends BaseAdapter {
	private Context context;
    private List<ListBMData> bmItems;
 
    public ListBMAdapter(Context context, List<ListBMData> items) {
        this.context = context;
        this.bmItems = items;
    }
 
    @Override
    public int getCount() {
        return bmItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return bmItems.get(position);
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
 
        final ListBMData m = bmItems.get(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.row_meet, null);
        
        ImageView row_avatar = (ImageView) convertView.findViewById(R.id.row_avatar);
        TextView row_name = (TextView) convertView.findViewById(R.id.row_name);
        ImageView row_rating = (ImageView) convertView.findViewById(R.id.row_rating);
        TextView row_content = (TextView) convertView.findViewById(R.id.row_content);
        
        //Functions.setRatingDisplay(context, m.getP_rate(), row_rating);
        switch (Integer.parseInt(m.getP_rate())) {
        case 0:
        	row_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_0)));
        	break;
        case 1:
        	row_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_1)));
        	break;
        case 2:
        	row_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_2)));
        	break;
        case 3:
        	row_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_3)));
        	break;
        case 4:
        	row_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_4)));
        	break;
        case 5:
        	row_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_5)));
        	break;
        }
        
        row_name.setText(m.getP_name());
        
        // Address > Email > Tel --> Set priority as that order, if empty, move to next
        row_content.setText(!m.getP_address().equals("") ? m.getP_address() : 
        	!m.getP_email().equals("") ? m.getP_email() : m.getP_tel());
        
        if (!m.getP_avatar().equals("")) {
	        String pathImg = Constants.URL_FILE_FOLDER + m.getP_avatar();
			new loadRemoteIMG(row_avatar).execute(pathImg);
        }
        
        convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				goToMyChat(m.getP_id(), m.getP_name());
			}
		});
        
        return convertView;
    }
    public class loadRemoteIMG extends AsyncTask<String, Void, Bitmap> {
    	private final WeakReference<ImageView> imageViewReference;
    	public loadRemoteIMG(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }
        @SuppressWarnings("null")
		@Override
        protected Bitmap doInBackground(String... args) {
            // updating UI from Background Thread
        	final String imageKey = Functions.getFileName(String.valueOf(args[0]));
        	Bitmap bm = null;
        	bm = Variables.mDCache.getBitmap(imageKey);
        	if (bm == null) {
        		if (!Functions.hasConnection(context)) {
        			return null;
        		}
	            try {  
	            	URL mUrl = new URL(args[0]);
	                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();   
	                conn.setDoInput(true);
	                conn.connect();
	                InputStream is = conn.getInputStream();
	                if (is != null) {
	                	bm = BitmapFactory.decodeStream(is);
	                	if (bm != null)
	                		Variables.mDCache.put(imageKey, bm);
	                } 
	            }
	            catch (IOException e) {  
	                e.printStackTrace();
	            }
        	}
        	
            return bm;
        }
        @Override       
        protected void onPostExecute(Bitmap bm) {
        	if (isCancelled()) {
                bm = null;
            }
        	if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bm != null) {
                        imageView.setImageBitmap(bm);
                    } else {
                        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.no_avatar);
                        imageView.setImageDrawable(placeholder);
                    }
                }
            }
        }
    }
    
    protected void goToMyChat(final String p_id, final String p_name) {
		// TODO Auto-generated method stub
		String crName = "";
		try {
			crName = URLEncoder.encode(Variables.userName+" - "+p_name, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String endPoint = Constants.CHAT_ROOM_NEW.replace("_ID1_", Variables.userID).replace("_ID2_", p_id).replace("_NAME_", crName);

		StringRequest strReq = new StringRequest(Request.Method.GET,
				endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
 
                    if (obj.getBoolean("error") == false) {
                        
                        Intent i = new Intent(context, MyChat.class);
        				i.putExtra("chat_room_id", obj.getString("chat_room_id"));
        				i.putExtra("chat_room_name", p_name);
        				context.startActivity(i);
                    } else {
                    }
                } catch (JSONException e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(Constants.APP_NAME, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        });
        AppController.getInstance().addToRequestQueue(strReq);
	}
    
    public static class ListBMData {
    	private String p_FBID, p_id, p_email, p_name, p_avatar, p_tel, p_address, p_rate;
        private int p_sex;
     
        public ListBMData() {
        }
     
        public ListBMData(String p_FBID, String p_id, String p_email, String p_name, 
        		String p_avatar, String p_tel, String p_address, String p_rate, int p_sex) {
            this.p_FBID = p_FBID;
            this.p_email = p_email;
            this.p_name = p_name;
            this.p_avatar = p_avatar;
            this.p_id = p_id;
            this.p_tel = p_tel;
            this.p_address = p_address;
            this.p_rate = p_rate;
            this.p_sex = p_sex;
        }

		public String getP_FBID() {
			return p_FBID;
		}

		public void setP_FBID(String p_FBID) {
			this.p_FBID = p_FBID;
		}

		public String getP_id() {
			return p_id;
		}

		public void setP_id(String p_id) {
			this.p_id = p_id;
		}

		public String getP_email() {
			return p_email;
		}

		public void setP_email(String p_email) {
			this.p_email = p_email;
		}

		public String getP_name() {
			return p_name;
		}

		public void setP_name(String p_name) {
			this.p_name = p_name;
		}

		public String getP_avatar() {
			return p_avatar;
		}

		public void setP_avatar(String p_avatar) {
			this.p_avatar = p_avatar;
		}

		public String getP_tel() {
			return p_tel;
		}

		public void setP_tel(String p_tel) {
			this.p_tel = p_tel;
		}

		public String getP_address() {
			return p_address;
		}

		public void setP_address(String p_address) {
			this.p_address = p_address;
		}

		public String getP_rate() {
			return p_rate;
		}

		public void setP_rate(String p_rate) {
			this.p_rate = p_rate;
		}

		public int getP_sex() {
			return p_sex;
		}

		public void setP_sex(int p_sex) {
			this.p_sex = p_sex;
		}

    }
}
