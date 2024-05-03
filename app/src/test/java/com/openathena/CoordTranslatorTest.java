package com.openathena;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;

public class CoordTranslatorTest {

    @Test
    public void testToMGRS1m() {
        // values from generated from https://www.earthpoint.us/convert.aspx
        assertEquals("31NAA6602100000", CoordTranslator.toMGRS1m(0.0,0.0));
        // NGA mgrs-java and mgrs-android libraries don't handle special polar regions correctly yet: https://github.com/ngageoint/mgrs-android/issues/3
        assertEquals("ZAH0000000000", CoordTranslator.toMGRS1m(90.0, 0.0));
        assertEquals("BAN0000000000", CoordTranslator.toMGRS1m(-90.0,0.0));
    }

    @Test
    public void testToMGRS10m() {
        // values from generated from https://www.earthpoint.us/convert.aspx
        assertEquals("12SVD97877615", CoordTranslator.toMGRS10m(35.028056, -111.023333));
        assertEquals("18SUJ23400740", CoordTranslator.toMGRS10m(38.897778, -77.036389));
        assertEquals("37UDB13247930", CoordTranslator.toMGRS10m(55.751667, 37.617778));
    }

    @Test
    public void testToMGRS100m() {
        // values from generated from https://www.earthpoint.us/convert.aspx
        assertEquals("45RVL926958", CoordTranslator.toMGRS100m(27.988056, 86.925278));
        assertEquals("54PXT736577", CoordTranslator.toMGRS100m(11.373333, 142.591667));
        assertEquals("19TCG023363", CoordTranslator.toMGRS100m(41.854021, -71.381068));
    }

    @Test
    public void testFromMGRS() {
        double lat = 0.0d;
        double lon = 0.0d;
        double[] pair = null;

        try {
            pair = CoordTranslator.fromMGRS("foo");
        } catch (java.text.ParseException ignored) {

        }
        assertNull("ERROR: fromMGRS returned invalid coord pair", pair);

        try {
            pair = CoordTranslator.fromMGRS("18SUJ2340407404");
        } catch (java.text.ParseException ignored) {

        }
        assertNotNull("ERROR: fromMGRS did not return valid coord pair", pair);
        lat = pair[0]; lon = pair[1];
        assertEquals("ERROR: fromMGRS latitude was wrong", lat, 38.897778, 0.0001);
        assertEquals("ERROR: fromMGRS longitude was wrong", lon, -77.036389, 0.0001);
    }

    @Test
    public void testDMSToDecimal() {
        double lat = 0.0d;
        double lon = 0.0d;

        String[] validLats = {
                "38° 53′ 52″ N", "38°53′ 52″ N", "38°53′52″N",
                "38°53′52.0″", "38°53'52.0\"", "+38.897778",
                "38.897778°"
        };

        for (String aString : validLats) {

            try {
                lat = CoordTranslator.dmsToDecimal(aString);
            } catch (java.text.ParseException ignored) {

            }
            assertEquals("ERROR: testDMSToDecimal latitude was wrong for " + aString, lat, 38.897778, 0.0001);
        }

        String[] validLons = {
                "77° 2′ 11″ W", "77°2′ 11″ W", "77°2′11″W",
                "77°2′11.0″W", "77°2'11\"W", "-77.036389",
                "-77.036389°"
        };

        for (String aString : validLons) {
            try {
                lon = CoordTranslator.dmsToDecimal(aString);
            } catch (java.text.ParseException ignored) {

            }
            assertEquals("ERROR: testDMSToDecimal longitude was wrong for " + aString, lon, -77.036389, 0.0001);
        }

        String[] invalidInputs = {
                "100° 00′ 00″ N", // Invalid degree
                "0° 60′ 00″ N", // Invalid minutes
                "0° 0′ 60″ N", // Invalid seconds
//                "38°53N", // Missing minutes and seconds
//                "N 38°53′52″", // Incorrect format
//                "38°53.52″N" // Missing separator
        };

        for (String invalidString : invalidInputs) {
            try {
                CoordTranslator.dmsToDecimal(invalidString);
                fail("Expected a ParseException for input: " + invalidString);
            } catch (ParseException ignored) {
                // Expected, so no action needed here
            }
        }
    }

    @Test
    public void testIsValidLatOrLon() {
        String[] validCoords = {
                "38° 53′ 52″ N", "38°53′ 52″ N", "38°53′52″N",
                "38°53′52.0″", "38°53'52.0\"", "+38.897778",
                "38.897778°",
                "77° 2′ 11″ W", "77°2′ 11″ W", "77°2′11″W",
                "77°2′11.0″W", "77°2'11\"W", "-77.036389",
                "-77.036389°"
        };

        for (String aString : validCoords) {
            assertTrue("Expected String " + aString + " to be valid", CoordTranslator.isValidLatOrLon(aString));
        }
    }

    @Test
    public void testParseLatLonValidInputs() {
        // Define some valid inputs with expected outputs
        Object[][] testData = {
                {"38°53′52″ N, 77°02′11″ W", 38.897778, -77.036389},
                {"38°53′52″ N 77°02′11″ W", 38.897778, -77.036389},
                {"38.897778°, -77.036389°", 38.897778, -77.036389},
                {"38.897778 N, -77.036389 W", 38.897778, -77.036389},
                {"38 53 52 N, 77 2 11 W", 38.897778, -77.036389}//,
                // Weird format used by Geocache app (fails test)
                //{"N 033° 55.878' W 084° 18.183'", 33.931300, -84.303050}
        };

        for (Object[] testCase : testData) {
            String input = (String) testCase[0];
            double expectedLat = (double) testCase[1];
            double expectedLon = (double) testCase[2];

            try {
                double[] result = CoordTranslator.parseLatLon(input);
                assertEquals("Latitude mismatch for input: " + input, expectedLat, result[0], 0.0001);
                assertEquals("Longitude mismatch for input: " + input, expectedLon, result[1], 0.0001);
            } catch (ParseException e) {
                fail("ParseException thrown for valid input: " + input);
            }
        }
    }

    @Test
    public void testParseLatLonInvalidInputs() {
        String[] invalidInputs = {
                "", // Empty string
                "38°53′52″ N", // Missing longitude
                "77°02′11″ W", // Missing latitude
                "Not a coordinate", // Non-coordinate string
                "100°00′00″ N, 200°00′00″ W", // Out of range
        };

        for (String input : invalidInputs) {
            try {
                CoordTranslator.parseLatLon(input);
                fail("Expected ParseException for invalid input: " + input);
            } catch (ParseException e) {
                // Expected behavior, test passes for this input
            }
        }
    }
}