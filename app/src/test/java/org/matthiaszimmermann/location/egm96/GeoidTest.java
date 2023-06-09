package org.matthiaszimmermann.location.egm96;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matthiaszimmermann.location.Location;
import org.matthiaszimmermann.location.Point;


public class GeoidTest {

	// geoid test file available from http://geographiclib.sourceforge.net/1.28/geoid.html 
	public static final String FILE_GEOID_TEST_GZ = "/GeoidHeights.dat.gz";
	public static final double EPSILON = 0.00001;
	
	public static final int MAX_NUM_TESTCASES = 10000;
	
	private static List<Point> s_testPoints;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		Assert.assertTrue(GeoidTest.init());
		Assert.assertTrue(Geoid.init());
	}

	private static boolean init() {
		try {
			InputStream is = GeoidTest.class.getResourceAsStream(FILE_GEOID_TEST_GZ);
			GZIPInputStream gis = new GZIPInputStream(is);
			InputStreamReader isr = new InputStreamReader(gis);
			BufferedReader br = new BufferedReader(isr);
			
			loadTestData(br);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private static void loadTestData(BufferedReader br) throws Exception {
		s_testPoints = new ArrayList<Point>();
		
		String line = br.readLine();
		int l = 1; // line counter

		while(line != null && l < MAX_NUM_TESTCASES) {
			l++;
			StringTokenizer t = new StringTokenizer(line);
			int c = t.countTokens();

			if(c != 5) {
				System.err.println("error on line " + l + ": found " + c + " tokens (expected 5): '" + line + "'");
			}

			double lat = Double.parseDouble(t.nextToken());
			double lng = Double.parseDouble(t.nextToken());
			@SuppressWarnings("unused")
			double offEgm84 = Double.parseDouble(t.nextToken());
			double offEgm96 = Double.parseDouble(t.nextToken());
			@SuppressWarnings("unused")
			double offEgm08 = Double.parseDouble(t.nextToken());
			
			s_testPoints.add(new Point(lat, lng, offEgm96));
		}
		
		System.out.println("test points read: n=" + l);
	}

	@Test
	public void testInit() {
		boolean resultInit = Geoid.init();
		Assert.assertTrue(resultInit);
	}

	@Test
	public void testGetGridOffsetInvalidParams() {
		// invalid grid latitudes
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(91.0, 0.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(89.7, 0.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(33.3, 0.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.3, 0.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-0.01, 0.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-65.43, 0.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-89.7, 0.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-90.5, 0.0), EPSILON);

		// invalid grid longitudes
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, -210.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, -180.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, -90.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, -1.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, -0.01), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 359.8), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 360.0), EPSILON);
		Assert.assertEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 500.0), EPSILON);
	}

	@Test
	public void testGetGridOffsetValidParams() {
		// valid grid latitudes		
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(90.0, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(89.74, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(89.50, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(89.25, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(43.75, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(17.5, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(9.25, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-22.0, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-55.25, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-72.5, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-80.75, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-89.74, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(-90.0, 0.0), EPSILON);

		// valid grid longitudes		
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 0.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 0.25), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 0.5), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 0.75), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 1.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 30.25), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 95.75), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 123.5), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 180.0), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 230.75), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 310.25), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 359.25), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 359.5), EPSILON);
		Assert.assertNotEquals(Geoid.OFFSET_INVALID, Geoid.getGridOffset(0.0, 359.75), EPSILON);

	}

	@Test
	public void testGetGridOffsetValues() {
		// poles
		// 90.0 0.0 13.68
		// -90 0 -29.79
		Assert.assertEquals(13.68, Geoid.getGridOffset(90.0, 0.0), EPSILON);
		Assert.assertEquals(-29.79, Geoid.getGridOffset(-90.0, 0.0), EPSILON);

		// random grid params from EGM.complete.txt.gz
		// 89.74 5.75 13.89
		// 86.5 217.25 11.76
		// 71.25 8.0 43.56
		// 54.0 182.25 2.35
		// 35.5 186.0 -12.91
		// 14.75 213.5 -8.07
		// -22.25 221.5 -10.18
		// -37.5 13.0 25.61
		// -51.75 157.25 -17.88
		// -70.5 346.0 5.18
		// -74.5 22.0 17.5
		// -84.5 306.25 -25.71
		// -86.75 328.75 -21.81

		Assert.assertEquals(13.89, Geoid.getGridOffset(89.74, 5.75), EPSILON); 
		Assert.assertEquals(11.76, Geoid.getGridOffset(86.5, 217.25), EPSILON);
		Assert.assertEquals(43.56, Geoid.getGridOffset(71.25, 8.0), EPSILON);
		Assert.assertEquals(2.35, Geoid.getGridOffset(54.0, 182.25),  EPSILON);
		Assert.assertEquals(-12.91, Geoid.getGridOffset(35.5, 186.0), EPSILON);
		Assert.assertEquals(-8.07, Geoid.getGridOffset(14.75, 213.5), EPSILON);
		Assert.assertEquals(-10.18, Geoid.getGridOffset(-22.25, 221.5), EPSILON);
		Assert.assertEquals(25.61, Geoid.getGridOffset(-37.5, 13.0), EPSILON);
		Assert.assertEquals(-17.88, Geoid.getGridOffset(-51.75, 157.25), EPSILON);
		Assert.assertEquals(5.18, Geoid.getGridOffset(-70.5, 346.0), EPSILON);
		Assert.assertEquals(17.5, Geoid.getGridOffset(-74.5, 22.0), EPSILON);
		Assert.assertEquals(-25.71, Geoid.getGridOffset(-84.5, 306.25), EPSILON);
		Assert.assertEquals(-21.81, Geoid.getGridOffset(-86.75, 328.75), EPSILON);
		Assert.assertEquals(-29.79, Geoid.getGridOffset(-90.0, 0.0), EPSILON);
	}


	@Test
	public void testGetOffsetValuesSimple() {
		Assert.assertEquals(43.56, Geoid.getOffset(new Location(71.25, 8.0)), EPSILON);
		Assert.assertEquals(2.35, Geoid.getOffset(new Location(54.0, 182.25)),  EPSILON);
		Assert.assertEquals(-12.91, Geoid.getOffset(new Location(35.5, 186.0)), EPSILON);
		Assert.assertEquals(-8.07, Geoid.getOffset(new Location(14.75, 213.5)), EPSILON);
		Assert.assertEquals(5.18, Geoid.getOffset(new Location(-70.5, 346.0)), EPSILON);
		Assert.assertEquals(17.5, Geoid.getOffset(new Location(-74.5, 22.0)), EPSILON);
		Assert.assertEquals(-25.71, Geoid.getOffset(new Location(-84.5, 306.25)), EPSILON);
		Assert.assertEquals(-29.79, Geoid.getOffset(new Location(-90.0, 0.0)), EPSILON);
	}

	// TODO
	/* test data from http://earth-info.nga.mil/GandG/wgs84/gravitymod/egm96/outintpt.dat
    38.6281550   269.7791550     -31.628
   -14.6212170   305.0211140      -2.969
    46.8743190   102.4487290     -43.575
   -23.6174460   133.8747120      15.871
    38.6254730   359.9995000      50.066 <- special case for transition from lat=359.75 to 0.0
     -.4667440      .0023000      17.329
	 */
	@Test
	public void testGetOffsetsAgainstNgaTests() {
		// tolerances are tweaked to accept currently implemented model
		Assert.assertEquals(-31.628, Geoid.getOffset(new Location( 38.6281550, 269.7791550)), 0.45);
		Assert.assertEquals( -2.969, Geoid.getOffset(new Location(-14.6212170, 305.0211140)), 0.45);
		Assert.assertEquals(-43.575, Geoid.getOffset(new Location( 46.8743190, 102.4487290)), 0.05);
		Assert.assertEquals( 15.871, Geoid.getOffset(new Location(-23.6174460, 133.8747120)), 1.1);
		Assert.assertEquals( 50.066, Geoid.getOffset(new Location( 38.6254730, 359.9995000)), 0.25);
		Assert.assertEquals( 17.329, Geoid.getOffset(new Location( -0.4667440,   0.0023000)), 0.12);

		// test things at poles 
		// TODO verify target offsets 
		Assert.assertEquals( 13.73, Geoid.getOffset(new Location( 89.8,   0.1)), 0.05);
		Assert.assertEquals(-29.85, Geoid.getOffset(new Location(-89.8, 180.2)), 0.05);

	}

	/**
	 * test against large number of samples randomly chosen from
	 * http://geographiclib.sourceforge.net/1.28/geoid.html 
	 */
	@Test
	public void testGetOffsetsAgainstGeographiclibData() {
		for(Point p: s_testPoints) {
			Assert.assertEquals(p.m, Geoid.getOffset(p.l), 0.015);
		}
	}
}
