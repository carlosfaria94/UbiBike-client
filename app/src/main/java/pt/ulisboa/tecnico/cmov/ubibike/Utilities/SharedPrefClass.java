package pt.ulisboa.tecnico.cmov.ubibike.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import pt.ulisboa.tecnico.cmov.ubibike.API.Model.User;

public class SharedPrefClass {
    public static void registerUser(User user, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("id", user.getId());
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.putString("email", user.getEmail());
        editor.putString("secretKey", user.getSecretKey());
        editor.putBoolean("bikeBooked", user.isBikeBooked());
        editor.apply();
    }

    public static String getId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPref.getString("id", "null");
    }

    public static String getFirstName(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPref.getString("firstName", "null");
    }

    public static String getLastName(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPref.getString("lastName", "null");
    }

    public static String getEmail(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPref.getString("email", "null");
    }

    public static int getPoints(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String points = sharedPref.getString("points", "0");

        return Integer.parseInt(points);
    }

    public static String updatePoints(String pointsSent, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        int points = Integer.parseInt(sharedPref.getString("points", "0"));
        int updatedPoints = points - Integer.parseInt(pointsSent);
        String newPoints = Integer.toString(updatedPoints);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("points", newPoints);
        editor.apply();

        return newPoints;

    }

    public static void addPoints(String points, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("points", points);
        editor.apply();
    }

    public static String getSecretKey(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPref.getString("secretKey", "null");
    }

    public static Boolean isBikeBooked(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("bikeBooked", false);
    }

    public static void setBikeBooked(Boolean bikeBooked, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("bikeBooked", bikeBooked);
        editor.apply();
    }
}
