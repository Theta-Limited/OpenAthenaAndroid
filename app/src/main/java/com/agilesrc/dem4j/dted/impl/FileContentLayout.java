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
 * <p>The FileContentLayout object is the key something,
 * doing something</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  hornm
 */
public interface FileContentLayout {
    //=========================================================================
    // CONSTANTS
    //=========================================================================

    //=========================================================================
    // PUBLIC METHODS
    //=========================================================================
    /**
     * @return the startPosition
     */
    public int getStartPosition();

    /**
     * @return the length
     */
    public int getLength();

    /**
     * @return the defaultValue
     */
    public String getDefaultValue();
}

