package com.openathena;

// Libraries from the U.S. National Geospatial Intelligence Agency https://www.nga.mil
import android.util.Log;

import mil.nga.mgrs.grid.GridType;
import mil.nga.tiff.util.TiffException;
import mil.nga.mgrs.*;
import mil.nga.grid.features.Point;

public class CoordTranslator {
    CoordTranslator() {
        super();
    }

    private static final String TAG = "CoordTranslator";

    public static String toMGRS1m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.METER);
    }

    public static String toMGRS10m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.TEN_METER);
    }

    public static String toMGRS100m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.HUNDRED_METER);
    }

    private static String toMGRS(double latitude, double longitude, GridType gridType) {
        MGRS mgrsObj = MGRS.from(new Point(longitude, latitude));
//        Log.d(TAG, "grid zone: " + mgrsObj.getGridZone().getName());
//        Log.d(TAG, "square identifier: " + mgrsObj.getColumn() + mgrsObj.getRow());
//        Log.d(TAG, "easting and northing: " + mgrsObj.getEastingAndNorthing(gridType));
        String mgrsText = mgrsObj.coordinate(gridType);
        return mgrsText;
    }

    public static String toMGRS1m_Space_Separated(double latitude, double longitude) {
        return toMGRS_Space_Separated(latitude, longitude, GridType.METER);
    }

    public static String toMGRS10m_Space_Separated(double latitude, double longitude) {
        return toMGRS_Space_Separated(latitude, longitude, GridType.TEN_METER);
    }

    public static String toMGRS100m_Space_Separated(double latitude, double longitude) {
        return toMGRS_Space_Separated(latitude, longitude, GridType.HUNDRED_METER);
    }

    private static String toMGRS_Space_Separated(double latitude, double longitude, GridType gridType) {
        MGRS mgrsObj = MGRS.from(new Point(longitude, latitude));
        String gridZone = mgrsObj.getGridZone().getName();
        String squareIdentifier = "" + mgrsObj.getColumn() + mgrsObj.getRow();
        String eastingAndNorthing = mgrsObj.getEastingAndNorthing(gridType);
        String easting = ""; String northing = "";
        switch (gridType) {
            case METER:
                easting = eastingAndNorthing.substring(0,5);
                northing = eastingAndNorthing.substring(5);
//                Log.d(TAG, "square size: METER");
//                Log.d(TAG, "easting: " + easting);
//                Log.d(TAG, "northing: " + northing);
                break;
            case TEN_METER:
                easting = eastingAndNorthing.substring(0,4);
                northing = eastingAndNorthing.substring(4);
//                Log.d(TAG, "square size: TEN_METER");
//                Log.d(TAG, "easting: " + easting);
//                Log.d(TAG, "northing: " + northing);
                break;
            case HUNDRED_METER:
                easting = eastingAndNorthing.substring(0,3);
                northing = eastingAndNorthing.substring(3);
//                Log.d(TAG, "square size: TEN_METER");
//                Log.d(TAG, "easting: " + easting);
//                Log.d(TAG, "northing: " + northing);
                break;
            // default should never happen
            default:
                throw new RuntimeException("gridType was an invalid value!");
        }
        String[] components = new String[] {gridZone, squareIdentifier, easting, northing};
        return String.join(" ", components);
    }

    public static double toCK42Lat(double WGS84_latitude, double WGS84_longitude, double WGS84_altitude_meters) {
       return WGS84_CK42_Geodetic_Translator.WGS84_CK42_Lat(WGS84_latitude, WGS84_longitude, WGS84_altitude_meters);
    }

    public static double toCK42Lon(double WGS84_latitude, double WGS84_longitude, double WGS84_altitude_meters) {
        return WGS84_CK42_Geodetic_Translator.WGS84_CK42_Long(WGS84_latitude, WGS84_longitude, WGS84_altitude_meters);
    }

    public static double fromCK42Alt(double CK42_latitude, double CK42_longitude, double CK42_altitude_meters) {
        return WGS84_CK42_Geodetic_Translator.CK42_WGS84_Alt(CK42_latitude, CK42_longitude, CK42_altitude_meters);
    }

    public static long[] fromCK42toCK42_GK(double CK42_latitude, double CK42_longitude) {
        return CK42_Gauss_Krüger_Translator.CK42_Geodetic_to_Gauss_Krüger(CK42_latitude, CK42_longitude);
    }
}
