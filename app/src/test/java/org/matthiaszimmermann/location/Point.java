package org.matthiaszimmermann.location;

public class Point {
	public Location l;
	public double m;
	
	public Point(double lat, double lng, double off) {
		l = new Location(lat, lng);
		m = off;
	}
}
