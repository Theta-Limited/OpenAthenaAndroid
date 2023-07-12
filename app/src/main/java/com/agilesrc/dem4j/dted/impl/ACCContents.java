/*
 *  Copyright 2001 AgileSrc LLC
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 */
package com.agilesrc.dem4j.dted.impl;

/**
 * <p>The ACCContents object is used to decode
 * the Accuracy Descriptor Record (ACC) part
 * of a DTED file.  
 * TODO: currently a stub
 * </p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum ACCContents implements FileContentLayout {
    ;
    //=========================================================================
    // CONSTANTS
    //=========================================================================
    public static final int LENGTH = 2700;
    
    //=========================================================================
    // VARIABLES
    //=========================================================================

    //=========================================================================
    // CONSTRUCTORS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getStartPosition()
     */
    public int getStartPosition() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getLength()
     */
    public int getLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.agilesrc.dted4j.impl.FileContentLayout#getDefaultValue()
     */
    public String getDefaultValue() {
        // TODO Auto-generated method stub
        return null;
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

