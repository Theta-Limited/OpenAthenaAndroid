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

import com.agilesrc.dem4j.Resolution;


/**
 * <p>Title:       DTEDLevelEnum</p>
 * <p>Description: The DTEDLevelEnum object is the key something,
 *                 doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum DTEDLevelEnum implements Resolution {
	DTED0(121,121,30.0),
	DTED1(1201,1201,3.0),
	DTED2(3601,3601,1.0),
	DTED3(900,900,1.0/3);
	
	//=========================================================================
	// CONSTANTS
	//=========================================================================

	//=========================================================================
	// VARIABLES
	//=========================================================================
	private int _rows;
	private int _columns;
	private double _spacing;
	
	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * @param rows
	 * @param columns
	 * @param spacing - in seconds
	 */
	DTEDLevelEnum(final int rows, final int columns, final double spacing) {
		_rows = rows;
		_columns = columns;
		_spacing = spacing/3600;
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

