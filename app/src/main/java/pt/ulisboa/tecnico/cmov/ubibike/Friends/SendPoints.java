package pt.ulisboa.tecnico.cmov.ubibike.Friends;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Security.Crypt;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SharedPrefClass;

public class SendPoints extends AppCompatActivity {

    private String friendName;
    private int numeroPontos;
    private SimWifiP2pSocket mCliSocket = null;
    String friendIP = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_points);

        //back button da action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //SharedPrefClass.saveInfo("points",Integer.toString(50),this);

        numeroPontos = SharedPrefClass.getPoints(this); //vamos buscar o numero de pontos atual do user

        Bundle dataReceived = getIntent().getExtras();

        friendName = dataReceived.getString("friend");
        friendIP = dataReceived.getString("friendsIP");

        updateGUI(Integer.toString(numeroPontos));
    }

    public void onClickContinue(View view)
    {
        EditText pontos = (EditText)findViewById(R.id.inputUser);
        final String pontosAEnviar = pontos.getText().toString();
        if(!pontosAEnviar.equals(""))
        {
            int pontosEnviar = Integer.parseInt(pontosAEnviar);

            if (pontosEnviar <= numeroPontos) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this).
                                setMessage("Send " + pontosEnviar + " points to " + friendName + " ?").
                                setPositiveButton("Send", new DialogInterface.OnClickListener() {
                                    @Override//se o user carregar sim
                                    public void onClick(DialogInterface dialog, int which) {
                                        //código para enviar pontos para o user
                                        new SendCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pontosAEnviar);
                                        dialog.dismiss();

                                        //atualizamos os pontos do user
                                        String newPoints = SharedPrefClass.updatePoints(pontosAEnviar, SendPoints.this);
                                        updateGUI(newPoints);
                                        Toast.makeText(SendPoints.this, "Sucessfully sent " + pontosAEnviar + " to " + friendName, Toast.LENGTH_SHORT).show();

                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.create().show();
            } else if (pontosEnviar == 0 || pontosEnviar < 0) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this).
                                setMessage("Please insert a value greater than 0").
                                setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                builder.create().show();
            } else //se o utilizador quiser enviar pontos que nao tem, mostramos uma mensagem de erro
            {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this).
                                setMessage("You don't have that much points to send. Please insert a value less or equal than " + numeroPontos).
                                setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                builder.create().show();
            }
        }
        else
        {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this).
                            setMessage("Please insert points to send").
                            setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

            builder.create().show();
        }
    }


    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            try {

                Log.d("Valor :", "a enviar " + msg[0]);

                mCliSocket = new SimWifiP2pSocket(friendIP, 10001);

                /*formato das mensagens:
                * pontos-> Sender,Receiver,timestamp,pontos;hash(Sender,Receiver,timestamp,pontos)
                * mensagens-> Nome:mensagem
                */
                //String formattedMessage = friendName + "=" + msg[0];

                //Incrementamos o timestamp, cada envio de pontos ira ter um timestamp diferente
                Crypt.incrementTimestamp(getApplicationContext());
                String tuple = SharedPrefClass.getId(getApplicationContext()) + "," + friendName + "," +
                        Crypt.getTimestamp(getApplicationContext()) + "," + msg[0];

                String dataHashed = Crypt.calculateHMAC(tuple, SharedPrefClass.getSecretKey(SendPoints.this));

                String sendData = tuple + ";" + dataHashed;
                Log.d("HMAC", sendData);

                mCliSocket.getOutputStream().write((sendData + "\n").getBytes());


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

    public void updateGUI(String points)
    {
        TextView text = (TextView)findViewById(R.id.text);
        TextView textSend = (TextView)findViewById(R.id.textSend);
        TextView textViewPontos = (TextView)findViewById(R.id.textViewPontos);
        EditText pontos = (EditText)findViewById(R.id.inputUser);

        pontos.setText("");

        text.setText("To: " + friendName);
        textViewPontos.setText("You currently have " + points + " points" );
        textSend.setText("Please insert the amount of points you want to send to " + friendName + ": ");
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        Toast.makeText(this, "Back pressed", Toast.LENGTH_SHORT);
        return super.onOptionsItemSelected(item);
    }
}
