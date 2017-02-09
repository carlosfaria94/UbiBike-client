package pt.ulisboa.tecnico.cmov.ubibike.Station;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.Common;

/**
 * Fragment for display other stations
 * Here we perform all tasks related to list view of all stations available to the user
 * Layout: tab_fragment_others.xml
 */
public class TabFragmentOthers extends Fragment {
    ListView listStations;
    TextView messageOthers;
    ListAdapter stationsAdapter;
    List<Station> sortedStations;
    Common common;
    View fragView;
    Callbacks callbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.tab_fragment_others, container, false);
        messageOthers = (TextView) fragView.findViewById(R.id.messageOthers);
        listStations = (ListView) fragView.findViewById(R.id.otherStations);
        callbacks = (Callbacks) getActivity();
        common = new Common(getContext());

        sortedStations = callbacks.getSortedStations();
        if (sortedStations.isEmpty()) {
            listStations.setVisibility(View.INVISIBLE);
            messageOthers.setText(R.string.stations_not);
            messageOthers.setVisibility(View.VISIBLE);
        } else {
            updateGUI(sortedStations);
            onItemClickListener(sortedStations);
        }

        return fragView;
    }

    private void updateGUI(List<Station> stations) {
        // Send info about stations do StationAdapter to customize each row
        stationsAdapter = new StationAdapter(getActivity(), stations);
        listStations.setAdapter(stationsAdapter);
    }

    private void onItemClickListener(List<Station> stations) {
        final List<Station> stationList = stations;
        listStations.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.i("onItemClickListener", stationList.get(position).getName());
                        callbacks.setStationSelected(stationList.get(position));
                    }
                }
        );
    }

    public interface Callbacks {
        void setStationSelected(Station station);
        Station getStationSelected();
        List<Station> getSortedStations();
    }
}