package com.openathena;

import android.util.Log;

import org.matthiaszimmermann.location.Location;
import org.matthiaszimmermann.location.egm96.Geoid;

import java.io.Serializable;

// Android will not support conversion from WGS84 to/from EGM96 until Android 14 Upsidedown Cake,
// which is still in beta at time of writing: https://github.com/googlemaps/android-maps-utils/issues/704
public class EGM96OffsetAdapter implements EGMOffsetProvider, Serializable {
    // this initialization must be done by the AthenaApp singleton if not done here
//    EGM96OffsetAdapter() {
//        super();
//        Geoid.init(); // this may consume significant memory resources
//    }

    public static String TAG = EGM96OffsetAdapter.class.getSimpleName();

    /**
     * Get the difference in altitude between the WGS84 ellipsoid and the EGM96 geoid at the given WGS84 lat/lon
     * @param latitude The latitude of the point of interest
     * @param longitude The longitude of the point of interest
     * @return The difference in altitude. Will be positive if the EGM96 geoid is higher than the WGS84 ellipsoid
     */
    @Override
    public double getEGM96OffsetAtLatLon(double latitude, double longitude) {
        double offset = Geoid.getOffset(new Location(latitude, longitude));
        //Log.d(TAG, "Offset at ("+latitude+","+longitude+") is: "+offset);
        return offset;
    }
}
