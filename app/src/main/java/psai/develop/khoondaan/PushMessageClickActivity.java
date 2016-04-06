package psai.develop.khoondaan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PushMessageClickActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    String message, msg, emailid;

    TextView tv;

    Button helpButton;

    UserHeap user;

    DatabaseHandler db;
    public CognitoCachingCredentialsProvider credentialsProvider;

    public AmazonDynamoDBClient ddbClient;
    public DynamoDBMapper mapper;

    dynamoDBUserDateUpdate asyncTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_message_click);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

  /*      FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        db = new DatabaseHandler(this);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                this,    // get the context for the current activity
                "identitypoolID",    /* Identity Pool ID */
                Regions.US_EAST_1           /* Region */
        );

        // Initialize the Cognito Sync client
        CognitoSyncManager syncClient = new CognitoSyncManager(
                this,
                Regions.US_EAST_1, // Region
                credentialsProvider);

        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        tv = (TextView) findViewById(R.id.tvPushMessage);
        helpButton = (Button) findViewById(R.id.helpButton);

        user = new UserHeap();
        Intent intent = getIntent();
        message = intent.getExtras().getString("com.parse.Data");
        try {
            JSONObject obj = new JSONObject(message);
            msg = obj.getString("message");
            //Log.i("Message: ", msg);
            String [] parts = msg.split("\n");
            if(parts[0].equalsIgnoreCase("NEEDBLOOD") && parts.length == 12) { // This msg is received when a person requests for help

                message = "Email id: " + parts[1] + "<br><br><HR />" + "<b>Blood Group: " + parts[2]+"<br><br><HR />"+"Mobile Number: "+parts[3]+"</b><br><br><HR />"
                        +"LandMark: "+parts[4]+"<br><br><HR />"+"City: "+parts[5];
                user.setEmailId(parts[8].substring(parts[8].lastIndexOf("MYEMAILID:") + 10));
                user.setBloodgroup(parts[9].substring(parts[9].lastIndexOf("MYBLOODGROUP:") + 13));
                user.setMobilenumber(parts[10].substring(parts[10].lastIndexOf("MYNUMBER:") + 9));
                user.setLandmark(parts[11].substring(parts[11].lastIndexOf("MYLANDMARK:")+11));

                tv.setText(Html.fromHtml("<b><font color=\"red\">This person is in need of blood. Please Help!</font></b><br><br>" + message));
            }
            else if(parts[0].equalsIgnoreCase("CANHELP") && parts.length == 5){ // This msg is received when a person wants to help

                message = "Email id: " + parts[1] + "<br><br> <HR />" + "<b>Blood Group: " + parts[2]+"<br><br><HR />"+"Mobile Number: "+parts[3] + "</b><br><br><HR />"
                        + "LandMark: " + parts[4];
                tv.setText(Html.fromHtml("<b><font color=\"#008000\">This person is ready to help!</font></b><br><br>" + message));

                helpButton.setEnabled(false);
                helpButton.setVisibility(View.GONE);

                // adding entry to Database
                Date date = new Date();
                String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                String stringDate = sdf.format(date );
                Entry entry = new Entry();
                entry.setBloodgroup(parts[2]);
                entry.setDate(stringDate);
                entry.setHelperEmail(parts[1]);
                db.addEntry(entry);
            } else {
                tv.setText(Html.fromHtml("<b><font color=\"red\">This person is in need of blood. Please Help!</font></b><br>" + msg));
            }
           // Log.i("PushMessageClick", message);

        } catch (JSONException e){
            //Log.e("Error in JSON Parsing:", e.toString());
        }

        helpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String [] parts = msg.split("\n");
                if(user != null){
                    String email = parts[1];
                    String n_str = user.getEmailId()+"\n"+user.getBloodgroup()+"\n"+user.getMobilenumber()+"\n"+ user.getLandmark();

                    String param = user.getBloodgroup()+"#"+user.getEmailId();
                    asyncTask = new dynamoDBUserDateUpdate();
                    asyncTask.execute(param);

                    ParseUtils.pushNotification("CANHELP\n" + n_str, email);

                    Toast.makeText(PushMessageClickActivity.this, "Help Sent",
                            Toast.LENGTH_LONG).show();
                    helpButton.setEnabled(false);
                }
            }
        });
    }


    // Update the date user helped

    private class dynamoDBUserDateUpdate extends AsyncTask<String, Void, Void> {

        //ProgressDialog dialog;

        /*public dynamoDBUserCheck(RegisterActivity activity) {
            dialog = new ProgressDialog(activity);
        }*/

        @Override
        protected Void doInBackground(String... params) {


            //TransferManager transferManager = new TransferManager(credentialsProvider);
            //Log.i("String in Async Task: ", params[0]);
            String[] parts = params[0].split("#");
            if(parts.length < 2)
                return null;
            String username = parts[1]; String bloodgroup = parts[0];


            //Check if this user and bllod group combination exists -- with exponential backoff
            int retry = 1, count = 3;
            while (retry ==1 && count >0) {
                retry = 0; count--;
                try {
                    Userddb user = mapper.load(Userddb.class, bloodgroup, username);
                    if (user != null) {
                        Date date = new Date();
                        String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                        String stringDate = sdf.format(date );
                        user.setDate(stringDate);
                        mapper.save(user);
                        //Log.i("User: ", user.getUsername() + "," + user.getLandmark());
                        return null; // USer already exists. choose different username
                    } else {
                        //Log.i("User in DB User check", " is null.");
                        return null; //No user with this username exists
                    }
                } catch (Exception e) {
                    retry = 1;
                    //Log.i("Error in User check:", e.toString());
                }
            }// end of while

            if(retry == 1 || count <=0 ){

                return null; // Internet error
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            super.onPostExecute(v);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog = new ProgressDialog(getApplicationContext());

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_push_message_click, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.home) {

            Intent activityChangeIntent = new Intent(PushMessageClickActivity.this, MainActivity.class);


            PushMessageClickActivity.this.startActivity(activityChangeIntent);
            return true;
        }

        if(id == R.id.about) {

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final TextView textview = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            textview.setLayoutParams(lp);

            textview.setPadding(40,5,10,10);
            textview.setText("The gift of blood is the gift of life. Blood cannot be manufactured, it can only be donated." +
                    " Every year our nation requires 5 crore units of blood out of which only 80 lakh units of blood is available. There is a huge requirement of blood. " +
                    "Let us all come together and help our people by donating blood. Cheers!" + System.getProperty ("line.separator")+
                    "Please register by using your Email Id, Blood group and mobile number. This will help donees in finding donors. When you request blood, you will be shown " +
                    "nearby donees. You can message them directly by clicking on the markers on map. On receiving Blood request from donee" +
                    ", please help them!");
            alert.setMessage("KhoonDaan");
            alert.setTitle("About Us");

            alert.setView(textview);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //What ever you want to do with the value


                }
            });
            alert.show();

        }



        return super.onOptionsItemSelected(item);
    }

}
