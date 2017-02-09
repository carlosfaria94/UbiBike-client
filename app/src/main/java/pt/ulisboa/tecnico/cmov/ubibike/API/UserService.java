package pt.ulisboa.tecnico.cmov.ubibike.API;


import pt.ulisboa.tecnico.cmov.ubibike.API.Model.LoginReq;
import pt.ulisboa.tecnico.cmov.ubibike.API.Model.User;
import pt.ulisboa.tecnico.cmov.ubibike.API.Model.ValidatePointsReq;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {
    /**
     * Register/Create a new User, we define the body content of POST request.
     * We don't expect any server body response, so we Void
     */
    @POST("users/")
    Call<User> createUser(@Body User user);

    @GET("users/{userId}")
    Call<User> getUser(@Path("userId") String userId);

    @POST("users/{userId}/validatePointsReceived")
    Call<Void> validatePointsReceived(@Path("userId") String userId, @Body ValidatePointsReq validate);

    @POST("users/login")
    Call<User> login(@Body LoginReq login);
}