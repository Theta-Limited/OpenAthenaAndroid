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

import org.threeten.bp.YearMonth;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.agilesrc.dem4j.*;
import com.agilesrc.dem4j.dted.DTEDLevelEnum;
import com.agilesrc.dem4j.dted.FIPS10_4CodeEnum;
import com.agilesrc.dem4j.dted.impl.FileDataSetIdentificationRecord;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.impl.ADTEDTest;

/**
 * <p>The FileUserHeaderLabelTest object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class FileDataSetIdentificationRecordTest extends ADTEDTest {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private Map<DTEDLevelEnum, FileDataSetIdentificationRecord> _dsis = null;
    
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

            _dsis = new HashMap<DTEDLevelEnum, FileDataSetIdentificationRecord>();
            for (DTEDLevelEnum dted: _dteds.keySet()) {
                if (_dteds.get(dted) != null) {
                    _dsis.put(dted, new FileDataSetIdentificationRecord(_dteds.get(dted)));
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
        for (DTEDLevelEnum dted: _dsis.keySet()) {
            FileDataSetIdentificationRecord dsi = _dsis.get(dted);
            if (dsi != null) {
                String dump = dsi.dump();
                System.out.println(dted + " dump: " + dump);
                assertNotNull(dump);
            } else {
                System.out.println(dted + " skipped, no file");
            }
        }
    }
    
    
    /**
     * 
     */
    @Test
    public void testGetDataEditionNumber() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    short value = dsi.getDataEditionNumber();
                    System.out.println(dted + " data edition number: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, 1);
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, 3);
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, 99);
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, 1);
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
    public void testGetDTEDLevel() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    DTEDLevelEnum value = dsi.getDTEDLevel();
                    System.out.println(dted + " dted level: " + value);
                    assertEquals(value, dted);
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
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    SecurityEnum value = dsi.getClassification();
                    System.out.println(dted + " classification: " + value);
                    assertEquals(value, SecurityEnum.UNCLASSIFIED);
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
    public void testGetMaintenanceDate() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    YearMonth value = dsi.getMaintenanceDate();
                    System.out.println(dted + " maint. date: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, YearMonth.of(2001, 6));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, YearMonth.of(1990, 5));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, YearMonth.of(2000, 1));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, YearMonth.of(2001, 1));
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
    public void testGetMatchMergeDate() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    YearMonth value = dsi.getMatchMergeDate();
                    System.out.println(dted + " match merge date: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, YearMonth.of(2000, 1));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, YearMonth.of(2000, 1));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, YearMonth.of(2000, 1));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, YearMonth.of(2000, 1));
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
    public void testGetMatchMergeVersion() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    char value = dsi.getMatchMergeVersion();
                    System.out.println(dted + " match merge version: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, 'A');
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, 'A');
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, 'A');
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, 'A');
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
    public void testGetSecurityControlMarkings() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    String value = dsi.getSecurityControlMarkings();
                    System.out.println(dted + " security control markings: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertTrue(StringUtils.isBlank(value));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, "DS");
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, "OO");
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, " ");
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
    public void testGetSecurityHandling() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    String value = dsi.getSecurityHandling();
                    System.out.println(dted + " security handling: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertTrue(StringUtils.isBlank(value));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, "LIMITED DISTRIBUTION       ");
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, "PUBLIC SALE/NO RESTRICTION ");
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, " ");
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
    public void testGetNorthEastCorner() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    Point value = dsi.getNorthEastCorner();
                    System.out.println(dted + " get north east corner: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, new Point(37, -75));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, new Point(31, -108));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, new Point(41, -105));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, null);
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
    public void testGetNorthWestCorner() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    Point value = dsi.getNorthWestCorner();
                    System.out.println(dted + " get north west corner: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, new Point(37, -76));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, new Point(31, -109));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, new Point(41, -106));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, null);
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
    public void testGetProducerCode() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    FIPS10_4CodeEnum value = dsi.getProducerCode();
                    System.out.println(dted + " get producer code: " + value);
                    assertEquals(value, FIPS10_4CodeEnum.UNITED_STATES);
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
    public void testGetSouthWestCorner() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    Point value = dsi.getSouthWestCorner();
                    System.out.println(dted + " get south west corner: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, new Point(36, -76));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, new Point(30, -109));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, new Point(40, -106));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, null);
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
    public void testGetSouthEastCorner() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    Point value = dsi.getSouthEastCorner();
                    System.out.println(dted + " get south east corner: " + value);
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertEquals(value, new Point(36, -75));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertEquals(value, new Point(30, -108));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertEquals(value, new Point(40, -105));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertEquals(value, null);
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
    public void testContains() {
        try {
            for (DTEDLevelEnum dted: _dsis.keySet()) {
                FileDataSetIdentificationRecord dsi = _dsis.get(dted);
                if (dsi != null) {
                    if (dted == DTEDLevelEnum.DTED0) {
                        assertTrue(dsi.contains(new Point(37, -75)));
                        assertFalse(dsi.contains(new Point(0,0)));
                    } else if (dted == DTEDLevelEnum.DTED1) {
                        assertTrue(dsi.contains(new Point(30.5, -108.5)));
                    } else if (dted == DTEDLevelEnum.DTED2) {
                        assertTrue(dsi.contains(new Point(40.1, -105.9)));
                    } else if (dted == DTEDLevelEnum.DTED3) {
                        assertTrue(dsi.contains(null));
                    }
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

