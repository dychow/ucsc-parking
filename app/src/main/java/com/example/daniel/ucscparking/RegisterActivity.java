package com.example.daniel.ucscparking;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.ArrayAdapter;
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
import com.google.api.client.util.Lists;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;
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
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private View mProgressView;
    private View mLoginFormView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String firstName;
    private String lastName;

    // Create parameters for an asynchronous task that will help users register an account on the datastore
    private static class accountParams {
        String acc_firstName;
        String acc_lastName;
        String acc_loginId;
        String acc_password;

        accountParams(String firstName, String lastName, String loginId, String password) {
            this.acc_firstName = firstName;
            this.acc_lastName = lastName;
            this.acc_loginId = loginId;
            this.acc_password = password;
        }
    }

    // Define the asynchronous task to help users register an account on the datastore
    private class CreateAccount extends AsyncTask<accountParams, Void, Void>{

        @Override
        protected Void doInBackground(accountParams... params) {

            // Set the URL that will be used to connect to the cloud
            String createUserUrl = "https://cmpe-123a-18-g11.appspot.com/new-user?";
            try {
                String final_str = createUserUrl + "message+type=user+register&";
                final_str = final_str + "first+name=" + params[0].acc_firstName + "&";
                final_str = final_str + "last+name=" + params[0].acc_lastName + "&";
                final_str = final_str + "user+email=" + params[0].acc_loginId + "&";
                final_str = final_str + "user+pwd=" + params[0].acc_password;

                // Create a link to the URL and get a response
                URL link = new URL(final_str);
                URLConnection con = link.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()
                        )
                );

                String inputLine;

                while((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
                in.close();
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void res) {

            System.out.println("Inside onPostExecute");

        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupActionBar();

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);


        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(mEmailView.getText().toString(), mPasswordView.getText().toString());
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();

    }

    private void createAccount(String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // Create the user with email and password on Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            String name = firstName + " " + lastName;

                            // Update the user's name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();

                            user.updateProfile(profileUpdates);

                            mDatabase = FirebaseDatabase.getInstance().getReference();
                            mDatabase.child("users").child(user.getUid()).child("email").setValue(user.getEmail());

                            accountParams params = new accountParams(firstName, lastName, user.getEmail(), password);
                            CreateAccount newAccount = new CreateAccount();
                            newAccount.execute(params);

                            // Send email to user for verification of identity
                            user.sendEmailVerification()
                                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Verification email sent to " + user.getEmail(),
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e(TAG, "sendEmailVerification", task.getException());
                                                Toast.makeText(RegisterActivity.this,
                                                        "Failed to send verification email.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Account Creation Failed",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        hideProgressDialog();
                    }
                });
    }

    // Sends user to login screen so that they can verify their email
    //  If their email is already verified, they will be taken to the home screen of the app
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null && user.isEmailVerified()) {
            Intent logInIntent = new Intent(this, HomeActivity.class);
            logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logInIntent);
            finish();

            System.out.println("user email is: " + user.getEmail());
            System.out.println("user verified status is: " + user.isEmailVerified());

        } else {
            Toast.makeText(RegisterActivity.this,
                    "Must verify email",
                    Toast.LENGTH_SHORT).show();
            Intent logInIntent = new Intent(this, LoginActivity.class);
            logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logInIntent);
            finish();
        }
    }

    private void showProgressDialog() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(View.VISIBLE);
            }
        });

        mProgressView.setVisibility(View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(View.GONE);
            }
        });
    }

    private void hideProgressDialog() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(View.VISIBLE);
            }
        });

        mProgressView.setVisibility(View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(View.GONE);
            }
        });
    }

    // Ensures that the parameters for the account creation are filled
    // Ensures that email and passwords fulfill conditions
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailView.getText().toString();
        if (TextUtils.isEmpty(email) && !isEmailValid(email)) {
            mEmailView.setError("Required.");
            valid = false;
        } else {
            mEmailView.setError(null);
        }

        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("Required.");
            valid = false;
        } else {
            mPasswordView.setError(null);
        }

        firstName = mFirstNameView.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError("Required.");
            valid = false;
        } else {
            mFirstNameView.setError(null);
        }

        lastName = mLastNameView.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError("Required.");
            valid = false;
        } else {
            mLastNameView.setError(null);
        }

        return valid;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    // Conditions to make sure that the email is valid
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    // Conditions to make sure that the password is valid
    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }
}

