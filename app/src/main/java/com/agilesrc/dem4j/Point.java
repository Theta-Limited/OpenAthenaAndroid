/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j;

import org.apache.commons.lang.builder.*;

/**
 * <p>The Point object is the representation of a 
 * geodetic point on the surface of the earth</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class Point implements Comparable<Point> {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    /**
     * radius of the earth in km
     */
    private static final double _EARTH_RADIUS = 6371;
    
    /**
     * number of arc seconds in 1 degree
     */
    public static final double ARCSECOND_IN_DEGREE = 3600;
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    private double _latitude = Double.NaN;
    private double _longitude = Double.NaN;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * 
     */
    public Point() {
        //do nothing
    }
    
    /**
     * @param latitude
     * @param longitude
     */
    public Point(final double latitude, final double longitude) {
        _latitude = latitude;
        _longitude = longitude;
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * @return the latitude
     */
    public double getLatitude() {
        return _latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final double latitude) {
        _latitude = latitude;
    }
    
    /**
     * add a lat/lon delta and get the new location back
     * 
     * @param latitude in degrees
     * @param longitude in degrees
     * @return
     */
    public Point add(final double latitude, final double longitude) {
        double newLat = _latitude + latitude;
        double newLon = _longitude + longitude;
        
        return new Point(newLat,newLon);
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return _longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final double longitude) {
        _longitude = longitude;
    }
    
    /**
     * @param point
     * @return distance in meters
     */
    public double distance(final Point point) {
        double result = Double.NaN;
        
        if (equals(point)) {
            result = 0;
        } else {
            double dLat = Math.toRadians(
                    point.getLatitude() - getLatitude());
            double dLon = Math.toRadians(
                    point.getLongitude() - getLongitude());
            double temp = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(getLatitude())) * 
                Math.cos(Math.toRadians(point.getLatitude())) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
            double a = 2 * Math.atan2(Math.sqrt(temp), Math.sqrt(1 - temp));
            
            result = _EARTH_RADIUS * a * 1000;
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean result = false;
        
        if (obj instanceof Point) {
            Point other = (Point)obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(_latitude, other.getLatitude());
            builder.append(_longitude, other.getLongitude());
            
            result = builder.isEquals();
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(_latitude);
        builder.append(_longitude);
        
        return builder.toHashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append(_latitude).append(_longitude).toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Point other) {
        int result = Integer.MIN_VALUE; 
        
        if (other != null) {
            CompareToBuilder builder = new CompareToBuilder();
            builder.append(_latitude, other.getLatitude());
            builder.append(_longitude, other.getLongitude());
            
            result = builder.toComparison();
        } 
        
        return result;
    }
    
    //=========================================================================
    // DEFAULT METHODS
    //=========================================================================

    //=========================================================================
    // PROTECTED METHODS
    //=========================================================================

    //=========================================================================
    // PRIVATE METHODS
    //=========================================================================

    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

