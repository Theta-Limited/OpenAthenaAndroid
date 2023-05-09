package com.openathena;

import android.util.Log;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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

public class GeoTIFFParser implements Serializable {
    private static final long serialVersionUID = 1L;

    public static String TAG = GeoTIFFParser.class.getSimpleName();

    private transient File geofile;

    private transient TIFFImage tiffImage;
    private transient List<FileDirectory> directories;
    private transient FileDirectory directory;
    private Rasters rasters; // implements Serializable

    private geodataAxisParams xParams; // implements Serializable
    private geodataAxisParams yParams; // implements Serializable

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

    /**
     * Loads a GeoTIFF Digital Elevation Model geofile into the parent GeoTIFFParser object's instance
     * <p>
     *     This function takes in a Java file object and loads it into the parent GeoTIFFParser object.
     *     Once loaded, the GeoTIFFParser object (via {@link com.openathena.GeoTIFFParser#getAltFromLatLon(double, double)} will be able to provide the nearest elevation value from a given latitude longitude pair
     *
     * </p>
     * @param geofile a Java file object which should represent a GeoTIFF DEM
     * @throws IllegalArgumentException if geofile cannot be read or is rotated or skewed
     */
    public void loadGeoTIFF(File geofile) throws IllegalArgumentException {
        this.geofile = geofile;
  /*      this.geodata = gdal.Open(geofile);
        this.geoTransform = getGeoTransform(geodata);*/

        try {
            tiffImage = TiffReader.readTiff(geofile);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read geofile: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
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

    /**
     * Gets the spacing between X datapoints of the loaded GeoTIFF DEM
     * @return double degrees between each datapoint along the X direction
     */
    public double getXResolution() { return xParams.stepwiseIncrement; }

    /**
     * Gets the spacing between Y datapoints of the loaded GeoTIFF DEM
     * @return double degrees between each datapoint along the Y direction
     */
    public double getYResolution() { return yParams.stepwiseIncrement; }

    /**
     * Gets the number of columns (width) of the loaded GeoTIFF DEM
     * @return long number of pixels equivalent to the DEM's width
     */
    public long getNumCols() { return xParams.numOfSteps; }

    /**
     * Gets the number of rows (height) of the loaded GeoTIFF DEM
     * @return long number of pixels equivalent to the DEM's height
     */
    public long getNumRows() { return yParams.numOfSteps; }

    /**
     * Gets the minimum longitude (inclusive) covered by the loaded GeoTIFF DEM
     * @return double the longitude of the western-most datapoint of the loaded GeoTIFF DEM
     */
    public double getMinLon() { return Math.min(xParams.end, xParams.start); }

    /**
     * Gets the maximum longitude (inclusive) covered by the loaded GeoTIFF DEM
     * @return double the longitude of the eastern-most datapoint of the loaded GeoTIFF DEM
     */
    public double getMaxLon() { return Math.max(xParams.end, xParams.start); }

    /**
     * Gets the minimum latitude (inclusive) covered by the loaded GeoTIFF DEM
     * @return double the latitude of the southern-most datapoint of the loaded GeoTIFF DEM
     */
    public double getMinLat() { return Math.min(yParams.end, yParams.start); }

    /**
     * Gets the maximum latitude (inclusive) covered by the loaded GeoTIFF DEM
     * @return double the latitude of the northern-most datpoint of the loaded GeoTIFF DEM
     */
    public double getMaxLat() { return Math.max(yParams.end, yParams.start); }

    private static class Location {
        double latitude;
        double longitude;
        double elevation;

        private Location(double latitude, double longitude, double elevation) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
        }

        private Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Inverse Distance Weighting for interpolating a target lat/lon from neighboring samples
     * <p>
     *     For more info, see:
     *         https://github.com/Theta-Limited/OpenAthenaAndroid/issues/70
     *         https://doi.org/10.3846/gac.2023.16591
     *         https://pro.arcgis.com/en/pro-app/latest/help/analysis/geostatistical-analyst/how-inverse-distance-weighted-interpolation-works.htm
     * </p>
     * @param target a lat/lon Location for which to interpolate its elevation
     * @param neighbors a list of lat/lon Location(s) of the target's neighboring samples
     * @param power the power parameter controls the degree of influence that the neighboring points have on the interpolated value. A higher power will result in a higher influence of closer points and a lower influence of more distant points.
     * @return interpolated elevation of the target in meters above the WGS84 reference ellipsoid
     */
    private static double idwInterpolation(Location target, Location[] neighbors, double power) {
        double sumWeights = 0.0d;
        double sumWeightedElevations = 0.0d;

        for (Location neighbor : neighbors) {
            double distance = TargetGetter.haversine(target.longitude, target.latitude, neighbor.longitude, neighbor.latitude, neighbor.elevation);
            if (distance <= 0.5d) { // if distance from sample is  < 0.5 meter, just use the raw sample
                return neighbor.elevation;
            }

            double weight = 1.0d / Math.pow(distance, power);
            sumWeights += weight;
            sumWeightedElevations += weight * neighbor.elevation;
        }

        return sumWeightedElevations / sumWeights;
    }

    /**
     * Using the loaded GeoTIFF DEM, obtains the nearest elevation value for a given Lat/Lon pair
     * <p>
     *     This function returns an interpolated elevation value using the nearest samples from the given Lat/Lon pair
     * </p>
     * @param lat The latitude of the result desired. [-90, 90]
     * @param lon The longitude of the result desired. [-180, 180]
     * @return The altitude of the terrain near the given Lat/Lon, in meters above the WGS84 reference ellipsoid
     * @throws RequestedValueOOBException
     */
    public double getAltFromLatLon(double lat, double lon) throws RequestedValueOOBException {
        if (rasters == null || xParams == null || yParams == null) {
            throw new NullPointerException("getAltFromLatLon pre-req was null!");
        }
        if ( xParams.numOfSteps <= 0 || yParams.numOfSteps <= 0) {
            throw new IllegalArgumentException("getAltFromLatLon dataset was empty!");
        }
//        Log.d(TAG, "lat: " + lat + " lon: " + lon);

        double x0 = xParams.start;
        double x1 = xParams.end;
        double dx = xParams.stepwiseIncrement;
        long ncols = xParams.numOfSteps;

        double y0 = yParams.start;
        double y1 = yParams.end;
        double dy = yParams.stepwiseIncrement;
        long nrows = yParams.numOfSteps;

//        Log.d(TAG, "x0: " + x0 + " x1: " + x1);
//        Log.d(TAG, "y0: " + y0 + " y1: " + y1);

        // Out of Bounds (OOB) check
        if (( lat > getMaxLat() || getMinLat() > lat ) || ( lon > getMaxLon() || getMinLon() > lon)) {
            throw new RequestedValueOOBException("getAltFromLatLon arguments out of bounds!", lat, lon);
        }

        long[] xNeighbors = binarySearchNearest(x0, ncols, lon, dx);
        long[] yNeighbors = binarySearchNearest(y0, nrows, lat, dy);
        long xL = xNeighbors[0];
        long xR = xNeighbors[1];
        long yT = yNeighbors[0];
        long yB = yNeighbors[1];
        // https://gdal.org/java/org/gdal/gdal/Dataset.html#ReadRaster(int,int,int,int,int,int,int,byte%5B%5D,int%5B%5D)
        // https://gis.stackexchange.com/questions/349760/get-elevation-of-geotiff-using-gdal-bindings-in-java
        Location L1 = new Location(y0 + yT * dy,x0 + xR * dx, rasters.getPixel((int) xR, (int) yT)[0].doubleValue());
        Location L2 = new Location(y0 + yT * dy, x0 + xL * dx, rasters.getPixel((int) xL, (int) yT)[0].doubleValue());
        Location L3 = new Location(y0 + yB * dy, x0 + xL * dx, rasters.getPixel((int) xL, (int) yB)[0].doubleValue());
        Location L4 = new Location(y0 + yB * dy, x0 + xR * dx, rasters.getPixel((int) xR, (int) yB)[0].doubleValue());

        Location target = new Location(lat, lon);
        Location[] neighbors = new Location[]{L1, L2, L3, L4};
        /* the power parameter controls the degree of influence that the neighboring points have
         * on the interpolated value. A higher power will result in a higher influence
         * of closer points and a lower influence of more distant points.
         */
        double power = 2.0d;
        // Inverse Distance Weighting interpolation using 4 neighbors
        // see: https://doi.org/10.3846/gac.2023.16591
        //      https://pro.arcgis.com/en/pro-app/latest/help/analysis/geostatistical-analyst/how-inverse-distance-weighted-interpolation-works.htm
        return idwInterpolation(target, neighbors, power);
    }

    /**
     * Performs a binary search, returning indices pointing to the two closest values to the input
     * @param start the start value, in degrees, of an axis of the geofile
     * @param n the number of items in an axis of the geofile
     * @param val an input value for which to find the two closest indices
     * @param dN the change in value for each increment of the index along an axis of the geofile
     * @return
     */
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
        //     therefore, either list[L] or list[R] must be closest to val
        return new long[]{R, L};
    }
}
