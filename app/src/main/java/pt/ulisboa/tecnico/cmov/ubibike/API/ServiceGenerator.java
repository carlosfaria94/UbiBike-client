package pt.ulisboa.tecnico.cmov.ubibike.API;


import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The ServiceGenerator is our API/HTTP client heart.
 * Defines one method to create a basic REST adapter for a given class/interface
 *
 */
public class ServiceGenerator {

    // Genymontion: http://10.0.3.2:8080/
    // AVD: http://10.0.2.2:8080/
    // TODO: Mudar o IP para AVD
    public static final String API_BASE_URL = "http://10.0.2.2:8080/"; // Server URL

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()); // JSON converter

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}
