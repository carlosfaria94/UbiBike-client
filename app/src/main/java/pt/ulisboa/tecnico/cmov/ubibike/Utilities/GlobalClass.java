package pt.ulisboa.tecnico.cmov.ubibike.Utilities;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;

public class GlobalClass extends Application{

    private SimWifiP2pManager mManager;
    private Channel channel;
    private HashMap<String,String> friendsInfo; //key é o nome, value é o endereco IP
    private boolean booking;

    private static int distTravelled;
    private static int pointsEarned;
    private static long travelTime;
    private static JSONArray pointList;
    private static float latitude;
    private static float longitude;

    public void setRoutesInfo(int distTravelled, int pointsEarned, long travelTime, JSONArray pointList)
    {
        this.distTravelled = distTravelled;
        this.pointsEarned = pointsEarned;
        this.travelTime = travelTime;
        this.pointList = pointList;
        this.latitude = 0;
        this.longitude = 0;
    }

    public float getLatitude()
    {
        return this.latitude;
    }

    public float getLongitude()
    {
        return this.longitude;
    }

    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    public int getDistTravelled()
    {
        return distTravelled;
    }

    public int getPointsEarned()
    {
        return pointsEarned;
    }

    public long getTravelTime()
    {
        return travelTime;
    }

    public JSONArray getPointList()
    {
        return pointList;
    }

    public boolean getBooking()
    {
        return this.booking;
    }

    public void setBooking(boolean booking)
    {
        this.booking = booking;
    }

    public void setChannel(Channel chan)
    {
        channel = chan;
    }

    public Channel getChannel()
    {
        return channel;
    }

    public void setManager(SimWifiP2pManager manager)
    {
        mManager = manager;
    }

    public SimWifiP2pManager getManager()
    {
        return mManager;
    }

    public void generateFriendsNearby(ArrayList<SimWifiP2pDevice> peersDeviceList)
    {
        friendsInfo = new HashMap<String,String>();
        for (SimWifiP2pDevice device : peersDeviceList)
        {
            friendsInfo.put(device.deviceName, device.getVirtIp());
            Log.d("IPINFO", device.getVirtIp());
        }
    }

    public HashMap<String, String> getFriends()
    {
        return friendsInfo;
    }

    public String getFriendIP(String friendName)
    {
        HashMap<String,String> friends = getFriends();
        if(friends.containsKey(friendName)) {
            String friendIP = friends.get(friendName);
            return friendIP;
        }
        else
            return null;

    }

    public static void saveTrajectoryDB(Context context)
    {
        GlobalClass gc = new GlobalClass();
        MyDBHandler dbHandler = new MyDBHandler(context, null,null,1);

        dbHandler.insertPendingTrajectoriesTable(distTravelled, pointsEarned, travelTime, pointList);
    }

}
