/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.impl;

import com.agilesrc.dem4j.Resolution;

/**
 * <p>The ResolutionImpl basic implementation for a Resolution</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Stephen Aument
 */
public class ResolutionImpl implements Resolution {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // VARIABLES
    //=========================================================================
    private int _rows;
    private int _columns;
    private double _resolution;
    
    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================
    /**
     * @param rows
     * @param cols
     * @param resolution
     */
    public ResolutionImpl(int rows, int cols, double resolution){
        _rows = rows;
        _columns = cols;
        _resolution = resolution;
        
    }
    
    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /* (non-Javadoc)
     * @see com.agilesrc.dem4j.Resolution#getRows()
     */
    public int getRows(){
        return _rows;
    }
    
    /* (non-Javadoc)
     * @see com.agilesrc.dem4j.Resolution#getColumns()
     */
    public int getColumns(){
        return _columns;
    }
    
    /* (non-Javadoc)
     * @see com.agilesrc.dem4j.Resolution#getSpacing()
     */
    public double getSpacing(){
        return _resolution;
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

