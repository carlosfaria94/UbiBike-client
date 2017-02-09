package pt.ulisboa.tecnico.cmov.ubibike.Friends;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.ubibike.Database.ChatEntry;
import pt.ulisboa.tecnico.cmov.ubibike.Database.MyDBHandler;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.GlobalClass;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SharedPrefClass;

public class ChatActivity extends AppCompatActivity {

    ArrayList<String> history = new ArrayList<>();
    String friendName = "";
    private SimWifiP2pSocket mCliSocket = null;
    String friendIP = "";
    String ourName;
    private GlobalClass globalVariable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        globalVariable = (GlobalClass) getApplicationContext();

        Bundle dataReceived = getIntent().getExtras(); //Argumentos passados pelo intent

        friendName = dataReceived.getString("friendSelected");
        friendIP = dataReceived.getString("friendsIP");
        String chat = dataReceived.getString("chatEntry");

        if(chat != null) //se o chat for null quer dizer que ainda nao houve uma conversacao com este user
            setupChat(chat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(friendName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onClickSendPoints(View v)
    {
        Intent i = new Intent(ChatActivity.this, SendPoints.class);
        i.putExtra("friend", friendName);
        String friendIP = globalVariable.getFriendIP(friendName);
        if(friendIP != null) {
            i.putExtra("friendsIP", friendIP);
            startActivity(i);
        }
        else {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this).
                            setMessage("You can't send the points to " + friendName + " because he isn't nearby. Please try again later").
                            setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

            builder.create().show();
        }


    }

    public void onSend(View v)
    {
        if(friendIP != null)
        {
            ListView chat = (ListView) findViewById(R.id.listView);
            EditText message = (EditText) findViewById(R.id.message);

            ListAdapter chat_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, history);

            String content = message.getText().toString();

            ourName = SharedPrefClass.getFirstName(this); //vamos buscar o nosso nome para utilizar no chat

            history.add(ourName + ": " + content);
            chat.setAdapter(chat_adapter);
            message.setText("");

            //atualizamos a DB, primeiro temos que ver se ja existe uma entry com este friend
            updateDB(friendName, content);

            //código para enviar o user
            new SendCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR, content);
        }
        else //se for null quer dizer que o amigo nao esta por perto por isso nao é possivel enviar mensagem
        {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this).
                            setMessage("You can't send the message to " + friendName + " because he isn't nearby. Please try again later").
                            setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

            builder.create().show();
        }
    }

    public void setupChat(String chatEntry)
    {
        ListView chat = (ListView) findViewById(R.id.listView);
        ListAdapter chat_adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,history);

        String[] chatParts = chatEntry.split(System.getProperty("line.separator"));

        for(int i = 0; i < chatParts.length; i++)
        {
            String[] message = chatParts[i].split(":");

            if(message.length == 2)
                history.add(message[0] +": " + message[1]); //[0] = Nome [1]= mensagem
        }
        chat.setAdapter(chat_adapter);
    }


    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            try {

                Log.d("Valor :", "a enviar " + msg[0]);

                 /*formato das mensagens:
                * pontos-> Nome=numeroPontos
                * mensagens-> Nome:mensagem
                */

                String formattedMessage = SharedPrefClass.getFirstName(getApplicationContext()) + ":" + msg[0];

                mCliSocket = new SimWifiP2pSocket(friendIP, 10001);
                mCliSocket.getOutputStream().write((formattedMessage + "\n").getBytes());

                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                sockIn.readLine();

                //mCliSocket.close();
            } catch (IOException e) {
                Log.d("Valor :","falhou ");

                e.printStackTrace();
            }
            //mCliSocket = null;
            Log.d("Valor:  ", "a sair SendComtask");

            return null;
        }
    }

    public void updateDB(String friendName, String message)
    {
        MyDBHandler dbHandler = new MyDBHandler(ChatActivity.this, null,null,1);
        String messageEntry = ourName +":" + message;

        //primeiro vemos se ja existe uma entry com este friend
        if(dbHandler.existEntry(friendName)) //se ja existe fazemos update
        {
            ChatEntry entry = new ChatEntry(friendName, messageEntry);
            dbHandler.updateChatEntry2(entry);
        }
        else  //se nao existe adicionamos uma nova entrada
        {
            ChatEntry entry = new ChatEntry(friendName, messageEntry);
            dbHandler.insertChatEntry(entry);
        }

    }



}
