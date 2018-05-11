package com.example.daniel.ucscparking;

import android.app.Dialog;
import android.content.DialogInterface;
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


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        mSelectedItems = new ArrayList();  // Where we track the selected items
        CharSequence[] charSequence = lots.toArray(new CharSequence[lots.size()]);
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
                        for(int i = 0; i < checkedItems.length; i++) {
                            System.out.println(checkedItems[i]);
                        }
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
