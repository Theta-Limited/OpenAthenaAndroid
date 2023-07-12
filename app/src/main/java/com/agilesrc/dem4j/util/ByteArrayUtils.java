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

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agilesrc.dem4j.dted.impl.FileContentLayout;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>The ByteArrayUtils object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  hornm
 */
public class ByteArrayUtils {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    private static final Logger _LOGGER = LoggerFactory
        .getLogger(ByteArrayUtils.class);
    
    //=========================================================================
    // VARIABLES
    //=========================================================================

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getAccuracy()
     */
    public static byte[] value(final RandomAccessFile file,
            final FileContentLayout location)
            throws CorruptTerrainException {
        byte[] result;

        try {
            file.seek(location.getStartPosition());
            result = new byte[location.getLength()];
            file.read(result);

            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("read [" + string(result) + "] for "
                        + location);
            }
        } catch (IOException e) {
            throw new CorruptTerrainException(e);
        }

        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getAccuracy()
     */
    @SuppressWarnings("unchecked")
    public static <TYPE> TYPE value(final RandomAccessFile file,
            final FileContentLayout location, final Class<TYPE> type)
            throws CorruptTerrainException {
        TYPE result;

        byte[] temp = value(file,location);

        if (type.isAssignableFrom(Short.class)) {
            result = (TYPE)shortValue(temp);
        } else if (type.isAssignableFrom(Integer.class)) {
            result = (TYPE)integer(temp);
        } else if (type.isAssignableFrom(String.class)) {
            result = (TYPE)string(temp);
        } else {
            result = (TYPE)temp;
        }

        return result;
    }
    
    /**
     * @param data
     * @return
     */
    public static String string(final byte[] data) {
        return new String(data);
    }

    /**
     * @param data
     * @return
     */
    public static Integer integer(final byte[] data) {
        return Integer.parseInt(string(data));
    }

    /**
     * @param data
     * @return
     */
    public static Short shortValue(final byte[] data) {
        final String str = string(data);
        
        return NumberUtils.isNumber(str) ? 
                Short.parseShort(str) : 0;
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

