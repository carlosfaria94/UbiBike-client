package pt.ulisboa.tecnico.cmov.ubibike.API.Model;


public class Station implements Comparable<Station> {
    private String id;
    private String name;
    private String location;
    private int bikesAvailable;
    private float latitude, longitude;
    private float distance;

    public Station() {
    }

    public Station(String id, String name, String location, int bikesAvailable, float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.bikesAvailable = bikesAvailable;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Station(String id, String name, String location, int bikesAvailable, float latitude, float longitude, float distance) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.bikesAvailable = bikesAvailable;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public int getBikesAvailable() {
        return bikesAvailable;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Station station) {
        float otherStation = ((Station) station).getDistance();
        return (int) Math.floor(this.distance - otherStation);
    }
}
