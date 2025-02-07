package com.openathena;

public class MissingDataException extends Exception{
    public enum dataSources {
        EXIF,
        EXIF_XMP
    }

    public enum missingValues {
        LATITUDE,
        LONGITUDE,
        ALTITUDE,
        ALTITUDE_ABSOLUTE_REFERENCE,
        AZIMUTH,
        THETA,
        ROLL,
        ALL
    }

    public dataSources dataSource;
    public missingValues missingValue;

    public MissingDataException(String errorMessage, dataSources dataSource, missingValues missingValue) {
        super(errorMessage);
        this.dataSource = dataSource;
        this.missingValue = missingValue;
    }
}
