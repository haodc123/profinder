package conghaodng.demo.profinder.global;

public class Constants {
	
	public static final String APP_NAME = "ProFinder";
	public static final String APP_VERSION = "1.3";
	public static final int APP_VERSION_CODE = 3;
	public static final String TAG_FRG_LISTCHSG = "tag_frg_listchsg";
	public static final String TAG_FRG_PERSON = "tag_frg_person";
	public static final String TAG_FRG_SETTING = "tag_frg_setting";
	
	// For APIs
	public static final String URL_BASE = "http://profindervn.com/ProFinder_API/";
	//public static final String URL_BASE = "http://192.168.0.119/ProFinder_API/";
	public static final String URL_LOGIN = URL_BASE + "otherway/owLogin.php";
	public static final String URL_REG = URL_BASE + "otherway/owRegister.php";
	public static final String URL_UPDATE_FBUSER = URL_BASE + "otherway/owUpdateFBUser.php";
	public static final String URL_FORGOTPASS = URL_BASE + "otherway/owForgot_pass.php";
	public static final String URL_UPLOAD = URL_BASE + "otherway/owUploadToServer.php";
	public static final String URL_LIST_SELECTION = URL_BASE + "otherway/owGetListSelection.php";
	public static final String URL_LIST_CHOOSING = URL_BASE + "otherway/owGetListChoosing.php";
	public static final String URL_LIST_CONNECT = URL_BASE + "otherway/owGetListConnect.php";
	public static final String URL_LIST_MEET = URL_BASE + "otherway/owGetListMeet.php";
	public static final String URL_SET_CHOOSING = URL_BASE + "otherway/owSetChoosing.php";
	public static final String URL_GET_CHOOSING = URL_BASE + "otherway/owGetChoosing.php";
	public static final String URL_DEL_CHOOSING = URL_BASE + "otherway/owDelChoosing.php";
	public static final String URL_INFO_PERSON = URL_BASE + "otherway/owInfoPerson.php";
	public static final String URL_UPDATE_RATING = URL_BASE + "otherway/owUpdateRating.php";
	public static final String URL_UPDATE_SETTING = URL_BASE + "otherway/owUpdateSettingUser.php";
	public static final String URL_UPDATE_AVATAR = URL_BASE + "otherway/owSendAvatar.php";
	public static final String URL_REFRESH_INFO = URL_BASE + "otherway/owRefreshInfo.php";
	public static final String URL_SET_BOOKMARK = URL_BASE + "otherway/owSetBookmark.php";
	public static final String URL_GET_BOOKMARK = URL_BASE + "otherway/owGetListBM.php";
	public static final String URL_DO_REQUEST = URL_BASE + "otherway/owDoRequest.php";
	public static final String URL_FILE_FOLDER = URL_BASE + "uploads/";
	
	// Chat
    public static final String CHAT_UPDATE_USER_GCMREGID = URL_BASE + "chat_api_v1/user/_ID_";
    public static final String CHAT_ROOMS_LIST = URL_BASE + "chat_api_v1/list_chat_rooms/_ID_";
    public static final String CHAT_ROOM_NEW = URL_BASE + "chat_api_v1/chat_rooms/new/_ID1_/_ID2_/_NAME_";
    public static final String CHAT_THREAD = URL_BASE + "chat_api_v1/chat_rooms/_ID_/_ID2_";
    public static final String CHAT_SEND_MESSAGE = URL_BASE + "chat_api_v1/chat_rooms/_ID_/message/_ISGROUP_";
    public static final String CHAT_SEND_FILE = URL_BASE + "chat_api_v1/chat_rooms/_ID_/sendfile/_ISGROUP_";
    public static final String CHAT_MESSAGE_DEL= URL_BASE + "chat_api_v1/delMessage";
    public static final String URL_FILE_FOLDER_CHAT = URL_BASE + "uploads_chat/";
	
	public static final String DELIMITER = "::::";
	
	public static final int MAX_DOCSIZE_UPLOAD = 2000; // (KB)
	public static final int MAX_IMGSIZE_UPLOAD = 1800; // (KB)
	public static final int MAX_NUM_FILE_UPLOAD = 2;
	
	public static final int MEDIA_TYPE_IMAGE = 100;
	public static final int MEDIA_TYPE_VIDEO = 200;
	
	public static final String GG_SENDER_ID = "274479450811";
	
	public static final int CAPTURE_IMAGE_REQUEST = 1;
	public static final int PICK_FILE_REQUEST = 0;
	
	// Icon
	public static final int NUM_ICON = 12;
	public static final String ICON0 = ":ic0:";
	public static final String ICON1 = ":ic1:";
	public static final String ICON2 = ":ic2:";
	public static final String ICON3 = ":ic3:";
	public static final String ICON4 = ":ic4:";
	public static final String ICON5 = ":ic5:";
	public static final String ICON6 = ":ic6:";
	public static final String ICON7 = ":ic7:";
	public static final String ICON8 = ":ic8:";
	public static final String ICON9 = ":ic9:";
	public static final String ICON10 = ":ic10:";
	public static final String ICON11 = ":ic11:";
	
	public static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB 
	public static final int RESIZE_SampleSize = 3;
	
	public static final String GO_TO_MYCHAT = "mychat";
	public static final String GO_TO_PERSON = "frgPerson";
	public static final String ACTION_LISTMEET = "listMeet";
	public static final String ACTION_LISTCONNECT = "listConnect";
	public static final String ACTION_PERSON = "person";
	public static final String ACTION_LEARN = "learn";
	public static final String ACTION_CATEGORIES = "cat";
	public static final String ACTION_FIELD = "field";
	
	// Convention DB
	public static final int CATEGORY_GS = 0;
	public static final int CATEGORY_NTG = 1;
	public static final int LEARN_LEARN = 1;
	public static final int LEARN_TEACH = 0;
	public static final int LEARN_BOTH = 2;
	public static final int SEX_NO_CHOICE = 0;
	public static final int SEX_MALE = 1;
	public static final int SEX_FEMALE = 2;
	public static final int CHAT_CTT_TEXT = 0; // and icon
	public static final int CHAT_CTT_IMG = 1;
	public static final int CHAT_CTT_DOC = 2;
	public static final int CONNECT_STATUS_NOT_CONNECT = 0; // not yet have connect
	public static final int CONNECT_STATUS_REQUESTING = 1;
	public static final int CONNECT_STATUS_CONFIRMED = 2;
	
	public static final String DATETIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
}