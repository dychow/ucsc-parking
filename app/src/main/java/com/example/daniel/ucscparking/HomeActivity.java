package com.example.daniel.ucscparking;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, OnMapReadyCallback {

    private static final String TAG = "HomeActivity";

    private GoogleMap parkingMap;
    private SupportMapFragment parkingMapFragment;
    private Fragment homeFragment;
    private ListFragment parkingListFragment;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener databaseListener;
    private GoogleApiClient googleApiClient;
    private TextView availSpots;
    private String spotId = "0";

    String freeSpots = "";
    String freeSpotList = "";
    String[] freeSpotArray;


    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    {
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction;

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.hide(parkingListFragment);
                        transaction.hide(parkingMapFragment);
                        transaction.show(homeFragment);
                        transaction.commit();

                        return true;
                    case R.id.navigation_map:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.hide(parkingListFragment);
                        transaction.hide(homeFragment);
                        transaction.show(parkingMapFragment);
                        transaction.commit();

                        return true;
                    case R.id.navigation_spots:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.hide(parkingMapFragment);
                        transaction.hide(homeFragment);
                        transaction.show(parkingListFragment);
                        transaction.commit();

                        return true;
                }

                return false;
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        parkingMap = googleMap;

        // Add a marker in Santa Cruz and move the camera
        LatLng sc = new LatLng(36.9915, -122.0583);
        parkingMap.addMarker(new MarkerOptions().position(sc).title("Marker in Santa Cruz").);
        parkingMap.moveCamera(CameraUpdateFactory.newLatLng(sc));
        parkingMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
        parkingMap.setMaxZoomPreference(17.0f);
        parkingMap.setMinZoomPreference(11.0f);
        parkingMap.getUiSettings().setZoomControlsEnabled(true);
    }

    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        backgroundPowerSaver = new BackgroundPowerSaver(this);


        if (findViewById(R.id.fragment_container) != null) {
//
//            // However, if we're being restored from a previous state,
//            // then we don't need to do anything and should return or else
//            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            parkingMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);

            homeFragment = new HomeFragment();
            parkingMapFragment = SupportMapFragment.newInstance();
            parkingListFragment = new ParkingListFragment();

            if (parkingMapFragment != null) {
                parkingMapFragment.getMapAsync(HomeActivity.this);
            }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, parkingMapFragment);
            fragmentTransaction.add(R.id.fragment_container, parkingListFragment);
            fragmentTransaction.add(R.id.fragment_container, homeFragment);
            fragmentTransaction.hide(parkingMapFragment);
            fragmentTransaction.hide(parkingListFragment);
            fragmentTransaction.commit();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Name, email address, and profile photo Url
                String name = user.getDisplayName();
                String email = user.getEmail();

                // Check if user's email is verified
                boolean emailVerified = user.isEmailVerified();

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getToken() instead.
                String uid = user.getUid();

                System.out.println(name);
                System.out.println(email);
                System.out.println(emailVerified);
                System.out.println(uid);

            }

            /******** Enable Bluetooth Beacon Communication  ********/

            mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
            // Detect the URL frame:
            mBeaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
            mBeaconManager.bind(this);
            Log.i(TAG, "Binding Beacon Manager");

            /******** Obtain Parking Lot Data from Cloud Datastore  ********/

            int areaId = 2;

            String getSpotsUrl = "https://cmpe-123a-18-g11.appspot.com/get-statistics?";
            getSpotsUrl = getSpotsUrl + "message+type=get+statistics&";
            getSpotsUrl = getSpotsUrl + "area+id=" + areaId;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, getSpotsUrl, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                freeSpots = response.getString("free spot number");
                                freeSpotList = response.getString("free spot list");
                                freeSpotArray = freeSpotList.split(", ");

                                String free = "";


                                for (int i = 0; i < freeSpotArray.length; i++) {
                                    CharSequence c1 = "[";
                                    CharSequence c2 = "]";
                                    if (freeSpotArray[i].contains(c1)) {
                                        freeSpotArray[i] = freeSpotArray[i].split("\\[")[1];
                                    } else if (freeSpotArray[i].contains(c2)) {
                                        freeSpotArray[i] = freeSpotArray[i].split("\\]")[0];
                                    }

                                    free += freeSpotArray[i] + " ";

                                }

                                availSpots = (TextView) findViewById(R.id.available_spots);
                                availSpots.setText("Free Spots: " + freeSpots + "\n" +
                                        "Free Spot List: " + free);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });

// Access the RequestQueue through your singleton class.
            MySingleton.getInstance(HomeActivity.this).addToRequestQueue(jsonObjectRequest);
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon : beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // This is a Eddystone-URL frame
                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                Log.d(TAG, "I see a beacon transmitting a url: " + url +
                        " approximately " + beacon.getDistance() + " meters away.");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                int areaId = 2;

                String getSpotsUrl = "https://cmpe-123a-18-g11.appspot.com/get-statistics?";
                getSpotsUrl = getSpotsUrl + "message+type=get+statistics&";
                getSpotsUrl = getSpotsUrl + "area+id=" + areaId;

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, getSpotsUrl, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    freeSpots = response.getString("free spot number");
                                    freeSpotList = response.getString("free spot list");
                                    freeSpotArray = freeSpotList.split(", ");

                                    String free = "";


                                    for (int i = 0; i < freeSpotArray.length; i++) {
                                        CharSequence c1 = "[";
                                        CharSequence c2 = "]";
                                        if (freeSpotArray[i].contains(c1)) {
                                            freeSpotArray[i] = freeSpotArray[i].split("\\[")[1];
                                        } else if (freeSpotArray[i].contains(c2)) {
                                            freeSpotArray[i] = freeSpotArray[i].split("\\]")[0];
                                        }

                                        free += freeSpotArray[i] + " ";

                                    }
                                    availSpots = (TextView) findViewById(R.id.available_spots);

                                    availSpots.setText("Free Spots: " + freeSpots + "\n" +
                                            "Free Spot List: " + free);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });

// Access the RequestQueue through your singleton class.
                MySingleton.getInstance(HomeActivity.this).addToRequestQueue(jsonObjectRequest);

                return true;
            case R.id.filter:
                DialogFragment dialog = new FilterDialog();
                dialog.show(getSupportFragmentManager(), "FilterDialog");
                return true;
            case R.id.profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.current_status:
                // TODO showStatus();
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();

                Intent logOutIntent = new Intent(this, LoginActivity.class);
                logOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(logOutIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
