package pt.ulisboa.tecnico.cmov.ubibike.Station;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station;
import pt.ulisboa.tecnico.cmov.ubibike.Maps.DisplayRoute;
import pt.ulisboa.tecnico.cmov.ubibike.R;

/**
 * Fragment for details view of Station
 * Here we perform all tasks related to a given selected station
 * Layout: tab_fragment_details.xml
 */
public class TabFragmentDetails extends Fragment implements pt.ulisboa.tecnico.cmov.ubibike.Station.Station.Callbacks {
    private TextView stationName, bikesAvailable, bikesAvailableText, messageDetails;
    private ImageView localizationIcon;
    private View fragView;
    private ArrayAdapter adapter;
    private ListView detailsItems;
    private Button bookBikeBtn;
    private Station station;
    private List<Station> sortedStations;
    private TabFragmentOthers.Callbacks callbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.tab_fragment_details, container, false);

        stationName = (TextView) fragView.findViewById(R.id.stationNameDetails);
        bikesAvailable = (TextView) fragView.findViewById(R.id.bikesAvailable);
        bookBikeBtn = (Button) fragView.findViewById(R.id.bookBtn);
        localizationIcon = (ImageView) fragView.findViewById(R.id.localizationIcon);
        bikesAvailableText = (TextView) fragView.findViewById(R.id.bikesAvailableText);
        detailsItems = (ListView) fragView.findViewById(R.id.detailsItems);
        messageDetails = (TextView) fragView.findViewById(R.id.messageDetails);

        showStation();

        return fragView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                station = callbacks.getStationSelected();
                if (station != null) {
                    updateGUI(station);
                }
            }
        }
    }

    @Override
    public void showStation() {
        callbacks = (TabFragmentOthers.Callbacks) getActivity();
        sortedStations = callbacks.getSortedStations();
        if (sortedStations.isEmpty()) {
            hideGUI();
        } else {
            station = sortedStations.get(0); // Get the closest station available (first in the list)
            callbacks.setStationSelected(station); // Update station variable in Station.class
            updateGUI(station); // Update the GUI with station information
        }
    }

    private void updateGUI(final Station station) {
        messageDetails.setVisibility(View.INVISIBLE);
        stationName.setText(station.getName());
        int numBikesAvailable = station.getBikesAvailable();
        String bikes = Integer.toString(numBikesAvailable);
        bikesAvailable.setText(bikes);

        // List more information about distance and location
        float distance = station.getDistance();
        String distanceToStation = "You are about " + (int) distance + " m away";
        String location = station.getLocation();
        ArrayList<String> details = new ArrayList<String>();
        details.add(distanceToStation);
        details.add(location);

        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, details);
        detailsItems.setAdapter(adapter);
        detailsItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent(getActivity(), DisplayRoute.class);
                        i.putExtra("latitude", station.getLatitude());
                        i.putExtra("longitude", station.getLongitude());
                        i.putExtra("checkingStation",true);
                        startActivity(i);
                    }
                });
    }

    private void hideGUI() {
        bikesAvailableText.setVisibility(View.INVISIBLE);
        localizationIcon.setVisibility(View.INVISIBLE);
        bookBikeBtn.setVisibility(View.INVISIBLE);
        messageDetails.setVisibility(View.VISIBLE);
    }
}