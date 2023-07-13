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
 * <p>Title:       ComputableAreaException</p>
 * <p>Description: The ComputableAreaException object is throw when
 * a point is outside the possible computation area</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Adam Nutt
 */
public class ComputableAreaException extends Exception {
	 /**
	 * 
	 */
	
		//=========================================================================
		// CONSTANTS
		//=========================================================================
//	    private static final long serialVersionUID = 5163628127654745095L;
		private static final long serialVersionUID = -6555784494257497877L;
		private static final String _MSG = "Outside computable boundaries";
		
		//=========================================================================
		// VARIABLES
		//=========================================================================

		//=========================================================================
		// CONSTRUCTORS
		//=========================================================================
		/**
		 * 
		 */
		public ComputableAreaException() {
			this(_MSG);
		}

		/**
		 * @param message
		 */
		public ComputableAreaException(final String message) {
			super(message);
		}

		/**
		 * @param cause
		 */
		public ComputableAreaException(final Throwable cause) {
			super(_MSG, cause);
		}

		/**
		 * @param message
		 * @param cause
		 */
		public ComputableAreaException(final String message, final Throwable cause) {
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
