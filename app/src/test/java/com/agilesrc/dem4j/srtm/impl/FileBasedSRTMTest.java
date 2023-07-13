/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.srtm.impl;

import static org.testng.Assert.*;

import java.io.File;
import java.util.Iterator;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.ADataTestCase;
import com.agilesrc.dem4j.DEM.Elevation;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.Utils;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.srtm.SRTMLevelEnum;

/**
 * <p>The FileBasedSRTMTest object is the test
 * class for the FileBasedSRTM class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class FileBasedSRTMTest extends ADataTestCase {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private File _file = null;
    private FileBasedSRTM _terrainFile;
    
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
     * 
     */
    @Test
    public void testResolution() {
    	try {
			Resolution resolution = _terrainFile.getResolution();
			System.out.println(resolution);
			assertEquals(resolution, SRTMLevelEnum.SRTM3);
			
			long length = _file.length();
			long expected = (long)((double)(resolution.getColumns() * 
					resolution.getRows()) *2.0);
			System.out.println(expected);
			System.out.println(length);
			assertEquals(length, expected);
		} catch (CorruptTerrainException e) {
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
            assertEquals(point, new Point(34.0,68.0));
            
            Elevation elevation = _terrainFile.getElevation(point);
            System.out.println(elevation);
            assertNotNull(elevation);
            
            double[] rowAndColumn = _terrainFile.rowAndColumn(point);
            assertEquals(rowAndColumn[0], 0, 0.0);
            assertEquals(rowAndColumn[1], _terrainFile.getResolution().getColumns() -1, 0.0);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test
    public void testGetNorthWestCorner() {
        try {
            final Point point = _terrainFile.getNorthWestCorner();
            
            Elevation elevation = _terrainFile.getElevation(point);
            System.out.println(elevation);
            
            double[] rowAndColumn = _terrainFile.rowAndColumn(point);
            assertEquals(rowAndColumn[0], 0, 0.0);
            assertEquals(rowAndColumn[1], 0, 0.0);
            assertNotNull(elevation);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * 
     */
    @Test
    public void testGetSouthEastCorner() {
        try {
            final Point southEast = _terrainFile.getSouthEastCorner();
            final Point northWest = _terrainFile.getNorthWestCorner();
            double lonCount = (southEast.getLongitude() - northWest.getLongitude()) / 
            	_terrainFile.getResolution().getSpacing();
            System.out.println(lonCount);
            double latCount = (northWest.getLatitude() - southEast.getLatitude()) / 
                	_terrainFile.getResolution().getSpacing();
            System.out.println(latCount);
            Elevation elevation = _terrainFile.getElevation(southEast);
            System.out.println(elevation);
            assertNotNull(elevation);
            
            double[] rowAndColumn = _terrainFile.rowAndColumn(southEast);
            assertEquals(rowAndColumn[0], _terrainFile.getResolution().getRows() -1, 0.0);
            assertEquals(rowAndColumn[1], _terrainFile.getResolution().getColumns() -1, 0.0);
        } catch (final Exception e) {
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
            assertEquals(point,new Point(33.0, 67.0));
            
            Elevation elevation = _terrainFile.getElevation(point);
            System.out.println(elevation);
            assertNotNull(elevation);
            
            double[] rowAndColumn = _terrainFile.rowAndColumn(point);
            assertEquals(rowAndColumn[0], _terrainFile.getResolution().getRows() - 1, 0.0);
            assertEquals(rowAndColumn[1], 0, 0.0);
        } catch (final Exception e) {
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
                //System.out.println("testing row/column # : " + row 
                //        + "/" + column);
                
                if (column == 0) {
                    assertEquals(point.getLongitude(),
                            southWest.getLongitude());
                } else if (column == 1200) {
                    assertEquals(point.getLongitude(),
                            northEast.getLongitude());
                }
                
                if (row == 0) {
                    assertEquals(point.getLatitude(),
                            southWest.getLatitude());
                } else if (row == 1200) {
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
            double lat = 33.0;
            double lon = 67.0;
            
            Point point = new Point(lat,lon);
            
            Elevation value = _terrainFile.getElevation(point);
            System.out.println("testGetElevation " + value);
            assertEquals(value.getElevation(),2856.0);
            
            
            lon = 67.0;
            point = new Point(lat,lon);
            
            value = _terrainFile.getElevation(point);
            System.out.println("testGetElevation " + value);
            assertEquals(value.getElevation(),2856.0);
            
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

