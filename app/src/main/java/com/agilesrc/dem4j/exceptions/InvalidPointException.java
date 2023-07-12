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
 * <p>Title:       InvalidPointException</p>
 * <p>Description: The InvalidPointException object is throw when
 * there is an point is requested outside the supporting data</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class InvalidPointException extends Exception {
    //=========================================================================
	// CONSTANTS
	//=========================================================================
    private static final long serialVersionUID = 8105523775742245883L;
	private static final String _MSG = "Invalid point";
	
	//=========================================================================
	// VARIABLES
	//=========================================================================

	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * 
	 */
	public InvalidPointException() {
		this(_MSG);
	}

	/**
	 * @param message
	 */
	public InvalidPointException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidPointException(final Throwable cause) {
		super(_MSG, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidPointException(final String message, final Throwable cause) {
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

