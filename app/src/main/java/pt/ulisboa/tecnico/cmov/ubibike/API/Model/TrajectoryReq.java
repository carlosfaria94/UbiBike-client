package pt.ulisboa.tecnico.cmov.ubibike.API.Model;

import org.json.JSONArray;

public class TrajectoryReq {
    private int distTravelled;
    private int pointsEarned;
    private long travelTime;
    private JSONArray pointList;

    public TrajectoryReq(int distTravelled, int pointsEarned, long travelTime, JSONArray pointList) {
        this.distTravelled = distTravelled;
        this.pointsEarned = pointsEarned;
        this.travelTime = travelTime;
        this.pointList = pointList;
    }

    public TrajectoryReq(int distTravelled, int pointsEarned, long travelTime) {
        this.distTravelled = distTravelled;
        this.pointsEarned = pointsEarned;
        this.travelTime = travelTime;
    }


    public int getDistTravelled() {
        return distTravelled;
    }

    public void setDistTravelled(int distTravelled) {
        this.distTravelled = distTravelled;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public long getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(long travelTime) {
        this.travelTime = travelTime;
    }

    public JSONArray getPointList() {
        return pointList;
    }

    public void setPointList(JSONArray pointList) {
        this.pointList = pointList;
    }
}
