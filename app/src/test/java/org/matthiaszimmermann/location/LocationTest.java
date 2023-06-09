package org.matthiaszimmermann.location;

import org.junit.Assert;
import org.junit.Test;


public class LocationTest {
	public static final double EPSILON = 0.00001;

	@Test
	public void testConstructorSimple() {
		Assert.assertEquals(0.0, (new Location(0.0, 1.0)).getLatitude(), EPSILON);
		Assert.assertEquals(1.0, (new Location(0.0, 1.0)).getLongitude(), EPSILON);
	}

	@Test
	public void testConstructorLatOutOfNormalBounds() {
		// positive lat
		Assert.assertEquals( 89.0, (new Location(91.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( 10.0, (new Location(170.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(-10.0, (new Location(190.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(-90.0, (new Location(270.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(-89.0, (new Location(271.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( -1.0, (new Location(359.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(  0.0, (new Location(360.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(  1.0, (new Location(361.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( 40.0, (new Location(400.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( 90.0, (new Location(450.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( 89.0, (new Location(451.0, 0.0)).getLatitude(), EPSILON);

		// negative lat
		Assert.assertEquals(-89.0, (new Location(-91.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(-10.0, (new Location(-170.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( 10.0, (new Location(-190.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( 90.0, (new Location(-270.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( 89.0, (new Location(-271.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(  1.0, (new Location(-359.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(  0.0, (new Location(-360.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals( -1.0, (new Location(-361.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(-40.0, (new Location(-400.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(-90.0, (new Location(-450.0, 0.0)).getLatitude(), EPSILON);
		Assert.assertEquals(-89.0, (new Location(-451.0, 0.0)).getLatitude(), EPSILON);
	}	


	@Test
	public void testConstructorLongOutOfNormalBounds() {
		// positive long
		Assert.assertEquals(359.0, (new Location(0.0,  359.0)).getLongitude(), EPSILON);
		Assert.assertEquals(  0.0, (new Location(0.0,  360.0)).getLongitude(), EPSILON);
		Assert.assertEquals(  1.0, (new Location(0.0,  361.0)).getLongitude(), EPSILON);
		Assert.assertEquals(  7.0, (new Location(0.0, 3607.0)).getLongitude(), EPSILON);

		// negative long
		Assert.assertEquals(359.0, (new Location(0.0,   -1.0)).getLongitude(), EPSILON);
		Assert.assertEquals(170.0, (new Location(0.0, -190.0)).getLongitude(), EPSILON);
		Assert.assertEquals(  1.0, (new Location(0.0, -359.0)).getLongitude(), EPSILON);
		Assert.assertEquals(  0.0, (new Location(0.0, -360.0)).getLongitude(), EPSILON);
		Assert.assertEquals(359.0, (new Location(0.0, -361.0)).getLongitude(), EPSILON);
		Assert.assertEquals(359.0, (new Location(0.0, -1441.0)).getLongitude(), EPSILON);
	}

	@Test
	public void testEquals() {
		Location l1 = new Location(45,90);
		Location l1a = new Location(45 + Location.EPSILON/2.0, 90-Location.EPSILON/2.0);
		Location l1b = new Location(45 + Location.EPSILON, 90-Location.EPSILON);
		Location l2 = new Location(45,91);
		Location l3 = new Location(44,90);

		Assert.assertTrue(l1.equals(l1));
		Assert.assertTrue(l1.equals(l1a));
		Assert.assertFalse(l1.equals(l1b));
		Assert.assertFalse(l1.equals(l2));
		Assert.assertFalse(l1.equals(l3));

		Assert.assertFalse(l1.equals(null));
		Assert.assertFalse(l1.equals(new String("hello world")));

	}

	@Test
	public void testFloorSimple() {
		Location l1      = new Location(20.0, 10.0);
		Location l1Floor = new Location(20.0, 10.0);

		Location l2      = new Location(20.0, 10.25);
		Location l2Floor = new Location(20.0, 10.25);

		Location l3      = new Location(20.0, 10.5);
		Location l3Floor = new Location(20.0, 10.5);

		Location l4      = new Location(20.0, 10.75);
		Location l4Floor = new Location(20.0, 10.75);

		Location l5      = new Location(-45.25, -20.0);
		Location l5Floor = new Location(-45.25, -20.0);

		Location l6      = new Location(-45.5, -20.0);
		Location l6Floor = new Location(-45.5, -20.0);

		Location l7      = new Location(-45.75, -20.0);
		Location l7Floor = new Location(-45.75, -20.0);

		Location l8      = new Location(-46.0, -20.0);
		Location l8Floor = new Location(-46.0, -20.0);

		// check for 0.25 precision
		Assert.assertEquals(l1Floor, l1.floor(0.25));
		Assert.assertEquals(l2Floor, l2.floor(0.25));
		Assert.assertEquals(l3Floor, l3.floor(0.25));
		Assert.assertEquals(l4Floor, l4.floor(0.25));

		Assert.assertEquals(l5Floor, l5.floor(0.25));
		Assert.assertEquals(l6Floor, l6.floor(0.25));
		Assert.assertEquals(l7Floor, l7.floor(0.25));
		Assert.assertEquals(l8Floor, l8.floor(0.25));

		// check for 0.5 precision
		Assert.assertEquals(l6Floor, l5.floor(0.5));
		Assert.assertEquals(l6Floor, l6.floor(0.5));
		Assert.assertEquals(l8Floor, l7.floor(0.5));
		Assert.assertEquals(l8Floor, l8.floor(0.5));

		// check for 1.0 precision
		Assert.assertEquals(l8Floor, l5.floor(1.0));
		Assert.assertEquals(l8Floor, l6.floor(1.0));
		Assert.assertEquals(l8Floor, l7.floor(1.0));
		Assert.assertEquals(l8Floor, l8.floor(1.0));
	}

	@Test
	public void testFloorRealistic() {
		Location l1      = new Location(12.345, 234.5678);
		Location l1Floor = new Location(12.25,  234.50);

		Location l2      = new Location(-23.45768,  -1.12345);
		Location l2Floor = new Location(-23.5,     358.5);

		Location l3      = new Location(-89.768, 180.0);
		Location l3Floor = new Location(-90.0,   180.0);

		Assert.assertEquals(l1Floor, l1.floor(0.25));
		Assert.assertEquals(l2Floor, l2.floor(0.5));
		Assert.assertEquals(l3Floor, l3.floor(0.25));
	}
}
