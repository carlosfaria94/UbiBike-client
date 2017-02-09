package pt.ulisboa.tecnico.cmov.ubibike.API.Model;

public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String secretKey;
    private String points;
    private boolean bikeBooked;

    // Register user
    public User(String firstName, String lastName, String email, String password, String secretKey, boolean bikeBooked) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.secretKey = secretKey;
        this.bikeBooked = bikeBooked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public boolean isBikeBooked() {
        return bikeBooked;
    }

    public void setBikeBooked(boolean bikeBooked) {
        this.bikeBooked = bikeBooked;
    }
}
