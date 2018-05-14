package com.example.daniel.ucscparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Daniel on 5/4/18.
 */

public class DetailedListActivity extends AppCompatActivity {

    private static final String TAG = "DetailedListActivity";

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


    // UI references.
    private TextView mLotView;
    private TextView mSpotView;
    private TextView mListView;

    String freeSpotList = "";
    String[] freeSpotArray;
    ArrayList<String> freeSpots;
    private ArrayAdapter<String> freeSpotsArrayAdapter;

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
        setContentView(R.layout.activity_parking);

        mLotView = (TextView) findViewById(R.id.parking_lot);
        mSpotView = (TextView) findViewById(R.id.lot_spots);
        mListView = (TextView) findViewById(R.id.parking_spots);

        mLotView.setText(R.string.not_parked_status);

        Intent i = getIntent();
        String areaCode = i.getStringExtra("area");
        mLotView.setText(areaCode);

        freeSpots = new ArrayList<String>();
        freeSpotsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, freeSpots);

        switch (areaCode) {
            case "East Remote":
                getSpotStatistics(EAST_REMOTE_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "West Remote":
                getSpotStatistics(WEST_REMOTE_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "West Core":
                getSpotStatistics(WEST_CORE_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "Cowell":
                getSpotStatistics(COWELL_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "Stevenson":
                getSpotStatistics(STEVENSON_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

            case "Crown":
                    getSpotStatistics(CROWN_ID, new DetailedListActivity.VolleyCallback() {
                        @Override
                        public void onSuccess(String areaId, String result, String freeList) {
                            mSpotView = (TextView) findViewById(R.id.lot_spots);
                            mListView = (TextView) findViewById(R.id.parking_spots);

                            mSpotView.setText("Free Spots: " + result + "\n");
                            mListView.setText("Spot List: \n" + freeList);
                            Log.d(TAG, "Total spots available from Home Screen is " + result);
                        }
                    });

                    break;
            case "Merill":
                getSpotStatistics(MERILL_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "Kresge":
                getSpotStatistics(KRESGE_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "Porter":
                getSpotStatistics(PORTER_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "Oakes":
                getSpotStatistics(OAKES_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "Rachel Carson":
                getSpotStatistics(RC_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "College Nine":
                getSpotStatistics(C9_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
            case "College Ten":
                getSpotStatistics(C10_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
//                    case "Hahn Student Services":
//                        marker.setSnippet("Free Spots: " + numSpots);
//                        break;
            case "Jack Baskin Engineering":
                getSpotStatistics(BASKIN_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText("Free Spots: " + result + "\n");
                        mListView.setText("Spot List: \n" + freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
//                    case "OPERS":
//                        marker.setSnippet("Free Spots: " + freeSpots[]);
//                        break;
            default:
                getSpotStatistics(CAMPUS_ID, new DetailedListActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String areaId, String result, String freeList) {
                        mSpotView = (TextView) findViewById(R.id.lot_spots);
                        mListView = (TextView) findViewById(R.id.parking_spots);

                        mSpotView.setText(result + "\n");
                        mListView.setText(freeList);
                        Log.d(TAG, "Total spots available from Home Screen is " + result);
                    }
                });

                break;
        }
    }

    public interface VolleyCallback {
        void onSuccess(String areaId, String result, String freeList);
    }

    private void getSpotStatistics(String areaId, final DetailedListActivity.VolleyCallback callback) {

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
                                    if (freeSpotArray[i].equals("]")) {
                                        freeSpotArray[i] = "";
                                    }
                                } else if (freeSpotArray[i].contains(c2)) {
                                    freeSpotArray[i] = freeSpotArray[i].split("\\]")[0];
                                }

                                free += freeSpotArray[i] + "\n";

                            }

                            callback.onSuccess(area_id, totalFreeSpots, free);

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
        MySingleton.getInstance(DetailedListActivity.this).addToRequestQueue(jsonObjectRequest);
    }

}
