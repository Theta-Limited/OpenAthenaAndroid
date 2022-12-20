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

    public double[] resolveTarget(double lat, double lon, double alt, double azimuth, double theta) throws RequestedValueOOBException{
        if (myGeoTIFFParser == null) {
            throw new NullPointerException("FATAL ERROR: resolveTarget attempted before geotiff loaded");
        }
        double radAzimuth = Math.toRadians(azimuth);
        double radTheta = Math.toRadians(theta);

        radAzimuth = normalize(radAzimuth);
        radTheta = Math.abs(radTheta); // for our purposes, angle of declination will always be expressed as a positive number

        double finalDist;
        double tarLat;
        double tarLon;
        double tarAlt;
        double terrainAlt;
        double[] outArr;

        // Check if target is exactly straight downwards,
        //     if so, skip iterative search b/c target is directly below us
        if (Math.abs(Double.compare(radTheta, Math.PI)) <= 0.005d) { // 0.005 radians ~= 0.29 degrees
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
        // during manual data entry, please avoid absolute values > 90
        if (radAzimuth > (Math.PI / 2)) {
            radAzimuth = normalize(radAzimuth + Math.PI);
            radTheta = Math.PI - radTheta;
        }

        // direction, convert azimuth to unit circle (just like math class)
        double radDirection = azimuthToUnitCircleRad(radAzimuth);

        // from Direction, determine rate of x and y change
        //     per unit travel (level with horizon for now)
        double deltaX = Math.cos(radDirection);
        double deltaY = Math.sin(radDirection);

        double deltaZ = -1.0d * Math.sin(radTheta); // neg because direction is downward

        // determines by how much of travel per unit is actually horiz
        // pythagorean theorem, deltaz^2 + deltax^2 + deltay^2 = 1
        double horizScalar = Math.cos(radTheta);
        deltaX *= horizScalar;
        deltaY *= horizScalar;

        // meters of acceptable distance between constructed line and datapoint
        // Somewhat arbitrary. SRTM has a horizontal resolution of 30m, but vertical accuracy is often more precise
        double post_spacing_meters = haversine(0, lat, myGeoTIFFParser.getXResolution(), lat, alt); // meters between datapoints, from degrees
        final double THRESHOLD = post_spacing_meters / 8.0d; // meters of acceptable distance between constructed line and datapoint. somewhat arbitrary

        double curLat = lat;
        double curLon = lon;
        double curAlt = alt;
        double groundAlt;
        try {
            groundAlt = myGeoTIFFParser.getAltFromLatLon(curLat, curLon);
        } catch (RequestedValueOOBException e) {
            throw e;
        }
        if (curAlt < groundAlt) {
            boolean isAltitudeBad = true;
            throw new RequestedValueOOBException("ERROR: bad altitude or terrain data. This picture is unusable. \uD83D\uDDBC\uD83D\uDEAB\uD83E\uDDEE\uD83C\uDFAF", curLat, curLon, isAltitudeBad);
        }
        double altDiff = curAlt - groundAlt;
        while (altDiff > THRESHOLD) {
            try {
                groundAlt = myGeoTIFFParser.getAltFromLatLon(curLat, curLon);
            } catch (RequestedValueOOBException e) {
                throw e;
            }
            altDiff = curAlt - groundAlt;

            double avgAlt = curAlt;
            // deltaZ should always be negative
            curAlt += deltaZ;
            avgAlt = (avgAlt + curAlt) / 2.0d;
            double[] nextIter = inverse_haversine(curLat, curLon, horizScalar*INCREMENT, radAzimuth, avgAlt);
            curLat = nextIter[0];
            curLon = nextIter[1];
        }

        // When the loop ends, curY, curX, and curZ are closeish to the target
        //     may be a bit biased ever so slightly long (beyond the target)
        //     this algorithum is crude, does not take into account the curvature of the earth over long distances
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

    public double normalize(double radAngle) {
        while (radAngle < 0) {
            radAngle += 2 * Math.PI;
        }
        while (radAngle >= (2 * Math.PI)) {
            radAngle -= 2 * Math.PI;
        }
        return radAngle;
    }

    public double azimuthToUnitCircleRad(double radAzimuth) {
        double radDirection = -1.0d * radAzimuth;
        radDirection += (0.5d * Math.PI);
        radDirection = normalize(radDirection);
        return radDirection;
    }

    public double radius_at_lat_lon(double lat, double lon) {
        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon); // not used

        final double A = 6378137.0d; // equatorial radius of WGS ellipsoid, in meters
        final double B = 6356752.3d; // polar radius of WGS ellipsoid, in meters
        double r = squared(A * A * cos(lat)) + squared(B * B * sin(lat)); // numerator
        r /= squared(A * cos(lat)) + squared(B * sin(lat)); // denominator
        r = Math.sqrt(r); // square root
        return r;
    }

    double[] inverse_haversine(double lat1, double lon1, double d /*distance*/, double radAzimuth, double alt) {
        if (d < 0.0d) {
            return inverse_haversine(lat1, lon1, -1.0d * d, normalize(radAzimuth + Math.PI), alt);
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

    double haversine(double lon1, double lat1, double lon2, double lat2, double alt) {
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

    double haversine_bearing(double lon1, double lat1, double lon2, double lat2) {
        lon1 = Math.toRadians(lon1);
        lat1 = Math.toRadians(lat1);
        lon2 = Math.toRadians(lon2);
        lat2 = Math.toRadians(lat2);

        double dLon = (lon2 - lon1);
        double x = cos(lat2) * sin(dLon);
        double y = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon);

        double brng = atan2(x,y); // arguments intentionally swapped out of order
        brng = normalize(brng);
        brng = Math.toDegrees(brng);

        return brng;
    }

    double squared(double val) {
        return val * val;
    }

    double sqrt(double val) {
        return Math.sqrt(val);
    }

    double sin(double radAngle) {
        return Math.sin(radAngle);
    }

    double asin(double radAngle) {
        return Math.asin(radAngle);
    }

    double cos(double radAngle) {
        return Math.cos(radAngle);
    }

    double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

}
