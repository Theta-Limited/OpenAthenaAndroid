package org.matthiaszimmermann.location;

public class Location {
	// TODO verify if this is meaningful (eg. if this is sufficient for cm accuracy on earth)
	public static final double EPSILON = 0.00000001;
	
	public static final double LATITUDE_MIN_STRICT = -90.0;
	public static final double LATITUDE_MAX_STRICT = 90.0;
	
	public static final double LONGITUDE_MIN_STRICT = 0.0;
	public static final double LONGITUDE_MAX_STRICT = 360.0;

	private double m_lat;
	private double m_lng;

	public Location() {
		init(0.0, 0.0, true);		
	}

	public Location(double lat, double lng) {
		init(lat, lng, true);		
	}

	public Location(double lat, double lng, boolean lenient) {
		init(lat, lng, lenient);
	}

	private void init(double lat, double lng, boolean lenient) {
		if(lenient) {
			m_lat = normalizeLat(lat);
			m_lng = normalizeLong(lng);
		}
		else {
			if(lat < LATITUDE_MIN_STRICT || lat > LATITUDE_MAX_STRICT) {
				throw new IllegalArgumentException("latitude out of bounds ["+LATITUDE_MIN_STRICT+","+LATITUDE_MAX_STRICT+"]");
			}
			
			if(lng < LONGITUDE_MIN_STRICT || lng >= LONGITUDE_MAX_STRICT) {
				throw new IllegalArgumentException("longitude out of bounds ["+LONGITUDE_MIN_STRICT+","+LONGITUDE_MAX_STRICT+")");
			}
			
			m_lat = lat;
			m_lng = lng;
		}
	}

	public double getLatitude() {
		return m_lat;
	}

	public double getLongitude() {
		return m_lng;
	}

	private double normalizeLat(double lat) {
		if(lat > 90.0) {
			return normalizeLatPositive(lat);
		}
		else if(lat < -90) {
			return -normalizeLatPositive(-lat);
		}

		return lat;
	}

	private double normalizeLatPositive(double lat) {
		double delta = (lat -  90.0) % 360.0;

		if(delta <= 180.0) {
			lat = 90.0 - delta;
		}
		else {
			lat = delta - 270.0;
		}

		return lat;		
	}

	private double normalizeLong(double lng) {
		lng %= 360.0;

		if(lng >= 0.0) {
			return lng; 
		}
		else {
			return lng + 360;
		}
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}

		if(!(o instanceof Location)) {
			return false;
		}

		Location other = (Location) o;

		if(Math.abs(getLatitude() - other.getLatitude()) > EPSILON) {
			return false;
		}

		if(Math.abs(getLongitude() - other.getLongitude()) > EPSILON) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "(" + getLatitude() + "," + getLongitude() + ")";
	}

	public Location floor(double precision) {
		if(precision <= 0.0 || precision > 1.0) {
			throw new IllegalArgumentException("precision out of bounds (0,1]");
		}
		
		double latFloor = Math.floor(getLatitude() / precision) * precision;
		double lngFloor = Math.floor(getLongitude() / precision) * precision;
		
		return new Location(latFloor, lngFloor);
	}
}