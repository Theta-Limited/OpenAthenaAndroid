/*
 *  Copyright 2001-2010 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.functions;

import com.agilesrc.dem4j.*;
import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.exceptions.ComputableAreaException;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidPointException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;

/**
 * <p>The DerivativeSlopeAndAspectFunction object is calculates slope
 * and aspect based on work done by 
 * Horn, B. K. P. (1981). Hill Shading and the Reflectance Map, Proceedings of the IEEE, 69(1):14-47.
 * </p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class DerivativeSlopeAndAspectFunction extends 
    A3x3MatrixFunction<SlopeAndAspectFunction.SlopeAndAspect>
        implements SlopeAndAspectFunction {
    
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private DEM _source = null;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param source
     */
    public DerivativeSlopeAndAspectFunction(final DEM source) {
        _source = source;
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================

    /* (non-Javadoc)
     * @see com.agilesrc.dem4j.SlopeAndAspectFunction#compute(com.agilesrc.dem4j.Point)
     */
    public SlopeAndAspect compute(final Point point) throws CorruptTerrainException,
            InvalidValueException, InvalidPointException, ComputableAreaException {
        
        Elevation[][] matrix = fill(_source, point);
        double[] derivatives = calculateDerivatives(matrix);
        double slope = calculateSlope(derivatives);
        double aspect = calculateAspect(derivatives);
        
        return new SlopeAndAspect(slope, aspect);
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
     * calculate the slope of the center point E
     * 
     * Horn, B. K. P. (1981). Hill Shading and the Reflectance Map, Proceedings of the IEEE, 69(1):14-47. 
     * 
     * @return in degrees
     */
    private double calculateSlope(final double[] derivatives) {
        double value = Math.sqrt((derivatives[0] * derivatives[0])
                + (derivatives[1] * derivatives[1]));
        
        return Math.toDegrees(Math.atan(value));
    }
    
    /**
     * calculate the aspect angle of the center point E
     * 
     * Horn, B. K. P. (1981). Hill Shading and the Reflectance Map, Proceedings of the IEEE, 69(1):14-47.
     * 
     * @return in degrees (North is 0)
     */
    private double calculateAspect(final double[] derivatives) {
        double yx = derivatives[0]/ derivatives[1];
        double result = Math.toDegrees(Math.atan(yx));
        
        if (derivatives[1] > 0) {
            result = result + 180;
        } else if (derivatives[1] < 0 && derivatives[0] > 0) {
            result = result + 360;
        }
        
        return result;
    }
    
    /**
     * based on a diff. algo
     * 
     * Horn, B. K. P. (1981). Hill Shading and the Reflectance Map, Proceedings of the IEEE, 69(1):14-47.
     * 
     * @return result[0] == dz/dx, result[1] == dz/dy
     */
    private double[] calculateDerivatives(final Elevation[][] matrix) {
        //calculate dz/dx
        double dz_dx_1 = (matrix[2][0].getElevation() 
                + 2*matrix[1][0].getElevation() 
                + matrix[0][0].getElevation());
        double dz_dx_2 = (matrix[2][2].getElevation() 
                + 2*matrix[1][2].getElevation() 
                + matrix[0][2].getElevation());
        double dz_dx_3 =  8.0*latitudeSpacing();
        
        double dz_dx = (dz_dx_2 - dz_dx_1) / dz_dx_3;
        
        //calculate dz/dy
        double dz_dy_1 = (matrix[2][0].getElevation()
                + 2*matrix[2][1].getElevation()
                + matrix[2][2].getElevation());
        double dz_dy_2 = (matrix[0][0].getElevation()
                + 2*matrix[0][1].getElevation()
                + matrix[0][2].getElevation());
        double dz_dy_3 =  8.0*rowLongitudeSpacing()[1];
        
        double dz_dy = (dz_dy_1 - dz_dy_2) / dz_dy_3;
        
        return new double[]{dz_dx, dz_dy};
    }
    
    //=========================================================================
    // INNER CLASSES
    //=========================================================================

}

