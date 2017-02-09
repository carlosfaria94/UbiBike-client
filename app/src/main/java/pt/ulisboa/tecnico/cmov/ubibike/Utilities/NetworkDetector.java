package pt.ulisboa.tecnico.cmov.ubibike.Utilities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station;
import pt.ulisboa.tecnico.cmov.ubibike.API.Model.TrajectoryReq;
import pt.ulisboa.tecnico.cmov.ubibike.API.ServiceGenerator;
import pt.ulisboa.tecnico.cmov.ubibike.API.StationService;
import pt.ulisboa.tecnico.cmov.ubibike.API.TrajectoryService;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.Database.TupleTrajectory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkDetector extends BroadcastReceiver {
    Context context;
    private double userLat = 0;
    private double userLong = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (cm == null)
            return;
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            Toast.makeText(context,"Connected. Getting information.", Toast.LENGTH_SHORT).show();
            defineLocation();
            // When the user go online do...
            getStationsFromServer();
            sendToServerPendingTrajectories();
        } else {
            Toast.makeText(context,"Connection lost.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send trajectories pending to the server
     */
    private void sendToServerPendingTrajectories() {
        final MyDBHandler dbHandler = new MyDBHandler(context, null, null, 1);
        ArrayList<TupleTrajectory> listOfPendingTrajectories = dbHandler.getAllPendingTrajectories();
        TrajectoryService service = ServiceGenerator.createService(TrajectoryService.class);
        if (!listOfPendingTrajectories.isEmpty()) {
            Log.d("Trajectory size ", Integer.toString(listOfPendingTrajectories.size()));
            for (final TupleTrajectory trajectory : listOfPendingTrajectories) {
                Log.d("Entrou aqui", "entrou aqui");
                TrajectoryReq trajectoryReq = new TrajectoryReq(
                        trajectory.getDistTravelled(),
                        trajectory.getPointsEarned(),
                        trajectory.getTravelTime()
                );
                Log.d("ID", SharedPrefClass.getId(context));
                Call<Void> call = service.registerTrajectories(trajectoryReq, SharedPrefClass.getId(context));
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d("Client", "Sending pending trajectories");
                        if (response.isSuccessful()) {
                            Log.d("Server", "Successfully send this trajectory:");
                            //alteramos o estado da trajetoria de pendente para nao pendente
                            dbHandler.changeIsPending(trajectory.getId());
                        } else {
                            Log.e("Server", "Not successful send this trajectory:");
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("Connection error", t.getMessage());
                    }
                });
                Log.d("distTravelled", Integer.toString(trajectory.getDistTravelled()));
                Log.d("pointsEarned", Integer.toString(trajectory.getPointsEarned()));
                Log.d("travelTime", Long.toString(trajectory.getTravelTime()));
            }
        } else {
            Log.d("PendingTrajectories", "Empty");
        }
    }

    /**
     * Get all stations from the server and sort the stations by client distance and save information offline in client DB
     */
    private void getStationsFromServer() {
        StationService service = ServiceGenerator.createService(StationService.class);
        Call<List<Station>> call = service.getStations(""); // Get all stations
        call.enqueue(new Callback<List<Station>>() {
            @Override
            public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                Log.i("Client", "Requesting stations from the server");
                if (response.isSuccessful()) {
                    List<Station> stationServer = response.body();
                    // Store the station information in client DB
                    MyDBHandler dbHandler = new MyDBHandler(context, null, null, 1);
                    // Sort the station list by distance from the client
                    ArrayList<Station> stationListSorted = new ArrayList<Station>();
                    for (pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station station : stationServer) {
                        float distance = calcDistance(station.getLatitude(), station.getLongitude());
                        Station newStation = new Station(
                                station.getId(),
                                station.getName(),
                                station.getLocation(),
                                station.getBikesAvailable(),
                                station.getLatitude(),
                                station.getLongitude()
                        );
                        newStation.setDistance(distance);
                        stationListSorted.add(newStation);
                    }
                    Collections.sort(stationListSorted); // Sort the list by distance
                    for (Station station : stationListSorted) {
                        dbHandler.insertStation(
                                station.getId(),
                                station.getName(),
                                station.getLocation(),
                                station.getBikesAvailable(),
                                station.getLatitude(),
                                station.getLongitude(),
                                station.getDistance()
                        );
                    }

                } else {
                    Log.e("Server", "Not successful send the stations");
                }
            }

            @Override
            public void onFailure(Call<List<Station>> call, Throwable t) {
                Log.e("Error", "Getting the stations" + t.getMessage());
            }
        });
    }

    /**
     * Receives as input the coordinates of a station and returns it's distance to the user
     */
    private float calcDistance(float latitude, float longitude) {
        Location cli_loc = new Location("Client");
        Location station_loc = new Location("Station");

        cli_loc.setLongitude(userLong);
        cli_loc.setLatitude(userLat);

        station_loc.setLatitude(latitude);
        station_loc.setLongitude(longitude);

        return cli_loc.distanceTo(station_loc);
    }

    private void defineLocation() {
        //Objecto que obtem a localização actual do utilizador
        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                final GlobalClass globalVariable = (GlobalClass) context.getApplicationContext();
                globalVariable.setLatitude((float) location.getLatitude());
                globalVariable.setLongitude((float) location.getLongitude());
                userLat = globalVariable.getLatitude();
                userLong = globalVariable.getLongitude();
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

        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

        //Utilizado para guardar na activity as coordenadas atuais do utilizador
        final GlobalClass globalVariable = (GlobalClass) context.getApplicationContext();
        userLat = globalVariable.getLatitude();
        userLong = globalVariable.getLongitude();
    }
}
