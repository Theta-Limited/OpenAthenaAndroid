package com.openathena;

import android.util.Log;

import com.openathena.GeoTIFFParser;
import com.openathena.geodataAxisParams;
import com.openathena.RequestedValueOOBException;
// // Convert from Nato ellipsoid to Warsaw
// import com.openathena.WGS84_SK42_Translator
// // Convert geodetic coords to Gauss Kruger grid ref
// import com.openathena.SK42_Gauss_Kruger_Projector

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.io.InputStream;
import java.io.PrintWriter;

import java.lang.Math;

import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;

import com.openathena.RequestedValueOOBException;
import com.openathena.geodataAxisParams;
import com.openathena.GeoTIFFParser;

import mil.nga.tiff.*;

public class TargetGetter {
    private static final String TAG = TargetGetter.class.getSimpleName();
    private GeoTIFFParser myGeoTIFFParser;
    private final double INCREMENT = 1.0d;
    // private MGRS mgrs;
    // private WGS84_SK42_Translator ellipsoidTranslator
    // private SK42_Gauss_Kruger_Projector gk_Projector

    TargetGetter() {
        myGeoTIFFParser = new GeoTIFFParser();
    }

    TargetGetter(File geofile) {
        this();
        myGeoTIFFParser.loadGeoTIFF(geofile);
    }

    TargetGetter(GeoTIFFParser gman) {
        this.myGeoTIFFParser = gman;
    }

    /**
     * Given sensor data from a drone, performs a raycast against the DEM held by this object's GeoTIFFParser and returns the target's location as a result
     * @param lat The latitude of the aircraft
     * @param lon The longitude of the aircraft
     * @param alt The altitude of the aircraft (in meters above the WGS84 reference ellipsoid)
     * @param azimuth The direction of the aircraft's camera. 0° is North, increases clockwise
     * @param theta The angle of depression (pitch) of the aircraft's camera. A positive value representing degrees downward from the horizon
     * @return double[] an array containing: 0 the distance to the target 1 the last latitude value along the raycast 2 the last longitude value along the raycast 3 the last altitude value along the raycast 4 the terrain altitude of the datapoint nearest the last raycast position
     * @throws RequestedValueOOBException Throws a RequestedValueOOBException if the raycast exceeds the coverage of the DEM held by this object's GeoTIFFParser. If the altitude of the start of the ray is already below terrain elevation data, isAltitudeBad boolean will be set to true
     */
    public double[] resolveTarget(double lat, double lon, double alt, double azimuth, double theta) throws RequestedValueOOBException{
        if (myGeoTIFFParser == null) {
            throw new NullPointerException("FATAL ERROR: resolveTarget attempted before geotiff loaded");
        }
        double radAzimuth = Math.toRadians(azimuth);
        double radTheta = Math.toRadians(theta);

        radAzimuth = radNormalize(radAzimuth);

        double finalDist;
        double tarLat;
        double tarLon;
        double tarAlt;
        double terrainAlt;
        double[] outArr;

        // Check if target is exactly straight downwards,
        //     if so, skip iterative search b/c target is directly below us
        if (Math.abs(Double.compare(radTheta, Math.PI / 2.0d)) <= 0.005d) { // 0.005 radians ~= 0.29 degrees
            try {
                terrainAlt = myGeoTIFFParser.getAltFromLatLon(lat, lon);
            } catch (RequestedValueOOBException e) {
                throw e;
            }
            finalDist = Math.abs(alt - terrainAlt);
            tarLat = lat;
            tarLon = lon;
            tarAlt = terrainAlt;
            outArr = new double[]{finalDist, tarLat, tarLon, tarAlt, terrainAlt}; // I hate Java
            return outArr;
        }

        // safety check: if theta > 90 degrees (pi / 2 radians)
        // then camera is facing backwards
        // to avoid undefined behavior, reverse radAzimuth,
        // then subtract theta from 180deg to determine
        // a new appropriate THETA for the reverse direction
        //
        if (radTheta > (Math.PI / 2)) {
            radAzimuth = radNormalize(radAzimuth + Math.PI);
            radTheta = Math.PI - radTheta;
        }

        double deltaZ = -1.0d * Math.sin(radTheta); // neg because direction is downward

        // determines by how much of travel per unit is actually horiz
        // pythagorean theorem, deltaz^2 + deltax^2 + deltay^2 = 1
        double horizScalar = Math.cos(radTheta);

        // meters of acceptable distance between constructed line and datapoint
        // Somewhat arbitrary. SRTM has a horizontal resolution of 30m, but vertical accuracy is often more precise
        double post_spacing_meters = haversine(0, lat, myGeoTIFFParser.getXResolution(), lat, alt); // meters between datapoints, from degrees
        final double THRESHOLD = post_spacing_meters / 16.0d; // meters of acceptable distance between constructed line and datapoint. somewhat arbitrary

        double curLat = lat;
        double curLon = lon;
        double curAlt = alt;
        double groundAlt;

        // account for curvature of Earth at each Epoch
        long iterCount = 0;
        long iterPerEpoch = 128;
        double lastEpochLat = curLat;
        double lastEpochLon = curLon;
        try {
            groundAlt = myGeoTIFFParser.getAltFromLatLon(curLat, curLon);
        } catch (RequestedValueOOBException e) {
            throw e;
        }
        if (curAlt < groundAlt) {
            boolean isAltitudeBad = true;
            throw new RequestedValueOOBException("ERROR: bad altitude or terrain data. This image is unusable. \uD83D\uDDBC\uD83D\uDEAB\uD83E\uDDEE\uD83C\uDFAF", curLat, curLon, isAltitudeBad);
        }
        double altDiff = curAlt - groundAlt;
        while (altDiff > THRESHOLD) {
            try {
                groundAlt = myGeoTIFFParser.getAltFromLatLon(curLat, curLon);
            } catch (RequestedValueOOBException e) {
                throw e;
            }
            altDiff = curAlt - groundAlt;

            // account for curvature of Earth at each Epoch
            if (iterCount > 0 && iterCount % iterPerEpoch == 0) {
                double minArcAngle = minArcAngle(lastEpochLat, lastEpochLon, curLat, curLon);
                Log.d(TAG, "iterCount: " + iterCount + ", minArcAngle: " + minArcAngle);
                radTheta += Math.toRadians(minArcAngle); // rotate ray upwards by same amount Earth's curvature rotated it downwards
                deltaZ = -1.0d * Math.sin(radTheta);
                horizScalar = Math.cos(radTheta);

                lastEpochLat = curLat;
                lastEpochLon = curLon;
            }

            double avgAlt = curAlt;
            // deltaZ should always be negative
            curAlt += deltaZ;
            avgAlt = (avgAlt + curAlt) / 2.0d;
            double[] nextIter = inverse_haversine(curLat, curLon, horizScalar*INCREMENT, radAzimuth, avgAlt);
            curLat = nextIter[0];
            curLon = nextIter[1];
            iterCount++;
        }

        // When the loop ends, curY, curX, and curZ are closeish to the target
        //     may be a bit biased ever so slightly long (beyond the target)
        //     this algorithm is crude, does not take into account the curvature of the earth over long distances
        //     could use refinement
        double finalHorizDist = Math.abs(haversine(lon, lat, curLon, curLat, alt));
        double finalVertDist = Math.abs(alt - curAlt);
        // simple pythagorean theorem
        // may be inaccurate for very very large horizontal distances
        finalDist = Math.sqrt(finalHorizDist * finalHorizDist + finalVertDist * finalVertDist);
        terrainAlt  = groundAlt;

        outArr = new double[]{finalDist, curLat, curLon, curAlt, terrainAlt}; // I hate Java
        return outArr;
    }

    /**
     * Given two lat/lon coordinate pairs (in degrees) , finds the approximate arc angle (in degrees) of the shortest great-circle path between them
     * This is useful for accounting for curvature of the Earth. Treats coordinates as on a sphere rather than ellipsoid for simplicity
     *
     * <p>
     *     Formula from: https://math.stackexchange.com/a/2940458
     * </p>
     * @param lat0 latitude of first coordinate (in degrees)
     * @param lon0 longitude of first coordinate (in degrees)
     * @param lat1 latitude of second coordinate (in degrees)
     * @param lon1 longitude of second coordinate (in degrees)
     * @return the arc angle (in degrees) of the shortest great-circle path between first and second coordinates
     */
    public static double minArcAngle(double lat0, double lon0, double lat1, double lon1) {
        lat0 = Math.toRadians(lat0);
        lon0 = Math.toRadians(lon0);
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        return Math.toDegrees(Math.acos(cos(lat0)*cos(lat1)*cos(lon0-lon1) + sin(lat0)*sin(lat1)));
    }

    /**
     * Given an angle (in radians), returns an equivalent angle where 0 ≤ angle < 2 * π
     * @param radAngle an angle input in Radians. May be any positive or negative number
     * @return double the normalized value within [0, 2π)
     */
    public static double radNormalize(double radAngle) {
        while (radAngle >= Math.PI * 2.0d) {
            radAngle -= Math.PI * 2.0d;
        }
        while (radAngle < 0.0d) {
            radAngle += Math.PI * 2.0d;
        }
        return radAngle;
    }

    /**
     * Given an angle (in degrees), returns an equivalent angle where 0 ≤ angle < 2 * π
     * @param degAngle an angle input in Radians. May be any positive or negative number
     * @return double the normalized value within [0, 360.0)
     */
    public static double degNormalize(double degAngle) {
        while (degAngle >= 360.0d) {
            degAngle -= 360.0d;
        }
        while (degAngle < 0.0d) {
            degAngle += 360.0d;
        }
        return degAngle;
    }

    /**
     * Given an azimuth, measured starting at North and increasing clockwise, return the same angle on a Mathematical Unit Circle, starting at East and increasing counter-clockwise
     * @param radAzimuth an angle input in Radians, in Azimuth format. 0 is North, π/2 is East, π is South, 3π/2 is West
     * @return double an angle output in Radains, in Unit Circle format. 0 is East, π/2 is North, π is West, 3π/2 is South
     */
    public static double azimuthToUnitCircleRad(double radAzimuth) {
        double radDirection = -1.0d * radAzimuth;
        radDirection += (0.5d * Math.PI);
        radDirection = radNormalize(radDirection);
        return radDirection;
    }

    /**
     * Given a Latitude, determines the distance from the center of the WGS84 reference ellipsoid to the surface
     * @param lat In degrees, [-90, 90]. The latitude for which to find the radius
     * @param lon In degrees. Not used at all for this calculation
     * @return double The radius of the WGS84 reference ellipsoid at the given Latitude, in meters
     */
    public static double radius_at_lat_lon(double lat, double lon) {
        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon); // not used

        final double A = 6378137.0d; // equatorial radius of WGS ellipsoid, in meters
        final double B = 6356752.3d; // polar radius of WGS ellipsoid, in meters
        double r = squared(A * A * cos(lat)) + squared(B * B * sin(lat)); // numerator
        r /= squared(A * cos(lat)) + squared(B * sin(lat)); // denominator
        r = Math.sqrt(r); // square root
        return r;
    }

    /**
     * Given a point, distance, and heading (azimuth format), return a new point the specified distance along the defined great circle
     * <p>
     *     Thanks github.com/jdeniau
     *     for short distances, this is close to the straight line distance
     * </p>
     *
     * @param lat1 The latitude of the starting point
     * @param lon1 The longitude of the starting point
     * @param d The distance of travel along the great circle, in meters
     * @param radAzimuth The heading of the direction of travel for the great circle. In radians, starting North at 0° and increasing clockwise
     * @param alt The altitude above the surface of the WGS84 reference ellipsoid, measured in meters.
     * @return double[] A Lat/Lon pair representing the point at the end of the great circle d meters away from the starting point
     */
    static double[] inverse_haversine(double lat1, double lon1, double d /*distance*/, double radAzimuth, double alt) {
        if (d < 0.0d) {
            return inverse_haversine(lat1, lon1, -1.0d * d, radNormalize(radAzimuth + Math.PI), alt);
        }

        // calculate WGS84 radius at lat/lon
        //     based on: gis.stackexchange.com/a/20250
        //     R(f)^2 = ( (a^2 cos(f))^2 + (b^2 sin(f))^2 ) / ( (a cos(f))^2 + (b sin(f))^2 )
        double r = radius_at_lat_lon(lat1, lon1);
        r += alt; // acutal height above or below idealized ellipsoid

        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);

        // based on code via github.com/jdeniau
        double lat2 = asin(sin(lat1) * cos(d / r) + cos(lat1) * sin(d / r) * cos(radAzimuth));
        double lon2 = lon1 + atan2((sin(radAzimuth) * sin(d/r) * cos(lat1)),(cos(d / r) - sin(lat1) * sin(lat2)));

        double[] returnArr = {Math.toDegrees(lat2), Math.toDegrees(lon2)};
        return returnArr;
    }

    // /* Accurate Inverse Haversine formula
    //  * via github.com/jdeniau and math.stackexchange.com/a/3707243
    //  * given a point, distance, and heading, return the new point (lat lon)
    //  * a certain distance along the great circle
    //  *
    //  * for short distances, this is close to the straight line distance
    //  */
    // double[] inverse_haversine_accurate(double lat1, double lon1, double distance, double radAzimuth, double alt) {


    /**
     * Determines the great circle distance between two Lat/Lon pairs
     * <p>
     *     For short distances, this is close to the straight-line distance
     *     adapted from https://stackoverflow.com/a/4913653
     * </p>
     * @param lon1 The longitude of the first point, in degrees
     * @param lat1 The latitude of the first point, in degrees
     * @param lon2 The longitude of the second point, in degrees
     * @param lat2 The latitude of the second point, in degrees
     * @param alt The altitude above the surface of the WGS84 reference ellipsoid, measured in meters. Used to determine the radius of the great circle
     * @return double The distance in meters along a great circle path between the two inputed points.
     */
    static double haversine(double lon1, double lat1, double lon2, double lat2, double alt) {
        lon1 = Math.toRadians(lon1);
        lat1 = Math.toRadians(lat1);
        lon2 = Math.toRadians(lon2);
        lat2 = Math.toRadians(lat2);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = squared(sin(dlat/2)) + cos(lat1) * cos(lat2) * squared(sin(dlon/2));
        double c = 2.0d * asin(sqrt(a));
        double r = radius_at_lat_lon((lat1+lat2)/2.0d, (lon1+lon2)/2.0d);
        r = r + alt; // actual height above or below idealized ellipsoid
        return c * r;
    }

    /**
     * Takes two Lat/Lon pairs (a start A and a destination B) and finds the heading of the shortest direction of travel from A to B
     * <p>
     *     Note: this function will work with Geodetic coords of any ellipsoid, provided both pairs are of the same ellipsoid
     *     adapted from https://stackoverflow.com/a/64747209
     * </p>
     * @param lon1
     * @param lat1
     * @param lon2
     * @param lat2
     * @return
     */
    static double haversine_bearing(double lon1, double lat1, double lon2, double lat2) {
        lon1 = Math.toRadians(lon1);
        lat1 = Math.toRadians(lat1);
        lon2 = Math.toRadians(lon2);
        lat2 = Math.toRadians(lat2);

        double dLon = (lon2 - lon1);
        double x = cos(lat2) * sin(dLon);
        double y = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon);

        double brng = atan2(x,y); // arguments intentionally swapped out of order
        brng = radNormalize(brng);
        brng = Math.toDegrees(brng);

        return brng;
    }

    static double squared(double val) {
        return val * val;
    }

    static double sqrt(double val) {
        return Math.sqrt(val);
    }

    static double sin(double radAngle) {
        return Math.sin(radAngle);
    }

    static double asin(double radAngle) {
        return Math.asin(radAngle);
    }

    static double cos(double radAngle) {
        return Math.cos(radAngle);
    }

    static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

}
