package pt.ulisboa.tecnico.cmov.ubibike.API.Model;


public class ValidatePointsReq {
    private String hmac, sender, receiverFirstName;
    private int timestamp, points;

    public ValidatePointsReq(String hmac, String sender, String receiverFirstName, int timestamp, int points) {
        this.hmac = hmac;
        this.sender = sender;
        this.receiverFirstName = receiverFirstName;
        this.timestamp = timestamp;
        this.points = points;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiverFirstName() {
        return receiverFirstName;
    }

    public void setReceiverFirstName(String receiverFirstName) {
        this.receiverFirstName = receiverFirstName;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
