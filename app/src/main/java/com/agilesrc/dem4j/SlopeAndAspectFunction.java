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

import com.agilesrc.dem4j.exceptions.ComputableAreaException;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidPointException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

/**
 * <p>The SlopeAndAspectFunction is a interface that
 * can compute the slope and aspect at a give point</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public interface SlopeAndAspectFunction 
    extends Function<SlopeAndAspectFunction.SlopeAndAspect> {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * Computer the slope & aspect for a given point.  {code}Double.NaN{code}
     * should be returned for any missing values.
     * 
     * @see com.agilesrc.dem4j.Function#compute(com.agilesrc.dem4j.Point)
     */
    public SlopeAndAspect compute(final Point point) throws CorruptTerrainException,
        InvalidValueException, InvalidPointException, ComputableAreaException;
    
    //=========================================================================
    // INNER CLASSES
    //========================================================================= 
    /**
     * <p>The SlopeAndAspect object is contains slope and aspect information</p>
     *
     * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
     * @author  Mark Horn
     */
    class SlopeAndAspect {
        //=====================================================================
        // VARIABLES
        //=====================================================================
        private double _slope = Double.NaN;
        private double _aspect = Double.NaN;
        
        //=====================================================================
        // CONSTRUCTORS
        //=====================================================================
        /**
         * @param slope
         * @param aspect
         */
        public SlopeAndAspect(double slope, double aspect) {
            _slope = slope;
            _aspect = aspect;
        }
        
        //=====================================================================
        // PUBLIC METHODS
        //=====================================================================

        /**
         * @return the slope
         */
        public double getSlope() {
            return _slope;
        }

        /**
         * @return the aspect
         */
        public double getAspect() {
            return _aspect;
        }
    }
}

