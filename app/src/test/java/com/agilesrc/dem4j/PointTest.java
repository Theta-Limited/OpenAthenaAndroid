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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.agilesrc.dem4j.Point;

/**
 * <p>The PointTest object is the test
 * class for the Point class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 */
public class PointTest {
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
    public void testDistance() {
        try {
            Point point1 = new Point(30,-109);
            Point point2 = new Point(30.0, -108.99916666666667);
            
            double distance = point2.distance(point1);
            System.out.println(distance);
            assertEquals(distance, 80.24, 0.1);
            
            distance = point1.distance(point2);
            System.out.println(distance);
            assertEquals(distance, 80.24, 0.1);
            
            distance = point1.distance(point1);
            System.out.println(distance);
            assertEquals(distance, 0, 0.1);
            
            
            point1 = new Point(30,-109);
            point2 = new Point(30.000833333333333, -109);
            distance = point1.distance(point2);
            System.out.println(distance);
            assertEquals(distance, 92.66, 0.1);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * test compareto
     */
    @Test
    public void testCompareTo() {
        Point point1 = new Point(-5, 5);
        Point point2 = new Point(-5, -5);
        Point point3 = new Point(5, 5);
        Point point4 = new Point(5, -5);
      
        List<Point> points = new ArrayList<Point>(5);
        points.add(point4);
        points.add(point3);
        points.add(point2);
        points.add(point1);
        
        Collections.sort(points);
        for (Point point: points) {
            System.out.println(point);
        }
      
        assertEquals(points.get(0), point2);
        assertEquals(points.get(1), point1);
        assertEquals(points.get(2), point4);
        assertEquals(points.get(3), point3);
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

