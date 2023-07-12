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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.agilesrc.dem4j.ADataTestCase;
import com.agilesrc.dem4j.DEM;
import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Utils;
import com.agilesrc.dem4j.ned.impl.FileBasedNED;

/**
 * <p>The MultiDEMPointIteratorTest object is the test class for the MultiDEMPointIterator</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 */
public class MultiDEMPointIteratorTest extends ADataTestCase {
    //=============================================================================================
    // CONSTANTS
    //=============================================================================================

    //=============================================================================================
    // VARIABLES
    //=============================================================================================

    //=============================================================================================
    // CONSTRUCTORS
    //=============================================================================================

    //=============================================================================================
    // PUBLIC METHODS
    //=============================================================================================
    /**
     * 
     */
    @Test
    public void testIterator() {
        try {
            File dataFile = Utils.N25_W081_FLT_FILE;
            File headerFile = Utils.N25_W081_HDR_FILE;
        
            DEM terrain = new FileBasedNED(dataFile, headerFile);
            List<DEM> dems = new ArrayList<DEM>();
            dems.add(terrain);
            
            dataFile = Utils.N25_W082_FLT_FILE;
            headerFile = Utils.N25_W082_HDR_FILE;
            terrain = new FileBasedNED(dataFile, headerFile);
            
            dems.add(terrain);
                        
            MultiDEMPointIterator iterator = new MultiDEMPointIterator(dems);
                        
            int i = 0;
            while (iterator.hasNext()) {
                Point next = iterator.next();
                if (i % 1000 == 0) {
                    System.out.println(next);
                }
                
                i++;                
            }
            assertEquals(i, 233668944);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    //=============================================================================================
    // DEFAULT METHODS
    //=============================================================================================

    //=============================================================================================
    // PROTECTED METHODS
    //=============================================================================================

    //=============================================================================================
    // PRIVATE METHODS
    //=============================================================================================

    //=============================================================================================
    // INNER CLASSES
    //=============================================================================================

}

