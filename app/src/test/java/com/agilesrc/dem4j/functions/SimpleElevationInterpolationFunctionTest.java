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
import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.Function;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.Utils;
import com.agilesrc.dem4j.ned.impl.FileBasedNED;
import com.agilesrc.dem4j.srtm.impl.FileBasedSRTM;

/**
 * <p>The MatrixTest object is the test
 * class for the Matrix class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class SimpleElevationInterpolationFunctionTest extends ADataTestCase {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private File _file = null;
    private DEM _terrainFile;
    private Function<DEM.Elevation> _function;
    
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
            _file = Utils.N38W104_FILE;
            _terrainFile = new FileBasedSRTM(_file);
            _function = new SimpleElevationInterpolationFunction(_terrainFile);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    /**
     * test calculation of slope   
     */
    @Test
    public void testCompute() {
        try {
            
            Resolution resolution = _terrainFile.getResolution();
            Point point = _terrainFile.getSouthWestCorner()
                .add(resolution.getSpacing(), resolution.getSpacing());
            Elevation elevation = _terrainFile.getElevation(point);
            Elevation smothed = _function.compute(point);
            System.out.println("ON GRID " + elevation + " smothed " +
                    point + " ["+ smothed + "]");
            assertEquals(smothed.getElevation(), elevation.getElevation(), 0.001);
            
            point = _terrainFile.getSouthWestCorner()
                .add(resolution.getSpacing()*1.5, resolution.getSpacing()*1.5);
            
            elevation = _terrainFile.getElevation(point);
            smothed = _function.compute(point);
            
            System.out.println("OFF GRID nearest " + elevation + " smothed " +
                    point + " ["+ smothed + "]");
            assertEquals(smothed.getElevation(), 1363.5, 0.001);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * test creation of 3x3 matrix
     */
     @Test(dependsOnMethods={"testCompute"})
     public void testAccuracy() {
         try {
        	 File _dataFile = Utils.N25_W081_FLT_FILE;
             File _headerFile = Utils.N25_W081_HDR_FILE;
             
 			_terrainFile = new FileBasedNED(_dataFile, _headerFile);
 			_function = new SimpleElevationInterpolationFunction(_terrainFile);
 			Resolution resolution = _terrainFile.getResolution();
 			Point point =_terrainFile.getSouthWestCorner()
 	                .add(resolution.getSpacing(), resolution.getSpacing());
 			Elevation e = _function.compute(point);
 			assertEquals(e.getElevation(), 0.0, 0.001);
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

