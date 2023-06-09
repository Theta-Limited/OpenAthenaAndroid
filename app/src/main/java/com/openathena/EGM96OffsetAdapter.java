package com.openathena;

import org.matthiaszimmermann.location.Location;
import org.matthiaszimmermann.location.egm96.Geoid;

// Android will not support conversion from WGS84 to/from EGM96 until Android 14 Upsidedown Cake,
// which is still in beta at time of writing: https://github.com/googlemaps/android-maps-utils/issues/704
// TODO once ready, we should try to use the Android native converter first, use matthias' library as a fallback option
public class EGM96OffsetAdapter implements EGMOffsetProvider{
    EGM96OffsetAdapter() {
        super();
        Geoid.init(); // this may consume significant memory resources
    }

    @Override
    public double getEGM96OffsetAtLatLon(double latitude, double longitude) {
        return Geoid.getOffset(new Location(latitude, longitude));
    }
}
