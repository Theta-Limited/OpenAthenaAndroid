package com.openathena;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class WGS84_CK42_Geodetic_TranslatorTest {
    // Define a delta value (roughly 4.5m) to allow for error from varying ellipsoid implementations
    double delta = 0.0000404381 * 2.0;

    // values calculated from converting epsg 4326 to epsg 4284 on epsg.io
    // https://epsg.io/transform#s_srs=4326&t_srs=4284&x=0.0000000&y=0.0000000
    // Conversions may not be accurate for areas far from former Soviet Union
    @Test
    public void testWGS84_CK42_Lat() {
        assertEquals(55.7524573, WGS84_CK42_Geodetic_Translator.WGS84_CK42_Lat(55.7525, 37.623056, 148.0),delta);
    }

    @Test
    public void testWGS84_CK42_Lon() {
        assertEquals(37.6249302, WGS84_CK42_Geodetic_Translator.WGS84_CK42_Long(55.7525, 37.623056, 148.0),delta);
    }

}
