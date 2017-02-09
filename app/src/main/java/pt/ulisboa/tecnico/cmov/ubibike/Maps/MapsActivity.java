package pt.ulisboa.tecnico.cmov.ubibike.Maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.GlobalClass;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final long INIT_TIME = System.currentTimeMillis()/1000;

    private PolylineOptions mPolyOpt = new PolylineOptions();

    private JSONArray pointList = new JSONArray();

    private GoogleMap map;

    private TextView distance_value;
    private TextView point_value;
    private TextView time_value;

    private int distTravelled = 0;
    private int pointsEarned = 0;
    private long travelTime = 0;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();

        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                map.clear();

                updateMap(map, mPolyOpt, location);

                try {
                    addToJSONArray(pointList, mPolyOpt.getPoints());
                    //Log.i("Location", pointList.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    PolylineOptions x = getFromJSONArray(pointList);
                    Log.i("Location", x.getPoints().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(mPolyOpt.getPoints().size()>=2)
                {
                    distTravelled += calcDistance(mPolyOpt.getPoints().get(mPolyOpt.getPoints().size()-1),
                            mPolyOpt.getPoints().get(mPolyOpt.getPoints().size()-2));
                    pointsEarned = distTravelled*2;
                    travelTime = (System.currentTimeMillis()/1000)-INIT_TIME;
                }

                //atualizamos a classe global com os valores da viagem
                final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
                globalVariable.setRoutesInfo(distTravelled,pointsEarned,travelTime,pointList);

                distance_value.setText(String.valueOf(distTravelled));
                point_value.setText(String.valueOf(pointsEarned));
                time_value.setText(String.valueOf(travelTime));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 70, mLocationListener);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        mPolyOpt.color(Color.LTGRAY);

        distance_value = (TextView) findViewById(R.id.distance_value);
        point_value = (TextView) findViewById(R.id.point_value);
        time_value = (TextView) findViewById(R.id.time_value);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mMap.setMyLocationEnabled(true);

        map = googleMap;
    }

    /**
     * Method used to generate a JSONArray from a list of coordinates.
     */
    public void addToJSONArray(JSONArray array, List<LatLng> item) throws Exception {

        JSONObject data = new JSONObject();
        for(LatLng x : item)
        {
            data.put("latitude", x.latitude);
            data.put("longitude", x.longitude);
        }

        array.put(data);

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

    /**
     * Adds a GeoJSON layer to the referenced map.
     * The layer contains info on the user's current path, which is updated using the user's current location.
     * A PolylineOptions object is used to store all the positions necessary to build the track.
     */
    public void updateMap(GoogleMap map, PolylineOptions options, Location location)
    {

        GeoJsonLayer x = new GeoJsonLayer(map,new JSONObject());
        options.add(new LatLng(location.getLatitude(),location.getLongitude()));

        GeoJsonLineString linestring = new GeoJsonLineString(options.getPoints());
        GeoJsonFeature linestringFeature = new GeoJsonFeature(linestring, "Path", null, null);
        x.addFeature(linestringFeature);

        x.addLayerToMap();
    }

    public float calcDistance(LatLng latlng_x, LatLng latlng_y)
    {
        Location loc_x = new Location("Beginning");
        Location loc_y = new Location("End");

        loc_x.setLatitude(latlng_x.latitude);
        loc_x.setLongitude(latlng_x.longitude);

        loc_y.setLatitude(latlng_y.latitude);
        loc_y.setLongitude(latlng_y.longitude);

        return loc_x.distanceTo(loc_y);
    }
}
