package pt.ulisboa.tecnico.cmov.ubibike.Friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;


import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.ubibike.Database.ChatEntry;
import pt.ulisboa.tecnico.cmov.ubibike.Friends.Friends;
import pt.ulisboa.tecnico.cmov.ubibike.Friends.TabConversations;
import pt.ulisboa.tecnico.cmov.ubibike.R;


public class TabFriendsList extends Fragment {

    ListView friends;
    ArrayList<String> amigos;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View x = inflater.inflate(R.layout.fragment_tab_friends_list, container, false);
        view = x;

        TabConversations.Callbacks callbacks = (TabConversations.Callbacks) getActivity();
        HashMap<String,String> friendsNearby = callbacks.getNearbyFriends();
        if(friendsNearby != null)
            updateGUI(friendsNearby);

        return x;
    }


    //código corrido quando vamos para esta tab
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
                if (getView() != null) {

                    TabConversations.Callbacks callbacks = (TabConversations.Callbacks) getActivity();
                    //callbacks.generateFriends(); //vamos gerar os amigos de novo (podem ter entrado novos no grupo)
                    HashMap<String, String> friendsNearby = callbacks.getNearbyFriends();
                    if(friendsNearby != null)
                         updateGUI(friendsNearby);
                }
            }
    }

    //update na lista de amigos visivel para o user
    public void updateGUI(final HashMap<String,String> friendsNearby)
    {
        amigos = new ArrayList<String>();
        final HashMap<String,String> friendsInfo = new HashMap<String,String>();

        for(String friend : friendsNearby.keySet())
        {
            Log.d("Valor", friend);
            amigos.add(friend);
        }


        ListAdapter adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, amigos);
        friends = (ListView) view.findViewById(R.id.friendsList);
        friends.setAdapter(adapter);
        Log.d("Valor", Integer.toString(adapter.getCount()));


        friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra("friendSelected", amigos.get(position));
                i.putExtra("friendsIP", friendsNearby.get(amigos.get(position)));
                String chatEntry = getChat(amigos.get(position));
                i.putExtra("chatEntry",chatEntry );
                startActivity(i);
            }

        });
    }

    public String getChat(String friendName)
    {
        TabConversations.Callbacks callbacks = (TabConversations.Callbacks) getActivity();
        String chat = callbacks.getChat(friendName);
        return chat;
    }

}
