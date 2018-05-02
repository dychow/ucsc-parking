package com.example.daniel.ucscparking;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, OnMapReadyCallback {

    private final double MAX_DISTANCE_FROM_BEACON = 1.0;

    private final int CAMPUS_ID = 2;

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

    String name;
    String email;


    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    private BluetoothDialog bluetoothDialog;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> bluetoothArray;

    private static class eventParams {
        String report_gateway_id;
        String report_gateway_key;
        String report_spot;
        String report_event;

        eventParams(String gateway_id, String gateway_key, String spot, String event) {
            this.report_gateway_id = gateway_id;
            this.report_gateway_key = gateway_key;
            this.report_spot = spot;
            this.report_event = event;
        }
    }

    private static class claimParams {
        String claim_email;
        String claim_userKey;
        String claim_spotId;

        claimParams(String email, String userKey, String spotId) {
            this.claim_email = email;
            this.claim_userKey = userKey;
            this.claim_spotId = spotId;
        }
    }

    private class ClaimSpot extends AsyncTask<claimParams, Void, Void>{

        @Override
        protected Void doInBackground(claimParams... params) {
            // TODO Auto-generated method stub

            System.out.println("Inside doInBackground");

            try {

                String claimSpotUrl = "https://cmpe-123a-18-g11.appspot.com/claim-spot?";
                claimSpotUrl = claimSpotUrl + "message+type=claim+spot&";
                claimSpotUrl = claimSpotUrl + "user+email=" + params[0].claim_email + "&";
                claimSpotUrl = claimSpotUrl + "user+key=" + params[0].claim_userKey + "&";
                claimSpotUrl = claimSpotUrl + "spot+id=" + params[0].claim_spotId;


                URL link = new URL(claimSpotUrl);
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

    private class ReportEvent extends AsyncTask<eventParams, Void, Void> {

        @Override
        protected Void doInBackground(eventParams... params) {
            // TODO Auto-generated method stub

            System.out.println("Inside doInBackground");

            try {
                String reportEventUrl = "https://cmpe-123a-18-g11.appspot.com/report-event?";
                reportEventUrl = reportEventUrl + "message+type=report+event&";
                reportEventUrl = reportEventUrl + "gateway+id=1001&";
                reportEventUrl = reportEventUrl + "gateway+key=not ready&";
                reportEventUrl = reportEventUrl + "spot=10006&";
                reportEventUrl = reportEventUrl + "event=arrival";


                URL link = new URL(reportEventUrl);
                URLConnection con = link.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream())
                );

                String inputLine;

                while ((inputLine = in.readLine()) != null)
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

//                        String gateway_id = "";
//                        String gateway_key = "";
//                        String spot = "";
//                        String event = "";
//
//                        eventParams params = new eventParams(gateway_id, gateway_key, spot, event);
//                        ReportEvent reportEvent = new ReportEvent();
//                        reportEvent.execute(params);

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
        LatLng ucsc = new LatLng(36.9915, -122.0583);
        LatLng eastRemote = new LatLng(36.9910, -122.0532);
        LatLng westRemote = new LatLng(36.9885, -122.0648);
        LatLng westCore = new LatLng(36.999173, -122.063669);

        parkingMap.addMarker(new MarkerOptions().position(ucsc).title("UC Santa Cruz"));
        parkingMap.addMarker(new MarkerOptions().position(eastRemote).title("East Remote"));
        parkingMap.addMarker(new MarkerOptions().position(westRemote).title("West Remote"));
        parkingMap.addMarker(new MarkerOptions().position(westCore).title("West Core"));

        parkingMap.moveCamera(CameraUpdateFactory.newLatLng(ucsc));
        parkingMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
        parkingMap.setMaxZoomPreference(17.0f);
        parkingMap.setMinZoomPreference(11.0f);
        parkingMap.getUiSettings().setZoomControlsEnabled(true);
        parkingMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, marker.getTitle());
                switch (marker.getTitle()) {
//                    case "UC Santa Cruz":
//                        marker.setSnippet("Free Spots: " + freeSpots);
//
//                        return false;
                    case "East Remote":
                        marker.setSnippet("East Remote Free Spots: " + freeSpots);

                        return false;
                    case "West Remote":
                        marker.setSnippet("West Remote Free Spots: " + freeSpots);

                        return false;
                    case "West Core":
                        marker.setSnippet("West Core Free Spots: " + freeSpots);

                        return false;
//                    case R.id.parking_status:
//                        marker.setSnippet("Free Spots: " + freeSpots);
//
//                        return true;
//                    case R.id.logout:
//                        marker.setSnippet("Free Spots: " + freeSpots);
//
//                        return true;
                    default:
                        marker.setSnippet("UCSC Free Spots: " + freeSpots);
                        return false;
                }
            }
        });
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
                name = user.getDisplayName();
                email = user.getEmail();

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

//            bluetoothDialog = new BluetoothDialog();
//            bluetoothArray = bluetoothDialog.getBluetoothArray();
//            arrayAdapter = bluetoothDialog.getArrayAdapter();
            bluetoothArray = new ArrayList<String>();
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bluetoothArray);

            mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
            // Detect the URL frame:
            mBeaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
            mBeaconManager.bind(this);
            Log.i(TAG, "Binding Beacon Manager");

            /******** Obtain Parking Lot Data from Cloud Datastore  ********/

            String totalSpots = getSpotStatistics(CAMPUS_ID);
            Log.d(TAG, "Total spots available from Home Screen is " + totalSpots);

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

                spotId = url.substring(8);
                spotId = spotId.substring(0, spotId.length()-4);

                if(!bluetoothArray.contains("Spot Number: " + spotId) && beacon.getDistance() < MAX_DISTANCE_FROM_BEACON) {
                    bluetoothArray.add("Spot Number: " + spotId);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String getSpotStatistics(int areaId){
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

                            availSpots.setText(freeSpots + "\n");

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

        return freeSpots;
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
                String totalSpots = getSpotStatistics(CAMPUS_ID);
                Log.d(TAG, "Total spots available is " + totalSpots);

                return true;
            case R.id.claim_spot:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        spotId = bluetoothArray.get(item);
                        bluetoothArray.clear();
                        arrayAdapter.notifyDataSetChanged();

                        Log.d(TAG, "Spot Id is " + spotId + " and the item selected is " + item);


                        claimParams params = new claimParams(email, "userKey", spotId);
                        ClaimSpot claimSpot = new ClaimSpot();
                        claimSpot.execute(params);
                    }
                });
                AlertDialog instance = b.create();
                if(!bluetoothArray.isEmpty()) {
                      instance.show();
                }
                return true;
            case R.id.filter:
                DialogFragment dialog = new FilterDialog();
                dialog.show(getSupportFragmentManager(), "FilterDialog");
                return true;
            case R.id.profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.parking_status:
                Intent statusIntent = new Intent(this, StatusActivity.class);
                startActivity(statusIntent);
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
