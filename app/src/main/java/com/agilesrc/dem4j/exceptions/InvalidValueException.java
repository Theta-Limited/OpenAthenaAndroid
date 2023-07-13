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
 * <p>Title:       InvalidValueException</p>
 * <p>Description: The InvalidValueException object is throw when
 * there is an invalid value of some kind</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public class InvalidValueException extends Exception {
	//=========================================================================
	// CONSTANTS
	//=========================================================================
	private static final long serialVersionUID = 2486785950866686081L;
	private static final String _MSG = "Invalid value";
	
	//=========================================================================
	// VARIABLES
	//=========================================================================

	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * 
	 */
	public InvalidValueException() {
		this(_MSG);
	}

	/**
	 * @param message
	 */
	public InvalidValueException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidValueException(final Throwable cause) {
		super(_MSG, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidValueException(final String message, final Throwable cause) {
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

