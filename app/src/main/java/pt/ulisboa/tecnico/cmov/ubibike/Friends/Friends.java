package pt.ulisboa.tecnico.cmov.ubibike.Friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
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

import java.util.ArrayList;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.ulisboa.tecnico.cmov.ubibike.Database.ChatEntry;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.History.History;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Station.Station;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.GlobalClass;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SharedPrefClass;


public class Friends extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SimWifiP2pManager.GroupInfoListener, TabConversations.Callbacks {

    private SimWifiP2pDeviceList peersDeviceList;
    private SimWifiP2pManager manager;
    private Channel channel;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        manager = globalVariable.getManager();
        channel = globalVariable.getChannel();
        manager.requestGroupInfo(channel, this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        updateDrawerUserInformation();

        // Define the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
        tabLayout.addTab(tabLayout.newTab().setText("Conversations"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Define the viewPager to attache the tabLayout
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final FriendsPagerAdapter adapter = new FriendsPagerAdapter (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout)); // viewPager attached to a page change listener of TabLayout
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                manager.requestGroupInfo(channel, Friends.this);


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                manager.requestGroupInfo(channel, Friends.this);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i;
        switch(id) {
            case R.id.nav_stations:
                i = new Intent(this, Station.class);
                startActivity(i);
                break;
            case R.id.nav_friends:
                drawer.closeDrawer(GravityCompat.START);
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



    public void onClickSendPoints(View view)
    {
        Intent i = new Intent(this, SendPointsTo.class);
        manager.requestGroupInfo(channel, Friends.this);
        startActivity(i);
    }

    public void onClickTalk(View view)
    {
        Intent i = new Intent(this, SendMessageTo.class);
        manager.requestGroupInfo(channel, Friends.this);
        startActivity(i);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo)
    {

        StringBuilder peersStr = new StringBuilder();

        ArrayList<SimWifiP2pDevice> devicesList = new ArrayList<SimWifiP2pDevice>();

        //guardamos a lista de devices por perto
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            if(deviceName.startsWith("B") || deviceName.startsWith("S") || deviceName.startsWith("b") || deviceName.startsWith("s"))
            {
                Log.d("Nada", " nao adiciona");
            }
            else {
                SimWifiP2pDevice device = devices.getByName(deviceName);
                devicesList.add(device);
            }
        }

        // compile list of network members

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        if(devicesList != null)
        {
            globalVariable.generateFriendsNearby(devicesList);
        }
    }


    //metodo utilizado para passar a lista de amigos por perto para as diferentes tabs
    @Override
    public HashMap<String,String> getNearbyFriends()
    {
        //Log.d("Aqui", "Aqui");
        /*for (SimWifiP2pDevice device : peersDeviceList.getDeviceList())
        {
            friends.add(device.deviceName); //guardamos o nome dos amigos
        }*/
        HashMap<String,String> friends = new HashMap<String,String>(); //conterá os amigos que estão por perto

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        friends = globalVariable.getFriends();
        return friends;
    }

    public void generateFriends()
    {
        manager.requestGroupInfo(channel, this);
    }

    //obter as conversations que se encontram na BD
    public ArrayList<ChatEntry> getConversations()
    {
        MyDBHandler dbHandler = new MyDBHandler(Friends.this, null,null,1);
        ArrayList<ChatEntry> conversations = dbHandler.getAllEntries();

        Log.d("Conversation", "conversation size " + conversations.size());

        return conversations;

    }

    //obter o chat com este amigo (se existir)
    public String getChat(String friendName)
    {
        MyDBHandler dbHandler = new MyDBHandler(Friends.this, null,null,1);
        if(dbHandler.existEntry(friendName))
        {
            String chat = dbHandler.getChatEntry(friendName).getChat();
            return chat;
        }
        else
            return null;

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
}
