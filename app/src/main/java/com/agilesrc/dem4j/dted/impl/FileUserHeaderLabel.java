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

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.builder.StandardToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.SecurityEnum;
import com.agilesrc.dem4j.decoders.PointDecoder;
import com.agilesrc.dem4j.dted.MultipleAccuracyEnum;
import com.agilesrc.dem4j.dted.UserHeaderLabel;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;
import com.agilesrc.dem4j.util.ByteArrayUtils;

/**
 * <p>
 * The FileUserHeaderLabel object is the key something, doing something
 * </p>
 * <p>
 * Organization: AgileSrc LLC (www.agilesrc.com)
 * </p>
 * 
 * @author Mark Horn
 */
public class FileUserHeaderLabel implements UserHeaderLabel {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private final RandomAccessFile _file;

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param file
     */
    FileUserHeaderLabel(final RandomAccessFile file) {
        _file = file;
    }

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * total length of UHL part of the DTED file
     * 
     * @return
     */
    public int getLength() {
        return UHLContents.LENGTH;
    }
    
    /**
     * @return
     */
    public String dump() {
        String result = "ERROR";
        try {
            byte[] bytes = new byte[UHLContents.LENGTH];
            _file.read(bytes, 0, UHLContents.LENGTH);
            result = new String(bytes);
        } catch (IOException e) {
            result = e.getMessage();
        }
        
        return result;
    }

    /**
     * @return
     * @throws CorruptTerrainException
     */
    public String getReserved() throws CorruptTerrainException {
        return ByteArrayUtils.value(_file, UHLContents.RESERVED, String.class);
    }

    /**
     * gets the fixed '1' by standard
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public short getOne() throws CorruptTerrainException {
        return ByteArrayUtils.value(_file, UHLContents.ONE, Short.class)
                .shortValue();
    }

    /**
     * @return
     * @throws CorruptTerrainException
     */
    public String getUHL() throws CorruptTerrainException {
        return ByteArrayUtils.value(_file, UHLContents.UHL, String.class);
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getAccuracy()
     */
    public short getVerticalAccuracy() throws CorruptTerrainException {
        UHLContents location = UHLContents.VERTICAL_ACCURACY;
        return ByteArrayUtils.value(_file, location, Short.class).shortValue();
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getClassification()
     */
    public SecurityEnum getClassification() throws CorruptTerrainException {
        UHLContents location = UHLContents.SECURITY_CODE;
        String code = ByteArrayUtils.value(_file, location, String.class);
        return SecurityEnum.getFromString(code);
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getColumns()
     */
    public int getColumns() throws CorruptTerrainException {
        return ByteArrayUtils.value(_file, UHLContents.NUMBER_LONGITUDE_LINES,
                Integer.class).intValue();
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getLatitudeInterval()
     */
    public double getLatitudeInterval() throws CorruptTerrainException {
        double result = (double) ByteArrayUtils.value(_file,
                UHLContents.LATITUDE_INTERVAL, Integer.class);

        //change to degrees
        result = result / (10 * 3600);

        return result;
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getLongitudeInterval()
     */
    public double getLongitudeInterval() throws CorruptTerrainException {
        double result = (double) ByteArrayUtils.value(_file,
                UHLContents.LONGITUDE_INTERVAL, Integer.class);

        //change to degrees
        result = result / (10 * 3600);

        return result;
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getMultipleAccuracy()
     */
    public MultipleAccuracyEnum getMultipleAccuracy()
            throws CorruptTerrainException {
        String result = ByteArrayUtils.value(_file,
                UHLContents.MULTIUPLE_ACCURACY, String.class);
        return MultipleAccuracyEnum.getFromString(result);
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getOrigin()
     */
    public Point getOrigin() throws CorruptTerrainException {
        PointDecoder decoder = new PointDecoder();

        byte[] lat = ByteArrayUtils.value(_file, UHLContents.ORIGIN_LATITUDE);
        byte[] lon = ByteArrayUtils.value(_file, UHLContents.ORIGIN_LONGITUDE);

        return new Point(decoder.decode(lat), decoder.decode(lon));
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getRows()
     */
    public int getRows() throws CorruptTerrainException {
        return ByteArrayUtils.value(_file, UHLContents.NUMBER_LATITUDE_LINES,
                Integer.class).intValue();
    }

    /*
     * (non-Javadoc)
     * @see com.agilsrc.dted4j.UserHeaderLabel#getUniqueReferenceNumber()
     */
    public String getUniqueReferenceNumber() throws CorruptTerrainException {
        return ByteArrayUtils.value(_file, UHLContents.UNIQUE_REFERENCE,
                String.class);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this,
                StandardToStringStyle.SHORT_PREFIX_STYLE);
        try {
            builder.append("referenceNumber", getUniqueReferenceNumber());
            builder.append("accuracy", getVerticalAccuracy());
            builder.append("multipleAccuracy",getMultipleAccuracy());
            builder.append("classification", getClassification());
            builder.append("origin", getOrigin());
            builder.append("rows", getRows());
            builder.append("columns", getColumns());
            builder.append("latitudeInterval", getLatitudeInterval());
            builder.append("longitudeInterval", getLongitudeInterval());
            builder.append("reserved", getReserved());
        } catch (CorruptTerrainException e) {
            builder.append("error");
        }

        return builder.toString();
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
