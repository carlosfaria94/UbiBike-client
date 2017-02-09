package pt.ulisboa.tecnico.cmov.ubibike.Friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.ubibike.Database.ChatEntry;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.Login.Login;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.CustomAdapter;


public class TabConversations extends Fragment{

    ListView conv;
    View rootView;
    HashMap<String,String> friendsNearby;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i("OnCreateView","View Created");

        TabConversations.Callbacks callbacks = (TabConversations.Callbacks) getActivity();
        friendsNearby = callbacks.getNearbyFriends();

        rootView = inflater.inflate(R.layout.fragment_tab_conversations, container, false);

        Button plus_button = (Button) rootView.findViewById(R.id.plus_button);
        final Button points_button = (Button) rootView.findViewById(R.id.point_button);
        final Button talk_button = (Button) rootView.findViewById(R.id.talk_button);

        points_button.setVisibility(View.GONE);
        talk_button.setVisibility(View.GONE);

        setupUI();

        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (talk_button.getVisibility() == View.VISIBLE && points_button.getVisibility() == View.VISIBLE) {
                    talk_button.setVisibility(View.GONE);
                    points_button.setVisibility(View.GONE);
                } else {
                    talk_button.setVisibility(View.VISIBLE);
                    points_button.setVisibility(View.VISIBLE);
                }
            }
        });
        return rootView;
    }


    public void setupUI()
    {
        final ArrayList<String[]> conversas = setupConversations();

        Log.d("Conversas", " Conversas size " + conversas.size());

        //ListAdapter adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, conversas);
        ListAdapter adapter = new CustomAdapter(getActivity(),conversas);
        conv = (ListView) rootView.findViewById(R.id.listView2);
        conv.setAdapter(adapter);

        conv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ChatActivity.class);
                String friendName = conversas.get(position)[1];
                i.putExtra("friendSelected", friendName);
                i.putExtra("friendsIP", friendsNearby.get(friendName));
                i.putExtra("chatEntry", conversas.get(position)[3]);
                startActivity(i);

            }

        });
    }

    //interface utilizada para passar informação entre as activities e as tabs
    public interface Callbacks
    {
        HashMap<String,String> getNearbyFriends();
        void generateFriends();
        ArrayList<ChatEntry> getConversations();
        String getChat(String friendName);
    }


    //código corrido quando vamos para esta tab
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                setupUI();
            }
        }
    }

    public ArrayList<String[]>  setupConversations()
    {

        TabConversations.Callbacks callbacks = (TabConversations.Callbacks) getActivity();
        ArrayList<ChatEntry> conversations = callbacks.getConversations();

         ArrayList<String[]> conversas = new ArrayList<String[]>();

        for (ChatEntry entry : conversations)
        {
            String chat = entry.getChat();
            String[] chatParts = chat.split(System.getProperty("line.separator"));
            int chatPartsLength = chatParts.length;
            String mostRecent = chatParts[chatPartsLength-1]; //obter o que foi dito mais recentemente
            String[] mostRecent2 = mostRecent.split(":");

            if(mostRecent2.length == 2)
                conversas.add(new String[]{mostRecent2[1], entry.getFriendName(), entry.getDate(), chat});
            else if(chatPartsLength == 1)
                conversas.add(new String[]{mostRecent2[1], entry.getFriendName(), entry.getDate(), chat});
        }

        return conversas;
    }

}
