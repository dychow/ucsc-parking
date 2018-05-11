package com.example.daniel.ucscparking;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Daniel on 5/4/18.
 */

public class DetailedListActivity extends AppCompatActivity{

        //    private UserLoginTask mAuthTask = null;
        private static final String TAG = "StatusActivity";


        // UI references.
        private TextView mLotView;
        private TextView mSpotView;
        private TextView mTimeView;
        private TextView mValidityView;

        private FirebaseAuth mAuth;

        private String name;
        private String firstName;
        private String lastName;

//    private static class accountParams {
//        String acc_firstName;
//        String acc_lastName;
//        String acc_loginId;
//        String acc_password;
//
//        accountParams(String firstName, String lastName, String loginId, String password) {
//            this.acc_firstName = firstName;
//            this.acc_lastName = lastName;
//            this.acc_loginId = loginId;
//            this.acc_password = password;
//        }
//    }
//
//    private class CreateAccount extends AsyncTask<accountParams, Void, Void>{
//
//        @Override
//        protected Void doInBackground(accountParams... params) {
//            // TODO Auto-generated method stub
//
//            System.out.println("Inside doInBackground");
//
//            String editUserUrl = "https://cmpe-123a-18-g11.appspot.com/edit-user?";
//            try {
//
//                String final_str = editUserUrl + "message+type=user+register&";
//                final_str = final_str + "first+name=" + params[0].acc_firstName + "&";
//                final_str = final_str + "last+name=" + params[0].acc_lastName + "&";
//                final_str = final_str + "user+email=" + params[0].acc_loginId + "&";
//                final_str = final_str + "user+pwd=" + params[0].acc_password;
//
//
//                URL link = new URL(final_str);
//                URLConnection con = link.openConnection();
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(
//                                con.getInputStream()
//                        )
//                );
//
//                String inputLine;
//
//                while((inputLine = in.readLine()) != null)
//                    System.out.println(inputLine);
//                in.close();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void res) {
//
//            System.out.println("Inside onPostExecute");
//
//        }
//
//
//    }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_status);

            mLotView = (TextView) findViewById(R.id.current_status);;
            mSpotView = (TextView) findViewById(R.id.parked_spot);;
            mTimeView = (TextView) findViewById(R.id.time_left);;
            mValidityView = (TextView) findViewById(R.id.validity);;

//        mVerifyParking = (Button) findViewById(R.id.scan_spots);

            mLotView.setText(R.string.not_parked_status);
            mTimeView.setText(R.string.time_remaining);
            mValidityView.setText(R.string.invalid_parking);

            mSpotView.setVisibility(View.GONE);
            mTimeView.setVisibility(View.GONE);
            mValidityView.setVisibility(View.GONE);

//        mVerifyParking.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                scanSpots();
//            }
//        });

        }

        private void scanSpots(){
            Log.d(TAG, "scanSpots");

            mSpotView.setVisibility(View.VISIBLE);
            mTimeView.setVisibility(View.VISIBLE);
            mValidityView.setVisibility(View.VISIBLE);

        }

    }
