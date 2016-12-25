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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.fragments.FrgDialogConfirm;
import conghaodng.demo.profinder.fragments.FrgListMeet;
import conghaodng.demo.profinder.fragments.FrgPerson;
import conghaodng.demo.profinder.fragments.FrgSbj;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.view.MyGridView;

public class ListFieldAdapter extends BaseAdapter {
	private Context context;
    private List<ListFieldData> fItems;
 
    public ListFieldAdapter(Context context, List<ListFieldData> items) {
        this.context = context;
        this.fItems = items;
    }
 
    @Override
    public int getCount() {
        return fItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return fItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint({ "InflateParams", "ViewHolder" }) @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        final ListFieldData m = fItems.get(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.unit_field, null);
        convertView.setLayoutParams(new MyGridView.LayoutParams(MyGridView.AUTO_FIT, Functions.dpToPx(100)));
 
        TextView tv_unit_field = (TextView) convertView.findViewById(R.id.tv_unit_field);
        ImageView img_unit_field = (ImageView) convertView.findViewById(R.id.img_unit_field);
        //img_unit_field.setVisibility(View.GONE);
        
        if (!m.getField_img().equals("")) {
	        String pathImg = Constants.URL_FILE_FOLDER + m.getField_img();
			new loadRemoteIMG(img_unit_field).execute(pathImg);
        }
        
        tv_unit_field.setText(m.getField_name());
        
        convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FrgSbj frgSbj = new FrgSbj();
				
				Bundle b = new Bundle();
				b.putInt("iField", m.getField_id());
				frgSbj.setArguments(b);
				
			    ((Activity) context).getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, frgSbj)
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
