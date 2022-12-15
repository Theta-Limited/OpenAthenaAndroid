package com.openathena;

public class RequestedValueOOBException extends Exception {
    public Double OOBLat;
    public Double OOBLon;
    public RequestedValueOOBException(String errorMessage) {
        super(errorMessage);
        OOBLat = null;
        OOBLon = null;
    }

    public RequestedValueOOBException(String errorMessage, double OOBLat, double OOBLon) {
        this(errorMessage);
        OOBLat = OOBLat;
        OOBLon = OOBLon;
    }
}
