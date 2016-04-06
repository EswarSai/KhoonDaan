package psai.develop.khoondaan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ProfileScreen extends AppCompatActivity {

    //private Toolbar mToolbar;
    String s, username, bloodgroup, mobilenumber;
    Button requestButton, editAndSubmitButton;

    TextView tvUsername, tvBloodgroup, tvMobilenumber, tvMobilevalue;
    EditText etMobilevalue;
    LinearLayout layout;

    int editAndNotSubmit = 1;

    DatabaseHandler db;
    public CognitoCachingCredentialsProvider credentialsProvider;

    public AmazonDynamoDBClient ddbClient;
    public DynamoDBMapper mapper;

  //  public FragmentPagerAdapter mCustomPagerAdapter;
   // public ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        db = new DatabaseHandler(this);

        layout = (LinearLayout) findViewById(R.id.PLLayout);

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

        Intent intent = getIntent();
        s = intent.getStringExtra("name");
        String[] parts = s.split("#");
        if(parts.length < 3)
            return;
        username = parts[0]; bloodgroup = parts[1]; mobilenumber = parts[2];


        tvUsername = (TextView) layout.findViewById(R.id.tvUserName);
        tvUsername.setText(Html.fromHtml("<b>Email Id: </b>" + username));
        tvBloodgroup = (TextView) layout.findViewById(R.id.tvBloodGroup);
        tvBloodgroup.setText(Html.fromHtml("<b>BloodGroup: </b>"+ bloodgroup));
        tvMobilenumber = (TextView) layout.findViewById(R.id.tvMobileNumber);
        tvMobilenumber.setText(Html.fromHtml("<b>Phone: </b>"));

        tvMobilevalue = (TextView) layout.findViewById(R.id.text_view_mobile);
        tvMobilevalue.setText(mobilenumber);
        etMobilevalue = (EditText) layout.findViewById(R.id.hidden_edit_view);


        requestButton = (Button) layout.findViewById(R.id.requestButton);
        requestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                Intent activityChangeIntent = new Intent(ProfileScreen.this, RequestActivity.class);

                ProfileScreen.this.startActivity(activityChangeIntent);
            }
        });

        editAndSubmitButton = (Button) layout.findViewById(R.id.editButton);
        editAndSubmitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(editAndNotSubmit == 1){
                    editAndNotSubmit = 0;
                    tvMobilevalue.setVisibility(View.GONE);
                    etMobilevalue.setVisibility(View.VISIBLE);
                    etMobilevalue.setHint(mobilenumber);
                    editAndSubmitButton.setText("SUBMIT");
                }
                else{
                    mobilenumber = etMobilevalue.getText().toString();
                    if(TextUtils.isEmpty(mobilenumber)) {
                            etMobilevalue.setError("Please enter 10 digit Mobile Number");
                            return;

                    }
                    else{

                        if(mobilenumber.matches("[0-9]{10}")){
                            etMobilevalue.setError(null);
                            editAndNotSubmit = 1;
                            dynamoDBUserNumberUpdate asyncTask = new dynamoDBUserNumberUpdate(ProfileScreen.this);
                            asyncTask.execute(username+"#"+bloodgroup+"#"+mobilenumber);
                        }
                        else{
                            etMobilevalue.setError("Please enter valid 10 digit Mobile Number");
                            //editAndSubmitButton.setText("EDIT");
                            return;
                        }
                    }

                }

            }
        });


/*        mCustomPagerAdapter = new FragmentPagerAdapter();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);
*/


        List<Entry> entryList = new ArrayList<Entry>();
        entryList = db.getAllEntries();
        int count = 0;
        Collections.reverse(entryList);

        for(Entry e : entryList) {

            if(count < 50){
                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView tv=new TextView(ProfileScreen.this);
                tv.setLayoutParams(lparams);
                //String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(e.getDate());
                if(e.getHelperEmail().equals("")){
                    tv.setText(Html.fromHtml("&#8226; You requested " + e.getBloodgroup() +" blood on "+e.getDate()));
                    //count++;
                }
                else{
                    String email = e.getHelperEmail();
                    int lastAt = email.lastIndexOf('@');
                    if(lastAt!=-1){
                        email = email.substring(0, lastAt);
                    }

                    tv.setText(Html.fromHtml("&#8226; You received help from " + email +" on "+ e.getDate()));
                }
                this.layout.addView(tv);

                // Drawing a line
                View v = new View(this);
                v.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                ));
                v.setBackgroundColor(Color.parseColor("#000000"));

                this.layout.addView(v);

            }
            count++;
        }
    }


    // Update the location of user

    private class dynamoDBUserNumberUpdate extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        public dynamoDBUserNumberUpdate(ProfileScreen activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected String doInBackground(String... params) {


            //TransferManager transferManager = new TransferManager(credentialsProvider);
            //Log.i("String in Async Task: ", params[0]);
            String[] parts = params[0].split("#");
            if (parts.length < 3)
                return null;
            String username = parts[0];
            String bloodgroup = parts[1];
            String mobilenumber = parts[2];


            //Check if this user and bllod group combination exists -- with exponential backoff
            int retry = 1, count = 3;
            while (retry == 1 && count > 0) {
                retry = 0;
                count--;
                try {
                    Userddb user = mapper.load(Userddb.class, bloodgroup, username);
                    if (user != null) {
                        //user.setDate(stringDate);
                        user.setMobilenumber(mobilenumber);
                        mapper.save(user);
                       // Log.i("User: ", user.getUsername() + "," + user.getLandmark());
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

            if (retry == 1 || count <= 0) {

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

            if(s != null){
                Toast.makeText(ProfileScreen.this, "Mobile number is updated!", Toast.LENGTH_LONG).show();
                editAndSubmitButton.setText("EDIT");
                tvMobilevalue.setVisibility(View.VISIBLE);
                etMobilevalue.setVisibility(View.GONE);
                tvMobilevalue.setText(mobilenumber);
            }
            else{
                Toast.makeText(ProfileScreen.this, "Error. Please check Internet Connectivity.", Toast.LENGTH_LONG).show();
            }
                super.onPostExecute(s);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait. Updating...");
            dialog.show();

        }
    }


    /*public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter
    {
        final int PAGE_COUNT = 2;

        private final String[] PAGE_TITLES =
                {
                        "Requested",
                        "Received"
                };

        public FragmentPagerAdapter()
        {
            super(getSupportFragmentManager());
        }

        @Override
        public int getCount()
        {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return PAGE_TITLES[position];
        }

        @Override
        public Fragment getItem(int position)
        {
            switch(position)
            {
                case 0:
                    DummyFragment fragment = new DummyFragment();
                    LinearLayout lLayout = (LinearLayout)findViewById(R.id.list_result);

                    List<Entry> entryList = new ArrayList<Entry>();
                    entryList = db.getAllEntries();
                    int count = 0;
                    Collections.reverse(entryList);

                    for(Entry e : entryList) {

                        if(count < 20){
                            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            TextView tv=new TextView(ProfileScreen.this);
                            tv.setLayoutParams(lparams);
                            if(e.getHelperEmail().equals("")){
                                tv.setText(Html.fromHtml("&#8226; You requested " + e.getBloodgroup() +" blood on "+e.getDate()));
                                count++;
                            }
                            lLayout.addView(tv);
                        }
                        //count++;
                    }
                    return fragment;
                case 1:
                    DummyFragment fragment1 = new DummyFragment();
                    LinearLayout lLayout1 = (LinearLayout)findViewById(R.id.list_result);

                    List<Entry> entryList1 = new ArrayList<Entry>();
                    entryList = db.getAllEntries();
                    int count1 = 0;
                    Collections.reverse(entryList);

                    for(Entry e : entryList) {

                        if(count1 < 20){
                            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            TextView tv=new TextView(ProfileScreen.this);
                            tv.setLayoutParams(lparams);
                            if(e.getHelperEmail().equals("")){
                                //tv.setText(Html.fromHtml("&#8226; You requested " + e.getBloodgroup() +" blood on "+e.getDate()));
                            }
                            else{
                                String email = e.getHelperEmail();
                                int lastAt = email.lastIndexOf('@');
                                if(lastAt!=-1){
                                    email = email.substring(0, lastAt);
                                }
                                tv.setText(Html.fromHtml("&#8226; You received help from " + email +"on "+e.getDate()));
                                count1++;
                            }
                            lLayout1.addView(tv);
                        }
                        //count1++;
                    }
                    return fragment1;
                default:
                    return null;
            }

        }
    }*/
}
