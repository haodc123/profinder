package conghaodng.demo.profinder.chat.utils;

import java.util.Arrays;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;
import conghaodng.demo.profinder.AppController;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.Config;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;
/**
 * 
 * @author HaoDC
 * Generate notification (on notification tray)
 */
public class ChatNotificationUtils {
	private final String TAG = ChatNotificationUtils.class.getSimpleName();
	private Context ct;
	public ChatNotificationUtils() {
		
	}
	public ChatNotificationUtils(Context ct) {
		this.ct = ct;
	}
	public void showNotificationMessage(String title, String msg, String timestamp, Intent intent) {
		showNotificationMessage(title, msg, timestamp, intent, null);
	}
	public void showNotificationMessage(final String title, final String msg, final String timestamp, final Intent intent, final String imgUrl) {
		// Check for empty push message
		if (TextUtils.isEmpty(msg))
			return;
		final int mIcon = R.drawable.ic_launcher;
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent mPendingIntent = PendingIntent.getActivity(ct, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ct);
		final Uri uriAlarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + ct.getPackageName() + "/raw/notification");
		// Check whether is contain IMG or not
		if (!TextUtils.isEmpty(imgUrl)) { // imgUrl not empty
			if (imgUrl != null & imgUrl.length() > 4 & Patterns.WEB_URL.matcher(imgUrl).matches()) { // imgUrl is right syntax
				Bitmap mBitmap = Functions.getBitmapFromURL(imgUrl);
				if (mBitmap != null)
					showBigNotification(mBitmap, mBuilder, mIcon, title, msg, timestamp, mPendingIntent, uriAlarmSound);
				else
					showSmallNotification(mBuilder, mIcon, title, msg, timestamp, mPendingIntent, uriAlarmSound);
			} 
		} else { // imgUrl is invalid
			showSmallNotification(mBuilder, mIcon, title, msg, timestamp, mPendingIntent, uriAlarmSound);
			Functions.playNotificationSound(ct, uriAlarmSound);
		}
	}
	private void showSmallNotification(Builder mBuilder, int mIcon, String title, String msg, String timestamp,
			PendingIntent mPendingIntent, Uri uriAlarmSound) {
		NotificationCompat.InboxStyle mInboxStyle = new NotificationCompat.InboxStyle();
		if (Config.appendNotificationMessages) {
			AppController.getInstance().getPrefManager().addNotification(msg);
			String sNoti = AppController.getInstance().getPrefManager().getNotifications();
			List<String> listNoti = Arrays.asList(sNoti.split(Constants.DELIMITER));
			for (int i = listNoti.size()-1; i >= 0; i--) {
				mInboxStyle.addLine(listNoti.get(i));
			}
		} else {
			mInboxStyle.addLine(msg);
		}
		
		NotificationManager mNotiManager = (NotificationManager)ct.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification mNotification;
		mNotification = mBuilder.setSmallIcon(mIcon).setTicker(title).setWhen(0)
				.setAutoCancel(true)
				.setContentTitle(title)
				.setContentIntent(mPendingIntent)
				.setSound(uriAlarmSound)
				.setStyle(mInboxStyle)
				.setWhen(timestamp == "" ? 0L : Functions.getTimeMilliSec(timestamp, "yyyy-MM-dd HH:mm:ss"))
				.setSmallIcon(mIcon)
				.setLargeIcon(BitmapFactory.decodeResource(ct.getResources(), mIcon))
				.setContentText(msg)
				.build();
		mNotification.ledARGB = 0xFF00ff00;
		mNotification.flags = Notification.FLAG_SHOW_LIGHTS;
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotification.ledOnMS = 1000; 
		mNotification.ledOffMS = 1000; 
		
		mNotiManager.notify(Config.NOTIFICATION_ID, mNotification);
	}
	private void showBigNotification(Bitmap mBitmap, Builder mBuilder, int mIcon, String title, String msg,
			String timestamp, PendingIntent mPendingIntent, Uri uriAlarmSound) {
		NotificationCompat.BigPictureStyle mPicStyle = new NotificationCompat.BigPictureStyle();
		mPicStyle.setBigContentTitle(title);
		mPicStyle.setSummaryText(Html.fromHtml(msg).toString());
		mPicStyle.bigPicture(mBitmap);
		
		NotificationManager mNotiManager = (NotificationManager)ct.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification mNotification;
		mNotification = mBuilder.setSmallIcon(mIcon).setTicker(title).setWhen(0)
				.setAutoCancel(true)
				.setContentTitle(title)
				.setContentIntent(mPendingIntent)
				.setSound(uriAlarmSound)
				.setStyle(mPicStyle)
				.setWhen(timestamp == "" ? 0L : Functions.getTimeMilliSec(timestamp, "yyyy-MM-dd HH:mm:ss"))
				.setSmallIcon(mIcon)
				.setLargeIcon(BitmapFactory.decodeResource(ct.getResources(), mIcon))
				.setContentText(msg)
				.build();
		mNotification.ledARGB = 0xFF00ff00;
		mNotification.flags = Notification.FLAG_SHOW_LIGHTS;
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotification.ledOnMS = 1000; 
		mNotification.ledOffMS = 1000; 
		
		mNotiManager.notify(Config.NOTIFICATION_ID, mNotification);
	}
	
	public static void clearNotifications() {
		NotificationManager mNotiManager = (NotificationManager)AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
		mNotiManager.cancelAll();
	}
}
