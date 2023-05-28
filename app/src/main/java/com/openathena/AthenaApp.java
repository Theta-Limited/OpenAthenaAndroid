package com.openathena;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

public class AthenaApp extends Application { // Android Singleton Class for holding persistent state information between activities
    private GeoTIFFParser geoTIFFParser;
    public GeoTIFFParser getGeoTIFFParser() {
        return geoTIFFParser;
    }

    public synchronized void setGeoTIFFParser(GeoTIFFParser geoTIFFParser) {
        this.geoTIFFParser = geoTIFFParser;
    }

    // flag which is set if any participating activity updates the selected point
    // should be cleared by participating activity upon calculation
    public boolean needsToCalculateForNewSelection = false;
    // flag which indicates whether CoT message should be sent on next calculation
    public boolean shouldISendCoT = false;

    // selected image pixel for use in ray offset calculation
    // represents (u, v) in pixels from the top left corner of the image
    private int selection_x = -1;
    private int selection_y = -1;

    public int get_selection_x() {
        return selection_x;
    }

    public void set_selection_x(int x) {
        selection_x = x;
    }

    public int get_selection_y() {
        return selection_y;
    }

    public void set_selection_y(int y) {
        selection_y = y;
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
