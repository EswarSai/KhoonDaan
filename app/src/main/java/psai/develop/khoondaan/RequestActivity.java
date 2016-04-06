package psai.develop.khoondaan;

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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RequestActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    TextView tvUsername, tvBloodgroup, tvMobilenumber;//, tvLandmark, tvCity;
    EditText etUsername, etBloodgroup, etMobilenumber;//, etLandmark, etCity;
    TextView tvStar1, tvStar2, tvStar3, tvStar4, tvStar5;
    RelativeLayout rLayout1, rLayout2, rLayout3, rLayout4, rLayout5;
    Button buttonRequest;
    AutoCompleteTextView actvBloodgroup;
    DatabaseHandler db;
    String userName, bloodGroup, mobileNumber, landMark, city;
    String userNameNew, bloodGroupNew, mobileNumberNew, landMarkNew, cityNew;

    String [] bloodGroups = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
    String userinfo; // this is the info about user who requested blood - This info sent to userlistactivity
    double latitude, longitude;
    public CognitoCachingCredentialsProvider credentialsProvider;

    public AmazonDynamoDBClient ddbClient;
    public DynamoDBMapper mapper;
    dynamoDBGetUsers asyncTask;

    // GPSTracker class
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new DatabaseHandler(this);

        // create class object
        gps = new GPSTracker(RequestActivity.this);

        /*// check if GPS enabled

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            //Log.i("RequestActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }*/

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

        userName = bloodGroup = mobileNumber = landMark = city = "";
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
        //etBloodgroup = (EditText) findViewById(R.id.etBloodgroup);
        etMobilenumber = (EditText) findViewById(R.id.etMobilenumber);
        //etLandmark = (EditText) findViewById(R.id.etLandmark);
        //etCity = (EditText) findViewById(R.id.etCity);

        actvBloodgroup = (AutoCompleteTextView) findViewById(R.id.actvBloodgroup);
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, bloodGroups);
        actvBloodgroup.setThreshold(1);
        actvBloodgroup.setAdapter(adapter);

        buttonRequest = (Button) findViewById(R.id.btnRequest);
        etUsername.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                etUsername.setError(null);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        /*etBloodgroup.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                etBloodgroup.setError(null);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });*/
        etMobilenumber.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if(etMobilenumber.getText().toString()!= null && etMobilenumber.getText().toString().matches("[0-9]{10}"))
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
        }); */

        List<User> userList = new ArrayList<User>();
        userList = db.getAllUsers();
        for(User u : userList){
            etUsername.setHint(u._username);
            etUsername.setText(u._username);
            userName = u._username;
            //Log.i("Username: ", userName);
            //etBloodgroup.setHint(u._bloodgroup);
            actvBloodgroup.setHint(u._bloodgroup);
            actvBloodgroup.setText(u._bloodgroup);
            bloodGroup = u._bloodgroup;
            //Log.i("Bloodgroup", bloodGroup);
            //etLandmark.setHint(u._landmark);
            landMark = u._landmark;
            landMarkNew = landMark;
            //Log.i("landMark", landMark);
            //etCity.setHint(u._city);
            city = u._city;
            cityNew = city;
            //Log.i("City", city);
            etMobilenumber.setHint(u._mobilenumber);
            etMobilenumber.setText(u._mobilenumber);
            mobileNumber = u._mobilenumber;
            break;
        }
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                buttonRequest.setEnabled(false);
                userNameNew = etUsername.getText().toString();
                if(TextUtils.isEmpty(userNameNew)) {
                    if(TextUtils.isEmpty(userName)) {
                        etUsername.setError("Please enter a email id");
                        buttonRequest.setEnabled(true);
                        return;
                    }
                    else {
                        etUsername.setText(userName);
                        userNameNew = userName;
                    }
                }
                else if ( ! android.util.Patterns.EMAIL_ADDRESS.matcher(userNameNew).matches()){
                    etUsername.setError("Please enter a valid email id");
                    buttonRequest.setEnabled(true);
                    return;
                }
                else{
                    etUsername.setError(null);
                    //return;
                }

                bloodGroupNew = actvBloodgroup.getText().toString();
                if(bloodGroupNew == null || TextUtils.isEmpty(bloodGroupNew)) {
                    if(TextUtils.isEmpty(bloodGroup)) {
                        actvBloodgroup.setError("Please enter BloodGroup");
                        buttonRequest.setEnabled(true);
                        return;
                    }
                    else{
                        actvBloodgroup.setText(bloodGroup);
                        bloodGroupNew = bloodGroup;
                    }
                }
                else{
                    if(bloodGroupNew.matches("(A|B|AB|O)[+-]")) {
                        actvBloodgroup.setError(null);
                    }
                    else{
                        actvBloodgroup.setError("Please enter valid BloodGroup");
                        buttonRequest.setEnabled(true);
                        return;
                    }
                }

                mobileNumberNew = etMobilenumber.getText().toString();
                if(TextUtils.isEmpty(mobileNumberNew)) {
                    if(TextUtils.isEmpty(mobileNumber)) {
                        etMobilenumber.setError("Please enter 10 digit Mobile Number");
                        buttonRequest.setEnabled(true);
                        return;
                    }
                    else{
                        etMobilenumber.setText(mobileNumber);
                        mobileNumberNew = mobileNumber;
                    }
                }
                else{

                    if(mobileNumberNew.matches("[0-9]{10}")){
                        etMobilenumber.setError(null);
                    }
                    else{
                        etMobilenumber.setError("Please enter valid 10 digit Mobile Number");
                        buttonRequest.setEnabled(true);
                        return;
                    }
                }

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

                    //Log.i("RequestActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    buttonRequest.setEnabled(true);
                    gps.showSettingsAlert();
                }

              /*  landMarkNew = etLandmark.getText().toString();
                if(TextUtils.isEmpty(landMarkNew)) {
                    if(TextUtils.isEmpty(landMark)) {
                        etLandmark.setError("Please enter nearby Landmark");
                        return;
                    }
                    else {
                        etLandmark.setText(landMark);
                        landMarkNew = landMark;
                    }
                }
                else{
                    etLandmark.setError(null);
                }

                cityNew = etCity.getText().toString();
                if(TextUtils.isEmpty(cityNew)) {
                    if(TextUtils.isEmpty(city)) {
                        etCity.setError("Please enter nearby City");
                        return;
                    }
                    else {
                        etCity.setText(city);
                        cityNew = city;
                    }
                }
                else{
                    etCity.setError(null);
                }

                String addr = landMarkNew+", "+cityNew;*/
                int count = 0, breakNow = 0;

                if(!landmark_pref.isEmpty()){
                    landMarkNew = landmark_pref;
                    cityNew = city_pref;
                }
                else {
                    Geocoder coder = new Geocoder(RequestActivity.this);
                    while (count < 5 && (breakNow == 0)) {
                        try {
                            count++;
                            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocation(latitude, longitude, 1);
                            if (adresses == null)
                                continue;
                            for (Address add : adresses) {
                                //longitude = add.getLongitude();
                                //latitude = add.getLatitude();
                                landMarkNew = add.getAddressLine(0);
                                cityNew = add.getLocality();
                                breakNow = 1;
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }// end of while
                }//else of If shared preference has landmark
                if(count == 5){
                    Toast.makeText(getApplicationContext(), "Not able to find your location. Please check your Internet Connectivity.",
                            Toast.LENGTH_LONG).show();
                    buttonRequest.setEnabled(true);
                }
                else{


                    String param = userNameNew+"#"+bloodGroupNew+"#"+mobileNumberNew+"#"+landMarkNew+"#"+cityNew+"#"+latitude+"#"+longitude;
                    userinfo = param;
                    ParseUtils.subscribeWithEmail(userNameNew);

                    dynamoDBGetUsers asyncTask = new dynamoDBGetUsers(RequestActivity.this);
                    asyncTask.execute(userNameNew+"#"+bloodGroupNew+"#"+cityNew);
                }

            }
        });
    }

    private class dynamoDBGetUsers extends AsyncTask<String, Void, List<Userddb>> {

        ProgressDialog dialog;

        public dynamoDBGetUsers(RequestActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected List<Userddb> doInBackground(String... params) {

            //TransferManager transferManager = new TransferManager(credentialsProvider);
            //Log.i("String in Async Task: ", params[0]);
            String[] parts = params[0].split("#");
            String username = parts[0]; String bloodgroup = parts[1]; String city = parts[2];

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
            expressionAttributeValues.put(":val", new AttributeValue().withS(city));

            DynamoDBQueryExpression<Userddb> query = new DynamoDBQueryExpression<Userddb>();
            Userddb hashKeyValues = new Userddb();
            hashKeyValues.setBloodgroup(bloodgroup);
            query.setHashKeyValues(hashKeyValues);
            /*query.setFilterExpression("city == :val");
            query.setExpressionAttributeValues(expressionAttributeValues);*/
            try {
                List<Userddb> results = mapper.query(Userddb.class, query);
                return results;
            }catch (Exception e){
                //Log.i("Request Activity", "Error in getting results from dynamodb.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Userddb> results) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            // Start Map activity
            ArrayList<String> userlist = new ArrayList<String>();
            if(results == null || results.isEmpty()){
                Toast.makeText(getApplicationContext(), "No users found. Please check your Internet Connectivity.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            for (Userddb u : results){
                String s = u.getUsername()+"#"+u.getBloodgroup()+"#"+u.getMobilenumber()+"#"+u.getLandmark()+"#"+u.getCity()+
                        "#"+u.getLocation()+"#"+u.getDate()+"#"+u.getRemainingNotifications();
                userlist.add(s);
            }

            Intent i = new Intent(RequestActivity.this, UserListActivity.class);
            i.putStringArrayListExtra("Results", userlist);
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            i.putExtra("requester", userinfo);
            startActivity(i);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog = new ProgressDialog(getApplicationContext());
            dialog.setMessage("Please wait. Searching...");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_request, menu);
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
                Intent activityChangeIntent = new Intent(RequestActivity.this, LoginActivity.class);

                activityChangeIntent.putExtra("user", "");
                RequestActivity.this.startActivity(activityChangeIntent);
            }
            else {
                List<User> userList = new ArrayList<User>();
                userList = db.getAllUsers();
                for (User u : userList) {

                    String s;
                    s = u.getName()+"#"+u.getBloodgroup()+"#"+u.getMobilenumber();
                    Intent activityChangeIntent = new Intent(RequestActivity.this, ProfileScreen.class);
                    activityChangeIntent.putExtra("name", s);
                    RequestActivity.this.startActivity(activityChangeIntent);
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
