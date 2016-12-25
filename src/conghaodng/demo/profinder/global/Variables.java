package conghaodng.demo.profinder.global;

import android.graphics.Typeface;
import conghaodng.demo.profinder.utils.MyDiskCache;

public class Variables {
	public static Typeface typeFace = null;
	
	public static String userID = "";
	public static String userFBID = "";
	public static String userToken = "";
	public static int userType = 0; // 0 = FB user, 1 = reg user
	public static String userName = "";
	public static String userAvatar = "";
	public static String userEmail = "";
	public static String userTel = "";
	public static String userAddress = "";
	public static int userSex = 0;
	public static String userGCMRegId = ""; 
	
	// Device
	public static int curApiVersion = 0;
	
	public static int isUpdatedInfo = 0;
	public static int isAlreadyAlertConnection = 0;
	
	public static MyDiskCache mDCache = null;
}
