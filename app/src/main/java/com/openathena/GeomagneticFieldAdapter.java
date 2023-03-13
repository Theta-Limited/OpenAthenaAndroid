package com.openathena;

// https://developer.android.com/reference/android/hardware/GeomagneticField#getDeclination()
import android.hardware.GeomagneticField;

public class GeomagneticFieldAdapter implements DeclinationProvider {

    public float getMagDeclinationFromLatLonAlt(float lat, float lon, float alt) {
        GeomagneticField f = new GeomagneticField(lat, lon, alt, System.currentTimeMillis());
        return f.getDeclination();
    }
}
