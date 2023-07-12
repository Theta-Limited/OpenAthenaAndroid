/*
 *  Copyright 2001-2010 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j;

import com.agilesrc.dem4j.exceptions.ComputableAreaException;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidPointException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;


/**
 * <p>The Function generic function</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public interface Function<T extends Object> {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * compute some property for a given point
     * 
     * @param point
     * @return
     * @throws CorruptTerrainException
     * @throws InvalidValueException
     * @throws InvalidPointException
     */
    public T compute(final Point point) throws CorruptTerrainException,
        InvalidValueException, InvalidPointException, ComputableAreaException;
}

