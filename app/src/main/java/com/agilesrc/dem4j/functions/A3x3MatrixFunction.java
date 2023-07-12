/*
 *  Copyright 2001-2010 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.functions;

import com.agilesrc.dem4j.BoundingBox;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.Function;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.exceptions.ComputableAreaException;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

/**
 * <p>The A3x3MatrixFunction object is an abstract class
 * that provides filling of a 3x3 matrix for processing by
 * an implementing function</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public abstract class A3x3MatrixFunction<T extends Object> implements Function<T> {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    /*
     * 3x3 matrix
     */
    private Elevation[][] _matrix = null;
    private double[] _rowLongitudeSpacing = null;
    private double _latitudeSpacing = Double.NaN;
    private BoundingBox _boundingBox = null;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================

    //=========================================================================
    // DEFAULT METHODS
    //=========================================================================

    //=========================================================================
    // PROTECTED METHODS
    //=========================================================================
    /**
     * set the matrix to evaluate the following point from the
     * DEM supplied
     * 
     * @param dem - digital elevation file
     * @param requested - center point supplied
     * @throws CorruptTerrainException 
     * @throws InvalidValueException 
     */
    protected Elevation[][] fill(final DEM dem, final Point requested) 
        throws CorruptTerrainException, InvalidValueException, ComputableAreaException {
    	
        final Resolution resolution = dem.getResolution();
        final double spacing = resolution.getSpacing();
        // Sanity check to make sure you're in the right area
        Point lowerLeft = dem.getSouthWestCorner();
        Point upperRight = dem.getNorthEastCorner();
        
        _boundingBox = new BoundingBox(lowerLeft.add(-spacing, -spacing), upperRight.add(spacing, spacing));
        
        if (!_boundingBox.contains(requested)) {
        	throw new ComputableAreaException("The lat/long is outside the computable area");
        }
        
        //get closest point
        Elevation closest = dem.getElevation(requested);
        Point point = closest.getPoint();
        
        _matrix = new Elevation[3][3];
        _rowLongitudeSpacing = new double[3];
        
        _boundingBox = new BoundingBox(
                point.add(-1*spacing, -1*spacing),
                point.add(spacing, spacing));
        
        Elevation[][] elevations = dem.getElevations(
                _boundingBox.getNorth(),
                _boundingBox.getSouth(),
                _boundingBox.getWest(), 
                _boundingBox.getEast());
        
        for (int i = 0 ; i < 3; i++) { //row
            for (int j = 0; j < 3; j++) { //column
                _matrix[i][j] = elevations[i][j];
            }
        }
        
        _latitudeSpacing = _matrix[1][0].getPoint().distance(
                _matrix[0][0].getPoint());
        _rowLongitudeSpacing[0] = _matrix[0][0].getPoint().distance(
                _matrix[0][1].getPoint());
        _rowLongitudeSpacing[1] = _matrix[1][0].getPoint().distance(
                _matrix[1][1].getPoint());
        _rowLongitudeSpacing[2] = _matrix[2][0].getPoint().distance(
                _matrix[2][1].getPoint());
        
        return _matrix;     
    }

    /**
     * @return the rowLongitudeSpacing
     */
    protected double[] rowLongitudeSpacing() {
        return _rowLongitudeSpacing;
    }

    /**
     * @return the latitudeSpacing
     */
    protected double latitudeSpacing() {
        return _latitudeSpacing;
    }

    /**
     * @return the boundingBox
     */
    protected BoundingBox boundingBox() {
        return _boundingBox;
    }
    
    //=========================================================================
    // PRIVATE METHODS
    //=========================================================================

    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

