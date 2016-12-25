package conghaodng.demo.profinder.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.utils.MyZoomableImageView;

public class FrgDialogFile extends Fragment {

	private Button bt_dlf_close;
	private ImageView img_dlf;
	private TextView tv_dlf;
	private Bundle b;
	private int isImg;
	private String filename;
	private Bitmap bmImg = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		getActivity().getActionBar().hide();
		View v = inflater.inflate(R.layout.dialog_display_file, container, false);
		getBundle();
		setInitView(v);
		
		setData();
	    return v;
	}

	private void setData() {
		// TODO Auto-generated method stub
		if (isImg == 1) {
			String url = Constants.URL_FILE_FOLDER+filename;
			new loadRemoteIMG().execute(url);
		}
	}
	public class loadRemoteIMG extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
        }
        @Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
            try {  
            	URL mUrl = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();   
                conn.setDoInput(true);   
                conn.connect();     
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is); 
            }
            catch (IOException e)
            {       
                e.printStackTrace();
            }

            return null;   
        }
        @Override       
        protected void onPostExecute(String args) {
            img_dlf.setImageBitmap(bmImg);
        }

    }
	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null) {
			filename = b.getString("filename");
			isImg = b.getInt("isImg");
		}
	}

	private void setInitView(View v) {
		// TODO Auto-generated method stub
		bt_dlf_close = (Button)v.findViewById(R.id.bt_dlf_close);
		img_dlf = (ImageView)v.findViewById(R.id.img_dlf);
		tv_dlf = (TextView)v.findViewById(R.id.tv_dlf);
		
		bt_dlf_close.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().getFragmentManager().popBackStack();
			}
		});
	}
}
