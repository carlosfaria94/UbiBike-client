package pt.ulisboa.tecnico.cmov.ubibike.History;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.Database.TupleTrajectory;
import pt.ulisboa.tecnico.cmov.ubibike.Maps.DisplayRoute;
import pt.ulisboa.tecnico.cmov.ubibike.R;

public class HistorySelected extends AppCompatActivity implements Serializable{

    private Button trajectoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_selected);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Bundle dataReceived = getIntent().getExtras(); //Argumentos passados pelo intent

        TextView infoDistance = (TextView) findViewById(R.id.infoDistance); //distancia percorrida
        TextView infoPace = (TextView)findViewById(R.id.infoPace); //tempo e ritmo
        TextView infoPoints = (TextView)findViewById(R.id.infoPoints);


        infoDistance.setText(dataReceived.getString("distance") + " m");
        infoPoints.setText(dataReceived.getString("points") + " pts");
        infoPace.setText(dataReceived.getString("time") + " sec");

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(dataReceived.getString("date") + " " + dataReceived.getString("time"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        trajectoryButton = (Button) findViewById(R.id.trajectoryButton);
        trajectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolylineOptions ptList = new PolylineOptions();
                Intent i = new Intent(getApplicationContext(),DisplayRoute.class);
                MyDBHandler dbHandler = new MyDBHandler(HistorySelected.this, null, null, 1);
                ArrayList<TupleTrajectory> trajectoriesList = dbHandler.getAllPendingTrajectories();
                for(TupleTrajectory trajectory : trajectoriesList)
                {
                    if((trajectory.getId()-1) == Integer.parseInt(dataReceived.getString("id")))
                    {
                        try {
                            ptList = getFromJSONArray(trajectory.getPointList());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        i.putExtra("Trajectory", (Serializable) ptList.getPoints());
                    }

                }
                startActivity(i);
            }
        });

    }

    /**
     * Method used to get coordinates from JSONArray.
     * The resulting PolylineOptions object is used to generate a path on the map.
     */
    public PolylineOptions getFromJSONArray(JSONArray array) throws JSONException {
        PolylineOptions pointsList = new PolylineOptions();

        for(int i = 0 ; i < array.length() ; i++)
        {
            LatLng point = new LatLng(array.getJSONObject(i).optDouble("latitude"),array.getJSONObject(i).optDouble("longitude"));

            pointsList.add(point);
        }

        return pointsList;
    }
}
