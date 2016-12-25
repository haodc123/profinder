package conghaodng.demo.profinder.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.fragments.FrgPerson;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;

public class ListMeetAdapter extends BaseAdapter {
	private Context context;
    private List<ListMeetData> meetItems;
 
    public ListMeetAdapter(Context context, List<ListMeetData> items) {
        this.context = context;
        this.meetItems = items;
    }
 
    @Override
    public int getCount() {
        return meetItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return meetItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint({ "InflateParams", "ViewHolder" }) @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        final ListMeetData m = meetItems.get(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.row_meet, null);
 
        ImageView row_avatar = (ImageView) convertView.findViewById(R.id.row_avatar);
        TextView row_name = (TextView) convertView.findViewById(R.id.row_name);
        ImageView row_rating = (ImageView) convertView.findViewById(R.id.row_rating);
        TextView row_content = (TextView) convertView.findViewById(R.id.row_content);
        
        //Functions.setRatingDisplay(context, m.getPerson_rating(), row_rating);
        switch (Integer.parseInt(m.getPerson_rating())) {
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
        
        row_name.setText(m.getPerson_name());
        
        row_content.setText(!m.getContent().equals("") ? m.getContent() : m.getTag());
        
        if (!m.getPerson_avatar().equals("")) {
	        String pathImg = Constants.URL_FILE_FOLDER + m.getPerson_avatar();
			new loadRemoteIMG(row_avatar).execute(pathImg);
        }
        
        convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FrgPerson frgPS = new FrgPerson();
				
				Bundle b = new Bundle();
				b.putInt("id_chsg", m.getId_chsg());
				b.putInt("from_chsg", m.getFrom_chsg());
				frgPS.setArguments(b);
				
			    ((Activity) context).getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, frgPS, Constants.TAG_FRG_PERSON)
			     .addToBackStack(null)
			     .commit();
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
}
