package psai.develop.khoondaan;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "usersManager";

    // Users table name
    private static final String TABLE_USERS = "users";

    // User history table
    private static final String TABLE_HISTORY = "history";

    //User Notification COunt table
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Contacts Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_MO_Number = "mobile_number";
    private static final String KEY_BL_Group = "blood_group";
    private static final String KEY_LD_Mark = "land_mark";
    private static final String KEY_CITY = "city";

    private static  final String KEY_ID = "id";
    private static final String KEY_Email = "email";
    private static final String KEY_Date = "date";
    private static final String KEY_Help_Email = "helpemail";

    private static final String KEY_NOTIF_COUNT = "notifcount";

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_NAME + " TEXT PRIMARY KEY," + KEY_BL_Group + " TEXT,"
            + KEY_MO_Number + " TEXT," + KEY_LD_Mark + " TEXT," + KEY_CITY + " TEXT" + ")";

    private static final String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_BL_Group + " TEXT,"
            + KEY_Help_Email + " TEXT," + KEY_Date + " TEXT" + ")";

    private static final String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE "+ TABLE_NOTIFICATIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_Date + " TEXT," + KEY_NOTIF_COUNT +
            " INTEGER DEFAULT 0" + ")";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_USERS_TABLE);

        //Log.i("DatabaseHandler:", "Creating table history");
        db.execSQL(CREATE_HISTORY_TABLE);
        //Log.i("DatabaseHandler:", "Creating table notifications");
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new user
    void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, user.getName()); // Contact Name
        values.put(KEY_MO_Number, user.getMobilenumber()); // Contact Phone
        values.put(KEY_CITY, user.getCity());
        values.put(KEY_LD_Mark, user.getLandmark());
        values.put(KEY_BL_Group, user.getBloodgroup());
        // Inserting Row
        db.insert(TABLE_USERS, null, values);
        db.close(); // Closing database connection
    }

    // Adding new entry
    void addEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_ID, entry.getId()); // Contact Name
        values.put(KEY_BL_Group, entry.getBloodgroup());
        values.put(KEY_Help_Email, entry.getHelperEmail());
        values.put(KEY_Date, entry.getDate());

        // Inserting Row
        db.insert(TABLE_HISTORY, null, values);
        db.close(); // Closing database connection
    }

    //Adding new entry in notifications table
    void addNotificationRow(NotificationRow notif){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_Date, notif.getDate());
        values.put(KEY_NOTIF_COUNT, notif.getCount());

        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close(); // Closing database connection

    }

    // Getting single user
    User getUser(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[] { KEY_NAME,
                        KEY_BL_Group, KEY_MO_Number, KEY_LD_Mark, KEY_CITY }, KEY_NAME + "=?",
                new String[] { name }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        User user = new User(cursor.getString(0),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        // return user
        return user;
    }

    // Getting All Users
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setName(cursor.getString(0));
                user.setBloodgroup(cursor.getString(1));
                user.setMobilenumber(cursor.getString(2));
                user.setLandmark(cursor.getString(3));
                user.setCity(cursor.getString(4));
                // Adding user to list
                userList.add(user);
            } while (cursor.moveToNext());
        }

        // return user list
        return userList;
    }

    // Getting All Entries
    public List<Entry> getAllEntries() {
        List<Entry> entryList = new ArrayList<Entry>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Entry entry = new Entry();
                entry.setId(cursor.getInt(0));
                entry.setBloodgroup(cursor.getString(1));
                entry.setHelperEmail(cursor.getString(2));
                entry.setDate(cursor.getString(3));
                // Adding entry to list
                entryList.add(entry);
            } while (cursor.moveToNext());
        }

        // return entry list
        return entryList;
    }


    // Getting All Notification rows
    public List<NotificationRow> getAllNotificationRows() {
        List<NotificationRow> notifList = new ArrayList<NotificationRow>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NotificationRow notif = new NotificationRow();
                notif.setId(cursor.getInt(0));
                notif.setDate(cursor.getString(1));
                notif.setCount(cursor.getInt(2));
                // Adding entry to list
                notifList.add(notif);
            } while (cursor.moveToNext());
        }

        // return notif list
        return notifList;
    }

    // Updating single user
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, user.getName());
        values.put(KEY_MO_Number, user.getMobilenumber());
        values.put(KEY_BL_Group, user.getBloodgroup());
        values.put(KEY_LD_Mark, user.getLandmark());
        values.put(KEY_CITY, user.getCity());
        // updating row
        return db.update(TABLE_USERS, values, KEY_NAME + " = ?",
                new String[] { user.getName() });
    }

    //Updating a notif row
    public int updateNotifRow(NotificationRow notif){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, notif.getId());
        values.put(KEY_Date, notif.getDate());
        values.put(KEY_NOTIF_COUNT, notif.getCount());
        return db.update(TABLE_NOTIFICATIONS, values, KEY_ID + " = ?",
                new String[] {Integer.toString(notif.getId())});
    }

    // Deleting single user
    public void deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, KEY_NAME + " = ?",
                new String[]{user.getName()});
        db.close();
    }


    // Getting users Count
    public int getUsersCount() {
        String countQuery = "SELECT  * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting entry Count
    public int getEntryCount() {
        String countQuery = "SELECT  * FROM " + TABLE_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    //Getting notification rows count
    public int getNotificationRowsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }



    public void delete() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ TABLE_USERS);
        db.execSQL("delete from "+ TABLE_HISTORY);
        db.execSQL("delete from "+ TABLE_NOTIFICATIONS);
    }
}
