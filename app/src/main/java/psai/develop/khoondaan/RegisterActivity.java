package psai.develop.khoondaan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    TextView tvUsername, tvBloodgroup, tvMobilenumber;//, tvLandmark, tvCity;
    EditText etUsername, etBloodgroup, etMobilenumber;//, etLandmark, etCity;
    TextView tvStar1, tvStar2, tvStar3, tvStar4, tvStar5;
    RelativeLayout rLayout1, rLayout2, rLayout3, rLayout4, rLayout5;
    Button buttonRegister;

    AutoCompleteTextView actvBloodgroup;
    User user, registereUser;
    String registeredUserName, registeredUserMobileNumber = "", registeredUserBloodGroup = "";
    DatabaseHandler db;
    String userName, bloodGroup, mobileNumber, landMark, city;
    double latitude, longitude;

    String [] bloodGroups = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
    int userRegistered = 0, internetError = 0;
    public CognitoCachingCredentialsProvider credentialsProvider;

    public AmazonDynamoDBClient ddbClient;
    public DynamoDBMapper mapper;
    dynamoDBUserUpdate asyncTask;

    dynamoDBUserCheck asyncTaskUserCheck;
    //dynamoDBItemUpdate asyncTask;


    // GPSTracker class
    GPSTracker gps;

    ProgressDialog dialog;

    boolean isUserCheckDone = false; // to check user email id before registering
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new DatabaseHandler(this);

        // create class object
        gps = new GPSTracker(RegisterActivity.this);

        // check if GPS enabled

            /*if (gps.canGetLocation()) {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

               // Log.i("RegisterActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }*/


        rLayout1 = (RelativeLayout) findViewById(R.id.rLayout1);
        rLayout2 = (RelativeLayout) findViewById(R.id.rLayout2);
        //rLayout3 = (RelativeLayout) findViewById(R.id.rLayout3);
        //rLayout4 = (RelativeLayout) findViewById(R.id.rLayout4);
        rLayout5 = (RelativeLayout) findViewById(R.id.rLayout5);

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvBloodgroup = (TextView) findViewById(R.id.tvBloodgroup);
        tvMobilenumber = (TextView) findViewById(R.id.tvMobilenumber);
        //tvLandmark = (TextView) findViewById(R.id.tvLandmark);
        //tvCity = (TextView) findViewById(R.id.tvCity);

        tvStar1 = (TextView) findViewById(R.id.tvStar1);
        tvStar2 = (TextView) findViewById(R.id.tvStar2);
        //tvStar3 = (TextView) findViewById(R.id.tvStar3);
        //tvStar4 = (TextView) findViewById(R.id.tvStar4);
        tvStar5 = (TextView) findViewById(R.id.tvStar5);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etUsername.setHint("Enter Email Id");
        //etBloodgroup = (EditText) findViewById(R.id.etBloodgroup);
        etMobilenumber = (EditText) findViewById(R.id.etMobilenumber);
        etMobilenumber.setHint("Enter 10 digit mobile number");
        //etLandmark = (EditText) findViewById(R.id.etLandmark);
        //etCity = (EditText) findViewById(R.id.etCity);

        actvBloodgroup = (AutoCompleteTextView) findViewById(R.id.actvBloodgroup);

        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, bloodGroups);
        actvBloodgroup.setThreshold(1);
        actvBloodgroup.setAdapter(adapter);

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

        // removing this as this is intensive
        /*etUsername.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                //etUsername.setError(null);
                String bgroup = actvBloodgroup.getText().toString();
                String uname = etUsername.getText().toString();
                if(bgroup != null && uname != null) {
                    dynamoDBUserCheck asyncTask1 = new dynamoDBUserCheck();

                    asyncTask1.execute(bgroup + "#" + uname);
                }
                else{
                    if(bgroup == null) {
                        etUsername.setError("Please enter Bloodgroup first!");
                    }
                    if(uname == null) {
                        etUsername.setError("Please enter email id!");
                    }
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });*/
        /*etBloodgroup.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                etBloodgroup.setError(null);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });*/
        etMobilenumber.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                etMobilenumber.setError(null);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        /*etLandmark.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                etLandmark.setError(null);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        etCity.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                etCity.setError(null);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });*/
        buttonRegister = (Button) findViewById(R.id.btnRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                buttonRegister.setEnabled(false);
                userName = bloodGroup = mobileNumber = landMark = city = "";
                userName = etUsername.getText().toString();
                bloodGroup = actvBloodgroup.getText().toString();
                //bloodGroup = etBloodgroup.getText().toString();
                if(TextUtils.isEmpty(userName)) {
                    etUsername.setError("Please enter email id");
                    buttonRegister.setEnabled(true);
                    return;
                }
                else if ( ! android.util.Patterns.EMAIL_ADDRESS.matcher(userName).matches()){
                    etUsername.setError("Please enter a valid email id");
                    buttonRegister.setEnabled(true);
                    return;
                }
                else{
                    /*if(TextUtils.isEmpty(bloodGroup)) {
                        //actvBloodgroup.setError("Please enter BloodGroup first!");
                        etUsername.setError("Please enter BloodGroup first!");
                        buttonRegister.setEnabled(true);
                        return;
                    }*/
                    asyncTaskUserCheck = new dynamoDBUserCheck();

                    asyncTaskUserCheck.execute(userName);
                    //etUsername.setError(null);
                }

                bloodGroup = actvBloodgroup.getText().toString();
               // Log.i("Bloodgroup: ", bloodGroup);
                if(!(bloodGroup != null && bloodGroup.matches("(A|B|AB|O)[+-]"))) {
                    actvBloodgroup.setError("Please enter valid BloodGroup");
                    buttonRegister.setEnabled(true);
                    return;
                }
                else{
                    actvBloodgroup.setError(null);
                }

                mobileNumber = etMobilenumber.getText().toString();
                if(TextUtils.isEmpty(mobileNumber)) {
                    etMobilenumber.setError("Please enter mobile number.");
                    buttonRegister.setEnabled(true);
                    return;
                }
                else if(!(mobileNumber != null && mobileNumber.matches("[0-9]{10}"))){
                    etMobilenumber.setError("Please enter valid 10 digit Mobile number");
                    buttonRegister.setEnabled(true);
                    return;
                }
                else {
                    etMobilenumber.setError(null);
                }

                /*SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                float lat_pref = pref.getFloat("latitude", 0);
                float lng_pref = pref.getFloat("longitude", 0);
                String landmark_pref = pref.getString("landmark", "");
                String city_pref = pref.getString("city", "");

                if(lat_pref != 0){
                    latitude = lat_pref;
                    longitude = lng_pref;

                }
                else if (gps.canGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                   // Log.i("RegisterActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    buttonRegister.setEnabled(true);
                    gps.showSettingsAlert();
                }*/

             /*   landMark = etLandmark.getText().toString();
                if(TextUtils.isEmpty(landMark)) {
                    etLandmark.setError("Please enter nearby Landmark.");
                    return;
                }
                else{
                    etLandmark.setError(null);
                }

                city = etCity.getText().toString();
                if(TextUtils.isEmpty(city)) {
                    etCity.setError("Please enter nearby City");
                    return;
                }
                else{
                    etCity.setError(null);
                }

                String addr = landMark+", "+city;*/
                /*int count = 0, breakNow = 0;

                if(!landmark_pref.isEmpty()){
                    landMark = landmark_pref;
                    city = city_pref;
                }
                else {
                    Geocoder coder = new Geocoder(RegisterActivity.this);
                    while (count < 5 && (breakNow == 0)) {
                        try {
                            count++;
                            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocation(latitude, longitude, 1);
                            if (adresses == null) {
                                Toast.makeText(getApplicationContext(), "Could not get your location.",
                                        Toast.LENGTH_LONG).show();
                                buttonRegister.setEnabled(true);
                                return;
                            }
                            for (Address add : adresses) {
                                //longitude = add.getLongitude();
                                //latitude = add.getLatitude();
                                landMark = add.getAddressLine(0);//add.toString();//
                                city = add.getLocality();
                                ;
                                breakNow = 1;
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }// end of while

                }
                if(count == 5){
                    Toast.makeText(getApplicationContext(), "Not able to find your location. Please check your Internet Connectivity.",
                            Toast.LENGTH_LONG).show();
                    buttonRegister.setEnabled(true);
                    return;
                }
                else{

                    while(!isUserCheckDone){
                        // wait till user check is done
                       // Log.i("Register Activity", "Waiting for USer check");
                    }
                    user = new User(userName, bloodGroup, mobileNumber, landMark, city);
                    //db.addUser(user);
                    //Log.i("COunt in DB:", String.valueOf(db.getUsersCount()));
                    String param = userName+"#"+bloodGroup+"#"+mobileNumber+"#"+landMark+"#"+city+"#"+latitude+"#"+longitude;
                    //Log.i("Adding entry in DB:", param);
                    if(userRegistered == 0 && internetError == 0) {
                        // subsrcibe to Parse with email
                        ParseUtils.subscribeWithEmail(userName);
                        asyncTask = new dynamoDBUserUpdate(RegisterActivity.this);

                        asyncTask.execute(param);
                        // Add this entry to DynamoDB
                    }
                    else if (internetError == 1){
                        Toast.makeText(getApplicationContext(), "Please check your Internet Connectivity.",
                                Toast.LENGTH_LONG).show();
                        buttonRegister.setEnabled(true);
                    }
                    else{// If user is already registered , show Registered Screen
                        Intent activityChangeIntent = new Intent(RegisterActivity.this, RegisteredActivity.class);
                        activityChangeIntent.putExtra("name", userName);
                        RegisterActivity.this.startActivity(activityChangeIntent);
                    }
                }*/
            }
        });
    }

    private class dynamoDBUserUpdate extends AsyncTask<String, Void, Boolean> {

        //ProgressDialog dialog;

        public dynamoDBUserUpdate(RegisterActivity activity) {
            //dialog = new ProgressDialog(activity);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            //TransferManager transferManager = new TransferManager(credentialsProvider);
           // Log.i("String in Async Task: ", params[0]);
            String[] parts = params[0].split("#");
            String username = parts[0]; String bloodgroup = parts[1]; String mobilenumber = parts[2];
            String landmark = parts[3]; String city = parts[4]; String latitude = parts[5]; String longitude = parts[6];


            Userddb userddb = new Userddb();
            userddb.setUsername(username);
            userddb.setBloodgroup(bloodgroup);
            userddb.setMobilenumber(mobilenumber);
            userddb.setLandmark(landmark);
            userddb.setCity(city);
            userddb.setLocation(latitude + "#" + longitude);
            userddb.setDate("0");
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            int rem_notif_count = pref.getInt("notif_count", 10);
            userddb.setRemainingNotifications(String.valueOf(rem_notif_count));
            // Exponential Backoff
            int count = 3, retry = 1;
            while(retry==1 && count > 0) {
                retry = 0;
                count--;
                try {
                   // Log.i("Adding entry to Dynamo:", userddb.getUsername());
                    mapper.save(userddb);
                } catch (Exception e) {
                   // Log.i("In Registering :", e.toString());
                    //Log.i("Error in adding entry ", "to Dynamo DB.");
                    retry = 1;
                    try {
                        Thread.sleep(1, 0);
                    } catch (Exception e1){

                    }

                }

            }// end of while
            if(retry == 1 || count == 0){
                internetError = 1;
                //Log.i("Failed in Registering ", "entry in Dynamo DB.");
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(result == false){
                Toast.makeText(getApplicationContext(), "Please check your Internet Connectivity.",
                        Toast.LENGTH_LONG).show();
            }
            else{
                db.addUser(user);
                Intent intent = new Intent(getApplicationContext(), RegisterResultActivity.class);
                intent.putExtra("name", user.getName());
                intent.putExtra("bloodgroup", user.getBloodgroup());
                RegisterActivity.this.startActivity(intent);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog = new ProgressDialog(getApplicationContext());
            //dialog.setMessage("Please wait. Registering...");
            //dialog.show();
            //dialog.setCanceledOnTouchOutside(false);
        }

    }

    // Check if USer already exists

    private class dynamoDBUserCheck extends AsyncTask<String, Void, Integer> {

        //ProgressDialog dialog;

        /*public dynamoDBUserCheck(RegisterActivity activity) {
            dialog = new ProgressDialog(activity);
        }*/

        @Override
        protected Integer doInBackground(String... params) {

            userRegistered = 0;
            //TransferManager transferManager = new TransferManager(credentialsProvider);
            //Log.i("String in Async Task: ", params[0]);
            //String[] parts = params[0].split("#");
            if(params[0] == null || params[0].isEmpty())
                return 0;
            String username = params[0]; String bloodgroup;// = parts[0];


            //Check if this user and bllod group combination exists -- with exponential backoff
            for(int i = 0; i < bloodGroups.length; i++) {
                bloodgroup = bloodGroups[i];
                int retry = 1, count = 3;
                while (retry == 1 && count > 0) {
                    retry = 0;
                    count--;
                    try {
                        Userddb userddb = mapper.load(Userddb.class, bloodgroup, username);
                        if (userddb != null) {
                           // Log.i("Check if name is taken:", userddb.getUsername() + "," + userddb.getLandmark());
                            registeredUserName = userddb.getUsername();
                            registeredUserMobileNumber = userddb.getMobilenumber();
                            registeredUserBloodGroup = userddb.getBloodgroup();
                            userRegistered = 1;
                            return 1; // USer already exists. choose different username
                        } else {
                            //Log.i("User in DB User check", " is null.");
                            //return 0; //No user with this username exists
                        }
                    } catch (Exception e) {
                        retry = 1;
                        //Log.i("Error in User check:", e.toString());
                    }
                }// end of while

                if (retry == 1 || count <= 0) {
                    internetError = 1;
                    return 2; // Internet error
                }
            }
            return 0;
        }


        @Override
        protected void onPostExecute(Integer result) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            if(result == 1){
                etUsername.setError("Email id already taken.");
                dialog.dismiss();
            }
            else if (result == 0) {
                etUsername.setError(null);
            }
            else if (result == 2) {
                etUsername.setError("Connection error");
                Toast.makeText(getApplicationContext(), "Please check your Internet Connectivity.",
                        Toast.LENGTH_LONG).show();
                dialog.dismiss();
                return;
            }

           // Log.i("Register activity", "User check done");
            isUserCheckDone = true;

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            float lat_pref = pref.getFloat("latitude", 0);
            float lng_pref = pref.getFloat("longitude", 0);
            String landmark_pref = pref.getString("landmark", "");
            String city_pref = pref.getString("city", "");

            if(lat_pref != 0){
                latitude = lat_pref;
                longitude = lng_pref;

            }
            else if (gps.canGetLocation()) {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                //Log.i("RegisterActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                buttonRegister.setEnabled(true);
                gps.showSettingsAlert();
            }

             /*   landMark = etLandmark.getText().toString();
                if(TextUtils.isEmpty(landMark)) {
                    etLandmark.setError("Please enter nearby Landmark.");
                    return;
                }
                else{
                    etLandmark.setError(null);
                }

                city = etCity.getText().toString();
                if(TextUtils.isEmpty(city)) {
                    etCity.setError("Please enter nearby City");
                    return;
                }
                else{
                    etCity.setError(null);
                }

                String addr = landMark+", "+city;*/
            int count = 0, breakNow = 0;

            if(!landmark_pref.isEmpty()){
                landMark = landmark_pref;
                city = city_pref;
            }
            else {
                Geocoder coder = new Geocoder(RegisterActivity.this);
                while (count < 5 && (breakNow == 0)) {
                    try {
                        count++;
                        ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocation(latitude, longitude, 1);
                        if (adresses == null) {
                            Toast.makeText(getApplicationContext(), "Could not get your location.",
                                    Toast.LENGTH_LONG).show();
                            buttonRegister.setEnabled(true);
                            return;
                        }
                        for (Address add : adresses) {
                            //longitude = add.getLongitude();
                            //latitude = add.getLatitude();
                            landMark = add.getAddressLine(0);//add.toString();//
                            city = add.getLocality();
                            ;
                            breakNow = 1;
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }// end of while

            }
            if(count == 5){
                Toast.makeText(getApplicationContext(), "Not able to find your location. Please check your Internet Connectivity.",
                        Toast.LENGTH_LONG).show();
                buttonRegister.setEnabled(true);
                return;
            }
            else{


                user = new User(userName, bloodGroup, mobileNumber, landMark, city);
                registereUser = new User(registeredUserName, registeredUserBloodGroup, registeredUserMobileNumber, landMark, city);
                //db.addUser(user);
                //Log.i("COunt in DB:", String.valueOf(db.getUsersCount()));
                String param = userName+"#"+bloodGroup+"#"+mobileNumber+"#"+landMark+"#"+city+"#"+latitude+"#"+longitude;
               // Log.i("Adding entry in DB:", param);
                if(userRegistered == 0 && internetError == 0) {
                    // subsrcibe to Parse with email
                    ParseUtils.subscribeWithEmail(userName);
                    asyncTask = new dynamoDBUserUpdate(RegisterActivity.this);

                    asyncTask.execute(param);
                    // Add this entry to DynamoDB
                }
                else if (internetError == 1){
                    Toast.makeText(getApplicationContext(), "Please check your Internet Connectivity.",
                            Toast.LENGTH_LONG).show();
                    buttonRegister.setEnabled(true);
                }
                else{// If user is already registered , show Registered Screen
                    db.addUser(registereUser); // adding the database entry as user is already registered
                    Intent activityChangeIntent = new Intent(RegisterActivity.this, RegisteredActivity.class);
                    activityChangeIntent.putExtra("name", userName);
                    RegisterActivity.this.startActivity(activityChangeIntent);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog = new ProgressDialog(getApplicationContext());
            dialog = new ProgressDialog(RegisterActivity.this);
            dialog.setMessage("Please wait. Registering...");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
                Intent activityChangeIntent = new Intent(RegisterActivity.this, LoginActivity.class);

                activityChangeIntent.putExtra("user", "");
                RegisterActivity.this.startActivity(activityChangeIntent);
            }
            else {
                List<User> userList = new ArrayList<User>();
                userList = db.getAllUsers();
                for (User u : userList) {

                    String s;
                    s = u.getName()+"#"+u.getBloodgroup()+"#"+u.getMobilenumber();
                    Intent activityChangeIntent = new Intent(RegisterActivity.this, ProfileScreen.class);
                    activityChangeIntent.putExtra("name", s);
                    RegisterActivity.this.startActivity(activityChangeIntent);
                    break;
                }
            }
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
