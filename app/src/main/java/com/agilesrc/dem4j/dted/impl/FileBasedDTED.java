/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.dted.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.threeten.bp.YearMonth;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.*;
import com.agilesrc.dem4j.dted.*;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;
import com.agilesrc.dem4j.impl.AbstractFileBasedTerrain;

/**
 * <p>The FileBasedDTED object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class FileBasedDTED extends AbstractFileBasedTerrain 
    implements DTED {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(FileBasedDTED.class);
    
    private static final int _DATA_RECORD_SENTINAL_LENGTH = 1;
    private static final int _DATA_RECORD_BLOCK_COUNT_LENGTH = 3;
    private static final int _DATA_RECORD_LONGITUDE_COUNT_LENGTH = 2;
    private static final int _DATA_RECORD_LATITUDE_COUNT_LENGTH = 2;
    private static final int _DATA_RECORD_ELEVATION_SIZE = 2;
    
    private static final int _DATA_RECORD_SKIP = UHLContents.LENGTH + 
        DSIContents.LENGTH + ACCContents.LENGTH +
        _DATA_RECORD_SENTINAL_LENGTH + _DATA_RECORD_BLOCK_COUNT_LENGTH +
        _DATA_RECORD_LONGITUDE_COUNT_LENGTH + _DATA_RECORD_LATITUDE_COUNT_LENGTH;
        
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    private FileUserHeaderLabel _userHeaderLabel;
    private FileDataSetIdentificationRecord _dataSetIdentification;
    private FileDataRecord _dataRecord;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * constructor
     * 
     * @param dtedFile
     * @throws InstantiationException
     * @throws FileNotFoundException
     */
    public FileBasedDTED(final File dtedFile) throws InstantiationException, 
        FileNotFoundException {
        super(dtedFile);
        init();
    }
 
    /**
     * constructor
     * 
     * @param dtedFile
     * @throws FileNotFoundException 
     */
    public FileBasedDTED(final RandomAccessFile dtedFile) throws InstantiationException {
        super(dtedFile);
        init();
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.Terrain#getResolution()
     */
    public Resolution getResolution() throws CorruptTerrainException {
        return getDTEDLevel();
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DTED#getElevation(java.awt.geom.Point2D)
     */
    public Elevation getElevation(final Point point) 
        throws InvalidValueException, CorruptTerrainException {
        Elevation result = null;
        if (contains(point)) {
            long skip = _DATA_RECORD_SKIP + 0;
            
            Point origin = _userHeaderLabel.getOrigin();
            double latitudeInterval = _userHeaderLabel.getLatitudeInterval();
            double longitudeInterval = _userHeaderLabel.getLongitudeInterval();
            
            int vertLocation = (int)Math.round(Math.abs((point.getLatitude() - 
                    _dataSetIdentification.south())) / latitudeInterval);
            int hozLocation = (int)Math.round(Math.abs((point.getLongitude() - 
                    _dataSetIdentification.west())) / longitudeInterval);
            
            
            skip = skip + vertLocation * hozLocation * 
                _DATA_RECORD_ELEVATION_SIZE;
            try {
                double elevation = _dataRecord.getElevation(vertLocation, hozLocation);
                final Point actual = new Point(
                        origin.getLatitude() + vertLocation * latitudeInterval, 
                        origin.getLongitude() + hozLocation * longitudeInterval);
                
                if (elevation == -32768) {
                    elevation = Double.NaN;
                }
                
                result = new Elevation(elevation, actual);
            } catch (Exception e) {
                if (_LOGGER.isErrorEnabled()) {
                    _LOGGER.error("failed to move to position " + skip,e);
                }
                
                throw new CorruptTerrainException(e);
            }
            
            
        } else {
            throw new InvalidValueException("Invalid point " + point);
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DTED#contains(java.awt.geom.Point2D)
     */
    public boolean contains(final Point point) {
        return _dataSetIdentification.contains(point);
    }
    
    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getOrigin()
     */
    public Point getOrigin() throws CorruptTerrainException {
        return _userHeaderLabel.getOrigin();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getLongitudeInterval()
     */
    public double getLongitudeInterval() throws CorruptTerrainException {
        return _userHeaderLabel.getLongitudeInterval();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getLatitudeInterval()
     */
    public double getLatitudeInterval() throws CorruptTerrainException {
        return _userHeaderLabel.getLatitudeInterval();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getVerticalAccuracy()
     */
    public short getVerticalAccuracy() throws CorruptTerrainException {
        return _userHeaderLabel.getVerticalAccuracy();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getClassification()
     */
    public SecurityEnum getClassification() throws CorruptTerrainException {
        return _userHeaderLabel.getClassification();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getRows()
     */
    public int getRows() throws CorruptTerrainException {
        return _userHeaderLabel.getRows();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getColumns()
     */
    public int getColumns() throws CorruptTerrainException {
        return _userHeaderLabel.getColumns();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getUniqueReferenceNumber()
     */
    public String getUniqueReferenceNumber() throws CorruptTerrainException {
        return _userHeaderLabel.getUniqueReferenceNumber();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getMultipleAccuracy()
     */
    public MultipleAccuracyEnum getMultipleAccuracy()
            throws CorruptTerrainException {
        return _userHeaderLabel.getMultipleAccuracy();
    }

    /**
     * @return
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getSecurityControlMarkings()
     */
    public String getSecurityControlMarkings() throws CorruptTerrainException {
        return _dataSetIdentification.getSecurityControlMarkings();
    }

    /**
     * @return
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getSecurityHandling()
     */
    public String getSecurityHandling() throws CorruptTerrainException {
        return _dataSetIdentification.getSecurityHandling();
    }

    /**
     * @return
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getDTEDLevel()
     */
    public DTEDLevelEnum getDTEDLevel() throws CorruptTerrainException {
        return _dataSetIdentification.getDTEDLevel();
    }

    /**
     * @return
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getDataEditionNumber()
     */
    public short getDataEditionNumber() throws CorruptTerrainException {
        return _dataSetIdentification.getDataEditionNumber();
    }

    /**
     * @return
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getMatchMergeVersion()
     */
    public char getMatchMergeVersion() throws CorruptTerrainException {
        return _dataSetIdentification.getMatchMergeVersion();
    }

    /**
     * @return
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getMaintenanceDate()
     */
    public YearMonth getMaintenanceDate() throws CorruptTerrainException {
        return _dataSetIdentification.getMaintenanceDate();
    }

    /**
     * @return
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getMatchMergeDate()
     */
    public YearMonth getMatchMergeDate() throws CorruptTerrainException {
        return _dataSetIdentification.getMatchMergeDate();
    }
   
    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.UserHeaderLabel#getReserved()
     */
    public String getReserved() throws CorruptTerrainException {
        return _userHeaderLabel.getReserved();
    }
    
    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getNorthWestCorner()
     */
    public Point getNorthWestCorner() throws CorruptTerrainException {
        return _dataSetIdentification.getNorthWestCorner();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getSouthWestCorner()
     */
    public Point getSouthWestCorner() throws CorruptTerrainException {
        return _dataSetIdentification.getSouthWestCorner();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getNorthEastCorner()
     */
    public Point getNorthEastCorner() throws CorruptTerrainException {
        return _dataSetIdentification.getNorthEastCorner();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     * @see com.agilesrc.dem4j.dted.DataSetIdentificationRecord#getSouthEastCorner()
     */
    public Point getSouthEastCorner() throws CorruptTerrainException {
        return _dataSetIdentification.getSouthEastCorner();
    }
    
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getProducerCode()
     */
    public FIPS10_4CodeEnum getProducerCode() throws CorruptTerrainException {
        return _dataSetIdentification.getProducerCode();
    }

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

    //=========================================================================
    // DEFAULT METHODS
    //=========================================================================

    //=========================================================================
    // PROTECTED METHODS
    //=========================================================================

    //=========================================================================
    // PRIVATE METHODS
    //=========================================================================
    /**
     * init 
     * @throws InstantiationException 
     */
    private void init() throws InstantiationException {
        try {
            _userHeaderLabel = new FileUserHeaderLabel(_file);
            _dataSetIdentification = new FileDataSetIdentificationRecord(_file);
            _dataRecord = new FileDataRecord(_file, 
                    _dataSetIdentification.getDTEDLevel());
        } catch (CorruptTerrainException e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("could not find file ",e);
            }
            
            throw new InstantiationException(e.getMessage());
        }
    }

    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

