package com.example.daniel.ucscparking;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


/**
 * A login screen that offers login via email/password.
 */
public class ProfileActivity extends AppCompatActivity {

    //    private UserLoginTask mAuthTask = null;
    private static final String TAG = "ProfileActivity";


    // UI references.
    private TextView mEmailView;
    private TextView mFirstNameView;
    private TextView mLastNameView;
    private EditText mEmailEdit;
    private EditText mFirstNameEdit;
    private EditText mLastNameEdit;

    private Button mEditProfile;
    private Button mSaveProfile;
    private Button mCancel;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        name = user.getDisplayName();
        firstName = name.split(" ")[0];
        lastName = name.split(" ")[1];

        mEmailView = (TextView) findViewById(R.id.email);
        mFirstNameView = (TextView) findViewById(R.id.first_name);
        mLastNameView = (TextView) findViewById(R.id.last_name);

        mEmailEdit = (EditText) findViewById(R.id.edit_email);
        mFirstNameEdit = (EditText) findViewById(R.id.edit_first_name);
        mLastNameEdit = (EditText) findViewById(R.id.edit_last_name);

        mEditProfile = (Button) findViewById(R.id.edit_profile);
        mSaveProfile = (Button) findViewById(R.id.save_profile);
        mCancel = (Button) findViewById(R.id.cancel_changes);

        mEmailView.setText(user.getEmail());
        mFirstNameView.setText(firstName);
        mLastNameView.setText(lastName);
        mEmailView.setVisibility(View.GONE);
        mFirstNameView.setVisibility(View.GONE);
        mLastNameView.setVisibility(View.GONE);

        mEmailEdit.setText(user.getEmail());
        mFirstNameEdit.setText(firstName);
        mLastNameEdit.setText(lastName);

        mEmailEdit.setEnabled(false);
        mFirstNameEdit.setEnabled(false);
        mLastNameEdit.setEnabled(false);

        mSaveProfile.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);

        mEditProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile();
            }
        });

        mSaveProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelChanges();
            }
        });

    }

    private void editProfile(){
        Log.d(TAG, "editProfile: " + firstName + " " + lastName);

        mFirstNameEdit.setEnabled(true);
        mLastNameEdit.setEnabled(true);

        mEditProfile.setVisibility(View.GONE);
        mSaveProfile.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
    }

    private void saveChanges(){
        Log.d(TAG, "saveChanges");
        firstName = mFirstNameEdit.getText().toString();
        lastName = mLastNameEdit.getText().toString();

        FirebaseUser user = mAuth.getCurrentUser();
        String name = firstName + " " + lastName;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name).build();

        user.updateProfile(profileUpdates);

        mFirstNameEdit.setEnabled(false);
        mLastNameEdit.setEnabled(false);

        mEditProfile.setVisibility(View.VISIBLE);
        mSaveProfile.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
    }

    private void cancelChanges(){
        Log.d(TAG, "cancelChanges");
        mFirstNameEdit.setText(firstName);
        mLastNameEdit.setText(lastName);

        mFirstNameEdit.setEnabled(false);
        mLastNameEdit.setEnabled(false);

        mEditProfile.setVisibility(View.VISIBLE);
        mSaveProfile.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
    }

}

