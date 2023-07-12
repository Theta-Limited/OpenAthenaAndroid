/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;
import com.agilesrc.dem4j.util.PointIterator;

/**
 * <p>The AbstractFileBasedTerrain object is the abstract
 * implementation of the Terrain interface</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public abstract class AbstractFileBasedTerrain implements DEM {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(AbstractFileBasedTerrain.class);
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    protected final RandomAccessFile _file;    
    private boolean _closed = false;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param file
     * @throws InstantiationException
     */
    public AbstractFileBasedTerrain(final File file) 
        throws InstantiationException {
        try {
            _file = new RandomAccessFile(file,"r");
        } catch (final FileNotFoundException e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("could not find file " + file,e);
            }
            
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * @param file
     * @throws InstantiationException
     */
    public AbstractFileBasedTerrain(final RandomAccessFile file) 
        throws InstantiationException {
        _file = file;
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#destroy()
     */
    public void destroy() {
        if (_file != null && ! _closed) {
            _closed = true;
            try {
                _file.close();
            } catch (IOException e) {
                if (_LOGGER.isWarnEnabled()) {
                    _LOGGER.warn("failed to close random access file",e);
                }
            }
        }
    }

    /**
     * This method will call getElevation for the south west corner and work
     * east and north based on the resolution until the grid is filled.  Some
     * 
     * Classes extending this class may want to override this method
     * to achieve greater efficiency.
     * 
     * @see com.agilesrc.dem4j.DEM#getElevations(double, double, double, double)
     */
    public Elevation[][] getElevations(final double north, final double south, final double west,
            final double east) throws InvalidValueException, CorruptTerrainException {
        
        Resolution resolution = getResolution();
        Elevation southWestElev = getElevation(new Point(south, west));
        Point southWest = southWestElev.getPoint();
        
        int rows = (int)Math.ceil((north - southWest.getLatitude())/
                resolution.getSpacing()) + 1;
        int cols = (int)Math.ceil((east - southWest.getLongitude())/
                resolution.getSpacing()) + 1;
        Elevation[][] result = new Elevation[rows][cols];
        
        result[0][0] = southWestElev;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (row == 0 && col == 0) {
                    continue;
                }
                
                double latDiff = row * resolution.getSpacing();
                double lonDiff = col * resolution.getSpacing();
                Point tmp = southWest.add(latDiff, lonDiff);
                result[row][col] = getElevation(tmp);
            }
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#interator()
     */
    public Iterator<Point> iterator() {
        Iterator<Point> result = null;
        
        try {
            result = new PointIterator(this);
        } catch (CorruptTerrainException e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("could not create iterator",e);
            }
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#iterator(double, double, double, double)
     */
    public Iterator<Point> iterator(final double north, final double south, final double west,
            final double east) {
        Iterator<Point> result = null;
        
        try {
            result = new PointIterator(this, north, south, west, east);
        } catch (CorruptTerrainException e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("could not create iterator",e);
            }
        }
        
        return result;
    }
    
    //=========================================================================
    // DEFAULT METHODS
    //=========================================================================

    //=========================================================================
    // PROTECTED METHODS
    //=========================================================================
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
    
    //=========================================================================
    // PRIVATE METHODS
    //=========================================================================

    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

