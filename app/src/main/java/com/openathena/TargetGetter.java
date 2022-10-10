package com.openathena;

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

import org.gdal.gdal.gdal;
import org.gdal.gdal.BuildVRTOptions;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.TranslateOptions;

public class TargetGetter {
    private GeoTIFFParser myGeoTIFFParser;
    private File geofile;
    private final double INCREMENT = 1.0d;
    // private MGRS mgrs;
    // private WGS84_SK42_Translator ellipsoidTranslator
    // private SK42_Gauss_Kruger_Projector gk_Projector

    TargetGetter() {
        myGeoTIFFParser = new GeoTIFFParser();
        geofile = null;
    }

    TargetGetter(File geofile) {
        this();
        this.geofile = geofile;
        myGeoTIFFParser.loadGeoTIFF(geofile);
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
                terrainAlt = new Double(myGeoTIFFParser.getAltFromLatLon(lat, lon));
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
        // pythagoran theorem, deltaz^2 + deltax^2 + deltay^2 = 1
        double horizScalar = Math.cos(radTheta);
        deltaX *= horizScalar;
        deltaY *= horizScalar;

        // meters of acceptable distance between constructed line and datapoint
        // Somewhat arbitrary. SRTM has a horizontal resolution of 30m, but vertical accuracy is often more precise
        final double THRESHOLD = myGeoTIFFParser.getXResolution() / 4.0d;

        double curLat = lat;
        double curLon = lon;
        double curAlt = alt;
        double groundAlt;
        try {
            groundAlt = myGeoTIFFParser.getAltFromLatLon(curLat, curLon);
        } catch (RequestedVauleOOBException e) {
            throw e;
        }
        double altDiff = curAlt - groundAlt;
        while (altDiff > THRESHOLD) {
            try {
                groundAlt = myGeoTIFFParser.getAltFromLatLon(curLat, curLon);
            } catch (RequestedVauleOOBException e) {
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

}
