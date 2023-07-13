/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.dted.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.Resolution;
import com.agilesrc.dem4j.dted.DTEDLevelEnum;
import com.agilesrc.dem4j.dted.impl.FileBasedDTED;
import com.agilesrc.dem4j.impl.ADTEDTest;

/**
 * <p>The FileUserHeaderLabelTest object is the test
 * class for the FileBasedDTED class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class FileBasedDTEDTest extends ADTEDTest {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private Map<DTEDLevelEnum, FileBasedDTED> _files = null;
    
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
        try {
            super.setUp();

            _files = new HashMap<DTEDLevelEnum, FileBasedDTED>();
            for (DTEDLevelEnum dted: _dteds.keySet()) {
                if (_dteds.get(dted) != null) {
                    _files.put(dted, new FileBasedDTED(_dteds.get(dted)));
                }
            }
        } catch (final Exception e) {
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
            for (DTEDLevelEnum dted: _files.keySet()) {
                FileBasedDTED file = _files.get(dted);
                if (file != null) {
                    Point point = null;
                    double expected = Short.MIN_VALUE;
                    
                    if (dted == DTEDLevelEnum.DTED0) {
                        point = new Point(36, -76);
                        expected = 0;
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        point = new Point(30, -109);
                        expected = 1064;
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        point = new Point(40, -105);
                        expected = 1596;
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        //skipped
                    }
                    
                    assertEquals(file.getElevation(point).getElevation()
                            , expected);
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }
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
            for (DTEDLevelEnum dted: _files.keySet()) {
                FileBasedDTED file = _files.get(dted);
                if (file != null) {
                    Point neCorner = file.getNorthEastCorner();
                    
                    //calcualte expected
                    Point origin = file.getOrigin();
                    Resolution resolution = file.getResolution();
                    Point expected = origin.add(resolution.getSpacing() * (resolution.getRows() - 1),
                            resolution.getSpacing() * (resolution.getColumns() - 1));

                    assertEquals(neCorner, expected);
                    System.out.println(file.getElevation(expected));
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }
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

