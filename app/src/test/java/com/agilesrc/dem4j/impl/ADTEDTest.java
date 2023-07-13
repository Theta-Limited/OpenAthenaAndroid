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

import static org.testng.Assert.fail;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.agilesrc.dem4j.ADataTestCase;
import com.agilesrc.dem4j.Utils;
import com.agilesrc.dem4j.dted.DTEDLevelEnum;


/**
 * <p>The FileUserHeaderLabelTest object is the test
 * class for the FileBasedDTED class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public abstract class ADTEDTest extends ADataTestCase {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    protected Map<DTEDLevelEnum, RandomAccessFile> _dteds = null;
    
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
            _dteds = new HashMap<DTEDLevelEnum,RandomAccessFile>();
            
            _dteds.put(DTEDLevelEnum.DTED0, new RandomAccessFile(
            		Utils.N36_DT0_FILE, "r"));
            _dteds.put(DTEDLevelEnum.DTED1, new RandomAccessFile(
            		Utils.N30_DT1_FILE, "r"));
            _dteds.put(DTEDLevelEnum.DTED2, new RandomAccessFile(
            		Utils.N40_DT2_FILE, "r"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @AfterClass
    public void tearDown() {
        for (RandomAccessFile file: _dteds.values()) {
            try {
                file.close();
            } catch (final Exception e) {
                e.printStackTrace();
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

