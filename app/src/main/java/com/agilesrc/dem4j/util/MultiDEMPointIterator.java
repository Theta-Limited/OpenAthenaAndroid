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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.BoundingBox;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.impl.ResolutionImpl;

/**
 * <p>The MultiDEMPointIterator object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Adam Nutt
 */
public class MultiDEMPointIterator implements Iterator<Point>{
    //=============================================================================================
    // CONSTANTS
    //=============================================================================================
    private static final Logger _LOGGER = LoggerFactory
            .getLogger(MultiDEMPointIterator.class);

    //=============================================================================================
    // VARIABLES
    //=============================================================================================
    private Point _origin = null;
    private Point _northEast = null;
    private Resolution _resolution;
    private long _currentPoint;
    private long _numberOfPoints;
    private int _numberOfRows = 0;
    private int _numberOfCols = 0;
    private int _currentRow = 0;
    private int _currentCol = 0;
    
    //only used in 'sub set' case
    private BoundingBox _subSet = null;
    
    transient private Point _next = null;

    //=============================================================================================
    // CONSTRUCTORS
    //=============================================================================================
    /**
     * @param dems
     * @throws CorruptTerrainException
     */
    public MultiDEMPointIterator(final List<DEM> dems) throws CorruptTerrainException {
        Collections.sort(dems);

        double north = -90.0;
        double south = 90.0;
        double west = 180.0;
        double east = -180.0;
        Resolution baseResolution = null;
        
        for (DEM dem : dems) {
            if (_origin == null) {
                _origin = dem.getSouthWestCorner();
            }
            
            if (baseResolution == null) {
            	baseResolution = dem.getResolution();
            } else if (! baseResolution.equals(dem.getResolution())) {
            	new CorruptTerrainException("mixed resolutions " + baseResolution + " != "
            			+ dem.getResolution());
            }
            
            Point northEast = dem.getNorthEastCorner();
            Point southWest = dem.getSouthWestCorner();
            
            if (northEast.getLatitude() > north) {
            	north = northEast.getLatitude();
            }
            
            if (northEast.getLongitude() > east) {
            	east = northEast.getLongitude();
            }
            
            if (southWest.getLatitude() < south) {
            	south = southWest.getLatitude();
            }
            
            if (southWest.getLongitude() < west) {
            	west = southWest.getLongitude();
            }
        }
        
        _northEast = new Point(north, east);
        int rows = (int) Math.round((north - south) / baseResolution.getSpacing()) + 1;
        int cols = (int) Math.round((east - west) / baseResolution.getSpacing()) + 1;
        _resolution = new ResolutionImpl(rows, cols, baseResolution.getSpacing());
        _numberOfPoints = (long)rows * (long)cols;
        
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
    public MultiDEMPointIterator(final List<DEM> terrain, final double north, 
            final double south, final double west, final double east) 
        throws CorruptTerrainException {
        this(terrain);
        _subSet = new BoundingBox(new Point(south,west),
                new Point(north,east));
    }

    //=============================================================================================
    // PUBLIC METHODS
    //=============================================================================================
    /**
     * @return
     */
    public Resolution getResolution() {
    	return _resolution;
    }
    
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
        // ignore
        
    }
    
    /**
     * @return
     */
    public BoundingBox getBounds() {
        return new BoundingBox(_origin, _northEast);
    }
    
    /**
     * @return
     */
    public int getRows() {
        return _numberOfRows;
    }
    
    /**
     * @return
     */
    public int getCols() {
        return _numberOfCols;
    }

    //=============================================================================================
    // DEFAULT METHODS
    //=============================================================================================

    //=============================================================================================
    // PROTECTED METHODS
    //=============================================================================================

    //=============================================================================================
    // PRIVATE METHODS
    //=============================================================================================
    /**
     * 
     */
    private void moveToNext() {
        Point result = _origin.add(_currentRow * _resolution.getSpacing() , 
        		_currentCol * _resolution.getSpacing());
        
        if (result.getLongitude() > _northEast.getLongitude()) {
            _currentCol = 0;
            _currentRow++;
            result = _origin.add(_currentRow * _resolution.getSpacing() , 
            		_currentCol * _resolution.getSpacing());
        }
        
        _currentCol++;
        _currentPoint++;
        
        if (_currentCol > _numberOfCols) {
            _numberOfCols = _currentCol;
        }
        
        if(_currentRow > _numberOfRows) {
            _numberOfRows = _currentRow;
        }
        
        if (_currentPoint <= _numberOfPoints) {
            _next = result;
        } else {
            _next = null;
        }
    }

    //=============================================================================================
    // INNER CLASSES
    //=============================================================================================

}

