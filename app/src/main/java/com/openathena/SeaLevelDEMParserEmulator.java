package com.openathena;

import java.io.File;
import java.util.ArrayList;

import mil.nga.tiff.FileDirectory;

public class SeaLevelDEMParserEmulator extends DEMParser{

    SeaLevelDEMParserEmulator() {
        super();

        xParams = new geodataAxisParams();
        xParams.start = -180.0d;
        xParams.stepwiseIncrement = 1.0d;
        xParams.numOfSteps = 360;
        xParams.end = 180.0d;

        yParams = new geodataAxisParams();
        yParams.start = 90.0d;
        yParams.stepwiseIncrement = -1.0d;
        yParams.numOfSteps = 180;
        yParams.end = -90.0d;
    }

    SeaLevelDEMParserEmulator(File geofile) {
        this();
    }

    @Override
    public void loadDEM(File geofile) {
        // do nothing
        assert(true);
    }

    @Override
    public boolean isHorizontalDatumWGS84(FileDirectory directory, ArrayList<Integer> geoKeys) {
        return true;
    }

    @Override
    public double getAltFromLatLon(double lat, double lon) {
        // Instead of using any DEM, this provider just returns average mean sea level height
        // Converts to WGS84 height above ellipsoid (HAE)

        // return 0.0d - offsetProvider.getEGM96OffsetAtLatLon(lat,lon);
        // re issue #180, fix incorrect equation for applying geoid offset
        return 0.0d + offsetProvider.getEGM96OffsetAtLatLon(lat,lon);
    }
}
