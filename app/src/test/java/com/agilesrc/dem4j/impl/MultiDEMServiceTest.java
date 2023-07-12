/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.ADataTestCase;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.Utils;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.impl.MultiDEMService;
import com.agilesrc.dem4j.srtm.impl.FileBasedSRTM;

/**
 * <p>The MultiDEMServiceTest object is a test</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class MultiDEMServiceTest extends ADataTestCase {
    //=========================================================================
    // CONSTANTS
    //=========================================================================
	private static final File _MULTISERVICE = new File(Utils.TARGET
			+ "multiservice");
	
    //=========================================================================
    // VARIABLES
    //=========================================================================
	private MultiDEMService _service = null;
	
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * 
     */
    @BeforeSuite
    public void setUp() {
    	super.setUp();
        try {
        	FileUtils.copyFileToDirectory(Utils.N38W104_FILE, _MULTISERVICE);
        	FileUtils.copyFileToDirectory(Utils.N38W105_FILE, _MULTISERVICE);
        	
            final List<DEM> dems = new ArrayList<DEM>();
            for(File file : _MULTISERVICE.listFiles()) {
            	dems.add(new FileBasedSRTM(file));
            }
            
            _service = new MultiDEMService(dems);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
	
    /**
     * 
     */
    @AfterSuite
    public void tearDown() {
        if (_service!= null) {
            try {
            	_service.destroy();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * test bounding box
     */
    @Test
    public void testBoundingBox() {
    	try {
			Point southWestCorner = _service.getSouthWestCorner();
			Point northEastCorner = _service.getNorthEastCorner();
			
			System.out.println(southWestCorner);
			System.out.println(northEastCorner);
			
			assertEquals(southWestCorner.getLatitude(), 38.0, 0.1);
			assertEquals(southWestCorner.getLongitude(), -105.0, 0.1);
			assertEquals(northEastCorner.getLatitude(), 39.0, 0.1);
			assertEquals(northEastCorner.getLongitude(), -103.0, 0.1);
		} catch (CorruptTerrainException e) {
			e.printStackTrace();
			fail();
		}
    }
    
    /**
     * test resolution
     */
    @Test
    public void testResolution() {
        try {
            Resolution resolution = _service.getResolution();
            
            System.out.println(resolution);
            
            assertEquals(resolution.getRows(), 3601);
            assertEquals(resolution.getColumns(), 3601*2-1);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    /**
     * 
     */
    @Test
    public void testGetPointDataFromOneFile() {
        try {
			final File file = Utils.N38W104_FILE;
			DEM reference = new FileBasedSRTM(file);
			
			Elevation elevation = _service.getElevation(new Point(38.1, -103.8));
			Elevation refElevation = reference.getElevation(new Point(38.1, -103.8));
			
			System.out.println(elevation);
			System.out.println(refElevation);
			assertEquals(elevation, refElevation);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
    }
    
    
    /**
     * 
     */
    @Test
    public void testGetBoxDataCrossFiles() {
    	DEM reference1 = null;
    	DEM reference2 = null;
        try {
			final File file1 = Utils.N38W105_FILE;
			final File file2 = Utils.N38W104_FILE;
			reference1 = new FileBasedSRTM(file1);
			reference2 = new FileBasedSRTM(file2);
			
			Resolution resolution = _service.getResolution();
	        int rows = (int) Math.round((38.055 - 38.05) / resolution.getSpacing());
	        int cols = (int) Math.round((-103.995 - -104.005) / resolution.getSpacing());
			
			
			Elevation[][] elevations = _service.getElevations(38.055, 38.05, -104.005, -103.995);	
			
			Map<Point, Elevation> refMap = new HashMap<Point, Elevation>();
			Elevation[][] ref1Elevations = reference1.getElevations(38.055, 38.05, -104.005, -104.0);
			for (int i = 0; i < ref1Elevations.length; i ++) {
				for (int j = 0; j < ref1Elevations[0].length; j++) {
					Elevation ref1Elevation = ref1Elevations[i][j];
					refMap.put(ref1Elevation.getPoint(), ref1Elevation);
				}
			}
			
			Elevation[][] ref2Elevations = reference1.getElevations(38.055, 38.05, -104.0, -103.995);
			for (int i = 0; i < ref2Elevations.length; i ++) {
				for (int j = 0; j < ref2Elevations[0].length; j++) {
					Elevation ref2Elevation = ref2Elevations[i][j];
					if (refMap.containsKey(ref2Elevation.getPoint())) {
						System.out.println("duplicate point " + refMap.get(
								ref2Elevation.getPoint()) + " vs "+ ref2Elevation);
					}
					refMap.put(ref2Elevation.getPoint(), ref2Elevation);
				}
			}
			
			Map<Point, Elevation> found = new HashMap<Point, Elevation>();
			for (int i = 0; i < elevations.length; i ++) {
				for (int j = 0; j < elevations[0].length; j++) {
					Elevation elevation = elevations[i][j];
					System.out.println(elevation);
					
					//ensure unique
					assertFalse(found.containsKey(elevation.getPoint()));
					found.put(elevation.getPoint(), elevation);
					
					/*
					if(refMap.containsKey(elevation.getPoint())) {
					    assertEquals(elevation, refMap.get(elevation.getPoint()));
					}
					*/
				}
			}
			
			System.out.println(elevations.length);
			System.out.println(elevations[0].length);
			assertEquals(elevations.length, rows, 2);
			assertEquals(elevations[0].length, cols, 2);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			if (reference1 != null) {
				reference1.destroy();
			}
			if (reference2 != null) {
				reference2.destroy();
			}
		}
    }
    
    /**
     * 
     */
    @Test
    public void testGetBoxDataFromOneFile() {
    	DEM reference = null;
    	
        try {
			final File file = Utils.N38W105_FILE;
			reference = new FileBasedSRTM(file);
			
			Elevation[][] elevations = _service.getElevations(38.1, 38.05, -105.0, -104.05);
			Elevation[][] refElevations = reference.getElevations(38.1, 38.05, -105.0, -104.05);
			
			for (int i = 0; i < elevations.length; i ++) {
				for (int j = 0; j < elevations[0].length; j++) {
					Elevation elevation = elevations[i][j];
					Elevation refElevation = refElevations[i][j];
					System.out.println(elevation);
					System.out.println(refElevation);
					assertEquals(elevation, refElevation);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			if (reference != null) {
				reference.destroy();
			}
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