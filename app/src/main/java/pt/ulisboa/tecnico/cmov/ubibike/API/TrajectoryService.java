package pt.ulisboa.tecnico.cmov.ubibike.API;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.TrajectoryReq;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TrajectoryService {
    /**
     * Register pending trajectories
     * @param trajectory
     * @return
     */
    @POST("users/{userId}/trajectories")
    Call<Void> registerTrajectories(@Body TrajectoryReq trajectory, @Path("userId") String userId);
}
