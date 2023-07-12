/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.dted;

import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.SecurityEnum;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>
 * The UserHeaderLabel represents the data in the UHL part of a DTED file.
 * </p>
 * <p>
 * Organization: AgileSrc LLC (www.agilesrc.com)
 * </p>
 * 
 * @author Mark Horn
 */
public interface UserHeaderLabel {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    public static final short VERTICAL_ACCURACY_NA = -1;

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * @return - point in degrees
     */
    public Point getOrigin() throws CorruptTerrainException;

    /**
     * @return - interval in degrees
     */
    public double getLongitudeInterval() throws CorruptTerrainException;

    /**
     * @return - interval in degrees
     */
    public double getLatitudeInterval() throws CorruptTerrainException;

    /**
     * 0000-9999 or NA. NA returned as -1
     * 
     * @return - in meters
     */
    public short getVerticalAccuracy() throws CorruptTerrainException;

    /**
     * @return
     */
    public SecurityEnum getClassification() throws CorruptTerrainException;

    /**
     * number of latitude points
     * 
     * @return
     */
    public int getRows() throws CorruptTerrainException;

    /**
     * number of longitude points
     * 
     * @return
     */
    public int getColumns() throws CorruptTerrainException;

    /**
     * Unique reference number. (For producing nations own use (free text or
     * zero filled)).
     * 
     * @return
     */
    public String getUniqueReferenceNumber() throws CorruptTerrainException;

    /**
     * @return
     */
    public MultipleAccuracyEnum getMultipleAccuracy()
            throws CorruptTerrainException;

    /**
     * Get the reserved section of the UHL
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public String getReserved() throws CorruptTerrainException;
    
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
