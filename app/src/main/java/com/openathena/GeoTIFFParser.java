package com.openathena;

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

public class geodataAxisParams {
    double start;
    double end;
    double stepwiseIncrement;
    long numOfSteps;

    public void calcEndValue() {
        end = start + stepwiseIncrement * numOfSteps;
    }
}

public class RequestedValueOOBException extends Exception {
    public RequestedValueOOBException(String errorMessage) {
        super(errorMessage);
    }
}

public class GeoTIFFParser {

    private double[] geoTransform;
    private File geofile;
    private Dataset geodata;
    // private long ncols;
    // private long nrows;
    private geodataAxisParams xParams;
    private geodataAxisParams yParams;

    GeoTIFFParser() {
        gdal.AllRegister();
        gdal.SetConfigOption("gdal_FILENAME_IS_UTF8", "YES");
        geoTransform = null;
        geofile = null;
        geodata = null;
    }

    GeoTIFFParser(File geofile) {
        this();
        loadGeoTIFF(geofile);
    }

    public void loadGeoTIFF(File geofile) {
        this.geofile = geofile;
        this.geodata = gdal.Open(geofile);
        this.geoTransform = getGeoTransform(geodata);

        // this.ncols = (long) geodata.getRasterXSize();
        this.xParams = new geodataAxisParams();
        xParams.start = geoTransform[0];
        xParams.stepwiseIncrement = geoTransform[1];
        xParams.numOfSteps = (long) geodata.getRasterXSize();
        xParams.calcEndValue();

        // this.nrows = (long) geodata.getRasterYSize();
        this.yParams = new geodataAxisParams();
        yParams.start = geoTransform[3];
        yParams.stepwiseIncrement = geoTransform[5];
        yParams.numOfSteps = (long) geodata.getRasterYSize();
        yParams.calcEndValue();
    }

    // private double[] getDummyGeoTransform(Dataset geodata) {
    //     // dummy output, from Rome-30m-DEM.tif
    //     double x0 = 12.34986111111111d;
    //     double dx = 0.000277777777776933d;
    //     double dxdy = 0.0d;
    //     double y0 = 42.00013888888889d;
    //     double dydx = 0.0d;
    //     double dy = -0.00027777777777515666d;

    //     double[] transformOut = new double[]{x0, dx, dxdy, y0, dydx, dy};
    //     return transformOut;
    // }

    private double[] getGeoTransform(Dataset geodata) {
        return geodata.GetGeoTransform();
    }

    public static byte[] floatsToBytes(float[] floats) {
        byte bytes[] = new byte[Float.BYTES * floats.length];
        ByteBuffer.wrap(bytes).asFloatBuffer().put(floats);
        return bytes;
    }


    public double getAltFromLatLon(double lat, double lon) throws RequestedValueOOBException {
        if (geoTransform == null || geofile == null || geodata == null || xParams == null || yParams == null) {
            throw new NullPointerException("getAltFromLatLon pre-req was null!");
        }
        if (ncols <= 0 || nrows <= 0) {
            throw new IllegalArgumentException("getAltFromLatLon dataset was empty!");
        }

        double x0 = xParams.start;
        double x1 = xParams.end;
        double dx = xParams.stepwiseIncrement;
        long ncols = xParams.numOfSteps;

        double y0 = yParams.start;
        double y1 = yParams.end;
        double dy = yParams.stepwiseIncrement;
        long nrows = yParams.numOfSteps;

        // Out of Bounds (OOB) check
        if (( lat > y0 || y1 > lat ) || ( lon > x1 || x0 > lon)) { // note: y0 > y1 but x0 < x1 (dy is always negative)
            throw new RequestedValueOOBException("getAltFromLatLon arguments out of bounds!");
        }

        long[] xNeighbors = binarySearchNearest(x0, ncols, lon, dx);
        long xL = xNeighbors[0];
        long xR = xNeighbors[1];
        long xIndex;
        if (Math.abs(lon - (x0 + xL * dx)) < Math.abs(lon - (x0 + xR * dx))) {
            xIndex = xL;
        } else {
            xIndex = xR;
        }

        long[] yNeighbors = binarySearchNearest(y0, nrows, lat, dy);
        long yT = yNeighbors[0];
        long yB = yNeighbors[1];
        long yIndex;
        if (Math.abs(lat - (y0 + yT * dy)) < Math.abs(lat - (y0 + yB * dy))) {
            yIndex = yT;
        } else {
            yIndex = yB;
        }


        float[] result = new float[1]; // float array of size 1 to store result
        int[] band = {1}; // first band should be elevation data (GDAL)
        // https://gdal.org/java/org/gdal/gdal/Dataset.html#ReadRaster(int,int,int,int,int,int,int,byte%5B%5D,int%5B%5D)
        // https://gis.stackexchange.com/questions/349760/get-elevation-of-geotiff-using-gdal-bindings-in-java
        dataset.ReadRaster((int) xIndex, (int) yIndex, 1, 1, 1, 1, 6, floatToBytes(result), band);
        double altitudeAtLatLon = result[0];
        return altitudeAtLatLon;
    }

    long[] binarySearchNearest(double start, long n, double val, double dN) {
        long[] out = new long[2];
        if ( n <= 0 ) { // dataset is empty
            return null;
        }

        if ( n == 1 ) { // if only one elevation datapoint; exceedingly rare
            if (Double.compare(start, val) <= 0.00000001d) {
                return new long[]{(long) 0, (long) 0};
            } else {
                return null;
            }
        }

        if (dN == 0.0d) {
            return null;
        }

        boolean isDecreasing = (dN < 0.0d);
        if (isDecreasing) {
            // if it's in decreasing order, uh, don't do that. Make it increasing instead!
            double reversedStart = start + n * dN;
            double reversedDN = -1.0d * dN;

            long[] recurseResult = binarySearchNearest(reversedStart, n, val, reversedDN);
            long a1 = recurseResult[0];
            long a2 = recurseResult[1];

            // kinda weird, but we reverse index result since we reversed the list
            a1 = n - a1 - 1;
            a2 = n - a2 - 1;
            return new long[]{a1, a2};
        }

        long L = 0;
        long lastIndex = n - 1;
        long R = lastIndex;
        while (L <= R) {
            long m = (long) Math.floor((L + R) / 2);
            if (start + m * dN < val) {
                L = m + 1;
            } else if (start + m * dN > val) {
                R = m - 1;
            } else {
                // exact match
                return new long[]{m, m};
            }
        }

        // if we've broken out of the loop, L > R
        //     which means the markers have flipped
        //     therfore, either list[L] or list[R] must be closest to val
        return new long[]{R, L};
    }
}
