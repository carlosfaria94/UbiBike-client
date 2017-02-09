package pt.ulisboa.tecnico.cmov.ubibike.Station;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.ubibike.API.ServiceGenerator;
import pt.ulisboa.tecnico.cmov.ubibike.API.StationService;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.Friends.Friends;
import pt.ulisboa.tecnico.cmov.ubibike.History.History;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.Common;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.GlobalClass;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.PagerAdapter;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SharedPrefClass;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SimWifiP2pBroadcastReceiver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Station extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SimWifiP2pManager.GroupInfoListener, TabFragmentOthers.Callbacks {

    private SimWifiP2pDeviceList peersDeviceList;
    private pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station station;
    private Common common;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private SimWifiP2pBroadcastReceiver mReceiver;
    private double userLat = 0;
    private double userLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);

        common = new Common(this);

        Log.i("SharedPref-id", SharedPrefClass.getId(this));
        Log.i("SharedPref-firstName", SharedPrefClass.getFirstName(this));
        Log.i("SharedPref-lastName", SharedPrefClass.getLastName(this));
        Log.i("SharedPref-email", SharedPrefClass.getEmail(this));
        Log.i("SharedPref-secretKey", SharedPrefClass.getSecretKey(this));

        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);

        getStationsFromServer();
        defineLocation();
        updateDrawerUserInformation();
        defineDrawer();
        defineTabs();
    }

    public void onClickBookBike(View view) {
        final View v = view;
        if (common.isOnline()) {
            if (!SharedPrefClass.isBikeBooked(Station.this)) {
                StationService service = ServiceGenerator.createService(StationService.class);
                Call<Void> call = service.bookBike(station.getId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // If returned from the server OK (200 http code)
                            dialogBookBike(station.getName());
                            SharedPrefClass.setBikeBooked(true, Station.this);
                            TextView bikesText = (TextView) findViewById(R.id.bikesAvailable);
                            int pastBikesNumber = Integer.parseInt(bikesText.getText().toString());
                            int currentBikesNumber = pastBikesNumber - 1;
                            bikesText.setText(Integer.toString(currentBikesNumber));
                        } else {
                            // If returned from the server NOT_ACCEPTABLE (406 http code)
                            Snackbar.make(v, R.string.sry, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("Error", t.getMessage());
                        Snackbar.make(v, R.string.server_not, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            } else {
                Snackbar.make(v, R.string.return_bike, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            Snackbar.make(v, R.string.connect_internet, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void dialogBookBike(String stationName) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Booking successful in " + stationName + ". Choose your bike!");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
                        globalVariable.setBooking(true);
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public void setStationSelected(pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station station) {
        this.station = station;
    }

    @Override
    public pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station getStationSelected() {
        return station;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.nav_stations:
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_friends:
                i = new Intent(this, Friends.class);
                generateFriendsNearby();
                startActivity(i);
                break;
            case R.id.nav_history:
                i = new Intent(this, History.class);
                startActivity(i);
                break;
            default:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //metodo utilizado para ir buscar os amigos que estão por perto e guarda-lo na global class
    public void generateFriendsNearby() {
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        SimWifiP2pManager manager = globalVariable.getManager();
        SimWifiP2pManager.Channel channel = globalVariable.getChannel();
        manager.requestGroupInfo(channel, this);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {

        StringBuilder peersStr = new StringBuilder();
        ArrayList<SimWifiP2pDevice> devicesList = new ArrayList<SimWifiP2pDevice>();

        //guardamos a lista de devices por perto
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            if (deviceName.startsWith("B") || deviceName.startsWith("S") || deviceName.startsWith("b") || deviceName.startsWith("s")) {
                Log.d("Nada", " nao adiciona");
            } else {
                SimWifiP2pDevice device = devices.getByName(deviceName);
                devicesList.add(device);
            }
        }

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        if (devicesList != null) {
            globalVariable.generateFriendsNearby(devicesList);
        }
    }

    public List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station> getSortedStations() {
        Log.d("Start", "getSortedStations");
        MyDBHandler dbHandler = new MyDBHandler(Station.this, null, null, 1);
        List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station> stations = dbHandler.getAllStations();
        ArrayList<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station> stationListSorted = new ArrayList<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>();
        for (pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station station : stations) {
            float distance = calcDistance(station.getLatitude(), station.getLongitude());
            pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station newStation = new pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station(
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
        Collections.sort(stationListSorted);
        for (pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station station : stationListSorted) {
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
        Log.d("End", "getSortedStations");
        return stationListSorted;
    }

    /**
     * Get all stations from the server and sort the stations by client distance and save information offline in client DB
     */
    private void getStationsFromServer() {
        if (common.isOnline()) {
            StationService service = ServiceGenerator.createService(StationService.class);
            Call<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> call = service.getStations(""); // Get all stations
            call.enqueue(new Callback<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>>() {
                @Override
                public void onResponse(Call<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> call, Response<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> response) {
                    if (response.isSuccessful()) {
                        List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station> stationServer = response.body();
                        // Store the station information in client DB
                        MyDBHandler dbHandler = new MyDBHandler(Station.this, null, null, 1);
                        for (pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station station : stationServer) {
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
                public void onFailure(Call<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> call, Throwable t) {
                    Log.e("Error", "Getting the stations" + t.getMessage());
                }
            });
        } else {
            Log.e("Error", "User not connected to Internet. Impossible to update the stations");
        }
    }

    public interface Callbacks {
        void showStation();
    }

    /**
     * Update user information in drawer
     */
    private void updateDrawerUserInformation() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_drawer);
        Menu menu = navigationView.getMenu();
        MenuItem userName = menu.findItem(R.id.nav_user_name);
        userName.setTitle(SharedPrefClass.getFirstName(this) + " " + SharedPrefClass.getLastName(this));
        MenuItem userEmail = menu.findItem(R.id.nav_user_email);
        userEmail.setTitle(SharedPrefClass.getEmail(this));
        MenuItem userPoints = menu.findItem(R.id.nav_user_points);
        userPoints.setTitle(Integer.toString(SharedPrefClass.getPoints(this)) + " points");
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
                final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
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

        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

        //Utilizado para guardar na activity as coordenadas atuais do utilizador
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        userLat = globalVariable.getLatitude();
        userLong = globalVariable.getLongitude();
    }

    private void defineTabs() {
        // Added to solve not clicking problem
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_drawer);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        // Define the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText("Details"));
            tabLayout.addTab(tabLayout.newTab().setText("Others"));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            // Define the viewPager to attach the tabLayout
            final ViewPager viewPager = (ViewPager) findViewById(R.id.pagerStations);
            final PagerAdapter adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount());
            if (viewPager != null) {
                viewPager.setAdapter(adapter);
                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout)); // viewPager attached to a page change listener of TabLayout
                tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
            }
        }
    }

    private void defineDrawer() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                }

                public void onDrawerOpened(View drawerView) {
                    drawerView.bringToFront();
                    drawer.getRootView().invalidate();
                    super.onDrawerOpened(drawerView);
                }
            };
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }
    }
}
