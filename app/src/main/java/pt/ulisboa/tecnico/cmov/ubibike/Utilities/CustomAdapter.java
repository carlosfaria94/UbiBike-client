package pt.ulisboa.tecnico.cmov.ubibike.Utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.ubibike.R;

public class CustomAdapter extends ArrayAdapter<String[]>{

    public CustomAdapter(Context context, ArrayList<String[]> history) {
        super(context, R.layout.custom_row, history);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView =  inflater.inflate(R.layout.custom_row, parent, false);

        String singleMessage = getItem(position)[0];
        String singleFriend = getItem(position)[1];
        String date = getItem(position)[2];

        TextView message = (TextView) customView.findViewById(R.id.message);
        TextView friend = (TextView) customView.findViewById(R.id.friend);
        TextView timestamp = (TextView) customView.findViewById(R.id.timestamp);
        ImageView image = (ImageView) customView.findViewById(R.id.image);

        message.setText(singleMessage);
        friend.setText(singleFriend);

       // DateFormat dateFormat = new SimpleDateFormat("MM/dd");
        //Date date = new Date();

        //timestamp.setText(dateFormat.format(date));
        timestamp.setText(date);
        image.setImageResource(R.drawable.smiley);

        return customView;
    }
}
