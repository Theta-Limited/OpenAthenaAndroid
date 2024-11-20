package com.openathena;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CK42_Gauss_Krüger_TranslatorTest {

    // Test of CK42 Gauss Krüger grid ref
    // A landmark in Berlin at 52.489349° N, 13.481969° E, 68.19m (WGS84) is compared
    // to a GK grid lines intersection
    // on an old Soviet map of Berlin with northing 3397km and easting 5819km
    // https://mapshow.s3.eu-west-2.amazonaws.com/N-33-123-4.html
    @Test
    public void testCK42_Geodetic_to_Gauss_Krüger() {
        long[] results = CK42_Gauss_Kruger_Translator.CK42_Geodetic_to_Gauss_Kruger(52.48969955423094, 13.484073132860129);
        assertEquals(5819, results[0] / 1000); // Easting line (latitude) in Km
        assertEquals(3397, results[1] / 1000); // Northing line (longitude) in Km
    }
}
