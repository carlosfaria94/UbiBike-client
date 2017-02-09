package pt.ulisboa.tecnico.cmov.ubibike.Friends;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.ubibike.Database.ChatEntry;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.GlobalClass;

public class SendPointsTo extends AppCompatActivity {

    private ListView listView;
    private EditText searchText;
    private ArrayList<String> friends_sorted = new ArrayList<String>();
    private ArrayList<String> listaFriends;
    int textLength = 0;
    private GlobalClass globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_points_to);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        updateGUI();
    }

    public void updateGUI()
    {
        globalVariable = (GlobalClass) getApplicationContext();
        HashMap<String,String> friendsNearby = globalVariable.getFriends();

        if(friendsNearby != null) {
            listaFriends = new ArrayList<String>();

            final HashMap<String, String> friendsInfo = new HashMap<String, String>();

            for (String friend : friendsNearby.keySet()) {
                Log.d("Valor", friend);
                listaFriends.add(friend);
            }

            listView = (ListView) findViewById(R.id.listView);
            searchText = (EditText) findViewById(R.id.searchText);

            //ao usar o setAdapter na listView adicionar um string array na lista
            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaFriends));

            //quando o utilizador clica num dos amigos
            listView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String item = String.valueOf(parent.getItemAtPosition(position));

                            Intent i = new Intent(SendPointsTo.this, SendPoints.class);
                            i.putExtra("friend", item);
                            i.putExtra("friendsIP", globalVariable.getFriendIP(item));

                            startActivity(i);

                            // Toast.makeText(MainActivity.this, item, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            setupListener();
        }

    }

    public void setupListener()
    {
        //listener utilizado no search, vamos respondendo em real time à medida que o utilizador introduz algo
        searchText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        textLength = searchText.getText().length(); //vamos buscar o tamanho do que o user introduziu
                        friends_sorted.clear(); //fazemos clear para atualizarmos a lista
                        for (int i = 0; i < listaFriends.size(); i++) //percorremos a lista de amigos
                        {
                            if (textLength <= listaFriends.get(i).length()) // comparamos o tamanho do nome introduzido com o nome do amigo, se o nome for maior, então nao é o nome a ser procurado
                            {
                                //ignoreCase -> determina se duas strings contém os mesmos dados, case insensitive
                                if (searchText.getText().toString().equalsIgnoreCase((String) listaFriends.get(i).subSequence(0, textLength)))
                                    friends_sorted.add(listaFriends.get(i));
                            }
                        }

                        listView.setAdapter(new ArrayAdapter<String>(SendPointsTo.this, android.R.layout.simple_list_item_1, friends_sorted));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }
        );
    }

}
