package com.openathena;

 public interface DeclinationProvider {
     /**
      * Obtains the declination of the horizontal component of the magnetic field from true north, in degrees (i.e. positive means the magnetic field is rotated east that much from true north).
       * @param lat float: Latitude in WGS84 geodetic coordinates -- positive is east.
      * @param lon float: Longitude in WGS84 geodetic coordinates -- positive is north.
      * @param alt float: Altitude in WGS84 geodetic coordinates (height above ellipsoid), in meters.
      * @return float: the declination of the horizontal component of the magnetic field from true north, in degrees
      */
    public abstract float getMagDeclinationFromLatLonAlt(float lat, float lon, float alt);
}
