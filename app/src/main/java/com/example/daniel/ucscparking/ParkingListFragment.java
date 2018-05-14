package com.example.daniel.ucscparking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {OnListFragmentInteractionListener}
 * interface.
 */
public class ParkingListFragment extends ListFragment implements OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private ArrayList<String> spotArray;
    ArrayAdapter adapter;

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


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Filter the parking areas depending on what is in the Shared Preferences file
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        for(int i = 0; i<lots.size(); i++){
            filterLots[i] = sharedPref.getBoolean(lots.get(i), true);
        }

        spotArray = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, spotArray);
        setListAdapter(adapter);

        // Populate the array that will be displayed
        for(int i = 0; i < filterLots.length; i++){
            if(filterLots[i] && !spotArray.contains(lots.get(i))){
                spotArray.add(lots.get(i));
            } else if(!filterLots[i] && spotArray.contains(lots.get(i))) {
                spotArray.remove(lots.get(i));
            }
        }

        getListView().setOnItemClickListener(this);
    }

    // Create a listener to determine whether the Shared Preferences file has been changed
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        for(int i = 0; i<lots.size(); i++){
            filterLots[i] = sharedPreferences.getBoolean(lots.get(i), true);
            if(filterLots[i] && !spotArray.contains(lots.get(i))){
                spotArray.add(lots.get(i));
            } else if(!filterLots[i] && spotArray.contains(lots.get(i))) {
                spotArray.remove(lots.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Takes user to the more detailed view of the parking area
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + spotArray.get(position), Toast.LENGTH_SHORT).show();

        Intent detailedListIntent = new Intent(getActivity(), DetailedListActivity.class);
        detailedListIntent.putExtra("area", spotArray.get(position));
        startActivity(detailedListIntent);


    }

}
