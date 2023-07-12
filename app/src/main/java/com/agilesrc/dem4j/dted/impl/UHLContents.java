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

import java.util.EnumSet;

/**
 * <p>The UHLContents object contains file contents
 * information</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum UHLContents implements FileContentLayout {
    UHL(0, 3, "UHL"), 
    ONE(3, 1, "1"), 
    ORIGIN_LONGITUDE(4, 8, null), 
    ORIGIN_LATITUDE(12, 8, null), 
    LONGITUDE_INTERVAL(20, 4, null), 
    LATITUDE_INTERVAL(24, 4, null), 
    VERTICAL_ACCURACY(28, 4, "  NA"), 
    SECURITY_CODE( 32, 3, "U  "), 
    UNIQUE_REFERENCE(35, 12, null), 
    NUMBER_LONGITUDE_LINES( 47, 4, null), 
    NUMBER_LATITUDE_LINES(51, 4, null), 
    MULTIUPLE_ACCURACY(55, 1, "0"), 
    RESERVED(56, 24, null);

    //=====================================================================
    // CONSTANTS
    //=====================================================================
    public static final int LENGTH;

    static {
        int length = 0;
        for (UHLContents s : EnumSet.allOf(UHLContents.class)) {
            length += s.getLength();
        }

        LENGTH = length;
    }

    //=====================================================================
    // VARIABLES
    //=====================================================================
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
    UHLContents(int start, int length, String defaultValue) {
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

