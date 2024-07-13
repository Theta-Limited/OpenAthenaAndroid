package com.openathena;

import android.app.Application;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.matthiaszimmermann.location.egm96.Geoid;

import java.io.IOException;
import java.util.HashMap;

public class AthenaApp extends Application { // Android Singleton Class for holding persistent state information between activities
    private DEMParser demParser;

    public static String TAG = AthenaApp.class.getSimpleName();

    public static final double FEET_PER_METER = 3937.0d/1200.0d; // Exact constant for US Survey Foot per Meter
    public static final double FEET_PER_MILE = 5280.0d;

    public static final double DEM_DOWNLOAD_DEFAULT_METERS_DIAMETER = 15000.0d;
    // https://earthscience.stackexchange.com/questions/7283/how-high-must-one-be-for-the-curvature-of-the-earth-to-be-visible-to-the-eye
    public static final double DEM_DOWNLOAD_RETRY_MAX_METERS_DIAMETER = 25000.0d;

    public static Resources resources;

    @Override
    public void onCreate() {
        Log.d(TAG, "AthenaApp onCreate() called");
        super.onCreate();
        resources = getResources();
        try {
            // Android asset manager has a bug with any filename ending in 'gz':
            // https://stackoverflow.com/a/3447148
            // Initialize matthias' library which calculates the offset between WGS84 reference ellipsoid and the EGM96 geoid
            // https://github.com/matthiaszimmermann/EGM96
            // More info: https://epsg.org/crs_4979/WGS-84.html https://epsg.org/crs_5773/EGM96-height.html
            Geoid.init(getAssets().open("EGM96complete.bin")); // op may consume significant memory
            // example usage for calculating offset between WGS84 and EGM96 at a lat/lon:
            // EGMOffsetProvider = new EGM96OffsetAdapter();
            // double height_diff = EGMOffsetProvider.getEGM96OffsetAtLatLon(latitude, longitude)
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.geoid_load_error_msg);
            builder.setPositiveButton(R.string.reset_prefs_text, (DialogInterface.OnClickListener) (dialog, which) -> {
                System.exit(1); // exit the app if the geoid data can't be loaded
            });
        }
    }

    public DEMParser getDEMParser() {
        return demParser;
    }

    public synchronized void setDEMParser(DEMParser demParser) {
        this.demParser = demParser;
    }

    // flag which is set if any participating activity updates the selected point
    // should be cleared by participating activity upon calculation
    public boolean needsToCalculateForNewSelection = false;
    // flag which indicates whether CoT message should be sent on next calculation
    public boolean shouldISendCoT = false;

    // selected image pixel for use in ray offset calculation
    // represents (u, v) in pixels from the top left corner of the image
    private static int selection_x = -1;
    private static int selection_y = -1;
    private static double proportion_selection_x = 0.5d;
    private static double proportion_selection_y = 0.5d;

    public static int get_selection_x() {
        return selection_x;
    }

    public static void set_selection_x(int x) {
        selection_x = x;
    }

    public static int get_selection_y() {
        return selection_y;
    }

    public static void set_selection_y(int y) {
        selection_y = y;
    }

    public static double get_proportion_selection_x() {
        return proportion_selection_x;
    }

    public static void set_proportion_selection_x(double prop_x) {
        proportion_selection_x = prop_x;
    }

    public static double get_proportion_selection_y() {
        return proportion_selection_y;
    }

    public static void set_proportion_selection_y(double prop_y) {
        proportion_selection_y = prop_y;
    }

    private HashMap<String, CharSequence> charSeqMap = new HashMap<String, CharSequence>();

    public void putCharSequence(String key, CharSequence value) {
        charSeqMap.put(key, value);
    }

    public CharSequence getCharSequence(String key) {
        return charSeqMap.get(key);
    }

    private HashMap<String, String> stringMap = new HashMap<String, String>();

    public void putString(String key, String value) {
        stringMap.put(key, value);
    }

    public String getString(String key) {
        return stringMap.get(key);
    }

    private HashMap<String, Integer> intMap = new HashMap<String, Integer>();

    public void putInt(String key, int value) {
        intMap.put(key, value);
    }

    public int getInt(String key) {
        Integer value = intMap.get(key);
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    private HashMap<String, Double> doubleMap = new HashMap<String, Double>();
    public DemCache demCache = null;

    public void putDouble(String key, double value) { doubleMap.put(key, value); }

    public double getDouble(String key) {
        Double value = doubleMap.get(key);
        if (value == null) {
            return 0.0d;
        } else {
            return value.doubleValue();
        }
    }

    private HashMap<String, Boolean> booleanMap = new HashMap<String, Boolean>();

    public void putBoolean(String key, Boolean value) {
        booleanMap.put(key, value);
    }

    public boolean getBoolean(String key) {
        Boolean result = booleanMap.get(key);
        if (result == null) {
            return false; // boolean defaults to false if uninitialized
        } else {
            return result;
        }
    }
}
