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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Iterator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.exceptions.InvalidValueException;
import com.agilesrc.dem4j.util.PointIterator;

/**
 * <p>The PointIteratorTest object is the test
 * class for the PointIterator class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class PointIteratorTest {
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
     * 
     */
    @Test
    public void testPointIterator() {
        try {
            PointIterator iterator = new PointIterator(new TestTerrain());
            int i = 0;
            while (iterator.hasNext()) {
                Point next = iterator.next();
                System.out.println(next);
                i++;
            }
            
            assertEquals(i, 6*6);
            
            iterator = new PointIterator(new TestTerrain(),
                    -1,-4,-4,-1);
            i = 0;
            while (iterator.hasNext()) {
                Point next = iterator.next();
                System.out.println(next);
                i++;
            }
            
            assertEquals(i, 16);
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
    /**
     * <p>The TestTerrain object is the key something,
     * doing something</p>
     *
     * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
     * @author  Mark Horn
     */
    class TestTerrain implements DEM {
        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#getResolution()
         */
        public Resolution getResolution()
                throws CorruptTerrainException {
            return new Resolution() {
                
                /* (non-Javadoc)
                 * @see com.agilesrc.dted4j.Resolution#getSpacing()
                 */
                public double getSpacing() {
                    return 1;
                }
                
                /* (non-Javadoc)
                 * @see com.agilesrc.dted4j.Resolution#getRows()
                 */
                public int getRows() {
                    return 6;
                }
                
                /* (non-Javadoc)
                 * @see com.agilesrc.dted4j.Resolution#getColumns()
                 */
                public int getColumns() {
                    return 6;
                }
            };
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#getElevation(com.agilesrc.dted4j.Point)
         */
        public Elevation getElevation(final Point point)
                throws InvalidValueException, CorruptTerrainException {
            return new Elevation((short)0, point);
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#destroy()
         */
        public void destroy() {}

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#contains(com.agilesrc.dted4j.Point)
         */
        public boolean contains(final Point point) {
            return false;
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#getNorthWestCorner()
         */
        public Point getNorthWestCorner()
                throws CorruptTerrainException {
            return new Point(0, -5);
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#getSouthWestCorner()
         */
        public Point getSouthWestCorner()
                throws CorruptTerrainException {
            return new Point(-5, -5);
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#getNorthEastCorner()
         */
        public Point getNorthEastCorner()
                throws CorruptTerrainException {
            return null;
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#getSouthEastCorner()
         */
        public Point getSouthEastCorner()
                throws CorruptTerrainException {
            return new Point(-5, 0);
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#interator()
         */
        public Iterator<Point> iterator() {
            return null;
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.Terrain#iterator(double, double, double, double)
         */
        public Iterator<Point> iterator(double north, double south,
                double west, double east) {
            return null;
        }

        /* (non-Javadoc)
         * @see com.agilesrc.dted4j.DEM#getElevations(double, double, double, double)
         */
        public Elevation[][] getElevations(double north, double south,
                double west, double east) throws InvalidValueException,
                CorruptTerrainException {
            // TODO Auto-generated method stub
            return null;
        }
        
    	/* (non-Javadoc)
    	 * @see java.lang.Comparable#compareTo(java.lang.Object)
    	 */
    	public int compareTo(final DEM other) {
    		int result = Integer.MIN_VALUE;
    		
    		try {
    			result = new CompareToBuilder().append(getSouthWestCorner(), 
    					other.getSouthWestCorner()).toComparison();
    		} catch (CorruptTerrainException e) {
    			e.printStackTrace();
    		}
    		
    		return result;
    	}
    }
}

