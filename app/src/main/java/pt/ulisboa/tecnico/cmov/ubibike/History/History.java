package pt.ulisboa.tecnico.cmov.ubibike.History;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.Database.TupleTrajectory;
import pt.ulisboa.tecnico.cmov.ubibike.R;

public class History extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        ArrayList<TupleTrajectory> trajectories = dbHandler.getAllTrajectories();

        final String[] date = new String[trajectories.size()];
        final String[] time = new String[trajectories.size()];
        final String[] distance = new String[trajectories.size()];
        final String[] points = new String[trajectories.size()];

        for (int i = 0; i<trajectories.size();i++)
        {
            date[i] = trajectories.get(i).getDate();
            time[i] = String.valueOf(trajectories.get(i).getTravelTime());
            distance[i] = String.valueOf(trajectories.get(i).getDistTravelled());
            points[i] = String.valueOf(trajectories.get(i).getPointsEarned());
        }

        //adapter da listView
        ListAdapter listAdapter = new HistoryAdapter(this, date, time, distance, points);
        ListView listView = (ListView) findViewById(R.id.listViewHistory);
        listView.setAdapter(listAdapter);


        //código para quando clicamos num dos elementos da lista
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent(History.this, HistorySelected.class);
                        i.putExtra("date", date[position]);
                        i.putExtra("time", time[position]);
                        i.putExtra("distance", distance[position]);
                        i.putExtra("points", points[position]);
                        i.putExtra("id", String.valueOf(position));
                        startActivity(i);

                    }
                }
        );
    }

}
