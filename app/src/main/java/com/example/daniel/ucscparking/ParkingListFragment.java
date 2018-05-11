package com.example.daniel.ucscparking;

import android.content.Context;
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

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {OnListFragmentInteractionListener}
 * interface.
 */
public class ParkingListFragment extends ListFragment implements OnItemClickListener {

    private ArrayList<String> spotArray;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        spotArray = new ArrayList<String>();
        spotArray.add("East Remote");
        spotArray.add("West Remote");
        spotArray.add("West Core");
        spotArray.add("Cowell");
        spotArray.add("Stevenson");
        spotArray.add("Crown");
        spotArray.add("Merill");
        spotArray.add("Kresge");
        spotArray.add("Porter");
        spotArray.add("Oakes");
        spotArray.add("Rachel Carson");
        spotArray.add("College 9");
        spotArray.add("College 10");

        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, spotArray);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + spotArray.get(position), Toast.LENGTH_SHORT).show();

    }

}
