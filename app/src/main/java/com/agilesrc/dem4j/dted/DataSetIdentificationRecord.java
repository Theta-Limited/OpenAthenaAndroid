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

import org.threeten.bp.YearMonth;

import com.agilesrc.dem4j.Point;
import com.agilesrc.dem4j.SecurityEnum;
import com.agilesrc.dem4j.exceptions.CorruptTerrainException;

/**
 * <p>Title:       DataSetIdentificationRecord</p>
 * <p>Description: The DataSetIdentificationRecord represents the data in 
 * the DSI part of a DTED file.</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public interface DataSetIdentificationRecord {
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
     * @see{com.agilesrc.dted4j.FIPS10_4CodeEnum} for codes
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public FIPS10_4CodeEnum getProducerCode() throws CorruptTerrainException;
    
	/**
	 * Security Classification Code
	 * 
	 * <ul>
	 *   <li>S - Secret</li>
	 *   <li>C - Confidential</li>
	 *   <li>U - Unclassified</li>
	 *   <li>R - Restricted<li>
	 * </ul>
	 * 
	 * @return
	 * @throws CorruptTerrainException
	 */
	public SecurityEnum getClassification() throws CorruptTerrainException;
	
	/**
	 * Security Control and Release Markings. For DoD use only.
	 * 
	 * @return
	 */
	public String getSecurityControlMarkings() throws CorruptTerrainException;
	
	/**
	 * Security Handling Description. Other security description. (Free text or blank filled).
	 * 
	 * @return
	 */
	public String getSecurityHandling() throws CorruptTerrainException;
	
	/**
	 * NIMA Series Designator for product level.
	 * 
	 * <ul>
	 *   <li>DTED0 - DTED Level 0</li>
	 *   <li>DTED1 - DTED Level 1</li>
	 *   <li>DTED2 - DTED Level 2</li>
	 * </ul>
	 * @return
	 */
	public DTEDLevelEnum getDTEDLevel() throws CorruptTerrainException;
	
	/**
	 * Unique reference number. (For producing nations own use (free text or zero filled)).
	 * 
	 * @return
	 * @throws CorruptTerrainException
	 */
	public String getUniqueReferenceNumber() throws CorruptTerrainException;
	
	/**
	 * Data Edition Number 01-99
	 * 
	 * @return
	 */
	public short getDataEditionNumber() throws CorruptTerrainException;
	
	/**
	 * Match/Merge Version A-Z
	 * 
	 * @return
	 */
	public char getMatchMergeVersion() throws CorruptTerrainException;
	
	/**
	 * Maintenance Date
	 * 
	 * @return
	 * @throws CorruptTerrainException
	 */
	public YearMonth getMaintenanceDate() throws CorruptTerrainException;
	
	/**
	 * Match/Merge Date
	 * 
	 * @return
	 * @throws CorruptTerrainException
	 */
	public YearMonth getMatchMergeDate() throws CorruptTerrainException;
	
	/**
	 * get the north west corner point of the data
	 * 
	 * @return
	 * @throws CorruptTerrainException
	 */
	public Point getNorthWestCorner() throws CorruptTerrainException;
	
	/**
     * get the south west corner point of the data
	 * 
     * @return
     * @throws CorruptTerrainException
     */
    public Point getSouthWestCorner() throws CorruptTerrainException;
    
    /**
     * get the north east corner point of the data
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public Point getNorthEastCorner() throws CorruptTerrainException;
    
    /**
     * get the south east corner point of the data
     * 
     * @return
     * @throws CorruptTerrainException
     */
    public Point getSouthEastCorner() throws CorruptTerrainException;
    
    /**
     * @param point
     * @return
     */
    public boolean contains(final Point point);
    
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

