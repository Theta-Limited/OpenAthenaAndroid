package com.openathena;

import android.app.Application;

public class AthenaApp extends Application { // Android Singleton Class for holding state information
    private GeoTIFFParser geoTIFFParser;
    public GeoTIFFParser getGeoTIFFParser() {
        return geoTIFFParser;
    }

    public synchronized void setGeoTIFFParser(GeoTIFFParser geoTIFFParser) {
        this.geoTIFFParser = geoTIFFParser;
    }
}
