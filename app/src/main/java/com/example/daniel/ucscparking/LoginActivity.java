package com.example.daniel.ucscparking;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    private static final String TAG = "LoginActivity";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    String firstName;
    String lastName;
    String permit;
    String userKey;
    String email;

    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle(R.string.title_activity_login);

//        mAuth = FirebaseAuth.getInstance();
        sharedPref = getApplicationContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);


        // See if the user is already logged in
        // If user is logged in and verified, take them to the home screen
        if(!sharedPref.getString("user email", "").equals("")){
            firstName = sharedPref.getString("first name", "");
            lastName = sharedPref.getString("last name", "");
            permit = sharedPref.getString("permit", "");
            userKey = sharedPref.getString("user key", "");
            email = sharedPref.getString("user email", "");

            String name = firstName + " " + lastName;

            System.out.println(name);
            System.out.println(permit);
            System.out.println(email);
            System.out.println(userKey);

            Intent logInIntent = new Intent(LoginActivity.this, HomeActivity.class);
            logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logInIntent);
            finish();
        }

//        if (user != null && user.isEmailVerified()) {
////        if (user != null) {
//        // Name and email address
//            name = user.getDisplayName();
//            email = user.getEmail();
//
//            // Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();
//
//            // The user's ID, unique to the Firebase project. Do NOT use this value to
//            // authenticate with your backend server, if you have one. Use
//            // FirebaseUser.getToken() instead.
//            String uid = user.getUid();
//
//            System.out.println(name);
//            System.out.println(email);
//            System.out.println(emailVerified);
//            System.out.println(uid);
//
//            Intent loggedIntent = new Intent(this, HomeActivity.class);
//            loggedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(loggedIntent);
//            finish();
//        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        // Button to let user sign in with credentials
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) LoginActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (view == null) {
                    view = new View(LoginActivity.this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                signIn(mEmailView.getText().toString(), mPasswordView.getText().toString());
            }
        });

        // Button to let the user make a new account if they do not already have one
        Button mRegisterButton = (Button) findViewById(R.id.email_register_button);
        mRegisterButton.setOnClickListener((new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        }));

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

    }

    // Function to facilitate user login
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!attemptLogin()) {
            return;
        }

        //        showProgressDialog();

        loginParams params = new loginParams(email, password);
        LogInTask logIn = new LogInTask();
        logIn.execute(params);

    }

//    // Sends the user to the home screen if they have logged in and are verified
//    private void updateUI(FirebaseUser user) {
////        hideProgressDialog();
//        if (user != null  && user.isEmailVerified()) {
////        if (user != null) {
//            Intent logInIntent = new Intent(this, HomeActivity.class);
//            logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(logInIntent);
//            finish();
//
//            System.out.println("user email is: " + user.getEmail());
//            System.out.println("user verified status is: " + user.isEmailVerified());
//
//        } else {
//            Toast.makeText(LoginActivity.this, "Email has not yet been verified.",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }

    private static class loginParams {
        String acc_loginId;
        String acc_password;

        loginParams(String loginId, String password) {
            this.acc_loginId = loginId;
            this.acc_password = password;
        }
    }

    // Define the asynchronous task to help users login to their account on the datastore
    private class LogInTask extends AsyncTask<loginParams, Void, Void>{
        JSONObject loginJson;

        @Override
        protected Void doInBackground(loginParams... params) {

            // Set the URL that will be used to connect to the cloud
            String loginUserUrl = "https://cmpe-123a-18-g11.appspot.com/login?";
            try {
                String final_str = loginUserUrl + "user+email=" + params[0].acc_loginId + "&";
                final_str = final_str + "user+pwd=" + params[0].acc_password;

                String jsonText;
                String inputLine;
                StringBuilder sb = new StringBuilder();

                // Create a link to the URL and get a response
                URL link = new URL(final_str);
                URLConnection con = link.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()
                        )
                );

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
                        Toast.makeText(LoginActivity.this, "User does not exist.",
                                Toast.LENGTH_SHORT).show();
                    } else if (errorMessage.equals("email pwd not match")) {
                        Toast.makeText(LoginActivity.this, "Login Credentials are incorrect.",
                                Toast.LENGTH_SHORT).show();
                    } else if (errorMessage.equals("user not verified")){
                        Toast.makeText(LoginActivity.this, "User is not verified.",
                                Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(LoginActivity.this, "Authentication failed. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String firstName = loginJson.getString("first name");
                    String lastName = loginJson.getString("last name");
                    String permit = loginJson.getString("permit");
                    String userKey = loginJson.getString("user key");
                    String email = mEmailView.getText().toString();

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("user email", email);
                    editor.putString("first name", firstName);
                    editor.putString("last name", lastName);
                    editor.putString("permit", permit);
                    editor.putString("user key", userKey);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Successfully signed in.",
                            Toast.LENGTH_SHORT).show();

                    Intent logInIntent = new Intent(LoginActivity.this, HomeActivity.class);
                    logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(logInIntent);
                    finish();
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

//    private void showProgressDialog() {
//        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//        mLoginFormView.setVisibility(View.VISIBLE);
//        mLoginFormView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mLoginFormView.setVisibility(View.VISIBLE);
//            }
//        });
//
//        mProgressView.setVisibility(View.GONE);
//        mProgressView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mProgressView.setVisibility(View.GONE);
//            }
//        });
//    }
//
//    private void hideProgressDialog() {
//        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//        mLoginFormView.setVisibility(View.VISIBLE);
//        mLoginFormView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mLoginFormView.setVisibility(View.VISIBLE);
//            }
//        });
//
//        mProgressView.setVisibility(View.GONE);
//        mProgressView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mProgressView.setVisibility(View.GONE);
//            }
//        });
//    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private boolean attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)){
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus on the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    // Conditions to make sure that the email is valid
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    // Conditions to make sure that the password is valid
    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}
