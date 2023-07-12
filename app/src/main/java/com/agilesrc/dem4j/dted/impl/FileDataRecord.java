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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.dted.DTEDLevelEnum;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>The FileDataRecord object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  hornm
 */
public class FileDataRecord {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(FileDataRecord.class);

    private static final int _DATA_RECORD_SENTINAL_LENGTH = 1;
    private static final int _DATA_RECORD_BLOCK_COUNT_LENGTH = 3;
    private static final int _DATA_RECORD_LONGITUDE_COUNT_LENGTH = 2;
    private static final int _DATA_RECORD_LATITUDE_COUNT_LENGTH = 2;
    private static final int _DATA_RECORD_CHECKSUM_LENGTH = 4;
    private static final int _DATA_RECORD_ELEVATION_SIZE = 2;
    
    private static final int _DATA_RECORD_HEADER = _DATA_RECORD_SENTINAL_LENGTH 
        + _DATA_RECORD_BLOCK_COUNT_LENGTH + _DATA_RECORD_LONGITUDE_COUNT_LENGTH 
        + _DATA_RECORD_LATITUDE_COUNT_LENGTH;
    
    private static final int _BEFORE_DATA_RECORD_SKIP = UHLContents.LENGTH + 
        DSIContents.LENGTH + ACCContents.LENGTH;
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    private final RandomAccessFile _file;
    private final DTEDLevelEnum _dtedLevel;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param file
     */
    FileDataRecord(final RandomAccessFile file, 
            final DTEDLevelEnum dtedLevel) {
        _file = file;
        _dtedLevel = dtedLevel;
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * @return
     * @throws CorruptTerrainException 
     */
    public short getLongitudeNumber(final int latPosition, 
            final int lonPosition) throws CorruptTerrainException {
        short result = Short.MIN_VALUE;
        try {
            moveToPosition(latPosition, lonPosition);
            result = DRDHeaderContents.LONGITUDE_COUNT.
                <Short>value(_file).shortValue();
        } catch (IOException e) {
            throw new CorruptTerrainException(e);
        }
        
        return result;
    }
    
    /**
     * @return
     * @throws CorruptTerrainException 
     */
    public short getLatitudeNumber(final int latPosition, 
            final int lonPosition) throws CorruptTerrainException {
        short result = Short.MIN_VALUE;
        
        try {
            moveToPosition(latPosition, lonPosition);
            result = DRDHeaderContents.LATITUDE_COUNT.
                <Short>value(_file).shortValue();
        } catch (IOException e) {
            throw new CorruptTerrainException(e);
        }
        
        return result;
    }
    
    /**
     * @param latPosition
     * @return
     */
    public short getElevation(final int latPosition, 
            final int lonPosition) throws CorruptTerrainException {
        short result = Short.MIN_VALUE;
        
        try {
            moveToPosition(latPosition, lonPosition);
            _file.skipBytes(_DATA_RECORD_HEADER);
            _file.skipBytes(latPosition * _DATA_RECORD_ELEVATION_SIZE);
            
            result = _file.readShort();
        } catch (IOException e) {
            throw new CorruptTerrainException(e);
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
     * file stores data where one DataRecord is stored per latitude line.
     * 
     * @param latPoistion
     * @param lonPosition
     * @throws IOException 
     */
    private void moveToPosition(final int latPoistion, 
            final int lonPosition) throws IOException {
    
        int columns = _dtedLevel.getColumns();
        int seekTo = _BEFORE_DATA_RECORD_SKIP;
        
        //full record size
        int recordSize = _DATA_RECORD_HEADER + _DATA_RECORD_CHECKSUM_LENGTH +
            columns  * _DATA_RECORD_ELEVATION_SIZE;
        
        //move over to the correct column
        seekTo = seekTo + lonPosition * recordSize;
        
        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("move to position " + seekTo);
        }
        
        _file.seek(seekTo);
    }
    
    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

