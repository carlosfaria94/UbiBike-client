package pt.ulisboa.tecnico.cmov.ubibike.Database;


import org.json.JSONArray;

public class TupleTrajectory
{
    private int id;
    private int distTravelled;
    private int pointsEarned;
    private long travelTime;
    private JSONArray pointList;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TupleTrajectory(int distTravelled, int pointsEarned, long travelTime, JSONArray pointList, int id, String date) {

        this.distTravelled = distTravelled;
        this.pointsEarned = pointsEarned;
        this.travelTime = travelTime;
        this.pointList = pointList;
        this.id = id;
        this.date = date;

    }
}
