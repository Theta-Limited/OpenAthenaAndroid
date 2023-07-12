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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>The BoundingBox object defines an area on the
 * earth in decimal degrees</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class BoundingBox {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private double _north;
    private double _south;
    private double _east;
    private double _west;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * 
     */
    public BoundingBox() {
        //nothing;
    }
    
    /**
     * @param north
     * @param south
     * @param west
     * @param east
     * @throws InstantiationException - throw if box is invalid
     */
    public BoundingBox(final double north, final double south, 
            final double west, final double east) {
        _north = north;
        _south = south;
        _west = west;
        _east = east;
    }

    /**
     * @param southWest
     * @param northEast
     * @throws InstantiationException - throw if box is invalid
     */
    public BoundingBox(final Point southWest, final Point northEast) {
        _north = northEast.getLatitude();
        _south = southWest.getLatitude();
        _west = southWest.getLongitude();
        _east = northEast.getLongitude();
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================

    /**
     * @return the north
     */
    public double getNorth() {
        return _north;
    }

    /**
     * @param north the north to set
     */
    public void setNorth(final double north) {
        _north = north;
    }

    /**
     * @return the south
     */
    public double getSouth() {
        return _south;
    }

    /**
     * @param south the south to set
     */
    public void setSouth(final double south) {
        _south = south;
    }

    /**
     * @return the east
     */
    public double getEast() {
        return _east;
    }

    /**
     * @param east the east to set
     */
    public void setEast(final double east) {
        _east = east;
    }

    /**
     * @return the west
     */
    public double getWest() {
        return _west;
    }

    /**
     * @param west the west to set
     */
    public void setWest(final double west) {
        _west = west;
    }
    
    /**
     * is the point in the box
     * 
     * @param point
     * @return
     */
    public boolean contains(final Point point) {
        boolean result = false;
        
        if (point != null) {
            result = point.getLatitude() <= _north 
                && point.getLatitude() >= _south 
                && point.getLongitude() >= _west
                && point.getLongitude() <= _east;
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("north",_north).append("south",_south)
            .append("west",_west).append("east",_east).toString();
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

