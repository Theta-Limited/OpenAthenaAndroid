package com.openathena;

import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java.lang.Math;

import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;
import java.util.Locale;

import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.dted.DTEDLevelEnum;
import com.agilesrc.dem4j.dted.impl.FileBasedDTED;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

import mil.nga.tiff.*;
import mil.nga.tiff.util.TiffException;

public class DEMParser implements Serializable {

    public static String TAG = DEMParser.class.getSimpleName();

    private transient File geofile;

    private transient TIFFImage tiffImage;
    private transient List<FileDirectory> directories;
    private transient FileDirectory directory;
    private Rasters rasters; // implements Serializable

    private geodataAxisParams xParams; // implements Serializable
    private geodataAxisParams yParams; // implements Serializable

    private EGMOffsetProvider offsetProvider = new EGM96OffsetAdapter();

    private verticalDatumTypes verticalDatum;

    private FileBasedDTED dted;
    public boolean isDTED = false;
    public DTEDLevelEnum dtedLevel = null;

    DEMParser() {
        geofile = null;

        TIFFImage tiffImage = null;
        List<FileDirectory> directories = null;
        FileDirectory directory = null;
        Rasters rasters = null;
    }

    DEMParser(File geofile) throws IllegalArgumentException {
        this();
        this.geofile = geofile;
        if (!geofile.exists()) {
            throw new IllegalArgumentException(Resources.getSystem().getString(R.string.error_the_file) + geofile.getAbsolutePath() + " " + Resources.getSystem().getString(R.string.error_geofile_does_not_exist_2));
        }
        try {
            loadDEM(geofile);
        } catch (IllegalArgumentException | TiffException e) {
            // If GeoTIFF parsing fails, try to parse as DTED
            try {
                this.dted = new FileBasedDTED(geofile);
                dtedLevel = this.dted.getDTEDLevel();
                Log.d(TAG, "DTED LEVEL IS: " + dtedLevel);
                if (dtedLevel.equals(DTEDLevelEnum.DTED0) || dtedLevel.equals(DTEDLevelEnum.DTED1)) {
                    throw new CorruptTerrainException(Resources.getSystem().getString(R.string.demparser_error_dted2_or_dted3_is_required));
                }
                this.xParams = new geodataAxisParams();
                this.xParams.start = this.dted.getNorthWestCorner().getLongitude();
                this.xParams.end = this.dted.getNorthEastCorner().getLongitude();
                this.xParams.stepwiseIncrement = this.dted.getLongitudeInterval();
                this.xParams.numOfSteps = (long) Math.ceil((xParams.end - xParams.start) / xParams.stepwiseIncrement);
                this.yParams = new geodataAxisParams();
                this.yParams.start = this.dted.getNorthWestCorner().getLatitude();
                this.yParams.end = this.dted.getSouthWestCorner().getLatitude();
                this.yParams.stepwiseIncrement = this.dted.getLatitudeInterval();
                this.yParams.numOfSteps = (long) Math.ceil((yParams.start - yParams.end) / Math.abs(yParams.stepwiseIncrement));
                this.isDTED = true;
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }
    }

    public enum verticalDatumTypes implements Serializable{
        WGS84,
        EGM96,
        NAVD88,
        UNKNOWN_OTHER
    }

//    final short VERTICAL_CS_TYPE_GEO_KEY = 4096;
//    final short VERTICAL_DATUM_GEO_KEY = 4097;
//    final short VERTICAL_UNITS_GEO_KEY = 4099;

    /**
     * Loads a GeoTIFF or DTED2 Digital Elevation Model geofile into the parent DEMParser object's instance
     * <p>
     *     This function takes in a Java file object and loads it into the parent DEMParser object.
     *     Once loaded, the DEMParser object (via {@link DEMParser#getAltFromLatLon(double, double)} will be able to provide the nearest elevation value from a given latitude longitude pair
     *
     * </p>
     * @param geofile a Java file object which should represent a GeoTIFF or DTED DEM
     * @throws IllegalArgumentException if geofile cannot be read or is rotated or skewed
     */
    public void loadDEM(File geofile) throws IllegalArgumentException {
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
        if (pixelAxisScales == null || pixelAxisScales.isEmpty()) {
            throw new IllegalArgumentException(Resources.getSystem().getString(R.string.demparser_error_geotiff_file_is_corrupted));
        }
        if (pixelAxisScales.get(2) != 0.0d) {
            throw new IllegalArgumentException(Resources.getSystem().getString(R.string.dem_parser_error_failed_to_load_a_rotated_or_skewed_geotiff));
        }

        FileDirectoryEntry fde = directory.get(FieldTagType.GeoKeyDirectory);
        // Because my sources don't properly store vertical datum,
        // we have to assume its EGM96 (for GeoTIFFs) and hope it's correct :(
        // When we implement the DTED2 and DTED3 format,
        // we know it will also be in EGM96
        verticalDatum = verticalDatumTypes.EGM96;

        if (fde != null) {
            ArrayList<Integer> values = (ArrayList<Integer>) fde.getValues();
            if (values != null) {
                boolean isWGS84 = isHorizontalDatumWGS84(directory, values);
                if (!isWGS84) {
                    throw new IllegalArgumentException(Resources.getSystem().getString(R.string.demparser_error_horizontal_datum_not_of_an_accepted_type));
                }
            } else {
                Log.e(TAG, "metadata values obtained were of an unknown type");
            }
        } else {
            Log.e(TAG, "Could not obtain FileDirectoryEntry for GeoTIFF metadata");
        }

//        Log.d(TAG, "vertical datum is: " + verticalDatum.name());
//        if (verticalDatum == verticalDatumTypes.UNKNOWN_OTHER || verticalDatum == verticalDatumTypes.NAVD88) {
//            throw new IllegalArgumentException("ERROR: vertical datum not of an accepted type");
//        }
//        if (verticalUnit == verticalUnitTypes.UNKNOWN_OTHER) {
//            throw new IllegalArgumentException("ERROR: vertical unit not of an accepted type");
//        }

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

    public boolean isHorizontalDatumWGS84(FileDirectory directory, ArrayList<Integer> geoKeys) {
        int numberOfKeys = geoKeys.get(3);
        for (int i = 0; i < numberOfKeys; i++) {
            int index = 4 + i * 4;
            int keyId = geoKeys.get(index);
            int tiffTagLocation = geoKeys.get(index + 1);
            int count = geoKeys.get(index + 2);
            int valueOffset = geoKeys.get(index + 3);

            if (keyId == 2048) { // GeographicTypeGeoKey
                if (tiffTagLocation == 0) {
                    // The valueOffset is the value of the key.
                    int value = valueOffset;
                    if (value == 4326) { // 4326 is the EPSG numnber for WGS84 ellipsoid
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    // The valueOffset is an offset into the tag specified by tiffTagLocation.
                    // Read the value from the specified tag.
                    FileDirectoryEntry tagEntry = directory.get(FieldTagType.getById(tiffTagLocation));
                    if (tagEntry != null) {
                        Object tagValues = tagEntry.getValues();
                        if (tagValues instanceof ArrayList) {
                            ArrayList<Integer> tagArray = (ArrayList<Integer>) tagValues;
                            int arrayIndex = valueOffset / 2; // Convert byte offset to array index
                            if (arrayIndex < tagArray.size()) {
                                int tagValue = tagArray.get(arrayIndex);
                                if (tagValue == 4326) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

//    public verticalDatumTypes getVerticalDatum(FileDirectory directory, ArrayList<Integer> geoKeys) {
//        int numberOfKeys = geoKeys.get(3);
//        for (int i = 0; i < numberOfKeys; i++) {
//            int index = 4 + i * 4;
//            int keyId = geoKeys.get(index);
//            int tiffTagLocation = geoKeys.get(index + 1);
//            int count = geoKeys.get(index + 2);
//            int valueOffset = geoKeys.get(index + 3);
//            Log.d(TAG, "KeyID: " + keyId + ", TIFF Tag Location: " + tiffTagLocation + ", Count: " + count + ", Value Offset: " + valueOffset);
//            if (keyId == VERTICAL_CS_TYPE_GEO_KEY || keyId == VERTICAL_DATUM_GEO_KEY) {
//                if (tiffTagLocation == 0) {
//                    // The valueOffset is the value of the key.
//                    int value = valueOffset;
//                    return datumValueFromKey(keyId, value);
//                    //System.out.println("KeyID: " + keyId + ", Value: " + value);
//                } else {
//                    // The valueOffset is an offset into the tag specified by tiffTagLocation.
//                    // Read the value from the specified tag.
//                    FileDirectoryEntry tagEntry = directory.get(FieldTagType.getById(tiffTagLocation));
//                    if (tagEntry != null) {
//                        Object tagValues = tagEntry.getValues();
//                        if (tagValues != null) {
//                            ArrayList arr = (ArrayList) tagValues;
//                            int tag0 = (Integer) arr.get(0);
//                            Log.d(TAG, "" + tag0);
//                            short[] tagArray = (short[]) tagValues;
//                            int byteIndex = valueOffset / 2; // Convert byte offset to index in uint16 array
//                            if (byteIndex < tagArray.length) {
//                                short tagValue = tagArray[byteIndex];
//                                return datumValueFromKey(keyId, tagValue);
//                                // System.out.println("KeyID: " + keyId + ", Value: " + value);
//                            } else {
//                                Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//                                return verticalDatumTypes.UNKNOWN_OTHER;
//                            }
//                        } else {
//                            Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//                            return verticalDatumTypes.UNKNOWN_OTHER;
//                        }
//                    } else {
//                        Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//                        return verticalDatumTypes.UNKNOWN_OTHER;
//                    }
//                }
//            }
//        }
//        Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//        return verticalDatumTypes.UNKNOWN_OTHER;
//    }
//
//    public verticalDatumTypes datumValueFromKey(int keyId, int value) {
//        switch (keyId) {
//            case VERTICAL_CS_TYPE_GEO_KEY:
//                switch (value) {
//                    case 5001:
//                        return verticalDatumTypes.EGM96;
//                    case 5002:
//                        Log.e(TAG, "Encountered an incompatible vertical datum: NavD88");
//                        return verticalDatumTypes.NAVD88; // NavD88 Height
//                    case 5003:
//                        return verticalDatumTypes.WGS84;
//                    // TODO Add more cases as needed.
//                    default:
//                        Log.e(TAG, "vertical datum type was missing or not recognized");
//                        return verticalDatumTypes.UNKNOWN_OTHER;
//                }
//            case VERTICAL_DATUM_GEO_KEY:
//                switch (value) {
//                    case 1027:
//                        return verticalDatumTypes.EGM96;
//                    case 5103:
//                        return verticalDatumTypes.WGS84;
//                    // Add more cases as needed.
//                    default:
//                        Log.e(TAG, "vertical datum type was missing or not recognized");
//                        return verticalDatumTypes.UNKNOWN_OTHER;
//                }
//            default:
//                Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//                return verticalDatumTypes.UNKNOWN_OTHER;
//        }
//    }
//
//    public verticalUnitTypes getVerticalUnit(FileDirectory directory, ArrayList<Integer> geoKeys) {
//        int numberOfKeys = geoKeys.get(3);
//        for (int i = 0; i < numberOfKeys; i++) {
//            int index = 4 + i * 4;
//            int keyId = geoKeys.get(index);
//            int tiffTagLocation = geoKeys.get(index + 1);
//            int count = geoKeys.get(index + 2);
//            int valueOffset = geoKeys.get(index + 3);
//            Log.d(TAG, "KeyID: " + keyId + ", TIFF Tag Location: " + tiffTagLocation + ", Count: " + count + ", Value Offset: " + valueOffset);
//
//            if (keyId == VERTICAL_UNITS_GEO_KEY) {
//                if (tiffTagLocation == 0) {
//                    // The valueOffset is the value of the key.
//                    int value = valueOffset;
//                    return unitTypeFromValue(value);
//                    //System.out.println("KeyID: " + keyId + ", Value: " + value);
//                } else {
//                    // The valueOffset is an offset into the tag specified by tiffTagLocation.
//                    // Read the value from the specified tag.
//                    FileDirectoryEntry tagEntry = directory.get(FieldTagType.getById(tiffTagLocation));
//                    if (tagEntry != null) {
//                        Object tagValues = tagEntry.getValues();
//                        if (tagValues instanceof short[]) {
//                            short[] tagArray = (short[]) tagValues;
//                            int byteIndex = valueOffset / 2; // Convert byte offset to index in uint16 array
//                            if (byteIndex < tagArray.length) {
//                                short tagValue = tagArray[byteIndex];
//                                return unitTypeFromValue(tagValue);
//                                // System.out.println("KeyID: " + keyId + ", Value: " + value);
//                            } else {
//                                Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//                                return verticalUnitTypes.UNKNOWN_OTHER;
//                            }
//                        } else {
//                            Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//                            return verticalUnitTypes.UNKNOWN_OTHER;
//                        }
//                    } else {
//                        Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//                        return verticalUnitTypes.UNKNOWN_OTHER;
//                    }
//                }
//            }
//        }
//        Log.e(TAG, "Encountered error while determining GeoTIFF vertical datum");
//        return verticalUnitTypes.UNKNOWN_OTHER;
//    }
//
//    public verticalUnitTypes unitTypeFromValue(int value) {
//        switch (value) {
//            case 9001:
//                return verticalUnitTypes.METERS;
//            case 9002:
//                return verticalUnitTypes.FEET;
//            case 9003:
//                return verticalUnitTypes.US_SURVEY_FEET;
//            // TODO Add more cases as needed.
//            default:
//                return verticalUnitTypes.UNKNOWN_OTHER;
//        }
//    }

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
    public double getAltFromLatLon(double lat, double lon) throws RequestedValueOOBException, CorruptTerrainException{
        if (this.isDTED) {
            Point point = new Point(lat, lon);
            try {
                double EGM96_altitude = this.dted.getElevation(point).getElevation();
                // DTED vertical datum is height above EGM96 geoid, we must convert it to height above WGS84 ellipsoid
                double WGS84_altitude = EGM96_altitude - offsetProvider.getEGM96OffsetAtLatLon(lat,lon);
                return WGS84_altitude;
            } catch (CorruptTerrainException e) {
                throw new CorruptTerrainException("The terrain data in the DTED file is corrupt.", e);
            } catch (InvalidValueException e) {
                throw new RequestedValueOOBException("getAltFromLatLon arguments out of bounds!", lat, lon);
            }
        }

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

        // Calculate indices algebraically
        long xL = (long) Math.floor((lon - x0) / dx);
        long xR = xL + 1;
        long yT = (long) Math.floor((lat - y0) / dy);
        long yB = yT + 1;

        if (lon == x0 || lon == x1) {
            xR = xL;
        }
        if (lat == y0 || lat == y1) {
            yB = yT;
        }

        double altitude;

        // on DEM edge check, if so just return nearest elevation
        if (lat == getMaxLat() || lat == getMinLat() || lon == getMaxLon() || lon == getMinLon()) {
            long xIndex;
            long yIndex;
            if (Math.abs(lon - (x0 + xL * dx)) < Math.abs(lon - (x0 + xR * dx))) {
                xIndex = xL;
            } else {
                xIndex = xR;
            }
            if (Math.abs(lat - (y0 + yT * dy)) < Math.abs(lat - (y0 + yB * dy))) {
                yIndex = yT;
            } else {
                yIndex = yB;
            }

            altitude = rasters.getPixel((int) xIndex, (int) yIndex)[0].doubleValue();

            if (verticalDatum == verticalDatumTypes.EGM96) {
                // convert from EGM96 AMSL orthometric height to WGS84 height above ellipsoid hae (if necessary)
                altitude = altitude - offsetProvider.getEGM96OffsetAtLatLon(lat,lon);
            }
            return altitude;
        } // end edge case handling

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
        double power = 1.875d;

        // Inverse Distance Weighting interpolation using 4 neighbors
        // see: https://doi.org/10.3846/gac.2023.16591
        //      https://pro.arcgis.com/en/pro-app/latest/help/analysis/geostatistical-analyst/how-inverse-distance-weighted-interpolation-works.htm
        altitude =  idwInterpolation(target, neighbors, power);

        if (verticalDatum == verticalDatumTypes.EGM96) {
            // convert from EGM96 AMSL orthometric height to WGS84 height above ellipsoid hae (if necessary)
            altitude = altitude - offsetProvider.getEGM96OffsetAtLatLon(lat,lon);
        }

        return altitude;
    }
}
