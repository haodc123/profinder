package conghaodng.demo.profinder.global;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import conghaodng.demo.profinder.R;

public class Functions {
	
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	public static void initFont(Context context) {
		Variables.typeFace = Typeface.createFromAsset(context.getAssets(),"fonts/GOTHAM-MEDIUM.OTF");
	}
	public static void setFont(TextView tv) {
		tv.setTypeface(Variables.typeFace);
	}
	public static String genRandomString(int number) {
		char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < number; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		return sb.toString();
	}
	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}
	public static int pxToDp(int px) {
		return (int) (px / Resources.getSystem().getDisplayMetrics().density);
	}
	
	public static Intent newFacebookIntent(PackageManager pm, String url) {
		  Uri uri = Uri.parse(url);
		  try {
		  	 ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
		  	 if (applicationInfo.enabled) {
		  		 // http://stackoverflow.com/a/24547437/1048340
		  		 uri = Uri.parse("fb://facewebmodal/f?href=" + url);
		  	 }
		  } catch (PackageManager.NameNotFoundException ignored) {
		  }
		  return new Intent(Intent.ACTION_VIEW, uri);
	}
	
	@SuppressLint("SimpleDateFormat") 
	public static String getCurrentTimestamp() {
		SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
		return s.format(new Date());
	}
	public static void openFacebookIntent(String fbid, Context ct) {
		Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://facebook.com/"+fbid)); //catches and opens a url to the desired page
    	ct.startActivity(i);
		/*try {
	        ct.getPackageManager()
	                .getPackageInfo("com.facebook.katana", 0); //Checks if FB is even installed.
	        Intent i = new Intent(Intent.ACTION_VIEW,
	                Uri.parse("fb://profile/"+fbid)); //Trys to make intent with FB's URI
	        ct.startActivity(i);
	    } catch (Exception e) {
	    	Intent i = new Intent(Intent.ACTION_VIEW,
	                Uri.parse("https://facebook.com/"+fbid)); //catches and opens a url to the desired page
	    	ct.startActivity(i);
	    }*/
	}
	
	
	/**
	 * Hiển thị phần joindate của group, staff theo kiểu:
	 * Nếu cách thời điểm hiện tại quá 1 năm, hiển thị ngày tháng năm như aformat
	 * Nếu cách thời điểm hiện tại không quá 1 năm, hiển thị số tháng cách đây
	 * Nếu cách thời điểm hiện tại không quá 1 tháng, hiển thị số ngày cách đây
	 * Nếu cách thời điểm hiện tại không quá 1 ngày, hiển thị số "today"
	 * @param today ngày hiện tại
	 * @param dateStr ngày joindate
	 * @param aformat format của today và dateStr
	 */
	public static String getFriendlyJoinDate(String today, String dateStr, String aformat) {
		DateFormat formatter = new SimpleDateFormat(aformat);
		Long sDate = 0L;
		Long sToday = 0L;
		try {
			Date date = (Date) formatter.parse(dateStr);
			sDate = date.getTime();
			Date todayDate = (Date) formatter.parse(today);
			sToday = todayDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Long distance = (sToday - sDate)/1000; // number of seconds
		if (distance > (60*60*24*365)) { // more than year
			try {
				DateFormat format = new SimpleDateFormat(aformat);
				Date date = format.parse(dateStr);
				format = new SimpleDateFormat("yyyy-MM-dd");
				return format.format(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (distance < (60*60*24*365) && distance > (60*60*24*30)) {
			return "About "+String.valueOf(distance/(60*60*24*30))+" months ago";
		} else if (distance < (60*60*24*30) && distance > (60*60*24)) {
			return "About "+String.valueOf(distance/(60*60*24))+" days ago";
		} else {
			return "Today";
		}
		return null;
	}
	
	public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
	public static void overrideFonts(final Context context, final View v) {
	    try {
	        if (v instanceof ViewGroup) {
	            ViewGroup vg = (ViewGroup) v;
	            for (int i = 0; i < vg.getChildCount(); i++) {
	                View child = vg.getChildAt(i);
	                overrideFonts(context, child);
	            }
	        } else if (v instanceof TextView ) {
	            Functions.setFont((TextView)v);
	        } else if (v instanceof Button ) {
	            Functions.setFont((Button)v);
	        }
	    } catch (Exception e) {
	    }
	}
	public static void findDataType(Intent intent, Uri filename) {
		// TODO Auto-generated method stub
		if (filename.toString().contains(".doc") || filename.toString().contains(".docx")) {
            // Word document
		    intent.setDataAndType(filename, "application/msword");
        } else if(filename.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(filename, "application/pdf");
        } else if(filename.toString().contains(".ppt") || filename.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(filename, "application/vnd.ms-powerpoint");
        } else if(filename.toString().contains(".xls") || filename.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(filename, "application/vnd.ms-excel");
        } else if(filename.toString().contains(".zip") || filename.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(filename, "application/x-wav");
        } else if(filename.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(filename, "application/rtf");
        } else if(filename.toString().contains(".wav") || filename.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(filename, "audio/x-wav");
        } else if(filename.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(filename, "image/gif");
        } else if(filename.toString().contains(".jpg") || filename.toString().contains(".jpeg") || filename.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(filename, "image/jpeg");
        } else if(filename.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(filename, "text/plain");
        } else if(filename.toString().contains(".3gp") || filename.toString().contains(".mpg") || filename.toString().contains(".mpeg") || filename.toString().contains(".mpe") || filename.toString().contains(".mp4") || filename.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(filename, "video/*");
        } else {
            //if you want you can also define the intent type for any other file
            
            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(filename, "*/*");
        }
	}
	public static boolean isEmailValid(String email) {
	    boolean isValid = false;

	    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	    CharSequence inputStr = email;

	    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(inputStr);
	    if (matcher.matches()) {
	        isValid = true;
	    }
	    return isValid;
	}
	public static boolean isPhoneValid(String phone) {
		return PhoneNumberUtils.isGlobalPhoneNumber(phone);
	}
	public static void toastString(String msg, Context context) {
		Toast mToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		mToast.show();
	}
	public static void toastInt(int msg, Context context) {
		Toast mToast = Toast.makeText(context, String.valueOf(msg), Toast.LENGTH_LONG);
		mToast.show();
	}
	// File
	public static boolean isImage(File file) {
	    if (file == null || !file.exists()) {
	        return false;
	    }
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file.getPath(), options);
	    return options.outWidth != -1 && options.outHeight != -1;
	}
	public static long getFileLength(String mPath) {        
	    File f = new File(mPath);
	    return f.length()/1024; // in KB
	}
	public static String getFileName(String mPath) {
		File f = new File(mPath);
		return f.getName();
	}
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try { 
		    String[] proj = {MediaStore.Images.Media.DATA};
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    if (cursor == null)
		    	return ""; // Not media file
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		} finally {
		    if (cursor != null) {
		    	cursor.close();
		    }
		}
	}
	
	// Check Network (not Internet)
	public static synchronized boolean hasConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		NetworkInfo wifiNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}
		@SuppressWarnings("deprecation")
		NetworkInfo mobileNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}
		return false;
	}
	// check Internet
	public static boolean hasActiveInternetConnection(Context context) {
	    if (hasConnection(context)) {
	        try {
	            HttpURLConnection urlc = (HttpURLConnection) (new URL(Constants.URL_BASE).openConnection());
	            urlc.setRequestProperty("User-Agent", "Test");
	            urlc.setRequestProperty("Connection", "close");
	            urlc.setConnectTimeout(1500); 
	            urlc.connect();
	            return (urlc.getResponseCode() == 200);
	        } catch (IOException e) {
	            Log.e(Constants.APP_NAME, "Error checking internet connection", e);
	        }
	    } else {
	        Log.d(Constants.APP_NAME, "No Network access");
	    }
	    return false;
	}
	public static String getDeviceID(Context context) {
    	//final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String androidId;
        //tmDevice = "" + tm.getDeviceId();
        //tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        //UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        //return deviceUuid.toString();
        return androidId;
    }
	
	public static Bitmap getBitmapFromPath(String path, int inSampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(path, options);
	}
	public static String getBase64Image(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
	public static String getBase64Doc(String fileName)
			throws IOException {

		File file = new File(fileName);
		byte[] bytes = loadFile(file);
		byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(bytes);
		String encodedString = new String(encoded);

		return encodedString;
	}
	public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static Bitmap rotateBitmapIfNeeded(Context ct, String path, Bitmap mBM) {
		// TODO Auto-generated method stub
		Bitmap bm = null;
		ExifInterface ei = null;
		try {
			ei = new ExifInterface(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
		switch(orientation) {
		    case ExifInterface.ORIENTATION_ROTATE_90:
		    	bm = rotateImage(mBM, 90);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_180:
		    	bm = rotateImage(mBM, 180);
		        break;
		    default:
		    	bm = mBM;
		    	break;
		}
		return bm;
	}
	public static Bitmap rotateImage(Bitmap source, float angle) {
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
	
	/**
	 * get dimension of image after resize to adapt size of container (can contain)
	 * @param dim: array of original dimension
	 * @param amaxW: max Width of image after resize (Width of container)
	 * @param amaxH: max Height of image after resize (Height of container)
	 * @return array of result dimension
	 */
	public static int[] getAdjustSize(int[] dim, int amaxW, int amaxH) {
		float orgW = dim[0];
		float orgH = dim[1];
		float maxW = amaxW;
		float maxH = amaxH;
		int[] res = new int[2];
		if (orgW <= maxW && orgH <= maxH) {
			res[0] = (int)orgW;
			res[1] = (int)orgH;
		} else {
			if (orgW/orgH > maxW/maxH) {
				res[0] = (int)maxW;
				res[1] = (int)(orgH/orgW*maxW);
			} else {
				res[1] = (int)maxH;
				res[0] = (int)(orgW/orgH*maxH);
			}
		}
		return res;
	}
	public static Bitmap getCircleBitmap(Bitmap bitmap, int w, int h) {
		int cW = bitmap.getWidth();
		int cH = bitmap.getHeight();
		int[] arr = {w, h};
		int resW = getAdjustSize(arr, cW, cH)[0];
		int resH = getAdjustSize(arr, cW, cH)[1];
		final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);
	
		final int color = Color.RED;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, resW, resH);
		final RectF rectF = new RectF(rect);
	
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);
	
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
	
		bitmap.recycle();
	
		return output;
	}
	
	public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
	private static byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }
	    byte[] bytes = new byte[(int)length];
	    
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    is.close();
	    return bytes;
	}
	/**
	 * Creating file uri to store image/video
	 */
	public static Uri getOutputMediaFileUri(int mediaType) {
	    return Uri.fromFile(getOutputMediaFile(mediaType));
	}
	 
	/*
	 * returning image / video
	 */
	public static File getOutputMediaFile(int mediaType) {
	 
	    // External sdcard location
	    File mediaStorageDir = new File(
	            Environment
	                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
	            Constants.APP_NAME);
	 
	    // Create the storage directory if it does not exist
	    if (!mediaStorageDir.exists()) {
	        if (!mediaStorageDir.mkdirs()) {
	            Log.d(Constants.APP_NAME, "Oops! Failed create "
	                    + Constants.APP_NAME + " directory");
	            return null;
	        }
	    }
	 
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
	            Locale.getDefault()).format(new Date());
	    File mediaFile;
	    if (mediaType == Constants.MEDIA_TYPE_IMAGE) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                + "IMG_" + timeStamp + ".jpg");
	    } else if (mediaType == Constants.MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                + "VID_" + timeStamp + ".mp4");
	    } else {
	        return null;
	    }
	 
	    return mediaFile;
	}
	
	public static void setRatingDisplay(Context context, String p_rating, ImageView img_rating) {
		// TODO Auto-generated method stub
		/**
		 * Convention in uRating:
		 * 1-2:3,4:5,
		 * 1 - Rating average
		 * 2, 4 - id of users rate for
		 * 3, 5 - user id 2 rate 3 start and user id 4 rate 5 start
		 */
		if (p_rating.equals("")) {
			img_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_0)));
        } else {
        	int rating = 0;
        	if (p_rating.split("-")[0].split("\\.").length > 1) {
        		rating = Integer.parseInt(p_rating.split("-")[0].split("\\.")[0])+1;
        	} else {
        		rating = Integer.parseInt(p_rating.split("-")[0]);
        	}
	        switch (rating) {
	        case 0:
	        	img_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_0)));
	        	break;
	        case 1:
	        	img_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_1)));
	        	break;
	        case 2:
	        	img_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_2)));
	        	break;
	        case 3:
	        	img_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_3)));
	        	break;
	        case 4:
	        	img_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_4)));
	        	break;
	        case 5:
	        	img_rating.setBackground(context.getResources().getDrawable((R.drawable.ic_rat_5)));
	        	break;
	        }
        }
	}
	
	private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");
 
    public static String getCurrentTime() {
 
        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }
 
    public static String getCurrentDate() {
 
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }
    
    @SuppressLint("SimpleDateFormat")
	public static long getTimeMilliSec(String timeStamp, String aformat) {
        SimpleDateFormat format = new SimpleDateFormat(aformat);
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static String getTimeStamp(String today, String dateStr, String aformat) {
    	if (dateStr.equalsIgnoreCase("sending..."))
    		return dateStr;
    	
        SimpleDateFormat format = new SimpleDateFormat(aformat);
        String timestamp = "";
 
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
            String dateToday = dateFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
 
        return timestamp;
    }
    public static String getIMEI(Context ct) {
    	TelephonyManager mngr = (TelephonyManager)ct.getSystemService(Context.TELEPHONY_SERVICE); 
    	return mngr.getDeviceId();
    }
    public static void playNotificationSound(Context ct) {
    	Uri uriAlarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
    			+ "://" + ct.getPackageName() + "/raw/notification");
		try {
			Ringtone mRingtone = RingtoneManager.getRingtone(ct, uriAlarmSound);
			mRingtone.play();
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
    public static void playNotificationSound(Context ct, Uri uriAlarmSound) {
		try {
			Ringtone mRingtone = RingtoneManager.getRingtone(ct, uriAlarmSound);
			mRingtone.play();
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
    public static boolean checkPlayServices(Context ct) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ct);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((Activity)ct, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(Constants.APP_NAME, "This device is not supported. Google Play Services not installed!");
            }
            return false;
        }
        return true;
    }
}
