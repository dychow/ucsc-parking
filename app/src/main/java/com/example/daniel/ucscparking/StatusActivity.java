package com.example.daniel.ucscparking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        sharedPref = getApplicationContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        // Get the current parking status of the user and display it
        verifySpot();

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
        String validity = sharedPref.getString("parking status", "");
        System.out.println("Validity is: " + validity);

        if(validity.equals("legal")){
            mStatusView.setText(R.string.parked_status);

            mSpotView.setText(sharedPref.getString("spot", ""));
            mSpotView.setVisibility(View.VISIBLE);

            mValidityView.setText(R.string.valid_parking);
            mValidityView.setVisibility(View.VISIBLE);

            mCancelParking.setVisibility(View.VISIBLE);
            mVerifyParking.setVisibility(View.GONE);
        } else if (validity.equals("illegal")) {
            mStatusView.setText(R.string.parked_status);

            mSpotView.setText(sharedPref.getString("spot", ""));
            mSpotView.setVisibility(View.VISIBLE);

            mValidityView.setText(R.string.invalid_parking);
            mValidityView.setVisibility(View.VISIBLE);

            mCancelParking.setVisibility(View.VISIBLE);
            mVerifyParking.setVisibility(View.GONE);
        } else if (validity.equals("spot already claimed")) {
            mStatusView.setText("Someone has already claimed spot: ");

            mSpotView.setText(sharedPref.getString("spot", ""));
            mSpotView.setVisibility(View.VISIBLE);

            mValidityView.setText(R.string.invalid_parking);
            mValidityView.setVisibility(View.VISIBLE);

            mCancelParking.setVisibility(View.VISIBLE);
            mVerifyParking.setVisibility(View.GONE);
        } else {
            mStatusView.setText(R.string.not_parked_status);

            mSpotView.setText("");
            mSpotView.setVisibility(View.GONE);

            mValidityView.setText(R.string.invalid_parking);
            mCancelParking.setVisibility(View.GONE);
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

        mStatusView.setText(R.string.not_parked_status);

        mSpotView.setText("");
        mSpotView.setVisibility(View.GONE);

        mValidityView.setText(R.string.invalid_parking);
        mValidityView.setVisibility(View.GONE);

        mCancelParking.setVisibility(View.GONE);
        mVerifyParking.setVisibility(View.VISIBLE);
    }

}
