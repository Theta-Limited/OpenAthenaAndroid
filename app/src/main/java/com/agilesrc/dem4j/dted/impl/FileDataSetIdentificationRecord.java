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

import java.io.IOException;
import java.io.RandomAccessFile;

import org.threeten.bp.YearMonth;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.SecurityEnum;
import com.agilesrc.dem4j.dted.DTEDLevelEnum;
import com.agilesrc.dem4j.dted.DataSetIdentificationRecord;
import com.agilesrc.dem4j.dted.FIPS10_4CodeEnum;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>The FileDataSetIdentificationRecord object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  hornm
 */
public class FileDataSetIdentificationRecord implements
        DataSetIdentificationRecord {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(FileDataSetIdentificationRecord.class);
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    private final RandomAccessFile _file;

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param file
     */
    FileDataSetIdentificationRecord(final RandomAccessFile file) {
        _file = file;
    }
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * expected length of this section
     * 
     * @return
     */
    public int getLength() {
        return DSIContents.LENGTH;
    }
    
    /**
     * @return
     */
    public String dump() {
        String result = "ERROR";
        try {
            byte[] bytes = new byte[DSIContents.LENGTH];
            _file.seek(UHLContents.LENGTH);
            _file.read(bytes);
            result = new String(bytes);
        } catch (IOException e) {
            result = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * only checking NW corner as NE should be the
     * same north value.
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public double north() throws CorruptTerrainException {
        return getNorthWestCorner().getLatitude();
    }
    
    /**
     * only checking SW corner as SE should be the
     * same south value.
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public double south() throws CorruptTerrainException {
        return getSouthWestCorner().getLatitude();
    }
    
    /**
     * only checking NW corner as SW should be the
     * same west value.
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public double west() throws CorruptTerrainException {
        return getNorthWestCorner().getLongitude();
    }
    
    /**
     * only checking NE corner as SE should be the
     * same east value.
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public double east() throws CorruptTerrainException {
        return getNorthEastCorner().getLongitude();
    }
    
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getClassification()
     */
    public SecurityEnum getClassification() throws CorruptTerrainException {
        return DSIContents.SECURITY_CODE.<SecurityEnum>value(_file);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getDTEDLevel()
     */
    public DTEDLevelEnum getDTEDLevel() throws CorruptTerrainException {
        return DSIContents.PRODUCT_LEVEL.<DTEDLevelEnum>value(_file);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getDataEditionNumber()
     */
    public short getDataEditionNumber() throws CorruptTerrainException {
        return DSIContents.DATA_EDITION.<Short>value(_file).shortValue();
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getMaintenanceDate()
     */
    public YearMonth getMaintenanceDate() throws CorruptTerrainException {
        return DSIContents.MAINTENANCE_DATE.<YearMonth>value(_file);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getMatchMergeDate()
     */
    public YearMonth getMatchMergeDate() throws CorruptTerrainException {
        return DSIContents.MATCH_MERGE_DATE.<YearMonth>value(_file);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getMatchMergeVersion()
     */
    public char getMatchMergeVersion() throws CorruptTerrainException {
        String result = DSIContents.MATCH_MERGE_VERSION.value(_file);
        return result.length() > 0 ? result.charAt(0) : ' ';
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getSecurityControlMarkings()
     */
    public String getSecurityControlMarkings() throws CorruptTerrainException {
        return DSIContents.SECURITY_MARKINGS.value(_file);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getSecurityHandling()
     */
    public String getSecurityHandling() throws CorruptTerrainException {
        return DSIContents.SECURITY_HANDLING.<String>value(_file);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getUniqueReferenceNumber()
     */
    public String getUniqueReferenceNumber() throws CorruptTerrainException {
        return DSIContents.UNIQUE_REFERENCE_NUMBER.<String>value(_file);
    }
    
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getNorthWestCorner()
     */
    public Point getNorthWestCorner() throws CorruptTerrainException {
        Double lat = DSIContents.NORTH_WEST_LATITUDE.<Double>value(_file);
        Double lon = DSIContents.NORTH_WEST_LONGITUDE.<Double>value(_file);
        
        return new Point(lat,lon);
    }
    
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getSouthWestCorner()
     */
    public Point getSouthWestCorner() throws CorruptTerrainException {
        Double lat = DSIContents.SOUTH_WEST_LATITUDE.<Double>value(_file);
        Double lon = DSIContents.SOUTH_WEST_LONGITUDE.<Double>value(_file);
        
        return new Point(lat,lon);
    }
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getNorthEastCorner()
     */
    public Point getNorthEastCorner() throws CorruptTerrainException {
        Double lat = DSIContents.NORTH_EAST_LATITUDE.<Double>value(_file);
        Double lon = DSIContents.NORTH_EAST_LONGITUDE.<Double>value(_file);
        
        return new Point(lat,lon);
    }
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getSouthEastCorner()
     */
    public Point getSouthEastCorner() throws CorruptTerrainException {
        Double lat = DSIContents.SOUTH_EAST_LATITUDE.<Double>value(_file);
        Double lon = DSIContents.SOUTH_EAST_LONGITUDE.<Double>value(_file);
        
        return new Point(lat,lon);
    }
    
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#getProducerCode()
     */
    public FIPS10_4CodeEnum getProducerCode() throws CorruptTerrainException {
        final String code = DSIContents.PRODUCER_CODE.<String>value(_file);
        return StringUtils.isBlank(code) ? null : 
            FIPS10_4CodeEnum.fromCodeString(code);
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.DataSetIdentificationRecord#contains(java.awt.geom.Point2D)
     */
    public boolean contains(final Point point) {
        boolean result = false;
        
        if (point != null) {
            try {
                Point nw = getNorthWestCorner();
                Point se = getSouthEastCorner();
                double north = nw.getLatitude();
                double south = se.getLatitude();
                double west = nw.getLongitude();
                double east = se.getLongitude();
                
                double latitude = point.getLatitude();
                double longitude = point.getLongitude();
                
                if (latitude >= south && latitude <= north &&
                        longitude >= west && longitude <= east) {
                    result = true;
                } 
            } catch (CorruptTerrainException e) {
                if (_LOGGER.isErrorEnabled()) {
                    _LOGGER.error("data file is corrupt",e);
                }
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

    //=========================================================================
    // INNER CLASSES
    //=========================================================================
}

