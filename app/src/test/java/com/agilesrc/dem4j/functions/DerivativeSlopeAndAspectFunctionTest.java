/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.functions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.ADataTestCase;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Function;
import com.agilesrc.dem4j.SlopeAndAspectFunction;
import com.agilesrc.dem4j.SlopeAndAspectFunction.SlopeAndAspect;
import com.agilesrc.dem4j.Utils;
import com.agilesrc.dem4j.srtm.impl.FileBasedSRTM;

/**
 * <p>The MatrixTest object is the test
 * class for the Matrix class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class DerivativeSlopeAndAspectFunctionTest extends ADataTestCase {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private File _file = null;
    private DEM _terrainFile;
    private Function<SlopeAndAspectFunction.SlopeAndAspect> _function;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * 
     */
    @Override
    @BeforeClass
    public void setUp() {
    	super.setUp();
        try {
            _file = Utils.N33E067_FILE;
            _terrainFile = new FileBasedSRTM(_file);
            _function = new DerivativeSlopeAndAspectFunction(_terrainFile);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    /**
     * test calculation of slope   
     */
    @Test(enabled = true)
    public void testCompute() {
        try {
            double spacing = _terrainFile.getResolution().getSpacing();
            SlopeAndAspect slopeAndAspect = _function.compute(
                    _terrainFile.getSouthWestCorner().add(spacing, spacing));
            
            System.out.println("Slope " + slopeAndAspect.getSlope());
            assertEquals(slopeAndAspect.getSlope(), 21.7693, 0.001);
            
            System.out.println("Aspect: " + slopeAndAspect.getAspect());
            assertEquals(slopeAndAspect.getAspect(), 187.7654, 0.001);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
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

