package com.example.daniel.ucscparking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Daniel on 4/29/18.
 */

public class StatusActivity extends AppCompatActivity {

    private static final String TAG = "StatusActivity";

    // UI references.
    private TextView mStatusView;
    private TextView mSpotView;
    private TextView mTimeView;
    private TextView mValidityView;

    private Button mVerifyParking;
    private Button mCancelParking;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Instantiate views and buttons
        mStatusView = (TextView) findViewById(R.id.current_status);
        mSpotView = (TextView) findViewById(R.id.parked_spot);
        mTimeView = (TextView) findViewById(R.id.time_left);
        mValidityView = (TextView) findViewById(R.id.validity);

        mVerifyParking = (Button) findViewById(R.id.verify_spot);
        mCancelParking = (Button) findViewById(R.id.cancel_spot);

        // Hide the Time view
        mTimeView.setVisibility(View.GONE);

        // Get the current parking status of the user and display it
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String validity = sharedPref.getString("parking status", "");
        if(validity == "legal"){
            mStatusView.setText("You are currently parked in spot: ");

            mSpotView.setText(sharedPref.getString("spot", ""));
            mSpotView.setVisibility(View.VISIBLE);

            mValidityView.setText("Valid");
            mValidityView.setVisibility(View.VISIBLE);

            mCancelParking.setVisibility(View.VISIBLE);
            mVerifyParking.setVisibility(View.GONE);
        } else if (validity == "illegal") {
            mStatusView.setText("You are currently parked in spot: ");

            mSpotView.setText(sharedPref.getString("spot", ""));
            mSpotView.setVisibility(View.VISIBLE);

            mValidityView.setText("Invalid");
            mValidityView.setVisibility(View.VISIBLE);

            mCancelParking.setVisibility(View.VISIBLE);
            mVerifyParking.setVisibility(View.GONE);
        } else {
            mStatusView.setText("You are currently not parked");

            mSpotView.setText("");
            mSpotView.setVisibility(View.GONE);

            mValidityView.setText("Invalid");
            mValidityView.setVisibility(View.GONE);

            mCancelParking.setVisibility(View.GONE);
            mVerifyParking.setVisibility(View.VISIBLE);

        }

        mVerifyParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySpot();
            }
        });

        mCancelParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSpot();
            }
        });

    }

    // Set text when user is legally, illegally, or not parked
    private void verifySpot(){
        Log.d(TAG, "verifySpot");
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String validity = sharedPref.getString("parking status", "");
        if(validity == "legal"){
            mStatusView.setText("You are currently parked in spot: ");

            mSpotView.setText(sharedPref.getString("spot", ""));
            mSpotView.setVisibility(View.VISIBLE);

            mValidityView.setText("Valid");
            mValidityView.setVisibility(View.VISIBLE);

            mCancelParking.setVisibility(View.VISIBLE);
            mVerifyParking.setVisibility(View.GONE);
        } else if (validity == "illegal") {
            mStatusView.setText("You are currently parked in spot: ");

            mSpotView.setText(sharedPref.getString("spot", ""));
            mSpotView.setVisibility(View.VISIBLE);

            mValidityView.setText("Invalid");
            mValidityView.setVisibility(View.VISIBLE);

            mCancelParking.setVisibility(View.VISIBLE);
            mVerifyParking.setVisibility(View.GONE);
        } else {
            mStatusView.setText("You are currently not parked");

            mSpotView.setText("");
            mSpotView.setVisibility(View.GONE);

            mValidityView.setText("Invalid");
            mValidityView.setVisibility(View.GONE);

        }

    }

    // User resets the display back to the default "not parked"
    private void cancelSpot() {
        Log.d(TAG, "cancelSpot");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("parking status", "");
        editor.putString("spot", "");
        editor.apply();

        mStatusView.setText("You are currently not parked");

        mSpotView.setText("");
        mSpotView.setVisibility(View.GONE);

        mValidityView.setText("Invalid");
        mValidityView.setVisibility(View.GONE);
    }


}
