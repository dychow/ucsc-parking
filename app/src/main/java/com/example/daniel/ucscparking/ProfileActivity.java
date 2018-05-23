package com.example.daniel.ucscparking;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


/**
 * A login screen that offers login via email/password.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI references.
    private TextInputLayout mEmailLayout;
    private TextInputLayout mFirstNameLayout;
    private TextInputLayout mLastNameLayout;
    private TextInputLayout mOldPasswordLayout;
    private TextInputLayout mPasswordLayout;
    private TextInputLayout mConfirmPasswordLayout;

    private EditText mEmail;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mOldPassword;
    private EditText mPassword;
    private EditText mConfirmPassword;

    private Button mEditProfile;
    private Button mChangePass;
    private Button mSaveProfile;
    private Button mCancel;

    private String userEmail;
    private String userKey;
    private String firstName;
    private String lastName;

    SharedPreferences sharedPref;

    InputMethodManager imm;


    private boolean profile_flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPref = getApplicationContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        // Obtain user's name and email
        firstName = sharedPref.getString("first name", "");
        lastName = sharedPref.getString("last name", "");
        userEmail = sharedPref.getString("user email", "");
        userKey = sharedPref.getString("user key", "");

        // Instantiate views and buttons
        mEmailLayout = (TextInputLayout) findViewById(R.id.layout_email);
        mFirstNameLayout = (TextInputLayout) findViewById(R.id.layout_first_name);
        mLastNameLayout = (TextInputLayout) findViewById(R.id.layout_last_name);
        mOldPasswordLayout = (TextInputLayout) findViewById(R.id.layout_old_password);
        mPasswordLayout = (TextInputLayout) findViewById(R.id.layout_password);
        mConfirmPasswordLayout = (TextInputLayout) findViewById(R.id.layout_password_confirm);

        mEmail = (EditText) findViewById(R.id.edit_email);
        mFirstName = (EditText) findViewById(R.id.edit_first_name);
        mLastName = (EditText) findViewById(R.id.edit_last_name);
        mOldPassword = (EditText) findViewById(R.id.old_password);
        mPassword = (EditText) findViewById(R.id.password);
        mConfirmPassword = (EditText) findViewById(R.id.password_confirm);

        mEditProfile = (Button) findViewById(R.id.edit_profile);
        mChangePass = (Button) findViewById(R.id.change_password);
        mSaveProfile = (Button) findViewById(R.id.save_profile);
        mCancel = (Button) findViewById(R.id.cancel_changes);

        // Display user information
        mEmail.setText(userEmail);
        mFirstName.setText(firstName);
        mLastName.setText(lastName);

        mEmail.setEnabled(false);
        mFirstName.setEnabled(false);
        mLastName.setEnabled(false);

        // Hide views and buttons for editing profile and password
        mOldPasswordLayout.setVisibility(View.GONE);
        mPasswordLayout.setVisibility(View.GONE);
        mConfirmPasswordLayout.setVisibility(View.GONE);

        mChangePass.setVisibility(View.GONE);
        mSaveProfile.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);

        imm = (InputMethodManager) ProfileActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        mEditProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == null) {
                    view = new View(ProfileActivity.this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                editProfile();
                mEmail.requestFocus();
            }
        });
        
        mChangePass.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == null) {
                    view = new View(ProfileActivity.this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                changePass();
                mEmail.requestFocus();
            }
        });

        mSaveProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == null) {
                    view = new View(ProfileActivity.this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                saveChanges();
                mEmail.requestFocus();
            }
        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == null) {
                    view = new View(ProfileActivity.this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                cancelChanges();
                mEmail.requestFocus();
            }
        });
//
//        View view = ProfileActivity.this.getCurrentFocus();
//
//        if (view == null) {
//            view = new View(ProfileActivity.this);
//        }
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    // Create parameters for an asynchronous task that will help users register an account on the datastore
    private static class passParams {
        String acc_email;
        String acc_old_pass;
        String acc_new_pass;

        passParams(String email, String old_pass, String new_pass) {
            this.acc_email = email;
            this.acc_old_pass = old_pass;
            this.acc_new_pass = new_pass;
        }
    }

    // Define the asynchronous task to help users register an account on the datastore
    private class UpdatePassword extends AsyncTask<passParams, Void, Void>{
        JSONObject loginJson;

        @Override
        protected Void doInBackground(passParams... params) {

            // Set the URL that will be used to connect to the cloud
            String createUserUrl = "https://cmpe-123a-18-g11.appspot.com/change-password?";
            try {
                String final_str = createUserUrl + "user+email=" + params[0].acc_email + "&";
                final_str = final_str + "user+pwd=" + params[0].acc_old_pass + "&";
                final_str = final_str + "new+pwd=" + params[0].acc_new_pass;

                // Create a link to the URL and get a response
                URL link = new URL(final_str);
                URLConnection con = link.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()
                        )
                );

                String jsonText;
                String inputLine;
                StringBuilder sb = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    sb.append(inputLine);
                }
                in.close();
                jsonText = sb.toString();
                loginJson = new JSONObject(jsonText);

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            System.out.println("Inside onPostExecute");
            try {
                if (loginJson.getString("status").equals("failure")) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ProfileActivity.this, "User does not exist.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    String errorMessage = loginJson.getString("message");
                    if (errorMessage.equals("user not exist")){
                        Toast.makeText(ProfileActivity.this, "User does not exist.",
                                Toast.LENGTH_SHORT).show();
                    } else if (errorMessage.equals("pwd incorrect")){
                        Toast.makeText(ProfileActivity.this, "Old password must be correct to change your password.",
                                Toast.LENGTH_SHORT).show();
                    } else if (errorMessage.equals("user not verified")){
                        Toast.makeText(ProfileActivity.this, "User is not verified.",
                                Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(ProfileActivity.this, "Password update failed. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    userKey = loginJson.getString("user key");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("user key", userKey);
                    Toast.makeText(ProfileActivity.this, "Password has been updated.",
                            Toast.LENGTH_SHORT).show();
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Create parameters for an asynchronous task that will help users register an account on the datastore
    private static class updateParams {
        String acc_firstName;
        String acc_lastName;
        String acc_userId;
        String acc_userKey;

        updateParams(String firstName, String lastName, String userId, String userKey) {
            this.acc_firstName = firstName;
            this.acc_lastName = lastName;
            this.acc_userId = userId;
            this.acc_userKey = userKey;
        }
    }

    // Define the asynchronous task to help users register an account on the datastore
    private class UpdateAccount extends AsyncTask<updateParams, Void, Void>{
        JSONObject loginJson;

        @Override
        protected Void doInBackground(updateParams... params) {

            // Set the URL that will be used to connect to the cloud
            String createUserUrl = "https://cmpe-123a-18-g11.appspot.com/change-profile?";
            try {
                String final_str = createUserUrl + "first+name=" + params[0].acc_firstName + "&";
                final_str = final_str + "last+name=" + params[0].acc_lastName + "&";
                final_str = final_str + "user+email=" + params[0].acc_userId + "&";
                final_str = final_str + "user+key=" + params[0].acc_userKey;

                // Create a link to the URL and get a response
                URL link = new URL(final_str);
                URLConnection con = link.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()
                        )
                );

                String jsonText;
                String inputLine;
                StringBuilder sb = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    sb.append(inputLine);
                }
                in.close();
                jsonText = sb.toString();
                loginJson = new JSONObject(jsonText);

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            System.out.println("Inside onPostExecute");
            try {
                if (loginJson.getString("status").equals("failure")) {
                    String errorMessage = loginJson.getString("message");
                    if (errorMessage.equals("user not exist")){
                        Toast.makeText(ProfileActivity.this, "User does not exist.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Profile update failed. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Allow user to make and save changes
    private void editProfile(){
        Log.d(TAG, "editProfile: " + firstName + " " + lastName);

        mFirstName.setEnabled(true);
        mLastName.setEnabled(true);

        mEditProfile.setVisibility(View.GONE);
        mChangePass.setVisibility(View.VISIBLE);
        mSaveProfile.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
    }
    
    private void changePass(){
        profile_flag = false;
        Log.d(TAG, "changePass");

        mEmailLayout.setVisibility(View.GONE);
        mFirstNameLayout.setVisibility(View.GONE);
        mLastNameLayout.setVisibility(View.GONE);

        mEditProfile.setVisibility(View.GONE);
        mChangePass.setVisibility(View.GONE);
        mOldPasswordLayout.setVisibility(View.VISIBLE);
        mPasswordLayout.setVisibility(View.VISIBLE);
        mConfirmPasswordLayout.setVisibility(View.VISIBLE);
    }

    // Saves profile changes on Database
    private void saveChanges(){
        Log.d(TAG, "saveChanges");
        if(profile_flag) {
            firstName = mFirstName.getText().toString();
            lastName = mLastName.getText().toString();

            updateParams params = new updateParams(firstName, lastName, userEmail, userKey);
            UpdateAccount updateAccount = new UpdateAccount();
            updateAccount.execute(params);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("first name", firstName);
            editor.putString("last name", lastName);

            mFirstName.setEnabled(false);
            mLastName.setEnabled(false);

            mEditProfile.setVisibility(View.VISIBLE);

            mChangePass.setVisibility(View.GONE);
            mSaveProfile.setVisibility(View.GONE);
            mCancel.setVisibility(View.GONE);
        } else {

            if (validatePass()){
                profile_flag = true;
                String password = mPassword.getText().toString();
                String oldpass = mOldPassword.getText().toString();

                passParams params = new passParams(userEmail, oldpass, password);
                UpdatePassword newPass = new UpdatePassword();
                newPass.execute(params);

                mOldPassword.setText("");
                mPassword.setText("");
                mConfirmPassword.setText("");

                mFirstName.setText(firstName);
                mLastName.setText(lastName);
                mFirstName.setEnabled(false);
                mLastName.setEnabled(false);

                mPassword.setError(null);
                mConfirmPassword.setError(null);

                mEditProfile.setVisibility(View.VISIBLE);
                mEmailLayout.setVisibility(View.VISIBLE);
                mFirstNameLayout.setVisibility(View.VISIBLE);
                mLastNameLayout.setVisibility(View.VISIBLE);

                mOldPasswordLayout.setVisibility(View.GONE);
                mPasswordLayout.setVisibility(View.GONE);
                mConfirmPasswordLayout.setVisibility(View.GONE);
                mChangePass.setVisibility(View.GONE);
                mSaveProfile.setVisibility(View.GONE);
                mCancel.setVisibility(View.GONE);
            }
        }
    }

    // Cancel any changes that the user made
    private void cancelChanges(){
        if(profile_flag) {
            Log.d(TAG, "cancelChanges");
            mFirstName.setText(firstName);
            mLastName.setText(lastName);

            mFirstName.setEnabled(false);
            mLastName.setEnabled(false);

            mEditProfile.setVisibility(View.VISIBLE);

            mChangePass.setVisibility(View.GONE);
            mSaveProfile.setVisibility(View.GONE);
            mCancel.setVisibility(View.GONE);
        } else {
            profile_flag = true;

            mFirstName.setText(firstName);
            mLastName.setText(lastName);
            mPassword.setText("");
            mConfirmPassword.setText("");

            mFirstName.setEnabled(false);
            mLastName.setEnabled(false);

            mPassword.setError(null);
            mConfirmPassword.setError(null);

            mEditProfile.setVisibility(View.VISIBLE);
            mEmailLayout.setVisibility(View.VISIBLE);
            mFirstNameLayout.setVisibility(View.VISIBLE);
            mLastNameLayout.setVisibility(View.VISIBLE);

            mOldPasswordLayout.setVisibility(View.GONE);
            mPasswordLayout.setVisibility(View.GONE);
            mConfirmPasswordLayout.setVisibility(View.GONE);
            mChangePass.setVisibility(View.GONE);
            mSaveProfile.setVisibility(View.GONE);
            mCancel.setVisibility(View.GONE);
        }
    }

    private boolean validatePass() {
        boolean valid = true;

        String oldPassword = mOldPassword.getText().toString();
        if (TextUtils.isEmpty(oldPassword)) {
            mPassword.setError("Required.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Required.");
            valid = false;
        } else if (!isPasswordValid(password)){
            mPassword.setError("Password is not valid.");
        } else {
            mPassword.setError(null);
        }

        String confirmPass = mConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(confirmPass)) {
            mConfirmPassword.setError("Required.");
            valid = false;
        } else if(!confirmPass.equals(password)){
            mConfirmPassword.setError("Passwords must match.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

}
