package com.example.daniel.ucscparking;

import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    private static final String TAG = "ProfileActivity";

    // UI references.
    private EditText mEmailEdit;
    private EditText mFirstNameEdit;
    private EditText mLastNameEdit;

    private Button mEditProfile;
    private Button mSaveProfile;
    private Button mCancel;

    private FirebaseAuth mAuth;

    private String name;
    private String firstName;
    private String lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Obtain user's name
        name = user.getDisplayName();
        firstName = name.split(" ")[0];
        lastName = name.split(" ")[1];

        // Instantiate views and buttons
        mEmailEdit = (EditText) findViewById(R.id.edit_email);
        mFirstNameEdit = (EditText) findViewById(R.id.edit_first_name);
        mLastNameEdit = (EditText) findViewById(R.id.edit_last_name);

        mEditProfile = (Button) findViewById(R.id.edit_profile);
        mSaveProfile = (Button) findViewById(R.id.save_profile);
        mCancel = (Button) findViewById(R.id.cancel_changes);

        // Display user information
        mEmailEdit.setText(user.getEmail());
        mFirstNameEdit.setText(firstName);
        mLastNameEdit.setText(lastName);

        mEmailEdit.setEnabled(false);
        mFirstNameEdit.setEnabled(false);
        mLastNameEdit.setEnabled(false);

        // Hide buttons for editing profile
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

    // Allow user to make and save changes
    private void editProfile(){
        Log.d(TAG, "editProfile: " + firstName + " " + lastName);

        mFirstNameEdit.setEnabled(true);
        mLastNameEdit.setEnabled(true);

        mEditProfile.setVisibility(View.GONE);
        mSaveProfile.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
    }

    // Saves profile changes on Firebase
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

    // Cancel any changes that the user made
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

