package pt.ulisboa.tecnico.cmov.ubibike.Maps;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineString;
import com.google.maps.android.geojson.GeoJsonPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.R;

public class DisplayRoute extends FragmentActivity implements OnMapReadyCallback, Serializable {

    private GoogleMap mMap;
    private JSONArray track = new JSONArray();
    private boolean checkingStation;
    private LatLng stationPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_route);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        checkingStation = i.getExtras().getBoolean("checkingStation");

        //Trajectory to print
        List<LatLng> wrap = (List<LatLng>) i.getSerializableExtra("Trajectory");

        if(wrap != null)
        {
            try {
                addToJSONArray(track,wrap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(!checkingStation)
        {
            Snackbar.make(findViewById(android.R.id.content), "Unable to retrieve path information.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

        stationPos = new LatLng(i.getExtras().getFloat("latitude"),i.getExtras().getFloat("longitude"));

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(checkingStation)
        {
            updateMap(mMap,stationPos);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(stationPos));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
        else
        {
            try {
                PolylineOptions mPolyOpt = getFromJSONArray(track);
                updateMap(mMap,mPolyOpt);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mPolyOpt.getPoints().get(0)));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void updateMap(GoogleMap map, PolylineOptions options)
    {

        GeoJsonLayer x = new GeoJsonLayer(map,new JSONObject());

        GeoJsonLineString linestring = new GeoJsonLineString(options.getPoints());
        GeoJsonFeature linestringFeature = new GeoJsonFeature(linestring, "Path", null, null);
        x.addFeature(linestringFeature);

        x.addLayerToMap();
    }

    public void updateMap(GoogleMap map, LatLng marker)
    {
        GeoJsonLayer x = new GeoJsonLayer(map,new JSONObject());

        GeoJsonPoint point = new GeoJsonPoint(marker);
        GeoJsonFeature feature = new GeoJsonFeature(point,"marker",null,null);
        x.addFeature(feature);

        x.addLayerToMap();
    }

    public PolylineOptions getFromJSONArray(JSONArray array) throws JSONException {
        PolylineOptions pointsList = new PolylineOptions();

        for(int i = 0 ; i < array.length() ; i++)
        {
            LatLng point = new LatLng(array.getJSONObject(i).optDouble("latitude"),array.getJSONObject(i).optDouble("longitude"));

            pointsList.add(point);
        }

        return pointsList;
    }

    public void addToJSONArray(JSONArray array, List<LatLng> item) throws Exception {

        for(LatLng x : item)
        {
            JSONObject data = new JSONObject();
            data.put("latitude", x.latitude);
            data.put("longitude", x.longitude);
            array.put(data);
        }

    }
}
