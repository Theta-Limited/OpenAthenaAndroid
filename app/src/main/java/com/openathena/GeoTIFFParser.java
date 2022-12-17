package com.openathena;

import android.util.Log;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.io.InputStream;
import java.io.PrintWriter;

import java.lang.Math;

import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;

import com.openathena.RequestedValueOOBException;
import com.openathena.geodataAxisParams;

import mil.nga.tiff.*;

public class GeoTIFFParser {

    public static String TAG = GeoTIFFParser.class.getSimpleName();

    private File geofile;

    private TIFFImage tiffImage;
    private List<FileDirectory> directories;
    private FileDirectory directory;
    private Rasters rasters;

    private geodataAxisParams xParams;
    private geodataAxisParams yParams;

    GeoTIFFParser() {
        geofile = null;

        TIFFImage tiffImage = null;
        List<FileDirectory> directories = null;
        FileDirectory directory = null;
        Rasters rasters = null;

    }

    GeoTIFFParser(File geofile) throws IllegalArgumentException{
        this();
        this.geofile = geofile;
        loadGeoTIFF(geofile);



    }

    public void loadGeoTIFF(File geofile) throws IllegalArgumentException {
        this.geofile = geofile;
  /*      this.geodata = gdal.Open(geofile);
        this.geoTransform = getGeoTransform(geodata);*/

        try {
            tiffImage = TiffReader.readTiff(geofile);
        } catch (IOException e) {
            Log.d("IOException", "Failed to read geofile: " + e.getMessage());
            return;
        }

        directories = tiffImage.getFileDirectories();
        directory = directories.get(0);
        rasters = directory.readRasters();

        for (int i = 0; i < directories.size(); i++ ) {
            FileDirectory aDirectory = directories.get(i);
            Log.d("info", "\nFile Directory:");
            Log.d("info", String.valueOf(i));
            Log.d("info","\n");
            Rasters theseRasters = aDirectory.readRasters();
            Log.d("info","\n");
            Log.d("info","Rasters:");
            Log.d("info", "Width: " + rasters.getWidth());
            Log.d("info", "Height: " + rasters.getHeight());
            Log.d("info", "Number of Pixels: " + rasters.getNumPixels());
            Log.d("info", "Samples Per Pixel: " + rasters.getSamplesPerPixel());
            Log.d("info", "Bits Per Sample: " + rasters.getBitsPerSample());

            Log.d("info", "0,0 is: " + theseRasters.getPixel(0, 0)[0].doubleValue() );

        }

        List<Double> pixelAxisScales = directory.getModelPixelScale();
        if (pixelAxisScales.get(2) != 0.0d) {
            throw new IllegalArgumentException("ERROR: failed to load a rotated or skewed GeoTIFF!");
        }
        List<Double> tiePoint = directory.getModelTiepoint();
        Number imgWidth = directory.getImageWidth();
        Number imgHeight = directory.getImageHeight();

        Log.d("info", "pixelAxisScales:" + pixelAxisScales.toString());
        Log.d("info", "tiePoint: " + tiePoint);
        Log.d("info", "imgWidth: " + imgWidth );
        Log.d("info", "imgHeight: " + imgHeight);

        this.xParams = new geodataAxisParams();
        this.xParams.start = tiePoint.get(3);
        this.xParams.stepwiseIncrement = pixelAxisScales.get(0);
        this.xParams.numOfSteps = imgWidth.longValue();
        this.xParams.calcEndValue();

        this.yParams = new geodataAxisParams();
        this.yParams.start = tiePoint.get(4);
        this.yParams.stepwiseIncrement = -1.0d * pixelAxisScales.get(1);
        this.yParams.numOfSteps = imgHeight.longValue();
        this.yParams.calcEndValue();
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

/*
    private double[] getGeoTransform(Dataset geodata) {
        return geodata.GetGeoTransform();
    }
*/
/*

    public boolean isGeoTIFFValid() {
        // {x0, dx, dxdy, y0, dydx, dy}
        double dxdy = (double) this.geoTransform[2];
        double dydx = (double) this.geoTransform[4];
        return ((dxdy == 0.0d) && (dydx == 0.0d));
    }
*/

    public static byte[] floatToBytes(float[] floats) {
        byte bytes[] = new byte[Float.BYTES * floats.length];
        ByteBuffer.wrap(bytes).asFloatBuffer().put(floats);
        return bytes;
    }

    public double getXResolution() {
        return xParams.stepwiseIncrement;
    }

    public double getAltFromLatLon(double lat, double lon) throws RequestedValueOOBException {
        if (geofile == null || rasters == null || xParams == null || yParams == null) {
            throw new NullPointerException("getAltFromLatLon pre-req was null!");
        }
        if ( xParams.numOfSteps <= 0 || yParams.numOfSteps <= 0) {
            throw new IllegalArgumentException("getAltFromLatLon dataset was empty!");
        }
        Log.d(TAG, "lat: " + lat + " lon: " + lon);

        double x0 = xParams.start;
        double x1 = xParams.end;
        double dx = xParams.stepwiseIncrement;
        long ncols = xParams.numOfSteps;

        double y0 = yParams.start;
        double y1 = yParams.end;
        double dy = yParams.stepwiseIncrement;
        long nrows = yParams.numOfSteps;

        Log.d(TAG, "x0: " + x0 + " x1: " + x1);
        Log.d(TAG, "y0: " + y0 + " y1: " + y1);

        // Out of Bounds (OOB) check
        if (( lat > y0 || y1 > lat ) || ( lon > x1 || x0 > lon)) { // note: y0 > y1 but x0 < x1 (dy is always negative)
            throw new RequestedValueOOBException("getAltFromLatLon arguments out of bounds!", lat, lon);
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

        // https://gdal.org/java/org/gdal/gdal/Dataset.html#ReadRaster(int,int,int,int,int,int,int,byte%5B%5D,int%5B%5D)
        // https://gis.stackexchange.com/questions/349760/get-elevation-of-geotiff-using-gdal-bindings-in-java
//        geodata.ReadRaster((int) xIndex, (int) yIndex, 1, 1, 1, 1, 6, floatToBytes(result), band);
        double result = rasters.getPixel((int) xIndex, (int) yIndex)[0].doubleValue();
        return result;
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
