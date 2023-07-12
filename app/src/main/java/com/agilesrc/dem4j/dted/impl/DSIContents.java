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

import java.io.RandomAccessFile;
import java.util.EnumSet;

import org.threeten.bp.*;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.apache.commons.lang3.StringUtils;

import com.agilesrc.dem4j.SecurityEnum;
import com.agilesrc.dem4j.decoders.PointDecoder;
import com.agilesrc.dem4j.dted.DTEDLevelEnum;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.util.ByteArrayUtils;

/**
 * <p>The DISContents object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum DSIContents implements FileContentLayout {
    DSI(0, 3, "DSI") {
        @SuppressWarnings("unchecked")
        @Override
        public String value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return ByteArrayUtils.value(file, this, 
                    String.class);
        }
    }, 
    SECURITY_CODE(3, 1, "U") {
        @SuppressWarnings("unchecked")
        @Override
        public SecurityEnum value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            final String text = ByteArrayUtils.value(file, this, 
                    String.class);
            return SecurityEnum.getFromString(text);
        }
    },
    SECURITY_MARKINGS(4,2,null) {
        @SuppressWarnings("unchecked")
        @Override
        public String value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return ByteArrayUtils.value(file, this, 
                    String.class);
        }
    },
    SECURITY_HANDLING(6,27,null) {
        @SuppressWarnings("unchecked")
        @Override
        public String value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return ByteArrayUtils.value(file, this, 
                    String.class);
        }
    },
    FUTURE_USE1(33,26,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    PRODUCT_LEVEL(59,5,null) {
        @SuppressWarnings("unchecked")
        @Override
        public DTEDLevelEnum value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            final String result = ByteArrayUtils.value(file, this, 
                    String.class);
            return StringUtils.isBlank(result) ?
                    DTEDLevelEnum.DTED0 : DTEDLevelEnum.valueOf(result);
        }
    },
    UNIQUE_REFERENCE_NUMBER(64,15,null) {
        @SuppressWarnings("unchecked")
        @Override
        public String value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return ByteArrayUtils.value(file, this, 
                    String.class);
        }
    },
    FUTURE_USE2(79,8,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    DATA_EDITION(87,2,"00") {
        @SuppressWarnings("unchecked")
        @Override
        public Short value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return ByteArrayUtils.value(file, this, 
                    Short.class);
        }
    },
    MATCH_MERGE_VERSION(89,1,null) {
        @SuppressWarnings("unchecked")
        @Override
        public String value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return ByteArrayUtils.value(file, this, 
                    String.class);
        }
    },
    MAINTENANCE_DATE(90,4,null) {
        @SuppressWarnings("unchecked")
        @Override
        public YearMonth value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return yearMonth(file,this);
        }
    },
    MATCH_MERGE_DATE(94,4,null) {
        @SuppressWarnings("unchecked")
        @Override
        public YearMonth value(final RandomAccessFile file)     
            throws CorruptTerrainException {
            return yearMonth(file,this);
        }
    },
    MAINTENANCE_CODE(98,4,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    PRODUCER_CODE(102,8,null) {
        @SuppressWarnings("unchecked")
        @Override
        public String value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            return ByteArrayUtils.value(file, this, 
                    String.class);
        }
    },
    FUTURE_USE3(110,16,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    PRODUCT_SPECIFICATION(126,9,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    AMENDMENT_NUMBER_CHANGE_NUMBER(135,2,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    PRODUCT_SPECIFICATION_DATE(137,4,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    VERTICAL_DATUM(141,3,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    HORIZONATL_DATUM(144,5,"WGS84") {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    DIGITIZING_SYSTEM(149,10,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    COMPILATION_DATE(159,4,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    FUTURE_USE4(163,22,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    ORIGIN_LATITUDE(185,9,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    ORIGIN_LONGITUDE(194,10,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    SOUTH_WEST_LATITUDE(204,7,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    SOUTH_WEST_LONGITUDE(211,8,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    NORTH_WEST_LATITUDE(219,7,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    NORTH_WEST_LONGITUDE(226,8,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    NORTH_EAST_LATITUDE(234,7,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    NORTH_EAST_LONGITUDE(241,8,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    SOUTH_EAST_LATITUDE(249,7,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    SOUTH_EAST_LONGITUDE(256,8,null) {
        @SuppressWarnings("unchecked")
        @Override
        public Double value(final RandomAccessFile file) 
            throws CorruptTerrainException {
            byte[] value = ByteArrayUtils.value(file, this);
            
            return _DECODER.decode(value);
        }
    },
    ORIENTATION_ANGLE_TO_TRUE_NORTH(264,9,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    LATITUDE_INTERVAL(273,4,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    LONGITUDE_INTERVAL(277,4,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    NUMBER_OF_LATITUDE_LINES(281,4,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    NUMBER_OF_LONGITUDE_LINES(285,4,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    PARTIAL_CELL_INDICATOR(289,2,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    NIMA_USE(291,101,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    PRODUCING_NATION_USE(292,100,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    },
    FREE_TEXT(492,156,null) {
        @Override
        public <TYPE> TYPE value(final RandomAccessFile file) {
            
            return null;
        }
    };

    //=====================================================================
    // CONSTANTS
    //=====================================================================
    static PointDecoder _DECODER = new PointDecoder();
    
    public static final int LENGTH;

    static {
        int length = 0;
        for (DSIContents s : EnumSet.allOf(DSIContents.class)) {
            length += s.getLength();
        }

        LENGTH = length;
    }
    //=====================================================================
    // VARIABLES
    //=====================================================================
    private final int _startPosition;
    private final int _length;
    private final String _defaultValue;

    //=====================================================================
    // CONSTRUCTORS
    //=====================================================================
    /**
     * @param start
     * @param length
     * @param defaultValue
     */
    DSIContents(int start, int length, String defaultValue) {
        _startPosition = start;
        _length = length;
        _defaultValue = defaultValue;
    }

    //=====================================================================
    // PUBLIC METHODS
    //=====================================================================
    /**
     * return the appropriate value
     * 
     * @param <TYPE>
     * @param file
     * @return
     */
    public abstract <TYPE> TYPE value(final RandomAccessFile file)
        throws CorruptTerrainException;
    
    /*
     * (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getStartPosition()
     */
    public int getStartPosition() {
        return _startPosition + UHLContents.LENGTH;
    }

    /*
     * (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getLength()
     */
    public int getLength() {
        return _length;
    }

    /*
     * (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getDefaultValue()
     */
    public String getDefaultValue() {
        return _defaultValue;
    }
    
    //=====================================================================
    // PRIVATE METHODS
    //=====================================================================
    /**
     * get a Year/Month value
     * 
     * @param file
     * @param conts
     * @return
     */
    private static YearMonth yearMonth(final RandomAccessFile file, 
            final DSIContents conts) throws CorruptTerrainException {
        YearMonth result = null;
        final String value = ByteArrayUtils.value(file, conts, 
                String.class);
        
        if (StringUtils.isNotBlank(value)) {
        	DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
            builder.appendPattern("yyMM");
            
            DateTimeFormatter pattern = builder.toFormatter();
            if(value.equalsIgnoreCase("0000")) {
            	result = YearMonth.of(2000, 01);
            } else {
            	result = YearMonth.parse(value,pattern);
            }
            
            int currentYear = Year.now(ZoneId.of("UTC")).getValue();
            
            //if date is in the future, then adjust 
            //due to two dig. dates in data file
            if (result.getYear() > currentYear) {
                result = result.minusYears(100);
            }
            
        }
        
        return result;
    }
}

