/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */

package com.agilesrc.dem4j.ned.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.BoundingBox;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;
import com.agilesrc.dem4j.impl.AbstractFileBasedTerrain;
import com.agilesrc.dem4j.impl.ResolutionImpl;

/**
 * <p>The FileBasedNED object is the NED (GridFloat)
 * implementation of the Terrain interface</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Stephen Aument
 */

public class FileBasedNED extends AbstractFileBasedTerrain {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(FileBasedNED.class);
    
    private static final int _DATA_SIZE_BYTES = 4;
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    private final BoundingBox _tile; 
    private Point _origin;
    private int _columns;
    private int _rows;
    private double _size;
    private int _noData;
    private ResolutionImpl _resolution;
    private boolean _leastSignificantBit = false;

    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param dataFile
     * @param headerFile
     * @throws InstantiationException
     */
    public FileBasedNED(final File dataFile, final File headerFile) 
    	throws InstantiationException {
    	super(dataFile);
    	
    	_origin = new Point();
        
        try {
            String lines= FileUtils.readFileToString(headerFile);

            String regex = "(\\w*)\\S+([-\\d.]*+)";
            Pattern p = Pattern.compile(regex);
            Matcher mat = p.matcher(lines);
                
            while (mat.find()) {            	
            	final String group = mat.group();
             	
                if (group.contains("ncols")){
                    mat.find();
                    final String columns = mat.group();
                    _columns = NumberUtils.createInteger(columns);
                } else if (group.contains("nrows")){
                    mat.find();
                    final String rows = mat.group();
                    _rows = NumberUtils.createInteger(rows);
                } else if (group.contains("xllcorner")){
                    mat.find();
                    final String xlowerleft = mat.group();
                    _origin.setLongitude(NumberUtils.createDouble(xlowerleft));
                } else if (group.contains("yllcorner")){
                    mat.find();
                    final String ylowerleft = mat.group();
                    _origin.setLatitude(NumberUtils.createDouble(ylowerleft));
                } else if (group.contains("cellsize")){
                    mat.find();
                    final String size = mat.group();
                    _size = NumberUtils.createFloat(size);
                } else if (group.contains("NODATA_value")){
                    mat.find();
                    final String noData = mat.group();
                    _noData = NumberUtils.createInteger(noData);
                } else if (group.contains("byteorder")){
                    mat.find();
                    final String byteOrder = mat.group();
                    if(byteOrder.contains("LSBFIRST")){
                    	_leastSignificantBit = true;
                    }
                  }
              }
            
            _resolution = new ResolutionImpl(_rows, _columns, _size);

            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("file size is " + _file.length() /  _DATA_SIZE_BYTES);
            }
            
            final Point northEast = _origin.add(_resolution.getRows() * _resolution.getSpacing(),
                    _resolution.getColumns() * _resolution.getSpacing());
            
            _tile = new BoundingBox(_origin, northEast);
    	} catch (final FileNotFoundException e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("could not find file " + dataFile,e);
            }
            
            throw new InstantiationException(e.getMessage());
    	} catch (final IOException e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("could not read file " + dataFile,e);
            }
            
            throw new InstantiationException(e.getMessage());
        }
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final DEM other) {
		int result = Integer.MIN_VALUE;
		
		try {
			result = new CompareToBuilder().append(getSouthWestCorner(), 
					other.getSouthWestCorner()).toComparison();
		} catch (CorruptTerrainException e) {
			if (_LOGGER.isErrorEnabled()) {
				_LOGGER.error("Unable to get point information",e);
			}
		}
		
		return result;
	}
	
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#getResolution()
     */
    public Resolution getResolution() throws CorruptTerrainException {
        return _resolution;
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#getElevation(com.agilesrc.dted4j.Point)
     */
    public Elevation getElevation(final Point point) throws InvalidValueException,
            CorruptTerrainException {
        
        double resolution = _size;
        double elevation = Short.MIN_VALUE;
        float tempElevation = 0.0f;
        
        int vertLocation = (int)Math.round(Math.abs((point.getLatitude() - 
                _origin.getLatitude())) / resolution);
        int hozLocation = (int)Math.round(Math.abs((point.getLongitude() - 
                _origin.getLongitude())) / resolution);
        
        long skipTo = vertLocation * _DATA_SIZE_BYTES * _columns
            + hozLocation * _DATA_SIZE_BYTES;
        
        try {
            if (skipTo >= _file.length()) {
                throw new CorruptTerrainException("ran past end of file requested move to was "
                        + skipTo + " but file length is " + _file.length());
            }
            
            _file.seek(skipTo);
            if(_leastSignificantBit) {
            	byte[] floatNum = new byte[4];
                _file.read(floatNum);
                tempElevation = EndianUtils.readSwappedFloat(floatNum, 0);
            }
            else {
            	tempElevation = _file.readFloat();
            }
            
            
            
//            if(_leastSignificantBit){
//            	
//            	EndianUtils.swapFloat(tempElevation);
//            }
            
            elevation = (short)tempElevation;
            
        } catch (IOException e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("Error reading file",e);
            }
            
            throw new CorruptTerrainException(e);
        }
        
        Point actual = new Point(_origin.getLatitude() + vertLocation * resolution,
                _origin.getLongitude() + hozLocation * resolution);
        
        if (elevation == _noData) {
            elevation = Double.NaN;
        }
        
        return new Elevation(elevation, actual);
    }   
    
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#getNorthWestCorner()
     */
    public Point getNorthWestCorner() throws CorruptTerrainException {
        return _origin.add(_size * (_rows - 1), 0);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#getSouthWestCorner()
     */
    public Point getSouthWestCorner() throws CorruptTerrainException {
        return _origin;
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#getNorthEastCorner()
     */
    public Point getNorthEastCorner() throws CorruptTerrainException {
        return _origin.add(_size * (_rows - 1), 
                _size * (_columns - 1));
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#getSouthEastCorner()
     */
    public Point getSouthEastCorner() throws CorruptTerrainException {
        return _origin.add(0, _size * (_columns - 1));
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#contains(com.agilesrc.dted4j.Point)
     */
    public boolean contains(final Point point) {
        return _tile.contains(point);
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

