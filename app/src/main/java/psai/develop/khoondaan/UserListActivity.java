package psai.develop.khoondaan;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
import android.telephony.SmsManager;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
//import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class UserListActivity extends ActionBarActivity implements LocationListener, GoogleMap.OnMarkerClickListener{

    ArrayList<String> userlist, userlistnew;
    ArrayList<UserHeap> userHeap, userHeapnew;

    double ulatitude, ulongitude;

    String requesterinfo, requesteremailid, requesterbloodgroup;


    String mobilenumbermarker;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 70;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 71;

    GoogleMap googleMap;
    MarkerOptions markerOptions;
    LocationManager locationManager;

    DatabaseHandler db;
    boolean isGPSEnabled = false, isNetworkEnabled = false;
    Location location;
    /*ListView listView;
    ArrayAdapter arrayAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
*/
    @Override
    public boolean onMarkerClick(Marker marker) {
        final String title = marker.getTitle();
        AlertDialog.Builder builder = new AlertDialog.Builder(UserListActivity.this);
        builder.setMessage("Would you like to send a message?");
        builder.setCancelable(true);

        int hasSMSPermission = ContextCompat.checkSelfPermission(UserListActivity.this, Manifest.permission.SEND_SMS);
        if ( Build.VERSION.SDK_INT >= 23) {

            if(hasSMSPermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(UserListActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

            }
        }
        final EditText input = new EditText(UserListActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setHint("Enter your message here");
        String msg = requesteremailid;
        if(msg != null){
            int index = msg.indexOf("@");
            if(index != -1){
                msg = msg.substring(0, index);
            }
        }
        input.setText("Hi! This is "+msg+". I need "+requesterbloodgroup+ " Blood urgently. Can you please help?");
        builder.setView(input);

        builder.setPositiveButton("Send Message", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String [] parts = title.split(",");
                        if(parts.length == 3){
                            String mobilenum = "+91"+mobilenumbermarker;
                            String sms = input.getText().toString();
                            int hasSMSPermissionHere = ContextCompat.checkSelfPermission(UserListActivity.this, Manifest.permission.SEND_SMS);
                            if(hasSMSPermissionHere == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    PendingIntent sentPI;
                                    String SENT = "SMS_SENT";

                                    sentPI = PendingIntent.getBroadcast(UserListActivity.this, 0, new Intent(SENT), 0);
                                    smsManager.sendTextMessage(mobilenum, null, sms, sentPI, null);
                                    Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_LONG).show();
                                    // Log.i("UserListActivity:", "Message is: " + sms);
                                    //Log.i("UserListActivity:", "Mobile num is: "+ mobilenum);

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "SMS failed, please try again later!", Toast.LENGTH_LONG).show();
                                    //e.printStackTrace();
                                }
                            }

                        }
                    }
                }
        );
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        db = new DatabaseHandler(this);

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.clear();
        googleMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(isGPSEnabled) {
            location = null;
            if ( Build.VERSION.SDK_INT >= 23) {
                int hasLocationPermission = ContextCompat.checkSelfPermission(UserListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                if ( Build.VERSION.SDK_INT >= 23) {

                    if(hasLocationPermission != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(UserListActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                    }
                    else{
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }

            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if(location != null) {
                onLocationChanged(location);
            }
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }

        Intent i = getIntent();
        ulatitude = i.getDoubleExtra("latitude", 0);
        ulongitude = i.getDoubleExtra("longitude", 0);
        requesterinfo = i.getStringExtra("requester");
        userlist = i.getStringArrayListExtra("Results");

        String [] requester = requesterinfo.split("#");
        if(requester.length == 7){
            requesteremailid = requester[0];
            requesterbloodgroup = requester[1];
        }

        userlistnew = new ArrayList<String>();
        userHeap = new ArrayList<UserHeap>();
        for(String s: userlist){
            String [] parts = s.split("#");
            if(parts.length == 9) {
                String n_str = "Email id: " + parts[0] + "<br>" + "<b>Blood Group: " + parts[1]+"<br>"+"Mobile Number: "+parts[2]+"</b><br>"
                        +"LandMark: "+parts[3]+"<br>"+"City: "+parts[4];
                userlistnew.add(n_str);
                UserHeap u = new UserHeap(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
                // To find the difference in dates - should be greater than 15 days to send a request to the user
                Date date = new Date();
                Date datenew, dateold;
                String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                String stringDate = sdf.format(date );

                int diff = 0;
                if(parts[7] != null && !parts[7].equals("0")){
                    try {
                        datenew = sdf.parse(stringDate);
                        dateold = sdf.parse(parts[7]);
                        diff = (int) ((datenew.getTime() - dateold.getTime())/(1000*60*60*24));
                    } catch(Exception e){
                        //handle exception
                    }

                }
                int rem_notif_count = 1;
                if(parts[8] != null && !parts[8].equalsIgnoreCase("null")){
                    rem_notif_count = Integer.parseInt(parts[8]);
                }
                if((parts[7].equals("0") || diff >= 14) && rem_notif_count > 0 ) {
                    userHeap.add(u);
                }
            }
        }

        // create a heap of size 30 from userHeap
        userHeapnew = createHeap(userHeap);
        if(userHeapnew != null) {
            userlistnew.clear();
            // adding entry to Database
            Date date = new Date();
            String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            String stringDate = sdf.format(date );
            Entry entry = new Entry();
            entry.setBloodgroup(requesterbloodgroup);
            entry.setDate(stringDate);
            entry.setHelperEmail("");
            db.addEntry(entry);

            for(UserHeap u : userHeapnew) {
                char [] emailArray = u.getEmailId().toCharArray();
                for(int j = 0; j < emailArray.length; j++){
                    if(j%2 == 1){
                        emailArray[j] = 'x';
                    }
                }
                String emailid = new String(emailArray);

                mobilenumbermarker = u.getMobilenumber();
                char[] charArray = u.getMobilenumber().toCharArray();
                charArray[3]=charArray[4]=charArray[5]=charArray[6]= 'x';
                String mobilenumber = new String(charArray);
                String n_str = "Email id: " + u.getEmailId() + "<br>" + "<b>Blood Group: " + u.getBloodgroup() + "<br>" +
                        "Mobile Number: " + mobilenumber + "</b><br>" + "LandMark: " + u.getLandmark() + "<br>"
                        + "City: " + u.getCity();
               // Log.i("UserListActivity: ", n_str);
                String m_str = emailid+", "+u.getBloodgroup()+", "+mobilenumber;
                if(requesteremailid != null && !requesteremailid.equals(u.getEmailId())) {
                    userlistnew.add(n_str);

                    String u_str = "\nMYEMAILID:" + u.getEmailId() + "\nMYBLOODGROUP:" + u.getBloodgroup() + "\nMYNUMBER:" + u.getMobilenumber()
                            + "\nMYLANDMARK:" + u.getLandmark() + "," + u.getCity();
                    // Sending push notification to each user , adding user email id also in msg
                    ParseUtils.pushNotification("NEEDBLOOD\n" + requesterinfo.replace("#", "\n") + u_str, u.getEmailId());

                    LatLng ll = new LatLng(Double.parseDouble(u.getLatitude()), Double.parseDouble(u.getLongitude()));
                    markerOptions = new MarkerOptions();
                    markerOptions.position(ll);
                    markerOptions.title(m_str);
                    googleMap.addMarker(markerOptions);
                }
            }
        }

/*
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        // specify an adapter (see also next example)
        if(userlistnew != null){
            String []data = new String[userlistnew.size()];
            userlistnew.toArray(data);
            mAdapter = new MyAdapter(userlistnew);
            //Log.i("UserList:", data[0]);
        }
        else {
            String []data = new String[userlist.size()];
            userlist.toArray(data);
            mAdapter = new MyAdapter(userlist);
        }

        mRecyclerView.setAdapter(mAdapter);

        */


        /*listView = (ListView) findViewById(R.id.listview);

        if(userlistnew != null) {
            arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.textviewitem, userlistnew);
        }
        else
        {
            arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.textviewitem, userlist);
        }
        listView.setAdapter(arrayAdapter);*/

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {

                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){


                }
                else{
                    Toast.makeText(UserListActivity.this, "Sorry. Please grant permission for Sending SMS.", Toast.LENGTH_LONG)
                            .show();
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(locationManager != null && ContextCompat.checkSelfPermission(UserListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
                else{
                    Toast.makeText(UserListActivity.this, "Sorry. Please grant permission for Accessing location.", Toast.LENGTH_LONG)
                            .show();
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(locationManager != null){

            if ( Build.VERSION.SDK_INT >= 23) {
                if(ContextCompat.checkSelfPermission(UserListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.removeUpdates(this);
                }
            }
            else{
                locationManager.removeUpdates(this);
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
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

        return super.onOptionsItemSelected(item);
    }

    public ArrayList<UserHeap> createHeap(ArrayList<UserHeap> list) {

        ArrayList<UserHeap> result;
        if(list.size() <= 30) {
            result = new ArrayList<UserHeap>(list);
            sort(result);
            //   return result;
        }
        else{
            if(ulatitude == 0 && ulongitude == 0) {
                result = new ArrayList<UserHeap>(list.subList(0, 30));
                sort(result);
            }
            else {
                result = new ArrayList<UserHeap>(list.subList(0, 30));
                sort(result);
                // for each element from 31 to end compare with last element and according add it and sort list or ignore it
                for(int i = 30; i < list.size(); i++) {
                    double dist1 = calculateDistance(ulatitude, ulongitude, Double.parseDouble(result.get(result.size() - 1).getLatitude()),
                            Double.parseDouble(result.get(result.size() - 1).getLongitude()));
                    double dist2 = calculateDistance(ulatitude, ulongitude, Double.parseDouble(list.get(i).getLatitude()),
                            Double.parseDouble(list.get(i).getLongitude()));
                    // add element from list to rsult by replacing last element in result
                    if(dist2 < dist1) {
                        result.set((result.size()-1), list.get(i));
                        sort(result);
                    }
                }// end of for
            }
        }

        return result;
    }

    public void sort(ArrayList<UserHeap> list) {
        //Sorting
        Collections.sort(list, new Comparator<UserHeap>() {
            @Override
            public int compare(UserHeap user1, UserHeap user2) {

                double distance1, distance2;
                distance1 = calculateDistance(ulatitude, ulongitude, Double.parseDouble(user1.getLatitude()),
                        Double.parseDouble(user1.getLongitude()));

                distance2 = calculateDistance(ulatitude, ulongitude, Double.parseDouble(user2.getLatitude()),
                        Double.parseDouble(user2.getLongitude()));

                return Double.compare(distance1, distance2);
            }
        });
    }

    public double calculateDistance(double latA, double lngA, double latB, double lngB) {
        Location locationA = new Location("point A");
        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);
        Location locationB = new Location("point B");
        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);
        double distance = locationA.distanceTo(locationB) ;
        return distance;
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onLocationChanged(Location locat) {

        double latitude = locat.getLatitude();
        double longitude = locat.getLongitude();

        LatLng loc = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    @Override
    public void onProviderEnabled(String provider) {

    }
}
