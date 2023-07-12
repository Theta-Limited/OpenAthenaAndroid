/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j;

import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.exceptions.ComputableAreaException;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidPointException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

/**
 * <p>The ElevationInterpolationFunction is an interface 
 * that will interpolate the elevation for a point that is not
 * on the data grid.</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public interface ElevationInterpolationFunction 
    extends Function<DEM.Elevation> {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * compute the elevation for a given point
     * 
     * @see com.agilesrc.dem4j.Function#compute(com.agilesrc.dem4j.Point)
     */
    public Elevation compute(final Point point) throws CorruptTerrainException,
        InvalidValueException, InvalidPointException, ComputableAreaException;
}

