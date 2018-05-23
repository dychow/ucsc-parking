package com.example.daniel.ucscparking;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Daniel on 3/5/18.
 */

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_home, container, false);

            return view;

        } catch (Exception e) {
            Log.e(TAG, "onCreateView", e);
            throw e;
        }

    }
}
