package psai.develop.khoondaan;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.parse.Parse;
import com.parse.ParseInstallation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity /*extends ActionBarActivity*/ {

    private Toolbar mToolbar;
    Button requestButton, registerButton;

    DatabaseHandler db;

    public CognitoCachingCredentialsProvider credentialsProvider;

    public AmazonDynamoDBClient ddbClient;
    public DynamoDBMapper mapper;

    //dynamoDBUserLocationUpdate asyncTask;

    // GPSTracker class
   // GPSTracker gps;
    double latitude = 0, longitude = 0;
    String landMark, city;

    boolean isUserRegistered = false;
    LocationManager mLocationManager;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 10; // 10 minutes

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean hasRequestedPermission = false; // to make sure not to request for permission twice

    Location location;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 69;

    boolean isSettingsALertShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);



       /* // create class object
        Log.i("MainActivity: ", "Calling GPS");
        gps = new GPSTracker(MainActivity.this);
*/
        db = new DatabaseHandler(this);
        //this.deleteDatabase("usersManager");
        //db.delete();

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

        requestButton = (Button) findViewById(R.id.buttonRequest);
        registerButton = (Button) findViewById(R.id.buttonRegister);
        //Parse.initialize(this, "", "");
        //ParseInstallation.getCurrentInstallation().saveInBackground();


        registerButton.setEnabled(false);
        requestButton.setEnabled(false);

        requestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                Intent activityChangeIntent = new Intent(MainActivity.this, RequestActivity.class);

                MainActivity.this.startActivity(activityChangeIntent);
            }
        });

        /*int userSize = 0;
        userSize = db.getUsersCount();
        if(userSize == 0) {

            isUserRegistered = false;
        }
        else {

            registerButton.setText("Profile"); // Changing register button to Profile button if user is already registered
            isUserRegistered = true;
        }*/


        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                int userSize = 0;
                //userSize = db.getUsersCount();
                if(!isUserRegistered/*userSize == 0*/) {
                    Intent activityChangeIntent = new Intent(MainActivity.this, RegisterActivity.class);

                    MainActivity.this.startActivity(activityChangeIntent);
                }
                else { // Goto Profile screen if user is already registered.
                    /*List<User> userList = new ArrayList<User>();
                    userList = db.getAllUsers();
                    for(User u : userList){

                        Intent activityChangeIntent = new Intent(MainActivity.this, RegisteredActivity.class);
                        activityChangeIntent.putExtra("name", u.getName());
                        MainActivity.this.startActivity(activityChangeIntent);
                        break;
                    }*/


                    List<User> userList = new ArrayList<User>();
                    userList = db.getAllUsers();
                    for (User u : userList) {

                        String s;
                        s = u.getName()+"#"+u.getBloodgroup()+"#"+u.getMobilenumber();
                        Intent activityChangeIntent = new Intent(MainActivity.this, ProfileScreen.class);
                        activityChangeIntent.putExtra("name", s);
                        MainActivity.this.startActivity(activityChangeIntent);
                        break;
                    }
                }
            }
        });

  /*      // check if GPS enabled

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Log.i("MainActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

        }
        if(!gps.canGetLocation() || latitude == 0) {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            Log.i("MainActivity,", " OnCreate loc not found");
            //gps.showSettingsAlert();
        }
*/


        /*dynamoDBUserLocationUpdate asyncTask = new dynamoDBUserLocationUpdate();
        asyncTask.execute("");*/



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            int max_notif_count = pref.getInt("notif_count", 10);

            final EditText edittext = new EditText(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            edittext.setLayoutParams(lp);

            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            edittext.setHint(String.valueOf(max_notif_count));
            if(max_notif_count != 10){
                edittext.setHint(String.valueOf(max_notif_count));
                edittext.setText(String.valueOf(max_notif_count));
            }
            alert.setMessage("Number of notifications you would like to receive in a day");
            alert.setTitle("Set Notification Limit");

            alert.setView(edittext);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //What ever you want to do with the value

                    String countValue = edittext.getText().toString();
                    int count;
                    if (countValue != null && !countValue.isEmpty()) {
                        count = Integer.parseInt(countValue);

                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("notif_count", count);
                        editor.commit();
                    }
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alert.show();

            return true;
        }
        if(id == R.id.profile) {
            int userSize = 0;
            userSize = db.getUsersCount();
            if(userSize == 0) {
                Intent activityChangeIntent = new Intent(MainActivity.this, LoginActivity.class);

                activityChangeIntent.putExtra("user", "");
                MainActivity.this.startActivity(activityChangeIntent);
            }
            else {
                List<User> userList = new ArrayList<User>();
                userList = db.getAllUsers();
                for (User u : userList) {

                    String s;
                    s = u.getName()+"#"+u.getBloodgroup()+"#"+u.getMobilenumber();
                    Intent activityChangeIntent = new Intent(MainActivity.this, ProfileScreen.class);
                    activityChangeIntent.putExtra("name", s);
                    MainActivity.this.startActivity(activityChangeIntent);
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

    @Override
    protected void onResume(){
        super.onResume();
        //Log.i("Main Activity:", "On Resume GPS");
        int userSize = 0;
        userSize = db.getUsersCount();
        if(userSize == 0) {

            isUserRegistered = false;
        }
        else {

            registerButton.setText("Profile"); // Changing register button to Profile button if user is already registered
            isUserRegistered = true;
        }

        // Get location
        if(latitude == 0)
            getLocationAndStartTask();

/*
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        float lat_pref = pref.getFloat("latitude", 0);
        float lng_pref = pref.getFloat("longitude", 0);
        if(lat_pref == 0) { // if latitude is not stored in preferences , find latitude and longitude


            // check if GPS enabled

            try {
                Thread.sleep(300, 0);
            } catch (Exception e) {

            }
            gps.getLocation(false);
            if (gps.canGetLocation()) {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                Log.i("Main,ResumeLoc: ", String.valueOf(latitude) + String.valueOf(longitude));


            }
            if (!gps.canGetLocation() || latitude == 0) {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                Log.i("Main Activity, Resume:", "Could not get GPS");
                Toast.makeText(MainActivity.this, "Couldn't find your location. " +
                        "Please turn on GPS.", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("Main Activity, Resume:", "Calling ASYNC tASK, LAT: "+String.valueOf(latitude));
                dynamoDBUserLocationUpdate asyncTask = new dynamoDBUserLocationUpdate();
                asyncTask.execute("");
            }
        }
        else{ // directly update location with out calling gps
            //dynamoDBUserLocationUpdate asyncTask = new dynamoDBUserLocationUpdate();
            //asyncTask.execute("");
            requestButton.setEnabled(true);
            registerButton.setEnabled(true);
        }
*/
    }

    // Update the location of user

    private class dynamoDBUserLocationUpdate extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        /*public dynamoDBUserCheck(RegisterActivity activity) {
            dialog = new ProgressDialog(activity);
        }*/

        @Override
        protected String doInBackground(String... params) {



            // Updating user location
            int userSize = 0;
            userSize = db.getUsersCount();
            if(userSize > 0) {
                List<User> userList = new ArrayList<User>();
                userList = db.getAllUsers();

                for (User u : userList) {

                    /*if (gps.canGetLocation()) {

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        //Log.i("MainActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

                    }
                    if(!gps.canGetLocation() || latitude == 0) {
                        return "NoGPS";
                    }*/

                    ParseUtils.subscribeWithEmail(u.getName());
                    int count = 0, breakNow = 0;

                    Geocoder coder = new Geocoder(MainActivity.this);
                    while (count < 5 && (breakNow == 0)) {
                        try {
                            count++;
                            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocation(latitude, longitude, 1);
                            if (adresses == null)
                                continue;
                            for (Address add : adresses) {
                                //longitude = add.getLongitude();
                                //latitude = add.getLatitude();
                                landMark = add.getAddressLine(0);
                                city = add.getLocality();
                                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putFloat("latitude", (float)latitude);
                                editor.putFloat("longitude", (float) longitude);
                                editor.putString("landmark", landMark);
                                editor.putString("city", city);

                                editor.commit();
                                breakNow = 1;
                                break;
                            }
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    }// end of while

                    if (count == 5) {
                        //Log.i("", "Not able to find your location in asynce task");
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(MainActivity.this, "Couldn't find your location. " +
                                        "Please check your Internet Connectivity.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {

                        if (latitude != 0.0 && longitude != 0.0) {
                            String param = u.getName() + "#" + u.getBloodgroup() + "#" + u.getMobilenumber() + "#" + landMark + "#" + city + "#" + latitude + "#" + longitude;

                            // Finding the remaining notification count
                            int notif_row_count = db.getNotificationRowsCount();
                            int rem_notif_count = 10, rec_notif_count = 0, max_notif_count = 10;
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            max_notif_count = pref.getInt("notif_count", 10);
                            if(notif_row_count == 0){
                                rem_notif_count = pref.getInt("notif_count", 10);
                                rec_notif_count = 0;
                            }
                            else{
                                Date date = new Date();
                                String DATE_FORMAT_NOW = "yyyy-MM-dd";
                                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                                String stringDate = sdf.format(date );

                                List<NotificationRow> notifList = db.getAllNotificationRows();
                                for(NotificationRow notif : notifList){
                                    if(stringDate.equals(notif.getDate())){ // if already entry exists for today
                                        rec_notif_count = notif.getCount();

                                        rem_notif_count = max_notif_count - rec_notif_count;
                                        break;
                                    }
                                    else{
                                        rem_notif_count = pref.getInt("notif_count", 10);
                                        rec_notif_count = 0;
                                    }
                                }
                            }




                            //TransferManager transferManager = new TransferManager(credentialsProvider);
                            //Log.i("String in Async Task: ", param);
                            String[] parts = param.split("#");
                            if(parts.length < 7)
                                return null;
                            String username = parts[0]; String bloodgroup = parts[1]; String landmark = parts[3];
                            String city = parts[4]; String lat = parts[5]; String lng = parts[6];


                            //Check if this user and bllod group combination exists -- with exponential backoff
                            int retry = 1, count1 = 3;
                            while (retry ==1 && count1 >0) {
                                retry = 0; count1--;
                                try {
                                    Userddb user = mapper.load(Userddb.class, bloodgroup, username);
                                    if (user != null) {
                                        //user.setDate(stringDate);
                                        user.setLandmark(landmark);
                                        user.setCity(city);
                                        user.setLocation(lat + "#" + lng);
                                        user.setRemainingNotifications(String.valueOf(rem_notif_count));
                                        mapper.save(user);
                                        //Log.i("User: ", user.getUsername() + "," + user.getLandmark());
                                        return user.getUsername(); // USer already exists. choose different username
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










                            //dynamoDBUserLocationUpdate asyncTask = new dynamoDBUserLocationUpdate();
                            //asyncTask.execute(param);
                        }
                    }

                    break;
                }
            }






            return null;


        }


        @Override
        protected void onPostExecute(String s) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            //super.onPostExecute(v);
            /*if(s != null) {
                Intent activityChangeIntent = new Intent(MainActivity.this, RegisteredActivity.class);
                activityChangeIntent.putExtra("name", s);
                MainActivity.this.startActivity(activityChangeIntent);
            }
            else*/
                super.onPostExecute(s);

            //Log.i("Main Activity: ", "onPostExecute"+ s);
            if(dialog != null){
                dialog.dismiss();
            }
                registerButton.setEnabled(true);
                requestButton.setEnabled(true);


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // check if GPS enabled


            /*if (gps.canGetLocation()) {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                //Log.i("MainActivity,Loc: ", String.valueOf(latitude) + String.valueOf(longitude));

            }
            if(!gps.canGetLocation() || latitude == 0) {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                Toast.makeText(MainActivity.this, "Couldn't find your location. " +
                        "Please turn on GPS.", Toast.LENGTH_SHORT).show();
            }*/

            //Log.i("Main Activity: ", "in on resume PreExecute");
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Finding your Location...");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
        }
    }



    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location loc) {
            //your code here
            /*if(loc != null){
                Log.i("GPSTracker:", "onLocationChanged - loc not null"+String.valueOf(loc.getLatitude()));
                location = loc;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }*/
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {

                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    getLocationAndStartTask();
                }
                else{
                    Toast.makeText(MainActivity.this, "Sorry. Please grant permission for accessing location.", Toast.LENGTH_LONG)
                            .show();
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    /*public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Turn on GPS");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Please enable GPS");

        // On pressing Settings button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });



        // Showing Alert Message
        alertDialog.show();
    }

*/
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isSettingsALertShown = false;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MainActivity.this.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isSettingsALertShown = false;
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    /**
     *Get location and if we can get location start async task to update location
     */
    public void getLocationAndStartTask(){

        try {
            // getting GPS status
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled

                if(!isSettingsALertShown) {
                    isSettingsALertShown = true;
                    showSettingsAlert();
                }

            } else {

                if (isNetworkEnabled) {
                    //Log.i("GPSTracker:", "network enabled");

                    //Log.i("GPSTracker:", "Permission- "+String.valueOf(hasLocationPermission));
                    //Log.i("GPSTracker:", "Permission- "+String.valueOf(hasLocationPermission));
                    if ( Build.VERSION.SDK_INT >= 23) {
                        int hasLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                        //Log.i("GPSTracker:", "Location Permission- "+String.valueOf(hasLocationPermission));
                        if(hasLocationPermission != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                            hasRequestedPermission = true;

                        }
                        else{
                            //Log.i("GPS Tracker:", "Permissions granted");
                            mLocationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                            //Log.d("Network", "Network");
                            if (mLocationManager != null) {
                                location = mLocationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }

                    }
                    else {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                        //Log.d("Network", "Network");
                        if (mLocationManager != null) {
                            location = mLocationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    //Log.i("GPSTracker: ", "Network provider, "+String.valueOf(latitude)+", "+String.valueOf(longitude));
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    //Log.i("GPSTracker: ", "GPS enabled");

                    int hasLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                    //int hasInternetPermission = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.INTERNET);
                    //Log.i("GPSTracker:", "Permission- "+String.valueOf(hasLocationPermission));
                    //Log.i("GPS Tracker:", "Intenet Permission -"+String.valueOf(hasInternetPermission) );
                    if (location == null) {
                        if ( Build.VERSION.SDK_INT >= 23) {
                            if(hasLocationPermission != PackageManager.PERMISSION_GRANTED){
                                if(!hasRequestedPermission) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                                }

                            }
                            else{
                                mLocationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                                //Log.d("GPS Enabled", "GPS Enabled");
                                if (mLocationManager != null) {
                                    location = mLocationManager
                                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if (location != null) {
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                    }
                                }
                            }

                        }
                        else {
                            mLocationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                            //Log.d("GPS Enabled", "GPS Enabled");
                            if (mLocationManager != null) {
                                location = mLocationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                        //Log.i("GPSTracker: ", "GPS provider, "+String.valueOf(latitude)+", "+String.valueOf(longitude));
                    }
                }
            }
        } catch(Exception e){

        }

        if(latitude == 0){
            Toast.makeText(MainActivity.this, "Please turn on GPS and check Internet connectivity.",
                    Toast.LENGTH_LONG).show();
        }
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        float lat_pref = pref.getFloat("latitude", 0);

        if(lat_pref == 0 || (latitude != 0 && (latitude - lat_pref >0.01 || lat_pref-latitude > 0.01))) {
         // if latitude is not stored in preferences , find latitude and longitude
            // or if diff between current latitude and lat_pref is greater then update.


            if (latitude != 0) {
                dynamoDBUserLocationUpdate asyncTask = new dynamoDBUserLocationUpdate();
                asyncTask.execute("");

            }
        }
        if(lat_pref != 0){
            registerButton.setEnabled(true);
            requestButton.setEnabled(true);
        }

    }// end of getLocationAndStartTask


    @Override
    protected void onStop() {
        super.onStop();
        if(mLocationManager != null){

            if ( Build.VERSION.SDK_INT >= 23) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.removeUpdates(mLocationListener);
                }
            }
            else{
                mLocationManager.removeUpdates(mLocationListener);
            }
        }


    }
}
