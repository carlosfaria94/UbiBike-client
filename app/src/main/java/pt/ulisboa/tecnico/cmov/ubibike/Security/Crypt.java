package pt.ulisboa.tecnico.cmov.ubibike.Security;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {

    private static final String HMAC_ALGORITHM = "HmacSHA1";

    public static String calculateHMAC(String data, String secretKey)
    {
        String hmac = "";

        try
        {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);

            //realizar o hmac
            byte[] rawMac = mac.doFinal(data.getBytes());
            //base64-encode hmac
            hmac = Base64.encodeToString(rawMac, Base64.DEFAULT);
        }catch(NoSuchAlgorithmException e)
        {
            Log.d("Exception", "No such algorithm");
            e.printStackTrace();
        }catch(InvalidKeyException e)
        {
            Log.d("Exception", "Invalid secret key exception");
            e.printStackTrace();
        }

        return hmac;
    }

    public static void createTimestamp(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("timestamp", 0);
        editor.apply();

    }

    public static int getTimestamp(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPref.getInt("timestamp",0);
    }

    public static void incrementTimestamp(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int currentTimestamp = sharedPref.getInt("timestamp", 0) + 1;
        editor.putInt("timestamp", currentTimestamp);
        editor.apply();

    }

}
