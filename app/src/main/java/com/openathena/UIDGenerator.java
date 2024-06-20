package com.openathena;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UIDGenerator {

    // list of NATO-phonetic code names for alphabet characters
    private static final List<String> PHONETIC_ALPHABET = Arrays.asList(
            "Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot",
            "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima",
            "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo",
            "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "Xray",
            "Yankee", "Zulu"
    );

    public interface HashCallback {
        void onHashComputed(String hash);
    }

    public static String getDeviceUniqueID(Context context) {
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidID;
    }

    public static String getDeviceHostnamePhonetic(Context context) {
        String uniqueID = getDeviceUniqueID(context);
        String phoneticUID = "unknown";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(uniqueID.getBytes());
            byte[] digest = md.digest();

            int index = ((digest[0] & 0xFF) << 16 | (digest[1] & 0xFF) << 8 | (digest[2] & 0xFF)) % 2600;
            String phonetic = PHONETIC_ALPHABET.get(index / 100);
            int digits = index % 100;

            phoneticUID = phonetic + String.format("%02d", digits);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return phoneticUID;
    }

    public static String buildUIDString(Context context, long eventuid) {
        return "OpenAthena-" + getDeviceHostnamePhonetic(context) + "-" + Long.toString(eventuid);
    }
}
