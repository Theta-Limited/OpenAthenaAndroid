package com.openathena;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CoordTranslatorTest {

    @Test
    public void testToMGRS1m() {
        // values from generated from https://www.earthpoint.us/convert.aspx
        assertEquals("31NAA6602100000", CoordTranslator.toMGRS1m(0.0,0.0));
        // NGA mgrs-java and mgrs-android libraries don't handle polar regions correctly yet: https://github.com/ngageoint/mgrs-android/issues/3
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
}