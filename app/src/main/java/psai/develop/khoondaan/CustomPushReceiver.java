package psai.develop.khoondaan;

/**
 * Created by psai on 11/2/2015.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.util.Log;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by Ravi on 01/06/15.
 */
public class CustomPushReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = CustomPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    private Intent parseIntent;

    DatabaseHandler db;

    public CustomPushReceiver() {
        super();
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);

        if (intent == null)
            return;

        boolean request = false; // request = false for help received notification and true for request received
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String msg = json.getString("message");
            if(msg.contains("NEEDBLOOD")){
                request = true;
                //Log.i("CustomPushReceiver:", "THis is a request");
            }
            else{
                request = false;
                //Log.i("CustomPushReceiver:", "This is help");
            }
            //Log.i("CustomPushReceiver,: ", "msg received is: "+msg);
        } catch(Exception e){
            //Log.i("CustonPushReceiver:", e.toString());
        }

        int received_notif_count = 0;
        db = new DatabaseHandler(context);
        int notif_row_count = db.getNotificationRowsCount();
        if(notif_row_count == 0){
            NotificationRow notif = new NotificationRow();
            Date date = new Date();
            String DATE_FORMAT_NOW = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            String stringDate = sdf.format(date );
            notif.setDate(stringDate);
            notif.setCount(1);
            db.addNotificationRow(notif);
            received_notif_count = 0;
        }
        else{
            Date date = new Date();
            String DATE_FORMAT_NOW = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            String stringDate = sdf.format(date );

            List<NotificationRow> notifList = db.getAllNotificationRows();
            for(NotificationRow notif : notifList){
                if(stringDate.equals(notif.getDate())){ // if already entry exists for today
                    NotificationRow row = new NotificationRow();
                    row.setDate(stringDate);
                    if(request) {
                        row.setCount(notif.getCount() + 1);
                    }
                    else{ // Dont count if its a help receiving notification
                        row.setCount(notif.getCount());
                    }
                    db.updateNotifRow(row);
                    received_notif_count = notif.getCount();
                }
                else{
                    NotificationRow row = new NotificationRow();
                    row.setDate(stringDate);
                    row.setCount(1);
                    db.updateNotifRow(row);
                    received_notif_count = 1;
                }
            }
        }


        SharedPreferences pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        int max_notif_count = pref.getInt("notif_count", 10);

        if(received_notif_count <= max_notif_count || !request) { // show notification only if received notif count less than max count
            // and also show help receiving notification
            try {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

                //Log.e(TAG, "Push received: " + json);

                parseIntent = intent;

                parsePushJson(context, json);

            } catch (JSONException e) {
                //Log.e(TAG, "Push message json exception: " + e.getMessage());
            }
        }// end of if
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);

        //To track "App Opens"
        ParseAnalytics.trackAppOpenedInBackground(intent);

        //Here is data you sent
        //Log.i("PushCLick", intent.getExtras().getString( "com.parse.Data" ));

        Intent i = new Intent(context, PushMessageClickActivity.class);
        i.putExtras(intent.getExtras());

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }

    /**
     * Parses the push notification json
     *
     * @param context
     * @param json
     */
    private void parsePushJson(Context context, JSONObject json) {
        try {
            //boolean isBackground = json.getBoolean("is_background");
            //JSONObject data = json.getJSONObject("data");
            //String title = data.getString("title");
            String message = json.getString("message");

            /*if (!isBackground) {
                Intent resultIntent = new Intent(context, PushMessageClickActivity.class);
                showNotificationMessage(context, title, message, resultIntent);
            }*/

            Intent resultIntent = new Intent(context, PushMessageClickActivity.class);
            showNotificationMessage(context, "KhoonDaan", message, resultIntent);

        } catch (JSONException e) {
            //Log.e(TAG, "Push message json exception: " + e.getMessage());
        }
    }


    /**
     * Shows the notification message in the notification bar
     * If the app is in background, launches the app
     *
     * @param context
     * @param title
     * @param message
     * @param intent
     */
    private void showNotificationMessage(Context context, String title, String message, Intent intent) {

        notificationUtils = new NotificationUtils(context);

        intent.putExtras(parseIntent.getExtras());

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notificationUtils.showNotificationMessage(title, message, intent);
    }
}