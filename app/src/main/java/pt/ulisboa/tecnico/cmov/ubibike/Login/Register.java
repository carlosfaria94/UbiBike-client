package pt.ulisboa.tecnico.cmov.ubibike.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.User;
import pt.ulisboa.tecnico.cmov.ubibike.API.ServiceGenerator;
import pt.ulisboa.tecnico.cmov.ubibike.API.UserService;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.Security.Crypt;
import pt.ulisboa.tecnico.cmov.ubibike.Station.Station;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.Common;
import pt.ulisboa.tecnico.cmov.ubibike.Utilities.SharedPrefClass;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Register extends AppCompatActivity {

    EditText inFirstName, inLastName, inEmail, inPassword, inSecretKey;
    ProgressBar pb;
    Common common;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        common = new Common(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inFirstName = (EditText) findViewById(R.id.firstName);
        inLastName = (EditText) findViewById(R.id.lastName);
        inEmail = (EditText) findViewById(R.id.email);
        inPassword = (EditText) findViewById(R.id.password);
        inSecretKey = (EditText) findViewById(R.id.secretKey);
        pb = (ProgressBar) findViewById(R.id.progressBarRegister);
    }

    public void onClickRegister(View view) {
        if (common.isOnline()) {
            String firstName = inFirstName.getText().toString();
            String lastName = inLastName.getText().toString();
            String email = inEmail.getText().toString();
            String password = inPassword.getText().toString();
            String secretKey = inSecretKey.getText().toString();
            if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !password.isEmpty() && !secretKey.isEmpty()) {
                registerUser(firstName, lastName, email, password, secretKey);
            } else {
                Snackbar.make(view, R.string.fill_form, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            Snackbar.make(view, R.string.connect_internet, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    /**
     * Register new user account in server
     *
     * @param firstName
     * @param lastName
     * @param email
     * @param password
     */
    private void registerUser(final String firstName, String lastName, String email, String password, String secretKey) {
        final View parentLayout = findViewById(R.id.registerView);
        pb.setVisibility(View.VISIBLE); // Progressbar it's visible waiting for a server response
        UserService service = ServiceGenerator.createService(UserService.class);
        User user = new User(firstName, lastName, email, password, secretKey, false); // Create a POJO User
        // Calling the service method createUser will convert the properties of User into JSON representation.
        Call<User> call = service.createUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Server response successful
                pb.setVisibility(View.INVISIBLE); // Put Progressbar invisible
                // Check if server respond with successful header code
                if (response.isSuccessful()) {
                    User user = response.body();
                    Snackbar.make(parentLayout, R.string.successfull_register, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                    //como o registo foi bem sucedido, guardamos o first name do user em shared preferences para depois aparecer no chat
                    SharedPrefClass.registerUser(user, Register.this);

                    //inicializamos o TimeStamp que sera utilizado para prevenir replay attacks
                    Crypt.createTimestamp(getApplicationContext());

                    Intent i = new Intent(Register.this, Station.class);
                    startActivity(i);
                    finish();
                } else {
                    Snackbar.make(parentLayout, R.string.registration, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                pb.setVisibility(View.INVISIBLE);
                common.clearForm((ViewGroup) findViewById(R.id.registerView));
                // something went completely south (like no internet connection)
                Log.e("Connection error", t.getMessage());
                Snackbar.make(parentLayout, R.string.server_not, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }
}
