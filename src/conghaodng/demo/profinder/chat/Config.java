package conghaodng.demo.profinder.chat;

public class Config {
	// flag to identify whether to show single line
    // or multi line text in push notification tray
    public static boolean appendNotificationMessages = false;
 
    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";
 
    // broadcast receiver intent filters
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_CHAT_LIVE = "pushChatLive";
    public static final String PUSH_CHOOSING_LIVE = "pushChoosingLive";
 
    // type of push messages
    public static final int PUSH_TYPE_CHATROOM = 1;
    public static final int PUSH_TYPE_USER = 2;
    public static final int PUSH_INFO = 3;
	public static final int PUSH_CHOOSING = 4;
	public static final int TYPE_ANNOUNCEMENT = 1;
	public static final int TYPE_ALERT = 2;
 
    // id to handle the notification in the notification try
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
}
