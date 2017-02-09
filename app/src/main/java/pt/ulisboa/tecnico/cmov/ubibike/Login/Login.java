package pt.ulisboa.tecnico.cmov.ubibike.Login;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.API.Model.LoginReq;
import pt.ulisboa.tecnico.cmov.ubibike.API.Model.TrajectoryReq;
import pt.ulisboa.tecnico.cmov.ubibike.API.Model.User;
import pt.ulisboa.tecnico.cmov.ubibike.API.Model.ValidatePointsReq;
import pt.ulisboa.tecnico.cmov.ubibike.API.ServiceGenerator;
import pt.ulisboa.tecnico.cmov.ubibike.API.StationService;
import pt.ulisboa.tecnico.cmov.ubibike.API.TrajectoryService;
import pt.ulisboa.tecnico.cmov.ubibike.API.UserService;
import pt.ulisboa.tecnico.cmov.ubibike.Database.ChatEntry;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.Database.TupleTrajectory;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Security.Crypt;
import pt.ulisboa.tecnico.cmov.ubibike.Station.Station;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.Common;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.GlobalClass;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.NetworkDetector;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SharedPrefClass;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SimWifiP2pBroadcastReceiver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private EditText inEmail, inPassword;
    private ProgressBar pb;
    private Common common;

    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private SimWifiP2pBroadcastReceiver mReceiver;

    private double userLat = 0;
    private double userLong = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1337);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MyDBHandler dbHandler = new MyDBHandler(Login.this, null, null, 1);
        ArrayList<TupleTrajectory> listOfPendingTrajectories = dbHandler.getAllPendingTrajectories();

        ArrayList<ChatEntry> entry = dbHandler.getAllEntries();
        Log.d("Chat", "chat entry size" + entry.size());

        inEmail = (EditText) findViewById(R.id.etEmail);
        inPassword = (EditText) findViewById(R.id.etPassword);
        pb = (ProgressBar) findViewById(R.id.progressBarLogin);

        common = new Common(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);

        //vamos criar o service para o Wifi Direct que irá correr em
        SimWifiP2pSocketManager.Init(getApplicationContext());
        Intent intent = new Intent(this, SimWifiP2pService.class);
        ComponentName service = startService(intent);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Intent serviceIntent = new Intent(this, NetworkDetector.class);
        startService(serviceIntent);

        defineLocation();
    }

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Valor :", "Incomming comecado");

            SimWifiP2pSocket sock = null;
            try {
                mSrvSocket = new SimWifiP2pSocketServer(10001);

            } catch (IOException e) {
                Log.d("Valor :", "Erro Server socket ");
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    sock = mSrvSocket.accept();
                    if (sock != null) {
                        BufferedReader sockIn = new BufferedReader(
                                new InputStreamReader(sock.getInputStream()));
                        String st = sockIn.readLine();
                        Log.d("Valor :", "Recebido " + st);

                        if (st.contains(":")) //mensagem
                        {
                            final String[] parts = st.split(Pattern.quote(":"));

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Received message from " + parts[0], Toast.LENGTH_SHORT).show();
                                }
                            });

                            addEntry(parts[0], parts[1]); //friendName, mensagem
                        } else if (st.contains(";")) //pontos
                        {
                            String[] parts = st.split(Pattern.quote(";"));
                            String tuple = parts[0];
                            String hashedData = parts[1];

                            Log.d("Hash", hashedData);
                            Log.d("Hashed data", Crypt.calculateHMAC(tuple,SharedPrefClass.getSecretKey(Login.this)));

                            String tupleParts[] = tuple.split(",");
                            String senderID = tupleParts[0];
                            String receiverName = tupleParts[1];
                            String timestamp = tupleParts[2];
                            final String points = tupleParts[3];

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(Login.this, "Received " + points + " points ", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(Login.this, "Waiting for points validation", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //se houver ligacao 'a rede fazemos a validacao com o servidor dos pontos recebidos
                            //caso contrario guardamos na DB para quando houver ligacao 'a internet fazermos a validacao
                            if (common.isOnline()) {
                                validatePointsReceivedOnServer(hashedData,senderID,Integer.parseInt(timestamp),Integer.parseInt(points));
                            } else {
                                Log.d("No net", "no net");
                                addEntryPendingPointsTable(senderID, Integer.parseInt(timestamp), Integer.parseInt(points), hashedData);
                            }
                        }
                        sock.getOutputStream().write(("\n").getBytes());
                    }

                } catch (IOException e) {
                    Log.d("Valor:", "IO Exception ");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.d("Valor", "Exception");
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public void addEntryPendingPointsTable(String sender, int timestamp, int points, String hash) {
        Log.d("Pending points", "Pending points");
        MyDBHandler dbHandler = new MyDBHandler(Login.this, null, null, 1);
        dbHandler.insertPendingPointsTable(sender, timestamp, points, hash);
    }

    /**
     * Método que adiciona à base de dados a mensagem recebido do user
     *
     * @param friendName
     * @param message
     */
    public void addEntry(String friendName, String message) {
        MyDBHandler dbHandler = new MyDBHandler(Login.this, null, null, 1);

        //vemos se ja existe uma conversa com este user, se nao , criamos uma nova na db
        if (dbHandler.existEntry(friendName)) {
            Log.d("Chat", "Recebido mensagem de " + friendName);
            ChatEntry entry = new ChatEntry(friendName, message);
            dbHandler.updateChatEntry(entry);

            ChatEntry dbString = dbHandler.getChatEntry(friendName);
            Log.d("Chat: ", "Chat Update: " + dbString.getChat());

        } else //se nao existir introduzimos uma nova entrada
        {
            ChatEntry entry = new ChatEntry(friendName, message);
            dbHandler.insertChatEntry(entry);

            ChatEntry dbString = dbHandler.getChatEntry(friendName);
            Log.d("Chat: ", "Chat: " + dbString.getChat());
        }

    }

    /**
     * If login was successful done
     */
    private void successfulLogin() {
        // Lançamos em background esta async task que permite receber mensagens dos utilizadores
        new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        getStationsFromServer();
        sendToServerPendingTrajectories();
        Intent i = new Intent(Login.this, Station.class);
        startActivity(i);
        finish();
    }

    public void onClickSignIn(View view) {
        if (common.isOnline()) {
            String email = inEmail.getText().toString();
            String password = inPassword.getText().toString();
            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password);
            } else {
                Snackbar.make(view, R.string.fill_form, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            Snackbar.make(view, R.string.connect_internet, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public void onClickSignUp(View view) {
        Intent i = new Intent(this, Register.class);
        startActivity(i);
    }

    /**
     * Get all stations from the server and sort the stations by client distance and save information offline in client DB
     */
    private void getStationsFromServer() {
        StationService service = ServiceGenerator.createService(StationService.class);
        Call<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> call = service.getStations(""); // Get all stations
        call.enqueue(new Callback<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>>() {
            @Override
            public void onResponse(Call<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> call, Response<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> response) {
                Log.i("Client", "Requesting stations from the server");
                if (response.isSuccessful()) {
                    List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station> stationServer = response.body();
                    // Store the station information in client DB
                    MyDBHandler dbHandler = new MyDBHandler(Login.this, null, null, 1);
                    // Sort the station list by distance from the client
                    ArrayList<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station> stationListSorted = new ArrayList<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>();
                    for (pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station station : stationServer) {
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
                    Collections.sort(stationListSorted); // Sort the list by distance
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

                } else {
                    Log.e("Server", "Not successful send the stations");
                }
            }

            @Override
            public void onFailure(Call<List<pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station>> call, Throwable t) {
                Log.e("Error", "Getting the stations" + t.getMessage());
            }
        });
    }

    /**
     * Send tuple and hash HMAC to validate in the server
     */
    public void validatePointsReceivedOnServer(String tupleHash, String senderID, int timestamp, int points) {
        UserService service = ServiceGenerator.createService(UserService.class);

        Log.d("Validate points", "Validate points ");

        ValidatePointsReq validate = new ValidatePointsReq(tupleHash, senderID, SharedPrefClass.getFirstName(Login.this), timestamp, points);
        final int pointsReceived = points;
        Call<Void> call = service.validatePointsReceived(SharedPrefClass.getId(Login.this), validate);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("Server", "Validating points");
                if (response.isSuccessful()) {
                    // If returned from the server OK (200 http code) Valid request. Authorized by the server
                    SharedPrefClass.addPoints(Integer.toString(pointsReceived), getApplicationContext());
                    Toast.makeText(getApplicationContext(), getString(R.string.points_validated) + pointsReceived, Toast.LENGTH_LONG).show();
                } else {
                    // If returned from the server NOT_ACCEPTABLE (406 http code) Validation process failed. Rejected.
                    Toast.makeText(getApplicationContext(), R.string.not_validated, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
               // Log.e("Error", t.getMessage());
                Toast.makeText(getApplicationContext(), R.string.server_not, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Will check in server if the email and password correspond
     *
     * @param email
     * @param password
     */
    private void loginUser(String email, String password) {
        final View parentLayout = findViewById(R.id.login_form);
        pb.setVisibility(View.VISIBLE);
        UserService service = ServiceGenerator.createService(UserService.class);
        LoginReq login = new LoginReq(email, password);
        Call<User> call = service.login(login);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Server response successful
                pb.setVisibility(View.INVISIBLE);
                // Check if server respond with successful header code
                if (response.isSuccessful()) {
                    User user = response.body();
                    //como o login foi bem sucedido, guardamos o first name do user em shared preferences para depois aparecer no chat
                    SharedPrefClass.registerUser(user, Login.this);
                    successfulLogin();
                } else {
                    common.clearForm((ViewGroup) findViewById(R.id.email_login_form));
                    Snackbar.make(parentLayout, R.string.login_failed, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                pb.setVisibility(View.INVISIBLE);
                // something went completely south (like no internet connection)
               // Log.e("Connection error", t.getMessage());
                Snackbar.make(parentLayout, R.string.server_not, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * Send trajectories pending to the server
     */
    public void sendToServerPendingTrajectories() {
        final MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        ArrayList<TupleTrajectory> listOfPendingTrajectories = dbHandler.getAllPendingTrajectories();
        TrajectoryService service = ServiceGenerator.createService(TrajectoryService.class);
        if (!listOfPendingTrajectories.isEmpty()) {
            Log.d("Trajectory size ", Integer.toString(listOfPendingTrajectories.size()));
            for (final TupleTrajectory trajectory : listOfPendingTrajectories) {
                TrajectoryReq trajectoryReq = new TrajectoryReq(
                        trajectory.getDistTravelled(),
                        trajectory.getPointsEarned(),
                        trajectory.getTravelTime(),
                        trajectory.getPointList()
                );
                Call<Void> call = service.registerTrajectories(trajectoryReq, SharedPrefClass.getId(this));
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d("Server", "Sending pending trajectories");
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

    public void addPoints(String points) {
        SharedPrefClass.addPoints(points, this);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);

            //fazemos o que está abaixo para pudermos aceder ao manager noutras activities
            final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
            globalVariable.setManager(mManager);
            globalVariable.setChannel(mChannel);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };

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
}

