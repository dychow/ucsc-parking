package com.example.daniel.ucscparking;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Daniel on 5/2/18.
 */

public class BluetoothDialog extends DialogFragment {
//    List<String> lots = Arrays.asList(
//            "East Remote",
//            "West Remote",
//            "West Core",
//            "Cowell",
//            "Stevenson",
//            "Crown",
//            "Merill",
//            "Kresge",
//            "Porter",
//            "Oakes",
//            "Rachel Carson",
//            "College 9",
//            "College 10");
//
//    CharSequence[] items = new CharSequence[]{
//            "East Remote",
//            "West Remote",
//            "West Core",
//            "Cowell",
//            "Stevenson",
//            "Crown",
//            "Merill",
//            "Kresge",
//            "Porter",
//            "Oakes",
//            "Rachel Carson",
//            "College 9",
//            "College 10"
//    };
//
//    static boolean[] checkedItems = {
//            true,
//            true,
//            true,
//            true,
//            true,
//            true,
//            true,
//            true,
//            true,
//            true,
//            true,
//            true,
//            true
//    };
//
//    public static ArrayList<Integer> mSelectedItems;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> bluetoothArray = new ArrayList<String>();


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, bluetoothArray);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.nearby_spots)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
//                        String claimSpotUrl = "https://cmpe-123a-18-g11.appspot.com/claim-spot?";
//                        claimSpotUrl = claimSpotUrl + "message+type=claim+spot&";
//                        claimSpotUrl = claimSpotUrl + "user+email=" + userEmail;
//                        claimSpotUrl = claimSpotUrl + "user+key=" + "userkey&";
//                        claimSpotUrl = claimSpotUrl + "spot+id="+ spotId;

                    }
                });
        return builder.create();

    }

    public ArrayList<String> getBluetoothArray(){
        return bluetoothArray;
    }

    public ArrayAdapter<String> getArrayAdapter(){
        return arrayAdapter;
    }


}
