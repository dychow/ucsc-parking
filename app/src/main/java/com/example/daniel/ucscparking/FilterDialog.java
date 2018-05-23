package com.example.daniel.ucscparking;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Daniel on 4/6/18.
 */

public class FilterDialog extends DialogFragment {
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

    CharSequence[] items = new CharSequence[]{
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
        "Jack Baskin Engineering"
    };

    static boolean[] checkedItems = {
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

    public static ArrayList<Integer> mSelectedItems;
    private FilterDialogListener listener;
    SharedPreferences sharedPref;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Track the selected items
        mSelectedItems = new ArrayList();

        // Store the items to keep in a Shared Preferences file
        sharedPref = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        for(int i = 0; i<lots.size(); i++){
            checkedItems[i] = sharedPref.getBoolean(lots.get(i), true);
        }

        //Build the dialog to let users select which areas to keep or filter out
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title)
                .setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            mSelectedItems.add(which);
                        } else if (mSelectedItems.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedItems.remove(Integer.valueOf(which));
                        }
                    }
                })
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Change the Shared Preferences file based on what the user has selected
                        SharedPreferences.Editor editor = sharedPref.edit();

                        for(int i = 0; i < checkedItems.length; i++) {
                            System.out.println(checkedItems[i]);
                            System.out.println(items[i]);
                            editor.putBoolean(lots.get(i), checkedItems[i]);
                        }
                        editor.apply();
                        listener = (FilterDialogListener) getActivity();
                        listener.onFinishFilterDialog(checkedItems);

                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface FilterDialogListener {
        void onFinishFilterDialog(boolean[] inputArray);
    }




}
