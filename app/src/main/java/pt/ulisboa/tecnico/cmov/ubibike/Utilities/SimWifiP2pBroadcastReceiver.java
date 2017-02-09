package pt.ulisboa.tecnico.cmov.ubibike.Utilities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.ulisboa.tecnico.cmov.ubibike.API.ServiceGenerator;
import pt.ulisboa.tecnico.cmov.ubibike.API.StationService;
import pt.ulisboa.tecnico.cmov.ubibike.Maps.MapsActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private Activity mActivity;
    private boolean isTravelling = false;
    private Common common;

    public SimWifiP2pBroadcastReceiver(Activity activity) {
        super();
        this.mActivity = activity;
    }

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();
        common = new Common(context);
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the Termite service changes state:
            // - creating the service generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                //Toast.makeText(mActivity, "WiFi Direct enabled", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(mActivity, "WiFi Direct disabled", Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

            //Toast.makeText(mActivity, "Peer list changed", Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();

            GlobalClass globalVariable = (GlobalClass) context.getApplicationContext();

            checkSituation(ginfo, globalVariable, context);

            //Toast.makeText(mActivity, "Network membership changed", Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            //Toast.makeText(mActivity, "Group ownership changed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Verifica se existe alguma bike perto do utilizador.
     */
    public boolean isBikeInRange(SimWifiP2pInfo info) {
        boolean checkBike = false;

        for (String nextIter : info.getDevicesInNetwork()) {
            if (nextIter.startsWith("b")) {
                checkBike = true;
            }
        }
        return checkBike;
    }

    /**
     * Verifica se o utilizador está numa estação.
     */
    public boolean isStationInRange(SimWifiP2pInfo info) {
        boolean checkStation = false;

        for (String nextIter : info.getDevicesInNetwork()) {
            if (nextIter.startsWith("s")) {
                checkStation = true;
            }
        }
        return checkStation;
    }

    /**
     * Verifica se o utilizador iniciou ou terminou o seu percurso.
     * Se está a iniciar, a MapActivity é iniciada.
     * Se está a terminar, guarda a informação do percurso.
     */
    public void checkSituation(SimWifiP2pInfo info, GlobalClass gb, Context context) {
        if (gb.getBooking()) {
            if (isStationInRange(info)) {
                if (isBikeInRange(info)) {
                    //Inicio de uma viagem
                    Intent i = new Intent(context, MapsActivity.class);
                    context.startActivity(i);
                    gb.setBooking(false);
                    isTravelling = true;
                }
            }
        } else {
            if (isStationInRange(info)) {
                if (isBikeInRange(info)) {
                    if (isTravelling) {
                        isTravelling = false;
                        SharedPrefClass.setBikeBooked(false, context);
                        bikeReturned();
                        Log.d("Chegou aqui", "chegou aqui");
                        //a viagem acabou, vamos guardar a informacao que temos na DB
                        GlobalClass.saveTrajectoryDB(context);
                    }
                }
            }
        }
    }

    private void bikeReturned() {
        if (common.isOnline()) {
            StationService service = ServiceGenerator.createService(StationService.class);
            Call<Void> call = service.bikeReturned("5731f3821a02b8060dcbf4eb");
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Error", t.getMessage());
                }
            });
        } else {
            Log.e("bikeReturned", "Not connected to Internet");
        }
    }
}
