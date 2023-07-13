/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.exceptions;

/**
 * <p>Title:       CorruptDTEDFileException</p>
 * <p>Description: The CorruptDTEDFileException object is throw when
 * a DTED file seems to be corrupted</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class CorruptTerrainException extends Exception {
    //=========================================================================
	// CONSTANTS
	//=========================================================================
    private static final long serialVersionUID = 5163628127654745095L;
	private static final String _MSG = "currupt file";
	
	//=========================================================================
	// VARIABLES
	//=========================================================================

	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * 
	 */
	public CorruptTerrainException() {
		this(_MSG);
	}

	/**
	 * @param message
	 */
	public CorruptTerrainException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public CorruptTerrainException(final Throwable cause) {
		super(_MSG, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CorruptTerrainException(final String message, final Throwable cause) {
		super(message, cause);
	}
	//=========================================================================
	// PUBLIC METHODS
	//=========================================================================

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

