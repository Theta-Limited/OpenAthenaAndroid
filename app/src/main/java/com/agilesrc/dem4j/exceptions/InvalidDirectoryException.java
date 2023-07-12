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
 * <p>The InvalidDirectoryException is thrown when a directory does not exist or other directory error</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Adam Nutt
 */
public class InvalidDirectoryException extends Exception {
    //=============================================================================================
    // CONSTANTS
    //=============================================================================================   
    private static final long serialVersionUID = 3206659771811426210L;

    //=============================================================================================
    // VARIABLES
    //=============================================================================================

    //=============================================================================================
    // CONSTRUCTORS
    //=============================================================================================

    //=============================================================================================
    // PUBLIC METHODS
    //=============================================================================================
    /**
     * 
     */
    public InvalidDirectoryException() {
    }

    /**
     * @param message
     */
    public InvalidDirectoryException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvalidDirectoryException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public InvalidDirectoryException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    //=============================================================================================
    // DEFAULT METHODS
    //=============================================================================================

    //=============================================================================================
    // PROTECTED METHODS
    //=============================================================================================

    //=============================================================================================
    // PRIVATE METHODS
    //=============================================================================================

    //=============================================================================================
    // INNER CLASSES
    //=============================================================================================

}

