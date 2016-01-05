package de.beuth.bva.viciberlin.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.beuth.bva.viciberlin.R;
import de.beuth.bva.viciberlin.util.Constants;
import de.beuth.bva.viciberlin.util.UserLoginControl;

public class LogoutActivity extends AppCompatActivity {

    private static final String TAG = "LogoutActivity";

    @Bind(R.id.logout_button) Button logoutButton;
    @Bind(R.id.logged_in_as_textview) TextView loggedInAsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        displayUserName();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSharedValues();
                goToLoginAcitivty();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goToHomeAcitivty();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        goToHomeAcitivty();
    }

    private void displayUserName(){
        String twitterUserName = UserLoginControl.getUserName(this);
        if (twitterUserName != null){
            loggedInAsTextView.setText(loggedInAsTextView.getText() + twitterUserName);
        } else {
            goToLoginAcitivty();
        }
    }

    private void deleteSharedValues(){
        UserLoginControl.deleteUserDataFromSharedPrefs(this);
    }

    private void goToHomeAcitivty(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void goToLoginAcitivty(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}
