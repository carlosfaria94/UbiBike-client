package pt.ulisboa.tecnico.cmov.ubibike.Station;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station;
import pt.ulisboa.tecnico.cmov.ubibike.R;

public class StationAdapter extends ArrayAdapter<Station> {
    View customRow;
    TextView stationName, stationLocation, stationDistance, stationNumBikesAvailable;
    List<Station> stations;

    StationAdapter(Context context, List<Station> stations) {
        super(context,R.layout.custom_view_stations, stations);

        this.stations = stations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater stationsInflater = LayoutInflater.from(getContext());
        customRow = stationsInflater.inflate(R.layout.custom_view_stations, parent, false);

        stationName = (TextView) customRow.findViewById(R.id.stationName);
        stationLocation = (TextView) customRow.findViewById(R.id.stationLocation);
        stationDistance = (TextView) customRow.findViewById(R.id.stationDistance);
        stationNumBikesAvailable = (TextView) customRow.findViewById(R.id.numberOfBikesAvailable);

        // Dynamically change the information according to the position
        stationName.setText(stations.get(position).getName());
        stationLocation.setText(stations.get(position).getLocation());
        stationDistance.setText((int) stations.get(position).getDistance() + "m");

        // Bikes available returned from the server
        int bikesAvailable = stations.get(position).getBikesAvailable();
        String bikes = Integer.toString(bikesAvailable);
        stationNumBikesAvailable.setText(bikes);

        return customRow;
    }
}
