package pt.ulisboa.tecnico.cmov.ubibike.Database;


public class TuplePlusHash {
    private String senderID;
    private int timestamp;
    private int points;
    private String tupleHash;

    public void setTupleHash(String tupleHash) {
        this.tupleHash = tupleHash;
    }

    public String getTupleHash() {

        return tupleHash;
    }

    public TuplePlusHash(String senderID, int timestamp, int points, String tupleHash)
    {
        this.senderID = senderID;
        this.timestamp = timestamp;
        this.points = points;
        this.tupleHash = tupleHash;

    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {

        return points;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getSenderID() {
        return senderID;
    }
}
