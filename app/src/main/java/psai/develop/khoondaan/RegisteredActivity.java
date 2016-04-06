package psai.develop.khoondaan;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class RegisteredActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    TextView tvRegistered;
    String userName;
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new DatabaseHandler(this);
        Intent i = getIntent();
        userName = i.getStringExtra("name");

        int lastAt = userName.lastIndexOf('@');
        if(lastAt != -1){
            userName = userName.substring(0, lastAt);
        }

        tvRegistered = (TextView) findViewById(R.id.tvRegistered);
        tvRegistered.setText("Welcome back "+userName+"! You are already registered.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registered, menu);
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
                Intent activityChangeIntent = new Intent(RegisteredActivity.this, LoginActivity.class);

                activityChangeIntent.putExtra("user", "");
                RegisteredActivity.this.startActivity(activityChangeIntent);
            }
            else {
                List<User> userList = new ArrayList<User>();
                userList = db.getAllUsers();
                for (User u : userList) {

                    String s;
                    s = u.getName()+"#"+u.getBloodgroup()+"#"+u.getMobilenumber();
                    Intent activityChangeIntent = new Intent(RegisteredActivity.this, ProfileScreen.class);
                    activityChangeIntent.putExtra("name", s);
                    RegisteredActivity.this.startActivity(activityChangeIntent);
                    break;
                }
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
