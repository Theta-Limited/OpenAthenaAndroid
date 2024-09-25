package com.openathena;

import java.util.ArrayList;
import java.util.List;

public class WGS84_To_SK63_Translator {

    // List of SK63 zones
    private static List<SK63Zone> sk63Zones = new ArrayList<>();

    static {
        // Initialize the SK63 zones
        sk63Zones.add(new SK63Zone("SK63C3_1", "СК-63 Район С (3°), Зона 1", 0.0, 24.95, 1.0, 1250000.0, -11057.626999999397, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63C3_2", "СК-63 Район С (3°), Зона 2", 0.0, 27.95, 1.0, 2250000.0, -11057.626999999397, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63C3_3", "СК-63 Район С (3°), Зона 3", 0.0, 30.95, 1.0, 3250000.0, -11057.626999999397, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63C3_4", "СК-63 Район С (3°), Зона 4", 0.0, 33.95, 1.0, 4250000.0, -11057.626999999397, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63C3_5", "СК-63 Район С (3°), Зона 5", 0.0, 36.95, 1.0, 5250000.0, -11057.626999999397, 3.0, 5));
        sk63Zones.add(new SK63Zone("SK63C3_6", "СК-63 Район С (3°), Зона 6", 0.0, 39.95, 1.0, 6250000.0, -11057.626999999397, 3.0, 6));
        sk63Zones.add(new SK63Zone("SK63D3_1", "СК-63 Район D (3°), Зона 1", 0.0, 38.55, 1.0, 1250000.0, -14743.504, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63D3_2", "СК-63 Район D (3°), Зона 2", 0.0, 41.55, 1.0, 2250000.0, -14743.504, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63D3_3", "СК-63 Район D (3°), Зона 3", 0.0, 44.55, 1.0, 3250000.0, -14743.504, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63D3_4", "СК-63 Район D (3°), Зона 4", 0.0, 47.55, 1.0, 4250000.0, -14743.504, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63D3_5", "СК-63 Район D (3°), Зона 5", 0.0, 50.55, 1.0, 5250000.0, -14743.504, 3.0, 5));
        sk63Zones.add(new SK63Zone("SK63D3_6", "СК-63 Район D (3°), Зона 6", 0.0, 53.55, 1.0, 6250000.0, -14743.504, 3.0, 6));
        sk63Zones.add(new SK63Zone("SK63D3_7", "СК-63 Район D (3°), Зона 7", 0.0, 56.55, 1.0, 7250000.0, -14743.504, 3.0, 7));
        sk63Zones.add(new SK63Zone("SK63D3_8", "СК-63 Район D (3°), Зона 8", 0.0, 59.55, 1.0, 8250000.0, -14743.504, 3.0, 8));
        sk63Zones.add(new SK63Zone("SK63E3_1", "СК-63 Район E (3°), Зона 1", 0.0, 77.783333333333, 1.0, 1300000.0, -14743.504, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63E3_2", "СК-63 Район E (3°), Зона 2", 0.0, 80.783333333333, 1.0, 2300000.0, -14743.504, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63E3_3", "СК-63 Район E (3°), Зона 3", 0.0, 83.783333333333, 1.0, 3300000.0, -14743.504, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63E3_4", "СК-63 Район E (3°), Зона 4", 0.0, 86.783333333333, 1.0, 4300000.0, -14743.504, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63E3_5", "СК-63 Район E (3°), Зона 5", 0.0, 89.783333333333, 1.0, 5300000.0, -14743.504, 3.0, 5));
        sk63Zones.add(new SK63Zone("SK63F3_1", "СК-63 Район F (3°), Зона 1", 0.0, 97.033333333333, 1.0, 1250000.0, -11057.628, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63F3_2", "СК-63 Район F (3°), Зона 2", 0.0, 100.033333333333, 1.0, 2250000.0, -11057.628, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63F3_3", "СК-63 Район F (3°), Зона 3", 0.0, 103.033333333333, 1.0, 3250000.0, -11057.628, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63G3_1", "СК-63 Район G (3°), Зона 1", 0.0, 121.71666666667, 1.0, 1300000.0, -16586.442, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63G3_2", "СК-63 Район G (3°), Зона 2", 0.0, 124.71666666667, 1.0, 2300000.0, -16586.442, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63G3_3", "СК-63 Район G (3°), Зона 3", 0.0, 127.71666666667, 1.0, 3300000.0, -16586.442, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63G3_4", "СК-63 Район G (3°), Зона 4", 0.0, 130.71666666667, 1.0, 4300000.0, -16586.442, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63G3_5", "СК-63 Район G (3°), Зона 5", 0.0, 133.71666666667, 1.0, 5300000.0, -16586.442, 3.0, 5));
        sk63Zones.add(new SK63Zone("SK63G3_6", "СК-63 Район G (3°), Зона 6", 0.0, 136.71666666667, 1.0, 6300000.0, -16586.442, 3.0, 6));
        sk63Zones.add(new SK63Zone("SK63G3_7", "СК-63 Район G (3°), Зона 7", 0.0, 139.71666666667, 1.0, 7300000.0, -16586.442, 3.0, 7));
        sk63Zones.add(new SK63Zone("SK63G3_8", "СК-63 Район G (3°), Зона 8", 0.0, 142.71666666667, 1.0, 8300000.0, -16586.442, 3.0, 8));
        sk63Zones.add(new SK63Zone("SK63G3_9", "СК-63 Район G (3°), Зона 9", 0.0, 145.71666666667, 1.0, 9300000.0, -16586.442, 3.0, 9));
        sk63Zones.add(new SK63Zone("SK63I3_1", "СК-63 Район I (3°), Зона 1", 0.0, 71.73333333333, 1.0, 1250000.0, -12900.566, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63I3_2", "СК-63 Район I (3°), Зона 2", 0.0, 74.73333333333, 1.0, 2250000.0, -12900.566, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63I3_3", "СК-63 Район I (3°), Зона 3", 0.0, 77.73333333333, 1.0, 3250000.0, -12900.566, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63I3_4", "СК-63 Район I (3°), Зона 4", 0.0, 80.73333333333, 1.0, 4250000.0, -12900.566, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63M3_1", "СК-63 Район M (3°), Зона 1", 0.0, 79.466666666677, 1.0, 1300000.0, -12900.566, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63M3_2", "СК-63 Район M (3°), Зона 2", 0.0, 82.466666666677, 1.0, 2300000.0, -12900.566, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63M3_3", "СК-63 Район M (3°), Зона 3", 0.0, 85.466666666677, 1.0, 3300000.0, -12900.566, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63M3_4", "СК-63 Район M (3°), Зона 4", 0.0, 88.466666666677, 1.0, 4300000.0, -12900.566, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63P3_1", "СК-63 Район P (3°), Зона 1", 0.0, 32.48333333333, 1.0, 1250000.0, -12900.566, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63P3_2", "СК-63 Район P (3°), Зона 2", 0.0, 35.48333333333, 1.0, 2250000.0, -12900.566, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63P3_3", "СК-63 Район P (3°), Зона 3", 0.0, 38.48333333333, 1.0, 3250000.0, -12900.566, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63P3_4", "СК-63 Район P (3°), Зона 4", 0.0, 41.48333333333, 1.0, 4250000.0, -12900.566, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63R3_1", "СК-63 Район R (3°), Зона 1", 0.0, 43.05, 1.0, 1300000.0, -14743.501, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63R3_2", "СК-63 Район R (3°), Зона 2", 0.0, 46.05, 1.0, 2300000.0, -14743.501, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63R3_3", "СК-63 Район R (3°), Зона 3", 0.0, 49.05, 1.0, 3300000.0, -14743.501, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63T3_1", "СК-63 Район T (3°), Зона 1", 0.0, 37.98333333333, 1.0, 1300000.0, -11057.628, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63T3_2", "СК-63 Район T (3°), Зона 2", 0.0, 40.98333333333, 1.0, 2300000.0, -11057.628, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63T3_3", "СК-63 Район T (3°), Зона 3", 0.0, 43.98333333333, 1.0, 3300000.0, -11057.628, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63T3_4", "СК-63 Район T (3°), Зона 4", 0.0, 46.98333333333, 1.0, 4300000.0, -11057.628, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63V3_1", "СК-63 Район V (3°), Зона 1", 0.0, 49.03333333333, 1.0, 1300000.0, -9414.7, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63V3_2", "СК-63 Район V (3°), Зона 2", 0.0, 52.03333333333, 1.0, 2300000.0, -9414.7, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63V3_3", "СК-63 Район V (3°), Зона 3", 0.0, 55.03333333333, 1.0, 3300000.0, -9414.7, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63V3_4", "СК-63 Район V (3°), Зона 4", 0.0, 58.03333333333, 1.0, 4300000.0, -9414.7, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63V3_5", "СК-63 Район V (3°), Зона 5", 0.0, 61.03333333333, 1.0, 5300000.0, -9414.7, 3.0, 5));
        sk63Zones.add(new SK63Zone("SK63V3_6", "СК-63 Район V (3°), Зона 6", 0.0, 64.03333333333, 1.0, 6300000.0, -9414.7, 3.0, 6));
        sk63Zones.add(new SK63Zone("SK63W3_1", "СК-63 Район W (3°), Зона 1", 0.0, 60.05, 1.0, 1500000.0, -11057.628, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63W3_2", "СК-63 Район W (3°), Зона 2", 0.0, 63.05, 1.0, 2500000.0, -11057.628, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63W3_3", "СК-63 Район W (3°), Зона 3", 0.0, 66.05, 1.0, 3500000.0, -11057.628, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63W3_4", "СК-63 Район W (3°), Зона 4", 0.0, 69.05, 1.0, 4500000.0, -11057.628, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63W3_5", "СК-63 Район W (3°), Зона 5", 0.0, 72.05, 1.0, 5500000.0, -11057.628, 3.0, 5));
        sk63Zones.add(new SK63Zone("SK63W3_6", "СК-63 Район W (3°), Зона 6", 0.0, 75.05, 1.0, 6500000.0, -11057.628, 3.0, 6));
        sk63Zones.add(new SK63Zone("SK63W3_7", "СК-63 Район W (3°), Зона 7", 0.0, 78.05, 1.0, 7500000.0, -11057.628, 3.0, 7));
        sk63Zones.add(new SK63Zone("SK63W3_8", "СК-63 Район W (3°), Зона 8", 0.0, 81.05, 1.0, 8500000.0, -11057.628, 3.0, 8));
        sk63Zones.add(new SK63Zone("SK63X3_1", "СК-63 Район X (3°), Зона 1", 0.0, 23.5, 1.0, 1300000.0, -9214.69, 3.0, 1));
        sk63Zones.add(new SK63Zone("SK63X3_2", "СК-63 Район X (3°), Зона 2", 0.0, 26.5, 1.0, 2300000.0, -9214.69, 3.0, 2));
        sk63Zones.add(new SK63Zone("SK63X3_3", "СК-63 Район X (3°), Зона 3", 0.0, 29.5, 1.0, 3300000.0, -9214.69, 3.0, 3));
        sk63Zones.add(new SK63Zone("SK63X3_4", "СК-63 Район X (3°), Зона 4", 0.0, 32.5, 1.0, 4300000.0, -9214.69, 3.0, 4));
        sk63Zones.add(new SK63Zone("SK63X3_5", "СК-63 Район X (3°), Зона 5", 0.0, 35.5, 1.0, 5300000.0, -9214.69, 3.0, 5));
        sk63Zones.add(new SK63Zone("SK63X3_6", "СК-63 Район X (3°), Зона 6", 0.0, 38.5, 1.0, 6300000.0, -9214.69, 3.0, 6));
    }

    /**
     * Converts WGS84 latitude and longitude to SK-63 easting and northing coordinates.
     *
     * @param latWGS84 Latitude in WGS84 (degrees)
     * @param lonWGS84 Longitude in WGS84 (degrees)
     * @param height   Height in meters (can be zero if unknown)
     * @return An array containing [Easting, Northing, Zone ID]
     */
    public static Object[] WGS84_to_SK63(double latWGS84, double lonWGS84, double height) {
        // Step 1: Convert WGS84 geodetic coordinates to SK-42 geodetic coordinates
        double Bd = WGS84_CK42_Geodetic_Translator.WGS84_CK42_Lat(latWGS84, lonWGS84, height);
        double Ld = WGS84_CK42_Geodetic_Translator.WGS84_CK42_Long(latWGS84, lonWGS84, height);

        // Step 2: Find the appropriate SK-63 zone based on longitude
        SK63Zone zone = findZone(Ld);
        if (zone == null) {
            throw new IllegalArgumentException("No SK-63 zone found for longitude: " + Ld);
        }

        // Step 3: Convert SK-42 geodetic coordinates to SK-63 easting and northing
        double[] EN = SK42_To_SK63_Translator.SK42_Geodetic_to_SK63(Bd, Ld, zone);

        // Return the easting, northing, and zone ID
        return new Object[]{EN[0], EN[1], zone.id};
    }

    /**
     * Finds the SK63Zone based on the given longitude.
     *
     * @param lonDegrees Longitude in degrees (SK-42)
     * @return The corresponding SK63Zone object
     */
    public static SK63Zone findZone(double lonDegrees) {
        for (SK63Zone zone : sk63Zones) {
            double halfZoneWidth = zone.zoneWidth / 2.0;
            if (lonDegrees >= (zone.lon0 - halfZoneWidth) && lonDegrees <= (zone.lon0 + halfZoneWidth)) {
                return zone;
            }
        }
        return null; // No zone found
    }

//    /**
//     * Main method for testing the conversion.
//     */
//    public static void main(String[] args) {
//        // Example WGS84 coordinates (latitude and longitude in degrees)
//        double latWGS84 = 42.18931944; // 42°11'01.55"
//        double lonWGS84 = 77.56304444; // 77°33'50.76"
//        double height = 0.0; // Height in meters
//
//        Object[] result = WGS84_to_SK63(latWGS84, lonWGS84, height);
//        double easting = (double) result[0];
//        double northing = (double) result[1];
//        String zoneId = (String) result[2];
//
//        System.out.println("Easting: " + easting);
//        System.out.println("Northing: " + northing);
//        System.out.println("Zone ID: " + zoneId);
//    }
}
