package com.example.daniel.ucscparking;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class ParkingMapFragment extends Fragment {

    private GoogleMap parkingMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

//        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        return view;
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        parkingMap = googleMap;
//
//        // Add a marker in Santa Cruz and move the camera
//        LatLng sc = new LatLng(37, -122);
//        parkingMap.addMarker(new MarkerOptions().position(sc).title("Marker in Santa Cruz"));
//        parkingMap.moveCamera(CameraUpdateFactory.newLatLng(sc));
//    }
}
