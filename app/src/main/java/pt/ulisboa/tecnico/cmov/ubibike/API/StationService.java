package pt.ulisboa.tecnico.cmov.ubibike.API;

import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.Station;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface StationService {
    /**
     * Get all stations
     */
    @GET("stations/{stationId}")
    Call<List<Station>> getStations(@Path("stationId") String stationId);

    @GET("stations/{stationId}/bookingBike")
    Call<Void> bookBike(@Path("stationId") String stationId);

    @GET("stations/{stationId}/bookingBike")
    Call<Void> bikeReturned(@Path("stationId") String stationId);
}
