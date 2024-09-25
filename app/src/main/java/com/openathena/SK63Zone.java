package com.openathena;

class SK63Zone {
    String id;
    String name;
    double lat0;     // Origin latitude (degrees)
    double lon0;     // Central meridian (degrees)
    double k0;       // Scale factor
    double x0;       // False easting (meters)
    double y0;       // False northing (meters)
    double zoneWidth; // Zone width (degrees)

    public SK63Zone(String id, String name, double lat0, double lon0, double k0, double x0, double y0, double zoneWidth) {
        this.id = id;
        this.name = name;
        this.lat0 = lat0;
        this.lon0 = lon0;
        this.k0 = k0;
        this.x0 = x0;
        this.y0 = y0;
        this.zoneWidth = zoneWidth;
    }
}