/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.srtm;

import java.io.File;

import com.agilesrc.dem4j.Resolution;


/**
 * <p>Title:       SRTMLevelEnum</p>
 * <p>Description: The SRTMLevelEnum is a enumerator that
 * defines what SRTM resolutions are supported</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum SRTMLevelEnum implements Resolution {
    SRTM3(3.0),
    SRTM1(1.0);
	//=========================================================================
	// CONSTANTS
	//=========================================================================

	//=========================================================================
	// VARIABLES
	//=========================================================================
	private int _rows;
	private int _columns;
	
	//in degrees
	private double _spacing;
	
	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * @param rows
	 * @param columns
	 * @param spacing - in seconds
	 */
	SRTMLevelEnum(final double spacing) {
		_spacing = spacing/3600.0;
		_rows = (int)(1.0/_spacing + _spacing*2) + 1;
		_columns = (int)(1.0/_spacing + _spacing*2) + 1;
		_spacing = spacing/3600.0;
	}
	
	//=========================================================================
	// PUBLIC METHODS
	//=========================================================================
	/* (non-Javadoc)
	 * @see com.agilesrc.dted4j.Resolution#getRows()
	 */
	public int getRows() {
		return _rows;
	}
	
	/**
	 * @return
	 */
	/* (non-Javadoc)
	 * @see com.agilesrc.dted4j.Resolution#getColumns()
	 */
	public int getColumns() {
		return _columns;
	}
	
	/* (non-Javadoc)
	 * @see com.agilesrc.dted4j.Resolution#getSpacing()
	 */
	public double getSpacing() {
		return _spacing;
	}
	
	/**
	 * @param file
	 * @return
	 */
	public static SRTMLevelEnum fromFile(final File file) {
		SRTMLevelEnum result = null;
		
		long length = file.length();
		if (length > 25000000) {
			result = SRTMLevelEnum.SRTM1;
		} else {
			result = SRTMLevelEnum.SRTM3;
		}
		
		return result;
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

