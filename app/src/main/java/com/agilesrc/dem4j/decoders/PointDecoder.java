/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */
package com.agilesrc.dem4j.decoders;

import org.apache.commons.lang3.StringUtils;

import com.agilesrc.dem4j.HemisphereEnum;
import com.agilesrc.dem4j.util.CooridnateConversion;


/**
 * <p>Title:       PointDecoder</p>
 *  <p>Description: The PointDecoder object is the key something, doing
 * something</p>
 *  <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 *
 * @author Mark Horn
 */
public class PointDecoder {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    /**
     * value length of a point
     */
    private static final int _POINT_LENGTH_LONG = 8;

    /**
     * value length of a point
     */
    private static final int _POINT_LENGTH_SHORT = 7;
    
    //=========================================================================
    // VARIABLES
    //=========================================================================

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * decoder data into a decimal degree value
     *
     * @param data
     *
     * @return
     */
    public double decode(final byte[] data) {
    	double result = 0.0;

        if (data != null) {
            final String dddmmssh = new String(data);
            int degrees = 0;
            int min = 0;
            int seconds = 0;
            HemisphereEnum hemisphere = null;
            
            if (data.length == _POINT_LENGTH_LONG) {
                degrees = Integer.parseInt(StringUtils.substring(
                        dddmmssh, 0, 3));
                min = Integer.parseInt(StringUtils.substring(dddmmssh, 3,
                        5));
                seconds = Integer.parseInt(StringUtils.substring(
                        dddmmssh, 5, 7));
                hemisphere = HemisphereEnum.getFromString(StringUtils.substring(
                        dddmmssh, 7, 8));
            } else if (data.length == _POINT_LENGTH_SHORT) {
                degrees = Integer.parseInt(StringUtils.substring(
                        dddmmssh, 0, 2));
                min = Integer.parseInt(StringUtils.substring(dddmmssh, 2,
                        4));
                seconds = Integer.parseInt(StringUtils.substring(
                        dddmmssh, 4, 6));
                hemisphere = HemisphereEnum.getFromString(StringUtils.substring(
                        dddmmssh, 6, 7));
            }
            
            result = CooridnateConversion.convertDegMinSecondsToDecimalDegrees(
                    degrees, min, seconds, hemisphere);
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
