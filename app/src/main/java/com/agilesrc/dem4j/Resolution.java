/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j;

/**
 * <p>The Resolution interface defines data resolution
 * characteristics</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public interface Resolution {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * Number of rows in the data set
     * 
     * @return
     */
    public int getRows();
    
    /**
     * Number of columns in the data set
     * 
     * @return
     */
    public int getColumns();
    
    /**
     * @return spacing in degrees
     */
    public double getSpacing();
}

