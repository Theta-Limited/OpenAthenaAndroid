package com.openathena;

/**
 * Class to handle the conversion from SK-42 geodetic coordinates to SK-63 easting and northing.
 */
class SK42_To_SK63_Translator {

    // Ellipsoid parameters for the Krasovsky ellipsoid
    static final double a = WGS84_CK42_Geodetic_Translator.aP; // 6378245
    static final double b = 6356863.019; // Minor (polar) semi-axis
    static final double e2 = (a * a - b * b) / (a * a); // Square of eccentricity
    static final double n = (a - b) / (a + b);

    /**
     * Converts SK-42 geodetic coordinates to SK-63 easting and northing.
     *
     * @param latDegrees Latitude in degrees (SK-42)
     * @param lonDegrees Longitude in degrees (SK-42)
     * @param zone       SK63Zone object containing projection parameters
     * @return An array containing [Easting, Northing]
     */
    public static double[] SK42_Geodetic_to_SK63(double latDegrees, double lonDegrees, SK63Zone zone) {

        // Convert degrees to radians
        double Lat = Math.toRadians(latDegrees);
        double Lon = Math.toRadians(lonDegrees);

        // Projection parameters from the zone
        double F = zone.k0;
        double Lat0 = Math.toRadians(zone.lat0);
        double Lon0 = Math.toRadians(zone.lon0);
        double N0 = zone.y0;
        double E0 = zone.x0;

        // Calculating variables for conversion
        double sinLat = Math.sin(Lat);
        double cosLat = Math.cos(Lat);
        double tanLat = Math.tan(Lat);

        double v = a * F / Math.sqrt(1 - e2 * sinLat * sinLat);
        double p = a * F * (1 - e2) / Math.pow(1 - e2 * sinLat * sinLat, 1.5);
        double n2 = v / p - 1;

        double M1 = (1 + n + (5.0 / 4.0) * n * n + (5.0 / 4.0) * n * n * n) * (Lat - Lat0);
        double M2 = (3 * n + 3 * n * n + (21.0 / 8.0) * n * n * n) * Math.sin(Lat - Lat0) * Math.cos(Lat + Lat0);
        double M3 = ((15.0 / 8.0) * n * n + (15.0 / 8.0) * n * n * n) * Math.sin(2 * (Lat - Lat0)) * Math.cos(2 * (Lat + Lat0));
        double M4 = (35.0 / 24.0) * n * n * n * Math.sin(3 * (Lat - Lat0)) * Math.cos(3 * (Lat + Lat0));
        double M = b * F * (M1 - M2 + M3 - M4);

        double I = M + N0;
        double II = v / 2 * sinLat * cosLat;
        double III = v / 24 * sinLat * Math.pow(cosLat, 3) * (5 - tanLat * tanLat + 9 * n2);
        double IIIA = v / 720 * sinLat * Math.pow(cosLat, 5) * (61 - 58 * tanLat * tanLat + Math.pow(tanLat, 4));
        double IV = v * cosLat;
        double V = v / 6 * Math.pow(cosLat, 3) * (v / p - tanLat * tanLat);
        double VI = v / 120 * Math.pow(cosLat, 5) * (5 - 18 * tanLat * tanLat + Math.pow(tanLat, 4) + 14 * n2 - 58 * tanLat * tanLat * n2);

        double deltaLon = Lon - Lon0;

        // Compute northing and easting
        double N = I + II * deltaLon * deltaLon + III * Math.pow(deltaLon, 4) + IIIA * Math.pow(deltaLon, 6);
        double E = E0 + IV * deltaLon + V * Math.pow(deltaLon, 3) + VI * Math.pow(deltaLon, 5);

        // Adjust easting with false easting and zone number
        double adjustedEasting = zone.zoneNumber * 1_000_000 + E0 + E;

        return new double[]{E, N}; // Return easting and northing
    }
}
