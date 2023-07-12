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

import com.agilesrc.dem4j.HemisphereEnum;


/**
 * <p>Title:       CooridnateConversion</p>
 *  <p>Description: The CooridnateConversion does stuff</p>
 *  <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 *
 * @author Mark Horn
 */
public class CooridnateConversion {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

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
     * convert DDMMSS [degree, min, seconds] (H) value to decimal degrees
     *
     * @param degrees
     * @param minutes
     * @param seconds
     * @param direction
     *
     * @return
     */
    public static double convertDegMinSecondsToDecimalDegrees(
        final int degrees, final int minutes, final int seconds,
        final HemisphereEnum direction) {
        
    	double result = degrees;
    	double temp = (minutes*60 + seconds)/3600;
    	result += temp;
    	
    	result = result * (double)direction.getMultiplier();
    	
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
