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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Title:       MultipleAccuracyEnum</p>
 * <p>Description: The MultipleAccuracyEnum object represents
 * the DTED accuracy level</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum MultipleAccuracyEnum {
	SINGLE(0),
	MULTIPLE(1);
	
	//=========================================================================
	// CONSTANTS
	//=========================================================================
    private static final Map<Integer,MultipleAccuracyEnum> _LOOKUP 
		= new HashMap<Integer,MultipleAccuracyEnum>();

	static {
	    for(MultipleAccuracyEnum s : EnumSet.allOf(MultipleAccuracyEnum.class)) {
	         _LOOKUP.put(Integer.valueOf(s.getValue()), s);
	    }
	}

	//=========================================================================
	// VARIABLES
	//=========================================================================
	private int _value;
	
	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * @param value
	 */
	MultipleAccuracyEnum(final int value) {
		_value = value;
	}
	
	//=========================================================================
	// PUBLIC METHODS
	//=========================================================================
	/**
	 * @return
	 */
	public int getValue() {
		return _value;
	}
	
	/**
	 * @param hemisphere
	 * @return
	 */
	public static MultipleAccuracyEnum getFromString(final String value) {
		MultipleAccuracyEnum result = null;
		
		if (StringUtils.isNotEmpty(value)) {
			result = _LOOKUP.get(Integer.parseInt(value));
		} 
		
		return result;
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

