package com.example.daniel.ucscparking;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

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
    //    private final int C19_ID = 15;
//    private final int CS_ID = 18;
//    private final int HAHN_ID = 2;
    private final String BASKIN_ID = "19";
//    private final int OPERS_ID = 2;


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
    private boolean ucsc_flag = false;
//    private int area_id;

    String numSpots = "";
    String freeSpotList = "";
    String[] freeSpotArray;
    ArrayList<String> freeSpots;
    ArrayList<Marker> markerList;

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
            //    C19_ID,
            //    CS_ID,
            //    HAHN_ID,
            //    OPERS_ID,
            BASKIN_ID
    };

    String name;
    String email;


    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    private ArrayAdapter<String> bluetoothArrayAdapter;
    private ArrayList<String> bluetoothArray;

    private ArrayAdapter<String> freeSpotsArrayAdapter;


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

    private class ClaimSpot extends AsyncTask<claimParams, Void, Void> {

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

                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
                in.close();

            } catch (Exception e) {
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
            } catch (Exception e) {
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
//        markerList.add(2, parkingMap.addMarker(new MarkerOptions().position(hahn).title("Hahn Student Services")));
        markerList.add(14, parkingMap.addMarker(new MarkerOptions().position(baskin).title("Jack Baskin Engineering")));
//        markerList.add(2, parkingMap.addMarker(new MarkerOptions().position(opers).title("OPERS"));

//        for(int i = 0; i < filterLots.length; i++){
//            if(!filterLots[i]){
//
//            }
//        }

        parkingMap.moveCamera(CameraUpdateFactory.newLatLng(ucsc));
        parkingMap.moveCamera(CameraUpdateFactory.zoomTo(14.5f));
        parkingMap.setMaxZoomPreference(17.0f);
        parkingMap.setMinZoomPreference(11.0f);
        parkingMap.getUiSettings().setZoomControlsEnabled(true);
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
//                    case "Hahn Student Services":
//                        marker.setSnippet("Free Spots: " + numSpots);
//                        return false;
                    case "Jack Baskin Engineering":
                        marker.setSnippet("Free Spots: " + freeSpots.get(14));
                        return false;
//                    case "OPERS":
//                        marker.setSnippet("Free Spots: " + freeSpots[]);
//                        return false;
                    default:
                        marker.setSnippet("Free Spots: " + freeSpots.get(0));
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

            freeSpots = new ArrayList<String>();
            freeSpotsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, freeSpots);
            for(int i = 0; i < filterLots.length + 1; i++){
                freeSpots.add(i, "0");
            }

//            availSpots = (TextView) findViewById(R.id.available_spots);
//            availSpots.setText(numSpots + "\n");

            for (int i = 0; i < filterLots.length; i++) {
                if (filterLots[i]) {
                    getSpotStatistics(idArray[i], new VolleyCallback() {
                        @Override
                        public void onSuccess(String areaId, String result) {
//                            availSpots = (TextView) findViewById(R.id.available_spots);
//                            availSpots.setText(numSpots + "\n");
                            System.out.println("areaID " + areaId + ": " + result);

                        }
                    });
//                    System.out.println("freeSpot #" + i + ": " + freeSpots.get(i));
//                } else {
//                    freeSpots.set(i, "0");
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
//            Log.d(TAG, "Total spots available from Home Screen is " + freeSpots.get(0));

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
                spotId = spotId.substring(0, spotId.length() - 4);

                if (!bluetoothArray.contains("Spot Number: " + spotId) && beacon.getDistance() < MAX_DISTANCE_FROM_BEACON) {
                    bluetoothArray.add("Spot Number: " + spotId);
                    bluetoothArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public interface VolleyCallback {
        void onSuccess(String areaId, String result);
    }

    private void getSpotStatistics(String areaId, final VolleyCallback callback) {

        String getSpotsUrl = "https://cmpe-123a-18-g11.appspot.com/get-statistics?";
        getSpotsUrl = getSpotsUrl + "message+type=get+statistics&";
        getSpotsUrl = getSpotsUrl + "area+id=" + areaId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getSpotsUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String totalFreeSpots = response.getString("free spot number");
                            String area_id = response.getString("area id");
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
//                                    availSpots = (TextView) findViewById(R.id.available_spots);
//                                    availSpots.setText(numSpots + "\n");
                            }

                            callback.onSuccess(area_id, totalFreeSpots);

//                            availSpots = (TextView) findViewById(R.id.available_spots);
//                            availSpots.setText(numSpots + "\n");

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

//        return numSpots;
    }

    @Override
    public void onFinishFilterDialog(boolean[] inputArray) {
        filterLots = inputArray;
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
//                for (int i = 0; i < filterLots.length; i++) {
//                    if (filterLots[i]) {
//                        getSpotStatistics(idArray[i], new VolleyCallback() {
//                            @Override
//                            public void onSuccess(int areaId, String result) {
//
//                            }
//                        });
//                        System.out.println("freeSpot #" + i + ": " + freeSpots.get(i));
//                    } else {
//                        freeSpots.add(i, "0");
//                    }
//                }
                getSpotStatistics(CAMPUS_ID, new VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result) {

                    }
                });
//                Log.d(TAG, "Total spots available is " + freeSpots.get(0));

                return true;
            case R.id.claim_spot:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setAdapter(bluetoothArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        spotId = bluetoothArray.get(item);
                        bluetoothArray.clear();
                        bluetoothArrayAdapter.notifyDataSetChanged();

                        Log.d(TAG, "Spot Id is " + spotId + " and the item selected is " + item);


                        claimParams params = new claimParams(email, "userKey", spotId);
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
