package com.openathena;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import android.util.Log;

// Libraries from the U.S. National Geospatial Intelligence Agency https://www.nga.mil
// KNOWN BUG: does not handle special MGRS polar regions correctly (for North pole or South pole):
// https://github.com/ngageoint/mgrs-java/issues/2
import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.*;
import mil.nga.grid.features.Point;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CoordTranslator {
    CoordTranslator() {
        super();
    }

    private static final String TAG = "CoordTranslator";

    public static String toLatLonDMS(double latitude, double longitude) {
        // Determine the hemisphere for latitude
        String latDirection = latitude >= 0 ? "N" : "S";
        // Determine the hemisphere for longitude
        String lonDirection = longitude >= 0 ? "E" : "W";

        // Convert latitude to absolute value for calculation
        double absLat = Math.abs(latitude);
        // Extract degrees
        int latDegrees = (int) absLat;
        // Calculate the total minutes
        double latMinutesTotal = (absLat - latDegrees) * 60;
        // Extract minutes
        int latMinutes = (int) latMinutesTotal;
        // Calculate seconds
        double latSeconds = (latMinutesTotal - latMinutes) * 60;
        // Round seconds to one decimal place
        latSeconds = Double.parseDouble(roundToOneDecimalPlace(latSeconds));

        // Handle rounding that causes seconds to be 60.0
        if (latSeconds >= 60.0) {
            latSeconds = 0.0;
            latMinutes += 1;
            // Handle case where minutes become 60
            if (latMinutes >= 60) {
                latMinutes = 0;
                latDegrees += 1;
            }
        }

        // Convert longitude to absolute value for calculation
        double absLon = Math.abs(longitude);
        // Extract degrees
        int lonDegrees = (int) absLon;
        // Calculate the total minutes
        double lonMinutesTotal = (absLon - lonDegrees) * 60;
        // Extract minutes
        int lonMinutes = (int) lonMinutesTotal;
        // Calculate seconds
        double lonSeconds = (lonMinutesTotal - lonMinutes) * 60;
        // Round seconds to one decimal place
        lonSeconds = Double.parseDouble(roundToOneDecimalPlace(lonSeconds));

        // Handle rounding that causes seconds to be 60.0
        if (lonSeconds >= 60.0) {
            lonSeconds = 0.0;
            lonMinutes += 1;
            // Handle case where minutes become 60
            if (lonMinutes >= 60) {
                lonMinutes = 0;
                lonDegrees += 1;
            }
        }

        // Format the DMS string
        return String.format(Locale.ENGLISH, "%d°%d'%.1f\" %s, %d°%d'%.1f\" %s",
                latDegrees, latMinutes, latSeconds, latDirection,
                lonDegrees, lonMinutes, lonSeconds, lonDirection);
    }

    public static String toUTM(double latitude, double longitude) {
        // mil.nga.mgrs library can also handle UTM out of the box
        // https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system
        MGRS mgrsObj = MGRS.from(new Point(longitude, latitude));
        String utmText = mgrsObj.toUTM().toString();
        return utmText;
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
//        Log.d(TAG, "grid zone: " + mgrsObj.getGridZone().getName());
//        Log.d(TAG, "square identifier: " + mgrsObj.getColumn() + mgrsObj.getRow());
//        Log.d(TAG, "easting and northing: " + mgrsObj.getEastingAndNorthing(gridType));
        String mgrsText = mgrsObj.coordinate(gridType);
        return mgrsText;
    }

    public static double[] fromMGRS(String mgrs_in) throws java.text.ParseException {
        MGRS mgrsObj = MGRS.parse(mgrs_in);
        Point p = mgrsObj.toPoint();
        return new double[] {p.getLatitude(), p.getLongitude()};
    }

    public static String toSelectedOutputMode(double latitude, double longitude, AthenaActivity.outputModes outputMode) {
        switch (outputMode) {
            case WGS84:
                return roundDouble(latitude) + "," + " " + roundDouble(longitude);
            case WGS84_DMS:
                return toLatLonDMS(latitude, longitude);
            case UTM:
                return toUTM(latitude,longitude);
            case USNG:
                // US National Grid is functionally equivalent to MGRS (unless using NAD 27 Datum)
                // https://www.maptools.com/tutorials/mgrs_usng_diffs
                // http://gpsinformation.info/USNG/USNG.html
                // https://web.archive.org/web/20230601064856/https://www.dco.uscg.mil/Portals/9/CG-5R/nsarc/Land_SAR_Addendum/Published_Land%20SAR%20Addendum%20(1118111)%20-%20Bookmark.pdf#page=160
                return toMGRS10m_Space_Separated(latitude, longitude);
            case MGRS1m:
                return toMGRS1m_Space_Separated(latitude, longitude);
            case MGRS10m:
                return toMGRS10m_Space_Separated(latitude, longitude);
            case MGRS100m:
                return toMGRS100m_Space_Separated(latitude, longitude);
            case CK42Geodetic:
                // WARNING: The following output will be in CK-42 lat/lon, which is easy to confuse with regular WGS84 coordinates
                // CK-42 coordinates must always be labeled as such in the consuming UI (to avoid confusion)
                //
                // This conversion introduces some inaccuracy due to lack of ellipsoidal height
                // therefore, result should only be used for unimportant uses such as for dem bounds UI
                return roundDouble(toCK42Lat(latitude,longitude,0.0d)) + "," + " " + roundDouble(toCK42Lon(latitude,longitude,0.0d));
            case CK42GaussKrüger:
                // This conversion introduces some inaccuracy due to lack of ellipsoidal height
                // therefore, result should only be used for unimportant uses such as for dem bounds UI
                return toCK42_GK_String(latitude, longitude, 0.0d);
            default:
                // This should never happen
                String errString = "ERROR: Attempted to call toSelectedOutputMode on invalid outputMode: " + outputMode.name();
                Log.e(TAG, errString);
                throw new IllegalArgumentException(errString);
        }
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

    public static String makeGKHumanReadable(long GK) {
        String human_readable;
        if (GK >= 10000000) {
            human_readable = Long.toString(GK);
        } else { // If value is not at least 5 digits, pad with leading zeros
            human_readable = Long.toString(GK + 10000000);
            human_readable = human_readable.substring(1);
        }
        human_readable = human_readable.substring(0, human_readable.length() - 5) + "-" + human_readable.substring(human_readable.length() - 5);
        return human_readable;
    }

    public static String toCK42_GK_String(double latitude, double longitude, double WGS84_altitude_meters) {
        double GK_lat = toCK42Lat(latitude,longitude,WGS84_altitude_meters);
        double GK_lon = toCK42Lon(latitude,longitude,WGS84_altitude_meters);
        long[] GK_northing_and_easting = fromCK42toCK42_GK(GK_lat,GK_lon);
        long GK_northing = GK_northing_and_easting[0];
        long GK_easting = GK_northing_and_easting[1];

        String northing_string = CoordTranslator.makeGKHumanReadable(GK_northing);
        String easting_string = CoordTranslator.makeGKHumanReadable(GK_easting);
        return northing_string + " " + easting_string;
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
    protected static double[] parseLatLon(String latLonStr) throws ParseException {
        if (latLonStr == null || latLonStr.trim().isEmpty()) {
            throw new java.text.ParseException("ERROR: input string was null or empty!", 0);
        }

        // Sanitize input
        latLonStr = latLonStr.trim().toUpperCase(Locale.ENGLISH).replaceAll("[()]", "");
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
        dms = dms.toUpperCase(Locale.ENGLISH);
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

    private static String roundDouble(double d) {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.######", decimalSymbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(d);
    }

    private static String roundToOneDecimalPlace(double d) {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.#", decimalSymbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(d);
    }
}
