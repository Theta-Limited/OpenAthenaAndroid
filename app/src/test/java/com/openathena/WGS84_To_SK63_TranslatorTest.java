package com.openathena;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class WGS84_To_SK63_TranslatorTest {
    @Test
    public void testWGS84_To_SK63_Translator() {
        double latWGS84 = 42.18376389; // 42°11'01.55"
        double lonWGS84 = 77.56410000; // 77°33'50.76"
        double height = 0.0; // Height in meters

        Object[] result = WGS84_To_SK63_Translator.WGS84_to_SK63(latWGS84, lonWGS84, height);
        double easting = (double) result[0];
        double northing = (double) result[1];
        String zoneId = (String) result[2];

        assertEquals(northing, 4657362.11, 20.0);
        assertEquals(easting, 9303965.60, 20.0);
    }
}
