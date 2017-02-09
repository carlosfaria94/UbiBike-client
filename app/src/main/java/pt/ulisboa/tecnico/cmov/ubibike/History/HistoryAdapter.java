package pt.ulisboa.tecnico.cmov.ubibike.History;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.ubibike.R;

class HistoryAdapter extends ArrayAdapter<String> {

    View customRow;
    String[] date, time, distance, points;
    TextView dateText, timeText, distanceText, pointsText;
    ImageView mapView;

    HistoryAdapter(Context context, String[] date, String[] time, String[] distance, String[] points) {
        super(context, R.layout.custom_view_history, date);

        this.date = date;
        this.time = time;
        this.distance = distance;
        this.points = points;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        customRow = inflater.inflate(R.layout.custom_view_history, parent, false);

        dateText = (TextView) customRow.findViewById(R.id.dateView);
        mapView = (ImageView) customRow.findViewById(R.id.availability_shape);
        timeText = (TextView) customRow.findViewById(R.id.timeView);
        distanceText = (TextView) customRow.findViewById(R.id.distanceView);
        pointsText = (TextView) customRow.findViewById(R.id.pointsView);

        // Dynamically change the information according to the position
        dateText.setText(date[position]);
        timeText.setText(time[position]);
        distanceText.setText(distance[position]);
        pointsText.setText(points[position]);

        return customRow;
    }

}
