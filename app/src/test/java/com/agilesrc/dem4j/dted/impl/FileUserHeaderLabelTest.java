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

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.*;
import com.agilesrc.dem4j.dted.DTEDLevelEnum;
import com.agilesrc.dem4j.dted.MultipleAccuracyEnum;
import com.agilesrc.dem4j.dted.impl.FileUserHeaderLabel;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.impl.ADTEDTest;

/**
 * <p>The FileUserHeaderLabelTest object is test case for the
 * FileUserHeaderLabel class</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class FileUserHeaderLabelTest extends ADTEDTest {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private Map<DTEDLevelEnum, FileUserHeaderLabel> _uhls = null;
    
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

            _uhls = new HashMap<DTEDLevelEnum, FileUserHeaderLabel>();
            for (DTEDLevelEnum dted: _dteds.keySet()) {
                if (_dteds.get(dted) != null) {
                    _uhls.put(dted, new FileUserHeaderLabel(_dteds.get(dted)));
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
    public void testDump() {
        for (DTEDLevelEnum dted: _uhls.keySet()) {
            FileUserHeaderLabel uhl = _uhls.get(dted);
            if (uhl != null) {
                String dump = uhl.dump();
                System.out.println(dted + " dump: " + dump);
                if (dted == DTEDLevelEnum.DTED0) {
                    assertEquals(dump, "UHL10760000W0360000N030003000003U              012101210                        ");
                } else if (dted == DTEDLevelEnum.DTED1) {
                    assertEquals(dump, "UHL11090000W0300000N003000300070U              120112010                        ");
                } else if (dted == DTEDLevelEnum.DTED2) {
                    assertEquals(dump, "UHL11060000W0400000N001000100006U  F17 004     360136010                        ");
                } else if (dted == DTEDLevelEnum.DTED3) {
                    assertEquals(dump, "UHL1    1060000W        0400000N        00040004          NAU                   "); //something wrong here
                }
            } else {
                System.out.println(dted + " skipped, no file");
            }
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetVerticalAccuracy() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    short value = uhl.getVerticalAccuracy();
                    System.out.println(dted + " testGetVerticalAccuracy: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value,3);
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value,70);
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value,6);
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value,70);
                    }
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetUHL() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    String result = uhl.getUHL();
                    System.out.println(dted + " UHL: " + result);
                    assertEquals(result, "UHL");
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetOne() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    short value = uhl.getOne();
                    System.out.println(dted + " one: " + value);
                    assertEquals(value, 1);
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testUniqueReferenceNumber() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    String value = uhl.getUniqueReferenceNumber();
                    System.out.println(dted + " unique reference: " + value);
                    if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, "F17 004     ");
                    } else {
                        assertEquals(value,"            ");
                    }
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testOrigin() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    Point value = uhl.getOrigin();
                    System.out.println(dted + " testOrigin " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value,new Point(36,-76));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value,new Point(30,-109));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value,new Point(40,-106));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value,new Point(40,-106));
                    }
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test
    public void testGetClassification() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    SecurityEnum value = uhl.getClassification();
                    System.out.println(dted + " getClassification: " + value);
                    assertEquals(value,SecurityEnum.UNCLASSIFIED);
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetRows() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    int value = uhl.getRows();
                    System.out.println(dted + " getRows: " + value);
                    assertEquals(value,dted.getRows());
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetColumns() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    int value = uhl.getColumns();
                    System.out.println(dted + " getColumns: " + value);
                    assertEquals(value,dted.getColumns());
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetLatitudeInterval() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    double value = uhl.getLatitudeInterval();
                    System.out.println(dted + " testGetLatitudeInterval: " + value);
                    assertEquals(value,dted.getSpacing());
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test
    public void testGetLongitudeInterval() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    double value = uhl.getLongitudeInterval();
                    System.out.println(dted + " getLongitudeInterval: " + value);
                    assertEquals(value,dted.getSpacing());
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetMultipleAccuracy() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    MultipleAccuracyEnum value = uhl.getMultipleAccuracy();
                    System.out.println(dted + " testGetMultipleAccuracy: " + value);
                    assertEquals(value,MultipleAccuracyEnum.SINGLE);
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
            e.printStackTrace();
            fail();
        }        
    }
    
    /**
     * 
     */
    @Test(dependsOnMethods={"testDump"})
    public void testGetReserved() {
        try {
            for (DTEDLevelEnum dted: _uhls.keySet()) {
                FileUserHeaderLabel uhl = _uhls.get(dted);
                if (uhl != null) {
                    String value = uhl.getReserved();
                    System.out.println(dted + " testGetReserved: " + value);
                    assertNotNull(value);
                } else {
                    System.out.println(dted + " skipped, no file");
                }
            }            
        } catch (final CorruptTerrainException e) {
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

