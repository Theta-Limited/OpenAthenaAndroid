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

import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>The ACCContents object is used to decode
 * the Accuracy Descriptor Record (ACC) part
 * of a DTED file.  
 * TODO: currently a stub
 * </p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum DRDHeaderContents implements FileContentLayout {
    SENTINEL(0,1,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file)
                throws CorruptTerrainException {
            return null;
        }
    },
    DATA_BLOCK_COUNT(1,3,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file)
                throws CorruptTerrainException {
            return null;
        }
    },
    LONGITUDE_COUNT(4,2,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Short value(final RandomAccessFile file)
                throws CorruptTerrainException {
            return binaryShort(file, this);
        }
    },
    LATITUDE_COUNT(6,2,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Short value(final RandomAccessFile file)
                throws CorruptTerrainException {
            return binaryShort(file, this);
        }
    };
    
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(DRDHeaderContents.class);
    
    //=========================================================================
    // VARIABLES
    //=========================================================================
    private final int _startPosition;
    private final int _length;
    private final String _defaultValue;

    //=====================================================================
    // CONSTRUCTORS
    //=====================================================================
    /**
     * @param start
     * @param length
     * @param defaultValue
     */
    DRDHeaderContents(int start, int length, String defaultValue) {
        _startPosition = start;
        _length = length;
        _defaultValue = defaultValue;
    }
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /*
     * (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getStartPosition()
     */
    public int getStartPosition() {
        return _startPosition;
    }

    /*
     * (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getLength()
     */
    public int getLength() {
        return _length;
    }

    /*
     * (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getDefaultValue()
     */
    public String getDefaultValue() {
        return _defaultValue;
    }
    
    /**
     * return the appropriate value
     * 
     * @param <TYPE>
     * @param file
     * @return
     */
    public abstract <TYPE> TYPE value(final RandomAccessFile file)
        throws CorruptTerrainException;
    
    //=========================================================================
    // DEFAULT METHODS
    //=========================================================================

    //=========================================================================
    // PROTECTED METHODS
    //=========================================================================

    //=========================================================================
    // PRIVATE METHODS
    //=========================================================================
    
    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getAccuracy()
     */
    public static short binaryShort(final RandomAccessFile file,
            final FileContentLayout location)
            throws CorruptTerrainException {
        short result;

        try {
            file.skipBytes(location.getStartPosition());
            result = file.readShort();

            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("read [" + result + "] for "
                        + location);
            }
        } catch (IOException e) {
            throw new CorruptTerrainException(e);
        }

        return result;
    }
    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

