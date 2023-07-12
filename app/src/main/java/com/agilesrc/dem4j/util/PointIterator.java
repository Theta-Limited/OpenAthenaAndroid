/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.util;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.*;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>The PointIterator object is iterator for looping
 * for terrain's points</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class PointIterator implements Iterator<Point> {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(PointIterator.class);
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    private final Resolution _resolution;
    private final Point _origin;
    private long _numberOfPoints;
    private long _currentPoint;
    
    //only used in 'sub set' case
    private BoundingBox _subSet = null;
    
    //temp
    transient private Point _next = null;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param terrain
     * @throws CorruptTerrainException 
     */
    public PointIterator(final DEM terrain) throws CorruptTerrainException {
        _resolution = terrain.getResolution();
        _origin = terrain.getSouthWestCorner();
        _numberOfPoints = (long)_resolution.getRows() * (long)_resolution.getColumns();
        _currentPoint = 0;
    }

    /**
     * @param terrain
     * @param north
     * @param south
     * @param west
     * @param east
     * @throws CorruptTerrainException
     */
    public PointIterator(final DEM terrain, final double north, 
            final double south, final double west, final double east) 
        throws CorruptTerrainException {
        this(terrain);
        _subSet = new BoundingBox(new Point(south,west),
                new Point(north,east));
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        moveToNext();
        
        if (_subSet == null) {
            if (_LOGGER.isTraceEnabled()) {
                _LOGGER.trace("current point" + _currentPoint + " out of " + 
                        _numberOfPoints);
            }
        } else {
            while(! _subSet.contains(_next) &&
                    _next != null) {
                moveToNext();
            }
        }
        
        return _next != null;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Point next() {  
        return _next;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        //ignore
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
    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public void moveToNext() {  
        long row = (long)_currentPoint / _resolution.getColumns();
        long column = (long)_currentPoint % _resolution.getColumns();
        
        Point result = _origin.add(row * _resolution.getSpacing(),
                column * _resolution.getSpacing());
        
        _currentPoint++;
        
        if  (_currentPoint <= _numberOfPoints) {
            _next = result;
        } else {
            _next = null;
        }
    }
    
    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

