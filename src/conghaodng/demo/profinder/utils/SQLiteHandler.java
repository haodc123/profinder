package conghaodng.demo.profinder.utils;

import java.util.HashMap;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import conghaodng.demo.profinder.global.Constants;

public class SQLiteHandler extends SQLiteOpenHelper {
 
    private static final String TAG = SQLiteHandler.class.getSimpleName();
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = Constants.APP_NAME;
 
    // Table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_CHOOSING = "choosing";
 
    // Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_FBID = "fbid";
    public static final String KEY_NAME = "name";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_TELEPHONE = "tel";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_USER_SEX = "sex";
    public static final String KEY_UID = "uid";
    public static final String KEY_TYPE = "type";
    public static final String KEY_TOKEN = "token";
    
    public static final String KEY_IDCHOOSING = "idChoosing";
    public static final String KEY_LEARN = "learn";
    public static final String KEY_CAT = "cat";
    public static final String KEY_FIE = "fie";
    public static final String KEY_SBJ = "sbj";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_TAG = "tag";
    public static final String KEY_DOC = "doc";
    public static final String KEY_IMG = "img";
    public static final String KEY_TEL = "tel";
    public static final String KEY_LOCATION = "loca";
    public static final String KEY_FEE = "fee";
    public static final String KEY_TIME = "time";
    public static final String KEY_SEX = "sex";
    public static final String KEY_CHG_DATE = "date";
 
    private SQLiteDatabase db;
 
    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        
        createTableUser(db);
        createTableChoosing(db);
 
        Log.d(TAG, "Database tables created");
    }
 
    private void createTableChoosing(SQLiteDatabase db) {
		// TODO Auto-generated method stub
    	String CREATE_TABLE = 
        		"CREATE TABLE " + TABLE_CHOOSING + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
        		+ KEY_LEARN + " INTEGER,"
                + KEY_CAT + " INTEGER,"
                + KEY_FIE + " INTEGER,"
                + KEY_SBJ + " INTEGER,"
                + KEY_CONTENT + " TEXT,"
                + KEY_TAG + " TEXT,"
                + KEY_DOC + " TEXT,"
                + KEY_IMG + " TEXT,"
                + KEY_TEL + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_FEE + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_SEX + " INTEGER,"
                + KEY_CHG_DATE + " TEXT,"
                + KEY_IDCHOOSING + " INTEGER,"
        		+ KEY_UID + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
	}

	private void createTableUser(SQLiteDatabase db) {
		// TODO Auto-generated method stub
    	String CREATE_TABLE = 
    			"CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_FBID + " TEXT,"
        		+ KEY_NAME + " TEXT,"
        		+ KEY_AVATAR + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_TELEPHONE + " TEXT UNIQUE,"
                + KEY_ADDRESS+ " TEXT,"
                + KEY_USER_SEX + " INTEGER,"
                + KEY_UID + " TEXT,"
                + KEY_TYPE + " INTEGER,"
        		+ KEY_TOKEN + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
	}

	// Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        /*db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHOOSING);
 
        // Create tables again
        onCreate(db);*/
    }
 
    /**
     * Storing user details in database
     * */
    public void addUser(String fbid, String name, String avatar, String email, String tel, String address, int sex, String uid, String token, int type) {
        db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_FBID, fbid); // FBID
        values.put(KEY_NAME, name); // Name
        values.put(KEY_AVATAR, avatar); // Avatar
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_TELEPHONE, tel); // Tel
        values.put(KEY_ADDRESS, address); // Address
        values.put(KEY_USER_SEX, sex); // Tel
        values.put(KEY_UID, uid); // ID
        values.put(KEY_TYPE, type); // Type
        values.put(KEY_TOKEN, token); // Token
 
        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection
 
        Log.d(TAG, "New user inserted into sqlite: " + id);
    }
    public void addChoosing(int aIdChoosing, int aLearn, int aCat, int aFie, int aSbj, String aContent,
    		String aTag, String aDoc, String aImg, String aTel, String aLocation,
    		String aFee, String aTime, int aSex, String aDate, String idUser) {
        db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_LEARN, aLearn);
        values.put(KEY_CAT, aCat);
        values.put(KEY_FIE, aFie); 
        values.put(KEY_SBJ, aSbj);
        values.put(KEY_CONTENT, aContent);
        values.put(KEY_TAG, aTag);
        values.put(KEY_DOC, aDoc);
        values.put(KEY_IMG, aImg); 
        values.put(KEY_TEL, aTel);
        values.put(KEY_LOCATION, aLocation);
        values.put(KEY_FEE, aFee);
        values.put(KEY_TIME, aTime);
        values.put(KEY_SEX, aSex); 
        values.put(KEY_CHG_DATE, aDate);
        values.put(KEY_IDCHOOSING, aIdChoosing);
        values.put(KEY_UID, idUser);
 
        // Inserting Row
        long id = db.insert(TABLE_CHOOSING, null, values);
        db.close(); // Closing database connection
 
        Log.d(TAG, "New choosing inserted into sqlite: " + id);
    }
 
    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;
 
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
        	user.put(KEY_FBID, cursor.getString(1));
            user.put(KEY_NAME, cursor.getString(2));
            user.put(KEY_AVATAR, cursor.getString(3));
            user.put(KEY_EMAIL, cursor.getString(4));
            user.put(KEY_TELEPHONE, cursor.getString(5));
            user.put(KEY_ADDRESS, cursor.getString(6));
            user.put(KEY_USER_SEX, cursor.getString(7));
            user.put(KEY_UID, cursor.getString(8));
            user.put(KEY_TYPE, cursor.getString(9));
            user.put(KEY_TOKEN, cursor.getString(10));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());
 
        return user;
    }
    public String getUserID() {
        String selectQuery = "SELECT  * FROM " + TABLE_USER;
 
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
        	return cursor.getString(8);
        }
        cursor.close();
        db.close();
        return "";
    }
    public HashMap<String, String> getChoosingDetails(String uid) {
        HashMap<String, String> choosing = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_CHOOSING + " WHERE " + KEY_UID + " = '" + uid + "'";
 
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            choosing.put(KEY_LEARN, cursor.getString(1));
            choosing.put(KEY_CAT, cursor.getString(2));
            choosing.put(KEY_FIE, cursor.getString(3));
            choosing.put(KEY_SBJ, cursor.getString(4));
            choosing.put(KEY_CONTENT, cursor.getString(5));
            choosing.put(KEY_TAG, cursor.getString(6));
            choosing.put(KEY_DOC, cursor.getString(7));
            choosing.put(KEY_IMG, cursor.getString(8));
            choosing.put(KEY_TEL, cursor.getString(9));
            choosing.put(KEY_LOCATION, cursor.getString(10));
            choosing.put(KEY_FEE, cursor.getString(11));
            choosing.put(KEY_TIME, cursor.getString(12));
            choosing.put(KEY_SEX, cursor.getString(13));
            choosing.put(KEY_CHG_DATE, cursor.getString(14));
            choosing.put(KEY_IDCHOOSING, cursor.getString(15));
        } else {
        	return null;
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching choosing from Sqlite: " + choosing.toString());
 
        return choosing;
    }
    public void updateUser(String name, String avatar, String email, String tel, String token, String uid) {
        db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_AVATAR, avatar); // Avatar
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_TELEPHONE, tel); // Tel
        values.put(KEY_TOKEN, token); // Token
 
        // Update Row
        db.update(TABLE_USER, values, KEY_UID+" = "+uid, null);
        db.close(); // Closing database connection
 
        Log.d(TAG, "User updated into sqlite: " + name);
    }
 
    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUser() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();
 
        Log.d(TAG, "Deleted all user info from sqlite");
    }
    public void deleteChoosing() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_CHOOSING, null, null);
        db.close();
 
        Log.d(TAG, "Deleted all choosing info from sqlite");
    }

}
