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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title:       HemisphereEnum</p>
 * <p>Description: The HemisphereEnum object is to identify what
 * hemisphere data is in</p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum HemisphereEnum {
	NORTH('N',1),
	SOUTH('S',-1),
	WEST('W',-1),
	EAST('E',1);
	//=========================================================================
	// CONSTANTS
	//=========================================================================
    private static final Map<Character,HemisphereEnum> _LOOKUP 
		= new HashMap<Character,HemisphereEnum>();

	static {
	    for(HemisphereEnum s : EnumSet.allOf(HemisphereEnum.class)) {
	         _LOOKUP.put(s.getHemisphere(), s);
	    }
	}
	
	//=========================================================================
	// VARIABLES
	//=========================================================================
	private final char _hemisphere;
	private final int _multiplier;
	
	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * @param hemisphere
	 * @param multiplier
	 */
	HemisphereEnum(final char hemisphere, final int multiplier) {
		_hemisphere = hemisphere;
		_multiplier = multiplier;
	}

	//=========================================================================
	// PUBLIC METHODS
	//=========================================================================
	/**
	 * @return the hemisphere
	 */
	public char getHemisphere() {
		return _hemisphere;
	}


	/**
	 * @return the multiplier
	 */
	public int getMultiplier() {
		return _multiplier;
	}
	
	/**
	 * @param hemisphere
	 * @return
	 */
	public static HemisphereEnum getFromString(final String hemisphere) {
		HemisphereEnum result = null;
		
		if (hemisphere != null && hemisphere.length() != 0) {
			char value = hemisphere.charAt(0);
			result = _LOOKUP.get(value);
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

