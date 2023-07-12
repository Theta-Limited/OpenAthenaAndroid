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

import java.util.Iterator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

/**
 * <p>The DEM interface defines a basic interface
 * for getting elevation data from some source</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public interface DEM extends Comparable<DEM> {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * get the data resolution
     * 
     * @return
     * @throws CorruptTerrainException - ref. data is corrupt
     */
    public Resolution getResolution() throws  CorruptTerrainException;
    
    /**
     * Will return the elevation based on the nearest point in the 
     * data.
     * 
     * @param point
     * @return an Elevation object
     * @throws InvalidValueException - throw in the point is not contained 
     *       within the DTED data.
     * @throws CorruptTerrainException - ref. data is corrupt
     */
    public Elevation getElevation(final Point point) 
        throws InvalidValueException, CorruptTerrainException;
    
    
    /**
     * returns a multidimensional array of elevation object based on the area defined
     * and the resolution of the underling data.  Data will be returned
     * in order of lower/left corner (sw) go +lon +lat
     * 
     * @param north
     * @param south
     * @param west
     * @param east
     * @return
     * @throws InvalidValueException
     * @throws CorruptTerrainException
     */
    public Elevation[][] getElevations(final double north, final double south,
            final double west, final double east) throws InvalidValueException, 
            CorruptTerrainException;
    
    /**
     * free up any resources
     */
    public void destroy();
    
    /**
     * Is the point contained within the data. This test does not
     * check to see if the data contains the exact point, but
     * if the point is within the bounds of the data.
     * 
     * @param point
     * @return
     */
    public boolean contains(final Point point);

    /**
     * get the north west corner point of the data
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public Point getNorthWestCorner() throws CorruptTerrainException;
    
    /**
     * get the south west corner point of the data
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public Point getSouthWestCorner() throws CorruptTerrainException;
    
    /**
     * get the north east corner point of the data
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public Point getNorthEastCorner() throws CorruptTerrainException;
    
    /**
     * get the south east corner point of the data
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public Point getSouthEastCorner() throws CorruptTerrainException;
    
    /**
     * get an iterator for all points in the terrain data
     * 
     * @return
     */
    public Iterator<Point> iterator();
    
    /**
     * get an iterator for a small area
     * 
     * @param north
     * @param south
     * @param west
     * @param east
     * @return
     */
    public Iterator<Point> iterator(final double north, final double south,
            final double west, final double east);
    
    //=========================================================================
    // INNER CLASSES
    //=========================================================================    
    /**
     * <p>The Elevation object contains elevation data along
     * with the actual point data came from.</p>
     *
     * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
     * @author  Mark Horn
     */
    class Elevation {
        //=====================================================================
        // VARIABLES
        //=====================================================================
        private double _elevation = Double.MIN_VALUE;
        private Point _point = null;
        
        //=====================================================================
        // CONSTRUCTORS
        //=====================================================================
        /**
         * Constructor
         * 
         * @param elevation
         * @param point
         */
        public Elevation(final double elevation, final Point point) {
            _point = point;
            _elevation = elevation;
        }
        
        //=====================================================================
        // PUBLIC METHODS
        //=====================================================================
        /**
         * get elevation in meters.  If data is missing
         * the result should be {code}Double.NaN{code}
         * 
         * @return
         */
        public double getElevation() {
            return _elevation;
        }
        
        /**
         * returns the actual lat/lon point that the data 
         * was associated with in the DTED file.
         * 
         * @return
         */
        public Point getPoint() {
            return _point;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            ToStringBuilder builder = new ToStringBuilder(this, 
                    ToStringStyle.SHORT_PREFIX_STYLE);
            builder.append("location",_point);
            builder.append("elevation [m]",_elevation);
            
            return builder.toString();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
        	return new HashCodeBuilder().append(_point).append(_elevation)
        			.toHashCode();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(final Object obj) {
        	boolean result = false;
        	
        	if (obj instanceof Elevation) {
        		Elevation other = (Elevation)obj;
        		result = new EqualsBuilder().append(_point, other.getPoint())
        				.append(_elevation, other.getElevation()).isEquals();
        	}
        	
        	return result;
        }
    }
}

