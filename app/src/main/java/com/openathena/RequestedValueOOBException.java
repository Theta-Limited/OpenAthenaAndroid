package com.openathena;

public class RequestedValueOOBException extends Exception {
    public Double OOBLat;
    public Double OOBLon;
    public boolean isAltitudeDataBad;
    public RequestedValueOOBException(String errorMessage) {
        super(errorMessage);
        OOBLat = null;
        OOBLon = null;
        isAltitudeDataBad = false;
    }

    public RequestedValueOOBException(String errorMessage, double OOBLat, double OOBLon) {
        this(errorMessage);
        this.OOBLat = OOBLat;
        this.OOBLon = OOBLon;
        isAltitudeDataBad = false;
    }

    public RequestedValueOOBException(String errorMessage, double OOBLat, double OOBLon, boolean isAltitudeDataBad) {
        this(errorMessage, OOBLat, OOBLon);
        this.isAltitudeDataBad = isAltitudeDataBad;
    }
}
