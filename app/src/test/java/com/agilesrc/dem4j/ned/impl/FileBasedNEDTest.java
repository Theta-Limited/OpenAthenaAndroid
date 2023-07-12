/*
 *  Copyright 2001-2012 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */

package com.agilesrc.dem4j.ned.impl;


import static org.testng.Assert.*;

import java.io.File;
import java.util.Iterator;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.ADataTestCase;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.Utils;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>
 * The FileBasedNEDTest object is the test
 * class for the FileBasedNED class
 * </p>
 * <p>
 * Organization: AgileSrc LLC (www.agilesrc.com)
 * </p>
 */
public class FileBasedNEDTest extends ADataTestCase {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private File _dataFile = null;
    private File _headerFile = null;
    private DEM _terrainFile;

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * 
     */
    @BeforeClass
    public void setUp() {
    	super.setUp();
        try {
            _dataFile = Utils.N28_W081_FLT_FILE;
            _headerFile = Utils.N28_W081_HDR_FILE;
            _terrainFile = new FileBasedNED(_dataFile, _headerFile);
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
        if (_terrainFile!= null) {
            try {
                _terrainFile.destroy();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * test read of 1 arch second data.  Other tests
     * test 1/3 arc second data
     */
    @Test
    public void test1ArcSecondRead() {
        try {
            File data = Utils.N29_W082_FLT_FILE;
            File header = Utils.N29_W082_HDR_FILE;;
            FileBasedNED terrainFile = new FileBasedNED(data, header);
            
            Elevation elevation = terrainFile.getElevation(
                    terrainFile.getSouthWestCorner());
            assertNotNull(elevation);
            
            Resolution resolution = terrainFile.getResolution();
            assertNotNull(resolution);
            assertEquals(1.0/3600.0, resolution.getSpacing(), 0.00001);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        } 
    }

    /**
     * 
     */
    @Test
    public void testGetNorthEastCorner() {
        try {
            final Point point = _terrainFile.getNorthEastCorner();
            System.out.println(point);
            assertEquals(point, new Point(28.000462960977444, -79.99953703902256));
        } catch (CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * 
     */
    @Test
    public void testGetSouthWestCorner() {
        try {
            final Point point = _terrainFile.getSouthWestCorner();
            System.out.println(point);
            assertEquals(point,new Point(26.99944444444, -81.00055555556));
        } catch (CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test
    public void testIterator() {
        try {
            Iterator<Point> iterator = _terrainFile.iterator();
            Resolution resolution = _terrainFile.getResolution();
            Point southWest = _terrainFile.getSouthWestCorner();
            Point northEast = _terrainFile.getNorthEastCorner();
            
            int i=0;
            while(iterator.hasNext()) {
                Point point = iterator.next();
                
                //end of 1st row
                int column = i % resolution.getColumns();
                int row = i / resolution.getColumns();
                
                if (column == 0) {
                    System.out.println("testing row # : " + row);
                    assertEquals(point.getLongitude(),
                            southWest.getLongitude());
                } else if (column == 10812) {
                    assertEquals(point.getLongitude(),
                            northEast.getLongitude());
                }
                
                if (row == 0) {
                    assertEquals(point.getLatitude(),
                            southWest.getLatitude());
                } else if (row == 10812) {
                    assertEquals(point.getLatitude(),
                            northEast.getLatitude());
                }
                
                i++;
            }
        } catch (CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
        
    }
    /**
     * 
     */
    @Test
    public void testGetElevation() {
        try {
            double lon = -81.00055555556;
            double lat = 26.99944444444;
            
            Point point = new Point(lat,lon);
            
            Elevation value = _terrainFile.getElevation(point);
            System.out.println("testGetElevation " + value);
            assertEquals(value.getElevation(),14.0);
            assertEquals(value.getPoint(),point);
            
            
            lat = 26.99944444444;
            point = new Point(lat,lon);
            
            value = _terrainFile.getElevation(point);
            System.out.println("testGetElevation " + value);
            assertEquals(value.getElevation(),14.0);
            assertEquals(value.getPoint(),point);
            
            //check full file
            Iterator<Point> iterator = _terrainFile.iterator();
            Point temp = null;
            while(iterator.hasNext()) {
                temp = iterator.next();
                //System.out.println(temp);
            }
            
            System.out.println("last point : " + temp);
            value = _terrainFile.getElevation(temp);
            System.out.println("last point elevation : " + value);
            assertEquals(temp, _terrainFile.getNorthEastCorner());
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
