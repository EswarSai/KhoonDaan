package psai.develop.khoondaan;

/**
 * Created by psai on 11/2/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
//import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ravi on 01/06/15.
 */
public class ParseUtils {

    private static String TAG = ParseUtils.class.getSimpleName();

    /*public static void verifyParseConfiguration(Context context) {
        if (TextUtils.isEmpty(AppConfig.PARSE_APPLICATION_ID) || TextUtils.isEmpty(AppConfig.PARSE_CLIENT_KEY)) {
            Toast.makeText(context, "Please configure your Parse Application ID and Client Key in AppConfig.java", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }
    }*/

    public static void registerParse(Context context) {
        // initializing parse library
        Parse.initialize(context, "", "");
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground("KhoonDaan", new SaveCallback() {
            @Override
            public void done(ParseException e) {
               // Log.e(TAG, "Successfully subscribed to Parse!");
            }
        });
    }

    public static void pushNotification(String msg, String email) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ParseQuery pQuery = installation.getQuery();
        pQuery.whereEqualTo("email", email);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", msg);
            //jsonObject.put("alert", "KhoonDaan");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
           // Log.e("JSONObject Error", e.toString());
        }

        ParsePush push = new ParsePush();
        push.setChannel("KhoonDaan");
        push.setData(jsonObject);
        //push.setMessage(msg);
        push.setQuery(pQuery);
        push.sendInBackground(new SendCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    //Log.i("push", "success!");
                } else {
                    //Log.i("push", e.getMessage());
                }
            }
        });
    }

    public static void subscribeWithEmail(String email) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        installation.put("email", email);

        installation.saveInBackground();
    }

    public static void unsubscribeWithEmail(String email) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        installation.remove("email");
        //installation.remove("email", email);

        installation.saveInBackground();
    }
}