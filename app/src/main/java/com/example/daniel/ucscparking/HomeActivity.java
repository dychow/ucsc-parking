package com.example.daniel.ucscparking;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, OnMapReadyCallback, FilterDialog.FilterDialogListener {

    private final double MAX_DISTANCE_FROM_BEACON = 1.0;

    private final String CAMPUS_ID = "2";
    private final String EAST_REMOTE_ID = "104";
    private final String WEST_REMOTE_ID = "127";
    private final String WEST_CORE_ID = "112";
    private final String COWELL_ID = "18";
    private final String STEVENSON_ID = "18";
    private final String CROWN_ID = "16";
    private final String MERILL_ID = "17";
    private final String KRESGE_ID = "13";
    private final String PORTER_ID = "14";
    private final String OAKES_ID = "12";
    private final String RC_ID = "21";
    private final String C9_ID = "15";
    private final String C10_ID = "15";
    private final String BASKIN_ID = "19";

    private static final String TAG = "HomeActivity";

    private GoogleMap parkingMap;
    private SupportMapFragment parkingMapFragment;
    private Fragment homeFragment;
    private ListFragment parkingListFragment;
    private TextView availSpots;
    private String spotId = "0";

    String numSpots = "";
    ArrayList<String> freeSpots;
    ArrayList<Marker> markerList;

    List<String> lots = Arrays.asList(
            "East Remote",
            "West Remote",
            "West Core",
            "Cowell",
            "Stevenson",
            "Crown",
            "Merill",
            "Kresge",
            "Porter",
            "Oakes",
            "Rachel Carson",
            "College 9",
            "College 10",
            "Jack Baskin Engineering");

    boolean[] filterLots = {
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true
    };

    String[] idArray = {
            EAST_REMOTE_ID,
            WEST_REMOTE_ID,
            WEST_CORE_ID,
            COWELL_ID,
            STEVENSON_ID,
            CROWN_ID,
            MERILL_ID,
            KRESGE_ID,
            PORTER_ID,
            OAKES_ID,
            RC_ID,
            C9_ID,
            C10_ID,
            BASKIN_ID
    };

    String firstName;
    String lastName;
    String name;
    String permit;
    String userKey;
    String email;

    String parking_status;
    String parking_spot;

    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    private ArrayAdapter<String> bluetoothArrayAdapter;
    private ArrayList<String> bluetoothArray;

    private ArrayAdapter<String> freeSpotsArrayAdapter;

    SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        backgroundPowerSaver = new BackgroundPowerSaver(this);

        if (findViewById(R.id.fragment_container) != null) {

            // If we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            } else {

                sharedPref = getApplicationContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                /******** Initialize Fragments  ********/
                parkingMapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                homeFragment = new HomeFragment();
                parkingMapFragment = SupportMapFragment.newInstance();
                parkingListFragment = new ParkingListFragment();

                if (parkingMapFragment != null) {
                    parkingMapFragment.getMapAsync(HomeActivity.this);
                }

                // Set the user on the Home display
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, parkingMapFragment);
                fragmentTransaction.add(R.id.fragment_container, parkingListFragment);
                fragmentTransaction.add(R.id.fragment_container, homeFragment);
                fragmentTransaction.hide(parkingMapFragment);
                fragmentTransaction.hide(parkingListFragment);
                fragmentTransaction.commit();

                /******** Enable Bluetooth Beacon Communication  ********/

                bluetoothArray = new ArrayList<String>();
                bluetoothArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bluetoothArray);

                mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
                // Detect the URL frame:
                mBeaconManager.getBeaconParsers().add(new BeaconParser().
                        setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
                mBeaconManager.bind(this);
                Log.i(TAG, "Binding Beacon Manager");

                /******** Obtain Parking Lot Data from Cloud Datastore  ********/

                // Initialize the array describing the free spots in each area
                freeSpots = new ArrayList<String>();
                freeSpotsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, freeSpots);
                for (int i = 0; i < filterLots.length + 1; i++) {
                    freeSpots.add(i, "0");
                }

                // Fill the array based on what areas have been filtered out
                for (int i = 0; i < filterLots.length; i++) {
                    if (filterLots[i]) {
                        getSpotStatistics(idArray[i], new VolleyCallback() {
                            @Override
                            public void onSuccess(String areaId, String result) {
                                System.out.println("areaID " + areaId + ": " + result);

                            }
                        });
                    }
                }

                // Obtain the statistics on how many spots there are on all of campus
                getSpotStatistics(CAMPUS_ID, new VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result) {
                        availSpots = (TextView) findViewById(R.id.available_spots);
                        availSpots.setText(numSpots + "\n");
                        Log.d(TAG, "Total spots available from Home Screen is " + freeSpots.get(0));
                    }
                });

                /******** Checking User Credentials  ********/
                firstName = sharedPref.getString("first name", "");
                lastName = sharedPref.getString("last name", "");
                permit = sharedPref.getString("permit", "");
                userKey = sharedPref.getString("user key", "");
                email = sharedPref.getString("user email", "");

                name = firstName + " " + lastName;

                System.out.println(name);
                System.out.println(permit);
                System.out.println(email);
                System.out.println(userKey);
            }

        }
    }

    /******** Initiale the Navigation Bar for Different Tabs  ********/

    // Set up the navigation bar for switching between tabs
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

    /******** Handle the Map Tab ********/

    // Set up the Map display tab
    @Override
    public void onMapReady(GoogleMap googleMap) {
        parkingMap = googleMap;

        // Initialize the locations of each area
        LatLng ucsc = new LatLng(36.9915, -122.0583);
        LatLng eastRemote = new LatLng(36.9910, -122.0532);
        LatLng westRemote = new LatLng(36.988555, -122.065900);
        LatLng westCore = new LatLng(36.999077, -122.063677);
        LatLng cowell = new LatLng(36.997126, -122.054269);
        LatLng stevenson = new LatLng(36.9970, -122.0517);
        LatLng crown = new LatLng(36.9996, -122.0550);
        LatLng merill = new LatLng(36.9999, -122.0533);
        LatLng kresge = new LatLng(36.9972, -122.0668);
        LatLng porter = new LatLng(36.994394, -122.065221);
        LatLng oakes = new LatLng(36.989029, -122.064636);
        LatLng rc = new LatLng(36.9912, -122.0647);
        LatLng c9 = new LatLng(37.001581, -122.057262);
        LatLng c10 = new LatLng(37.0004, -122.0584);
        LatLng hahn = new LatLng(36.996082, -122.057033);
        LatLng baskin = new LatLng(37.000370, -122.063237);
        LatLng opers = new LatLng(36.995032, -122.054087);

        // Add markers to the map based on each location
        markerList = new ArrayList<Marker>();

        markerList.add(0, parkingMap.addMarker(new MarkerOptions().position(ucsc).title("UC Santa Cruz")));
        markerList.add(1, parkingMap.addMarker(new MarkerOptions().position(eastRemote).title("East Remote")));
        markerList.add(2, parkingMap.addMarker(new MarkerOptions().position(westRemote).title("West Remote")));
        markerList.add(3, parkingMap.addMarker(new MarkerOptions().position(westCore).title("West Core")));
        markerList.add(4, parkingMap.addMarker(new MarkerOptions().position(cowell).title("Cowell")));
        markerList.add(5, parkingMap.addMarker(new MarkerOptions().position(stevenson).title("Stevenson")));
        markerList.add(6, parkingMap.addMarker(new MarkerOptions().position(crown).title("Crown")));
        markerList.add(7, parkingMap.addMarker(new MarkerOptions().position(merill).title("Merill")));
        markerList.add(8, parkingMap.addMarker(new MarkerOptions().position(kresge).title("Kresge")));
        markerList.add(9, parkingMap.addMarker(new MarkerOptions().position(porter).title("Porter")));
        markerList.add(10, parkingMap.addMarker(new MarkerOptions().position(oakes).title("Oakes")));
        markerList.add(11, parkingMap.addMarker(new MarkerOptions().position(rc).title("Rachel Carson")));
        markerList.add(12, parkingMap.addMarker(new MarkerOptions().position(c9).title("College Nine")));
        markerList.add(13, parkingMap.addMarker(new MarkerOptions().position(c10).title("College Ten")));
        markerList.add(14, parkingMap.addMarker(new MarkerOptions().position(baskin).title("Jack Baskin Engineering")));


        // Make filtered out areas invisible
        sharedPref = getApplicationContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        for (int i = 0; i < lots.size(); i++) {
            filterLots[i] = sharedPref.getBoolean(lots.get(i), true);
            if (!filterLots[i]) {
                markerList.get(i + 1).setVisible(false);
            } else {
                markerList.get(i + 1).setVisible(true);
            }
        }

        // Focus the camera onto the UCSC campus
        parkingMap.moveCamera(CameraUpdateFactory.newLatLng(ucsc));
        parkingMap.moveCamera(CameraUpdateFactory.zoomTo(14.5f));
        parkingMap.setMaxZoomPreference(17.0f);
        parkingMap.setMinZoomPreference(11.0f);
        parkingMap.getUiSettings().setZoomControlsEnabled(true);

        // When a user clicks on a marker, the number of free spots is returned
        parkingMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, marker.getTitle());
                switch (marker.getTitle()) {
                    case "East Remote":
                        marker.setSnippet("Free Spots: " + freeSpots.get(1));
                        return false;
                    case "West Remote":
                        marker.setSnippet("Free Spots: " + freeSpots.get(2));
                        return false;
                    case "West Core":
                        marker.setSnippet("Free Spots: " + freeSpots.get(3));
                        return false;
                    case "Cowell":
                        marker.setSnippet("Free Spots: " + freeSpots.get(4));
                        return false;
                    case "Stevenson":
                        marker.setSnippet("Free Spots: " + freeSpots.get(5));
                        return false;
                    case "Crown":
                        marker.setSnippet("Free Spots: " + freeSpots.get(6));
                        return false;
                    case "Merill":
                        marker.setSnippet("Free Spots: " + freeSpots.get(7));
                        return false;
                    case "Kresge":
                        marker.setSnippet("Free Spots: " + freeSpots.get(8));
                        return false;
                    case "Porter":
                        marker.setSnippet("Free Spots: " + freeSpots.get(9));
                        return false;
                    case "Oakes":
                        marker.setSnippet("Free Spots: " + freeSpots.get(10));
                        return false;
                    case "Rachel Carson":
                        marker.setSnippet("Free Spots: " + freeSpots.get(11));
                        return false;
                    case "College Nine":
                        marker.setSnippet("Free Spots: " + freeSpots.get(12));
                        return false;
                    case "College Ten":
                        marker.setSnippet("Free Spots: " + freeSpots.get(13));
                        return false;
                    case "Jack Baskin Engineering":
                        marker.setSnippet("Free Spots: " + freeSpots.get(14));
                        return false;
                    default:
                        marker.setSnippet("Free Spots: " + freeSpots.get(0));
                        return false;
                }
            }
        });
    }

    /******** Method to Obtain Statistics based on Area  ********/

    // Callback for getting the spot statistics
    public interface VolleyCallback {
        void onSuccess(String areaId, String result);
    }

    // Obtain spot statistics depending on which area was selected
    private void getSpotStatistics(String areaId, final VolleyCallback callback) {

        // Set the URL that will be used to connect to the cloud
        String getSpotsUrl = "https://cmpe-123a-18-g11.appspot.com/get-statistics?";
        getSpotsUrl = getSpotsUrl + "message+type=get+statistics&";
        getSpotsUrl = getSpotsUrl + "area+id=" + areaId;

        // Get statistics about selected area
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getSpotsUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtain information
                            String totalFreeSpots = response.getString("free spot number");
                            String area_id = response.getString("area id");

                            // Set the array based on how many free spots are available for the area
                            switch (area_id) {
                                case EAST_REMOTE_ID:
                                    freeSpots.set(1, totalFreeSpots);
                                    break;
                                case WEST_REMOTE_ID:
                                    freeSpots.set(2, totalFreeSpots);
                                    break;
                                case WEST_CORE_ID:
                                    freeSpots.set(3, totalFreeSpots);
                                    break;
                                case COWELL_ID:
                                    freeSpots.set(4, totalFreeSpots);
                                    freeSpots.set(5, totalFreeSpots);
                                    break;
//            case STEVENSON_ID:
//                freeSpots[5] = numSpots;
//                return freeSpots[5];
                                case CROWN_ID:
                                    freeSpots.set(6, totalFreeSpots);
                                    break;
                                case MERILL_ID:
                                    freeSpots.set(7, totalFreeSpots);
                                    break;
                                case KRESGE_ID:
                                    freeSpots.set(8, totalFreeSpots);
                                    break;
                                case PORTER_ID:
                                    freeSpots.set(9, totalFreeSpots);
                                    break;
                                case OAKES_ID:
                                    freeSpots.set(10, totalFreeSpots);
                                    break;
                                case RC_ID:
                                    freeSpots.set(11, totalFreeSpots);
                                    break;
                                case C9_ID:
                                    freeSpots.set(12, totalFreeSpots);
                                    freeSpots.set(13, totalFreeSpots);
                                    break;
//            case C10_ID:
//                freeSpots[13] = numSpots;
//                return freeSpots[13];
                                case BASKIN_ID:
                                    freeSpots.set(14, totalFreeSpots);
                                    break;
                                default:
                                    freeSpots.set(0, totalFreeSpots);
                                    numSpots = totalFreeSpots;
                            }

                            // Return response when successfully completed
                            callback.onSuccess(area_id, totalFreeSpots);

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

    /******** Connect and Handle Bluetooth Beacon Data  ********/

    // Connect to all beacons nearby
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(this);
    }

    // Obtain information from nearby beacons and store in an array
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon : beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // This is a Eddystone-URL frame
                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                Log.d(TAG, "I see a beacon transmitting a url: " + url +
                        " approximately " + beacon.getDistance() + " meters away.");

                spotId = url.substring(8);
                spotId = spotId.substring(0, spotId.length() - 4);

                if (!bluetoothArray.contains("Spot Number: " + spotId) && beacon.getDistance() < MAX_DISTANCE_FROM_BEACON) {
                    bluetoothArray.add("Spot Number: " + spotId);
                    bluetoothArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /******** Asynchronous Task to Request Parking Authorization  ********/

    // Create parameters for an asynchronous task that will help users claim a parking spot
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

    // Define the asynchronous task to help users claim a parking spot
    private class ClaimSpot extends AsyncTask<claimParams, Void, Void> {

        String inputLine;
        Boolean parked_flag = false;

        @Override
        protected Void doInBackground(claimParams... params) {

            System.out.println("Inside doInBackground");

            try {
                // Set the URL that will be used to connect to the cloud

                String claimSpotUrl = "https://cmpe-123a-18-g11.appspot.com/claim-spot?";
                claimSpotUrl = claimSpotUrl + "message+type=claim+spot&";
                claimSpotUrl = claimSpotUrl + "user+email=" + params[0].claim_email + "&";
                claimSpotUrl = claimSpotUrl + "user+key=" + params[0].claim_userKey + "&";
                claimSpotUrl = claimSpotUrl + "spot+id=" + params[0].claim_spotId;
                System.out.println(claimSpotUrl);

                // Create a link to the URL and get a response
                URL link = new URL(claimSpotUrl);
                URLConnection con = link.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()
                        )
                );
                sharedPref = getApplicationContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                // Check to see what the response was
                // Change the Shared Preferences file depending on the legality of the parking
                while ((inputLine = in.readLine()) != null && !parked_flag) {
                    System.out.println(inputLine);
                    if (inputLine.contains("illegal")) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("parking status", "illegal");
                        editor.putString("spot", params[0].claim_spotId);
                        editor.apply();
                        parking_status = "illegal";
                        parking_spot = params[0].claim_spotId;
                        parked_flag = true;
                    } else if (inputLine.contains("legal")) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("parking status", "legal");
                        editor.putString("spot", params[0].claim_spotId);
                        editor.apply();
                        parking_status = "legal";
                        parking_spot = params[0].claim_spotId;
                        parked_flag = true;
                    } else if (inputLine.contains("spot already claimed")){
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("parking status", "spot already claimed");
                        editor.putString("spot", params[0].claim_spotId);
                        editor.apply();

                        System.out.println(sharedPref.getString("parking status", ""));

                        parking_status = "spot already claimed";
                        parking_spot = params[0].claim_spotId;
                        parked_flag = true;
                    } else {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("parking status", "unknown error");
                        editor.putString("parking status", "");
                        editor.putString("spot", "");
                        editor.apply();

                        parking_status = "";
                        parking_spot = "";
                    }
                }
                parked_flag = false;
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            System.out.println("Inside onPostExecute");

            Intent statusIntent = new Intent(HomeActivity.this, StatusActivity.class);
            startActivity(statusIntent);
        }
    }


    /******** Respond to User's Filter Request  ********/
    // Response when user is done selecting which areas to filter
    @Override
    public void onFinishFilterDialog(boolean[] inputArray) {
        filterLots = inputArray;

        for (int i = 0; i < filterLots.length; i++) {
            if (!filterLots[i]) {
                markerList.get(i + 1).setVisible(false);
            } else {
                markerList.get(i + 1).setVisible(true);
            }
        }
    }

    /******** Define cases for when an Option is selected  ********/
    // Create cases for when an option is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh:
                // When user clicks on refresh, spot data is refreshed,
                // while taking into account which areas were filtered.
                // This is done to save the amount of data usage from the user
                for (int i = 0; i < filterLots.length; i++) {
                    if (filterLots[i]) {
                        getSpotStatistics(idArray[i], new VolleyCallback() {
                            @Override
                            public void onSuccess(String areaId, String result) {
                                System.out.println("areaID " + areaId + ": " + result);

                            }
                        });
                    }
                }

                getSpotStatistics(CAMPUS_ID, new VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result) {
                        availSpots = (TextView) findViewById(R.id.available_spots);
                        availSpots.setText(numSpots + "\n");
                        Log.d(TAG, "Total spots available from Home Screen is " + freeSpots.get(0));
                    }
                });

                return true;
            case R.id.claim_spot:
                // List of nearby spots is displayed, and user selects which spot they have parked in
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setAdapter(bluetoothArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        spotId = bluetoothArray.get(item).substring(13);
                        bluetoothArray.clear();
                        bluetoothArrayAdapter.notifyDataSetChanged();

                        Log.d(TAG, "Spot Id is " + spotId + " and the item selected is " + item);

                        claimParams params = new claimParams(email, userKey, spotId);
                        ClaimSpot claimSpot = new ClaimSpot();
                        claimSpot.execute(params);
                    }
                });
                b.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        bluetoothArray.clear();
                        bluetoothArrayAdapter.notifyDataSetChanged();
                    }
                });
                AlertDialog instance = b.create();
                if (!bluetoothArray.isEmpty()) {
                    instance.show();
                }
                return true;
            case R.id.filter:
                // Create a dialog that lets the user filter out which areas they do not want to see
                DialogFragment dialog = new FilterDialog();
                dialog.show(getSupportFragmentManager(), "FilterDialog");

                return true;
            case R.id.profile:
                // Takes the user to a screen to view their personal information
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.parking_status:
                // Takes the user to a screen to view their current parking status
                Intent statusIntent = new Intent(this, StatusActivity.class);
                startActivity(statusIntent);
                return true;
            case R.id.logout:
                // Logs out the user, and takes the user to the login screen
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("first name", "");
                editor.putString("last name", "");
                editor.putString("permit", "");
                editor.putString("user key", "");
                editor.putString("user email", "");
                editor.apply();

                Intent logOutIntent = new Intent(this, LoginActivity.class);
                logOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(logOutIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    // Stop scanning for beacons when app is paused
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

}
