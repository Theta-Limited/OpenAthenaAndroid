package com.openathena;

// Libraries from the U.S. National Geospatial Intelligence Agency https://www.nga.mil
import mil.nga.mgrs.grid.GridType;
import mil.nga.tiff.util.TiffException;
import mil.nga.mgrs.*;
import mil.nga.grid.features.Point;

public class CoordTranslator {
    CoordTranslator() {
        super();
    }

    public String toMGRS1m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.METER);
    }

    public String toMGRS10m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.TEN_METER);
    }

    public String toMGRS100m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.HUNDRED_METER);
    }

    private String toMGRS(double latitude, double longitude, GridType gridType) {
        MGRS mgrsObj = MGRS.from(new Point(longitude, latitude));
        String mgrsText = mgrsObj.coordinate(gridType);
        return mgrsText;
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
