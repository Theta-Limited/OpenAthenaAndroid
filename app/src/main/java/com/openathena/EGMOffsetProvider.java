package com.openathena;

public interface EGMOffsetProvider {
    public abstract double getEGM96OffsetAtLatLon(double latitude, double longitude);
}
