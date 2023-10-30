
package com.openathena;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.matthiaszimmermann.location.Location;
import org.matthiaszimmermann.location.egm96.Geoid;

import java.io.InputStream;
import java.net.URL;

public class EGM96OffsetAdapterTest{

    @Before
    public void setUp() throws Exception {
        // load from app/src/test/resources folder
        URL url = getClass().getClassLoader().getResource("EGM96complete.bin");
        InputStream inputStream = url.openStream();
        Geoid.init(inputStream);
    }

    @Test
    public void testGetEGM96OffsetAtLatLon() {
        // Define a small delta value to allow for floating-point inaccuracies
        double delta = 0.0000001;

        // tests for the geoid offset at various lat/lon pairs
        // atlanta +33.7490, -84.3880
        assertEquals(-30.413448028014034, Geoid.getOffset(new Location(33.7490, -84.3880)), delta);

        // 0.0, 0.0 in midle atlantic off coast of Africa
        assertEquals(17.16,Geoid.getOffset(new Location(0.0, 0.0)), delta);

        // LA: 34.0522, -118.2437,  -35.422992382534765
        assertEquals(-35.422992382534765, Geoid.getOffset(new Location(34.0522, -118.2437)), delta);

        // Reykjavík: 64.1466, -21.9426, 66.26275258040069
        assertEquals(66.26275258040069, Geoid.getOffset(new Location(64.1466, -21.9426)), delta);

        // London: 51.5074, -0.1278, 46.19035376421903
        assertEquals(46.19035376421903, Geoid.getOffset(new Location(51.5074, -0.1278)), delta);

        // Kyiv: 50.4501, 30.5234, 25.471509018494615
        assertEquals(25.471509018494615, Geoid.getOffset(new Location(50.4501, 30.5234)), delta);

        // Melbourne, Australia: -37.8136, 144.9631, 4.105052826603198
        assertEquals(4.105052826603198, Geoid.getOffset(new Location(-37.8136, 144.9631)), delta);

        // Beijing, China: 39.9042, 116.4074, -10.187067662099095
        assertEquals(-10.187067662099095, Geoid.getOffset(new Location(39.9042, 116.4074)), delta);

        // Cape Town, South Africa: -33.9249, 18.4241, 31.109644518382122
        assertEquals(31.109644518382122, Geoid.getOffset(new Location(-33.9249, 18.4241)), delta);

        // Santiago, Chile: -33.4489, -70.6693, 26.411616854944732
        assertEquals(26.411616854944732, Geoid.getOffset(new Location(-33.4489,  -70.6693)), delta);
    }
}
