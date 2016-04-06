package psai.develop.khoondaan;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.List;


public class RegisterResultActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    String username, bloodgroup;

    TextView tvuname, tvbgroup, tvmnumber, tvlmark, tvcty;

    public CognitoCachingCredentialsProvider credentialsProvider;

    public AmazonDynamoDBClient ddbClient;
    public DynamoDBMapper mapper;

    dynamoDBUserCheck asyncTask;
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_result);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new DatabaseHandler(this);
        tvuname = (TextView) findViewById(R.id.tvuname);
        tvbgroup = (TextView) findViewById(R.id.tvbgroup);
        tvmnumber = (TextView) findViewById(R.id.tvmnumber);
        tvlmark = (TextView) findViewById(R.id.tvlmark);
        tvcty = (TextView) findViewById(R.id.tvcty);

        Intent i = getIntent();
        username = i.getStringExtra("name");
        bloodgroup = i.getStringExtra("bloodgroup");

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


        asyncTask = new dynamoDBUserCheck();
        asyncTask.execute(bloodgroup+"#"+username);
    }


    // Get User information from DynamoDB table
    private class dynamoDBUserCheck extends AsyncTask<String, Void, Userddb> {

        //ProgressDialog dialog;

        /*public dynamoDBUserCheck(RegisterActivity activity) {
            dialog = new ProgressDialog(activity);
        }*/

        @Override
        protected Userddb doInBackground(String... params) {

            //TransferManager transferManager = new TransferManager(credentialsProvider);
           // Log.i("String in Async Task: ", params[0]);
            String[] parts = params[0].split("#");
            String username = parts[1]; String bloodgroup = parts[0];


            //Check if this user and bllod group combination exists -- with exponential backoff
            int retry = 1, count = 3;
            while (retry ==1 && count >0) {
                retry = 0; count--;
                try {
                    Userddb user = mapper.load(Userddb.class, bloodgroup, username);
                    if (user != null) {
                        //Log.i("User is present:", user.getUsername() + "," + user.getLandmark());
                        return user; // USer already exists. choose different username
                    } else {
                        //Log.i("User in DB User check", " is null.");
                        return null; //No user with this username exists
                    }
                } catch (Exception e) {
                    retry = 1;
                   // Log.i("Error in User check:", e.toString());
                }
            }// end of while

            if(retry == 1 || count <=0 ){
                return null; // Internet error
            }
            return null;
        }


        @Override
        protected void onPostExecute(Userddb result) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            if(result != null){
                tvuname.setText("Username : "+result.getUsername());
                tvuname.setTextColor(Color.BLACK);
                tvbgroup.setText("Bloodgroup : " + result.getBloodgroup());
                tvbgroup.setTextColor(Color.BLACK);
                tvmnumber.setText("Mobile Number : " + result.getMobilenumber());
                tvmnumber.setTextColor(Color.BLACK);
                tvlmark.setText("Landmark : " + result.getLandmark());
                tvlmark.setTextColor(Color.BLACK);
                tvcty.setText("City : " + result.getCity());
                tvcty.setTextColor(Color.BLACK);
            }
            else{
                tvuname.setVisibility(View.GONE);
                tvbgroup.setVisibility(View.GONE);
                tvmnumber.setVisibility(View.GONE);
                tvlmark.setVisibility(View.GONE);
                tvcty.setVisibility(View.GONE);
            }

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
        getMenuInflater().inflate(R.menu.menu_register_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.profile) {
            int userSize = 0;
            userSize = db.getUsersCount();
            if(userSize == 0) {
                Intent activityChangeIntent = new Intent(RegisterResultActivity.this, LoginActivity.class);

                activityChangeIntent.putExtra("user", "");
                RegisterResultActivity.this.startActivity(activityChangeIntent);
            }
            else {
                List<User> userList = new ArrayList<User>();
                userList = db.getAllUsers();
                for (User u : userList) {

                    String s;
                    s = u.getName()+"#"+u.getBloodgroup()+"#"+u.getMobilenumber();
                    Intent activityChangeIntent = new Intent(RegisterResultActivity.this, ProfileScreen.class);
                    activityChangeIntent.putExtra("name", s);
                    RegisterResultActivity.this.startActivity(activityChangeIntent);
                    break;
                }
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
