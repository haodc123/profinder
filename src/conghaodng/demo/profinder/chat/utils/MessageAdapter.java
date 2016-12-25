package conghaodng.demo.profinder.chat.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.global.Variables;
import conghaodng.demo.profinder.utils.MyZoomableImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MViewHolder> {
	
	private static String TAG = MessageAdapter.class.getSimpleName();
	 
    private List<MessageData> messageList;
    private static String today;
    private Context context;
    private String userID; // my user id
    private int SELF = -1;
    private Bitmap mBitmap;
    private ProgressDialog pDialog;
    
    private Dialog mDialogFile;
    private MyZoomableImageView img_dlf;
    private TextView tv_dlf;
    private Button bt_dlf_close;
 // For download file DOC
 	private Uri pathDocOnDevice;
 	private int downloadedSize = 0, totalsize;
 	float per = 0;
    
    private int position;
    
    public MessageAdapter() {
		// TODO Auto-generated constructor stub
	}
    public MessageAdapter(Context ct, List<MessageData> data, String uID) {
    	this.context = ct;
    	this.messageList = data;
        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        this.userID = uID;
    }
    
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
 
    public class MViewHolder extends RecyclerView.ViewHolder {
        TextView row_tv, row_timestamp;
        ImageView row_img;
 
        public MViewHolder(View view) {
            super(view);
            row_tv = (TextView) itemView.findViewById(R.id.row_tv);
            row_timestamp = (TextView) itemView.findViewById(R.id.row_timestamp);
            row_img = (ImageView)itemView.findViewById(R.id.row_img);
            row_img.setMaxWidth(900); // only display,so, for best performance, need to resize bitmap below
        	row_img.setMaxHeight(700);
        	pDialog = new ProgressDialog(context);
            pDialog.setCancelable(true);
        }
    }
    
    @Override
    public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        // view type is to identify where to render the chat message
        // left or right
        if (viewType == SELF) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_chat_item_self, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_chat_item_other, parent, false);
        }
        return new MViewHolder(itemView);
    }
    @Override
    public int getItemViewType(int position) {
        MessageData message = messageList.get(position);
        if (message.getSender().getId().equals(userID)) {
            return SELF;
        }
        return position;
    }
    @Override
    public void onBindViewHolder(final MViewHolder holder, int position) {
        final MessageData data = messageList.get(position);
        
        holder.row_timestamp.setText(Functions.getTimeStamp(today, data.getCreatedAt(), "yyyy-MM-dd HH:mm:ss"));
        
        final String content = data.getMessage();
        if (data.getFileType() == Constants.CHAT_CTT_TEXT) { // Text
        	int iconID = filterIcon(content);
        	if (iconID == 0) {
        		holder.row_tv.setText(content);
	        	holder.row_img.setVisibility(View.GONE);
	        	holder.row_tv.setVisibility(View.VISIBLE);
	        	//setLayoutIMG(data, 0, holder.row_img);
        	} else { // load icon from local img
        		holder.row_tv.setVisibility(View.GONE);
        		holder.row_img.setVisibility(View.VISIBLE);
        		//setLayoutIMG(data, 3, holder.row_img);
        		holder.row_img.setImageDrawable(context.getResources().getDrawable(iconID));
        	}
        } else if (data.getFileType() == Constants.CHAT_CTT_IMG) { // Image
        	// load Image
        	if (Integer.parseInt(data.getId()) > 0) { // with real id, mean that is message have confirm from server, so load image from server
	        	if (content != null) { // imgUrl is right syntax
	        		String pathImg = Constants.URL_FILE_FOLDER_CHAT + content;
	        		new loadRemoteIMG(holder.row_img).execute(pathImg);
	        	} else {
	        		holder.row_img.setImageDrawable(context.getResources().getDrawable(R.drawable.no_img));
	        	}
        	} else { // with temp id, so load image from local
        		if (Functions.getFileLength(content) > Constants.MAX_IMGSIZE_UPLOAD)
        			mBitmap = Functions.getBitmapFromPath(content, Constants.RESIZE_SampleSize);
        		else
        			mBitmap = Functions.getBitmapFromPath(content, 1);
        		mBitmap = Functions.rotateBitmapIfNeeded(context, content, mBitmap);
        		holder.row_img.setImageBitmap(mBitmap);
        	}
        	holder.row_tv.setVisibility(View.GONE);
        	holder.row_img.setVisibility(View.VISIBLE);
        	//setLayoutIMG(data, 1, holder.row_img);
        } else if (data.getFileType() == Constants.CHAT_CTT_DOC) { // Doc
        	holder.row_img.setImageDrawable(context.getResources().getDrawable(R.drawable.attach_file));
        	holder.row_tv.setText(Functions.getFileName(content));
        	holder.row_tv.setBackgroundColor(Color.TRANSPARENT);
        	holder.row_tv.setTextColor(Color.BLACK);
        	holder.row_tv.setGravity(Gravity.RIGHT);
        	holder.row_tv.setVisibility(View.VISIBLE);
        	holder.row_img.setVisibility(View.VISIBLE);
        	//setLayoutIMG(data, 2, holder.row_img);
        	
        }
        
        holder.row_img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (data.getFileType() == Constants.CHAT_CTT_IMG || data.getFileType() == Constants.CHAT_CTT_IMG)
				onDisplay(content, data.getFileType());
			}
		});
        holder.row_tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (data.getFileType() == Constants.CHAT_CTT_DOC)
					onDisplay(content, data.getFileType());
			}
		});
    }
    protected int filterIcon(String s) {
    	switch (s) {
		case Constants.ICON0:
			return R.drawable.ics_afraid;
		case Constants.ICON1:
			return R.drawable.ics_angry;
		case Constants.ICON2:
			return R.drawable.ics_cool;
		case Constants.ICON3:
			return R.drawable.ics_cry;
		case Constants.ICON4:
			return R.drawable.ics_funny;
		case Constants.ICON5:
			return R.drawable.ics_kiss1;
		case Constants.ICON6:
			return R.drawable.ics_kiss2;
		case Constants.ICON7:
			return R.drawable.ics_love;
		case Constants.ICON8:
			return R.drawable.ics_sad1;
		case Constants.ICON9:
			return R.drawable.ics_sad2;
		case Constants.ICON10:
			return R.drawable.ics_smile;
		case Constants.ICON11:
			return R.drawable.ics_wow;
		default:
			return 0;
		}
    }
    protected void onDisplay(String filename, int isImg) {
		// TODO Auto-generated method stub  
		mDialogFile = new Dialog(context);
		mDialogFile.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		mDialogFile.setContentView(R.layout.dialog_display_file);
		mDialogFile.setTitle("");

		tv_dlf = (TextView) mDialogFile.findViewById(R.id.tv_dlf);
		img_dlf = (MyZoomableImageView) mDialogFile.findViewById(R.id.img_dlf);
		
		bt_dlf_close = (Button) mDialogFile.findViewById(R.id.bt_dlf_close);
		bt_dlf_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialogFile.dismiss();
			}
		});
		
		String url = Constants.URL_FILE_FOLDER_CHAT+filename;
		if (isImg == 1) {
			new loadRemoteIMGDialog().execute(url);
		} else {
			new loadRemoteDOCDialog().execute(url, filename);
		}

	}
    
    public class loadRemoteIMGDialog extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Đang tải file...");
            showDialog();
        }
        @SuppressWarnings("null")
		@Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
        	final String imageKey = Functions.getFileName(String.valueOf(args[0]));
        	mBitmap = Variables.mDCache.getBitmap(imageKey);
        	if (mBitmap == null) {
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
	                	mBitmap = BitmapFactory.decodeStream(is); 
	                	if (mBitmap != null)
	                		Variables.mDCache.put(imageKey, mBitmap);
	                } 
	            }
	            catch (IOException e) {  
	                e.printStackTrace();
	            }
        	}
        	
            return null;
        }
        @Override       
        protected void onPostExecute(String args) {
        	hideDialog();
        	if (mBitmap != null) {
	            img_dlf.setImageBitmap(mBitmap);
	            mDialogFile.show();
	            Window window = mDialogFile.getWindow();
	            window.setLayout(LayoutParams.MATCH_PARENT, 1400);
        	} else {
        		Functions.toastString("Không thể tải file", context);
        	}
        }

    }
	public class loadRemoteDOCDialog extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Đang tải file...");
            showDialog();
        }
        @Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
        	File f = downloadDoc(args[0], args[1]);
        	if (f != null)
        		pathDocOnDevice = Uri.fromFile(f);
            
            return null;
        }
        @Override       
        protected void onPostExecute(String args) {
        	hideDialog();
        	if (pathDocOnDevice != null || !pathDocOnDevice.toString().equals("")) {
	        	try {
	                Intent intent = new Intent(Intent.ACTION_VIEW);
	                Functions.findDataType(intent, pathDocOnDevice);
	                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                context.startActivity(intent);
	
	            } catch (ActivityNotFoundException e) {
	            	e.printStackTrace();
	            }
        	} else {
        		Functions.toastString("File đã bị xóa hoặc không tồn tại", context);
        	}
        }
    }
	
	File downloadDoc(String urlOnServer, String filenamOnServer) {
        File file = null;
        try {
            URL url = new URL(urlOnServer);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
 
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            // connect
            urlConnection.connect();
 
            // set the path where we want to save the file
            File SDCardRoot = Environment.getExternalStorageDirectory();
            // create a new file, to save the downloaded file
            file = new File(SDCardRoot, filenamOnServer);
 
            FileOutputStream fileOutput = new FileOutputStream(file);
            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];  
            int bufferLength = 0;
 
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                per = ((float) downloadedSize / totalsize) * 100;
            }
            // close the output stream when complete //
            fileOutput.close();
            
 
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
        	e.printStackTrace();
        } catch (final Exception e) {
        	e.printStackTrace();
        }
        return file;
    }
	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}
	 
	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
	
	/**
	 * @type: 0 text, 1 img, 2 doc
	 */
	private void setLayoutIMG(MessageData data, int type, ImageView row_img) {
		// TODO Auto-generated method stub
		FrameLayout.LayoutParams imageViewParams = null;
		if (type == Constants.CHAT_CTT_IMG || type == Constants.CHAT_CTT_DOC) {
			imageViewParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
		} else if (type == Constants.CHAT_CTT_TEXT) {
			imageViewParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
	                0);
		}
    	if (data.getSender().getId().equals(Variables.userID))
    		imageViewParams.gravity = Gravity.RIGHT;
    	else
    		imageViewParams.gravity = Gravity.LEFT;
    	row_img.setLayoutParams(imageViewParams);
	}
	@Override
    public int getItemCount() {
        return messageList.size();
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
	                	if (bm != null && (bm.getWidth() > 900 || bm.getHeight() > 700)) { // if image too big
	                		/*int[] res = new int[2];
	                		int[] org = {bm.getWidth(), bm.getHeight()};
	                		res = Functions.getAdjustSize(org,  900, 700);
	                		bm = Bitmap.createScaledBitmap(bm, res[0], res[1], false);*/
	                		//Variables.mDCache.put(imageKey, bm);
	                	}
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
                        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.no_img);
                        imageView.setImageDrawable(placeholder);
                    }
                }
            }
        }
    }
}