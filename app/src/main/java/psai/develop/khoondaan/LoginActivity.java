package psai.develop.khoondaan;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
//import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    TextView textView, tvUsername, tvStar1, tvStar2;
    EditText etUsername, etBloodgroup;
    RelativeLayout rLayout1, rLayout2;
    LinearLayout lLayout;
    AutoCompleteTextView actvBloodgroup;
    Button loginButton;

    String username, bloodgroup;

    String [] bloodGroups = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};

    public CognitoCachingCredentialsProvider credentialsProvider;

    public AmazonDynamoDBClient ddbClient;
    public DynamoDBMapper mapper;

    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new DatabaseHandler(this);
        lLayout = (LinearLayout) findViewById(R.id.loginLL);
        textView = (TextView) lLayout.findViewById(R.id.text);
        textView.setText("If you haven't already registered, Please go back and Register!");

        rLayout1 = (RelativeLayout) lLayout.findViewById(R.id.rLayout1);
        rLayout2 = (RelativeLayout) lLayout.findViewById(R.id.rLayout2);

        tvUsername = (TextView) rLayout1.findViewById(R.id.tvUsername);
        actvBloodgroup = (AutoCompleteTextView) lLayout.findViewById(R.id.actvBloodgroup);
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, bloodGroups);
        actvBloodgroup.setThreshold(1);
        actvBloodgroup.setAdapter(adapter);

        tvStar1 = (TextView) rLayout1.findViewById(R.id.tvStar1);
        tvStar2 = (TextView) rLayout2.findViewById(R.id.tvStar2);

        etUsername = (EditText) lLayout.findViewById(R.id.etUsername);
        loginButton = (Button) lLayout.findViewById(R.id.btnLogin);

        etUsername.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                etUsername.setError(null);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

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


        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                username = etUsername.getText().toString();
                if(TextUtils.isEmpty(username)) {
                    if(TextUtils.isEmpty(username)) {
                        etUsername.setError("Please enter a email id");
                        return;
                    }
                }
                else if ( ! android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()){
                    etUsername.setError("Please enter a valid email id");
                    return;
                }
                else{
                    etUsername.setError(null);
                    //return;
                }

                bloodgroup = actvBloodgroup.getText().toString();
                if(TextUtils.isEmpty(bloodgroup)) {
                    if(TextUtils.isEmpty(bloodgroup)) {
                        actvBloodgroup.setError("Please enter BloodGroup");
                        return;
                    }
                }
                else{
                    actvBloodgroup.setError(null);
                }


                dynamoDBGetUser asyncTask = new dynamoDBGetUser(LoginActivity.this);
                String param = username + "#" + bloodgroup;
                asyncTask.execute(param);
            }
        });



    }



    // Get USer details

    private class dynamoDBGetUser extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        public dynamoDBGetUser(LoginActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected String doInBackground(String... params) {


            //TransferManager transferManager = new TransferManager(credentialsProvider);
            //Log.i("String in Async Task: ", params[0]);
            String[] parts = params[0].split("#");
            if(parts.length < 2)
                return null;
            String username = parts[0]; String bloodgroup = parts[1];


            //Check if this user and bllod group combination exists -- with exponential backoff
            int retry = 1, count = 3;
            while (retry ==1 && count >0) {
                retry = 0; count--;
                try {
                    Userddb user = mapper.load(Userddb.class, bloodgroup, username);
                    if (user != null) {
                        //user.setDate(stringDate);
                        User u = new User();
                        u.setBloodgroup(user.getBloodgroup());
                        u.setName(user.getUsername());
                        u.setMobilenumber(user.getMobilenumber());
                        u.setLandmark(user.getLandmark());
                        u.setCity(user.getCity());
                        db.addUser(u);
                        //Log.i("User: ", user.getUsername() + "," + user.getLandmark());
                        return user.getUsername()+"#"+user.getBloodgroup()+"#"+user.getMobilenumber(); // USer already exists. choose different username
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
        protected void onPostExecute(String s) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            //super.onPostExecute(v);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(s != null) {
                // Starting Main Activity
                Intent activityChangeIntent = new Intent(LoginActivity.this, MainActivity.class);
                //activityChangeIntent.putExtra("name", s);
                LoginActivity.this.startActivity(activityChangeIntent);
            }
            else{
                Toast.makeText(getApplicationContext(), "You are not registered. Please register! or May be there is some problem with your Internet Connectivity.",
                        Toast.LENGTH_SHORT).show();
                super.onPostExecute(s);
            }


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait. Logging in...");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

        }
    }

}
