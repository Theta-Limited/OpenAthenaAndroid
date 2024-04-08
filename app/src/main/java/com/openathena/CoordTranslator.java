package com.openathena;

// Libraries from the U.S. National Geospatial Intelligence Agency https://www.nga.mil

import java.text.ParseException;

// KNOWN BUG: does not handle special MGRS polar regions correctly (for North pole or South pole):
// https://github.com/ngageoint/mgrs-java/issues/2
import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.*;
import mil.nga.grid.features.Point;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CoordTranslator {
    CoordTranslator() {
        super();
    }

    public static String toMGRS1m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.METER);
    }

    public static String toMGRS10m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.TEN_METER);
    }

    public static String toMGRS100m(double latitude, double longitude) {
        return toMGRS(latitude, longitude, GridType.HUNDRED_METER);
    }

    public static String toMGRS(double latitude, double longitude, GridType gridType) {
        MGRS mgrsObj = MGRS.from(new Point(longitude, latitude));
        String mgrsText = mgrsObj.coordinate(gridType);
        return mgrsText;
    }

    public static double[] fromMGRS(String mgrs_in) throws java.text.ParseException {
        MGRS mgrsObj = MGRS.parse(mgrs_in);
        Point p = mgrsObj.toPoint();
        return new double[] {p.getLatitude(), p.getLongitude()};
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

    // Method to handle coordinate String parsing
    // accepts either lat,lon (in either DMS or decimal) or MGRS
    // returns a double[] containing WGS84 lat,lon
    public static double[] parseCoordinates(String coordinates) throws ParseException {
        double[] latLonPair;
        try {
            latLonPair = parseLatLon(coordinates);
        } catch (java.text.ParseException pe) {
            latLonPair = fromMGRS(coordinates); // may throw ParseException
        }
        return latLonPair;
    }

    // Method to handle Lat/Lon String parsing
    public static double[] parseLatLon(String latLonStr) throws ParseException {
        if (latLonStr == null || latLonStr.isEmpty()) {
            throw new java.text.ParseException("ERROR: input string was null or empty!", 0);
        }

        // Sanitize input
        latLonStr = latLonStr.trim().toUpperCase().replaceAll("[()]", "");
        latLonStr = latLonStr.replaceAll("[Dd]egrees","°");
        latLonStr = latLonStr.replaceAll("[Dd]eg","°");

        // Tokenize input using space and comma as separators
        String[] tokens = latLonStr.split("[ ,]+");
        if (tokens.length < 2) {
            throw new ParseException("Input does not contain enough data for latitude and longitude: " + latLonStr, 0);
        }

        // Try to find a valid partition that separates latitude from longitude
        for (int i = 1; i < tokens.length; i++) {
            String latPart = String.join(" ", Arrays.copyOfRange(tokens, 0, i));
            String lonPart = String.join(" ", Arrays.copyOfRange(tokens, i, tokens.length));

            if (isValidLatOrLon(latPart) && isValidLatOrLon(lonPart)) {
                double lat = dmsToDecimal(latPart);
                double lon = dmsToDecimal(lonPart);
                return new double[]{lat, lon};
            }
        }

        // If no valid partition can be found, parsing has lat/lon failed
        throw new ParseException("Unable to parse valid latitude and longitude from input: " + latLonStr, 0);
    }

    // Regex expression for almost any valid DMS lat or lon String
    // breakdown:
    // ^ and $ are start and end of string anchors, ensuring the whole string matches the pattern.
    // ([-+]?\d{1,3}(?:\.\d+)?) matches the degrees part, which can be an integer or decimal number, optionally prefixed with + or - signs. It allows for 1 to 3 digits before the decimal point.
    // [°\s]? allows for an optional degree symbol or space after the degrees part.
    // (\d{1,2}(?:\.\d+)?['\s]?)? is for the minutes part, matching 1 to 2 digits followed by an optional decimal, with an optional minute symbol (') or space. This entire group is optional to allow for inputs without minutes.
    // (\d{1,2}(?:\.\d+)?["\s]?)?\s* captures the seconds in a similar manner to minutes, also optional, followed by any amount of whitespace.
    // ([NSEW])? matches an optional directional letter at the end.
    private static final String DMS_REGEX = "^([-+]?\\d{1,3}(?:\\.\\d+)?)\\s*[°\\s]?\\s*(\\d{1,2}(?:\\.\\d+)?\\s*['′\\s]?)?\\s*(\\d{1,2}(?:\\.\\d+)?\\s*[\"″\\s]?)?\\s*([NSEW])?$";

    // Helper method to convert degrees, minutes, seconds values to decimal
    public static double dmsToDecimal(String dms) throws ParseException {
        dms = dms.trim();
        dms = dms.toUpperCase();
        dms = dms.replaceAll("[()]","");
        dms = dms.replaceAll("[Dd]egrees","°");
        dms = dms.replaceAll("[Dd]eg","°");

        // Updated regex pattern to correctly match DMS components
        Pattern pattern = Pattern.compile(DMS_REGEX);
        Matcher matcher = pattern.matcher(dms);

        if (!matcher.find()) {
            throw new java.text.ParseException("Malformed DMS input: " + dms, dms.length() - 1);
        }

        double degrees = matcher.group(1) != null ? Double.parseDouble(matcher.group(1).replaceAll("[^\\d.-]","")) : 0;
        double minutes = matcher.group(2) != null ? Double.parseDouble(matcher.group(2).replaceAll("[^\\d.]", "")) : 0;
        double seconds = matcher.group(3) != null ? Double.parseDouble(matcher.group(3).replaceAll("[^\\d.]", "")) : 0;
        String direction = matcher.group(4);

        if (Math.abs(degrees) < 0 || Math.abs(degrees) > 180) {
            throw new ParseException("Degrees out of range: " + dms, 0);
        }
        if (minutes < 0 || minutes >= 60 || seconds < 0 || seconds >= 60) {
            throw new ParseException("Minutes or seconds out of range: " + dms, 0);
        }

        minutes /= 60;
        seconds /= 3600;
        if (degrees < 0.0) {
            minutes *= -1;
            seconds *= -1;
        }

        double decimalDegrees = degrees + minutes + seconds;

        if ("W".equals(direction) || "S".equals(direction)) {
            if (decimalDegrees > 0.0) {
                decimalDegrees *= -1;
            }
        }

        if (Math.abs(decimalDegrees) > 180.0) {
            throw new ParseException("Lat or Lon out of range: " + dms, 0);
        }

        if (("N".equals(direction) || "S".equals(direction)) && Math.abs(decimalDegrees) > 90.0) {
            throw new ParseException("Lat out of range: " + dms, 0);
        }

        return decimalDegrees;
    }

    public static boolean isValidLatOrLon(String dms) {
        try {
            double foo = dmsToDecimal(dms);
        } catch (ParseException ignored) {
            return false;
        }
        return true;
    }

}
