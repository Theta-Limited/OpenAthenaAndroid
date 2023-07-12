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
 * <p>Title:       SecurityEnum</p>
 * <p>Description: The SecurityEnum represents the 
 * available security levels in DTED files
 * <ul>
 *   <li>S - Secret</li>
 *   <li>C - Confidential</li>
 *   <li>U - Unclassified</li>
 *   <li>R - Restricted<li>
 * </ul>
 * </p>
 *
 * <p>Organization: AgileSrc LLC (www.agilesrc.com)</p>
 * @author  Mark Horn
 */
public enum SecurityEnum {
	UNCLASSIFIED("U","Unclassified"),
	CONFIDENTAL("C","Confidental"),
	SECRET("S","Secret"),
	RESTRICTED("R","Restricted");
	
	//=========================================================================
	// CONSTANTS
	//=========================================================================
    private static final Map<String,SecurityEnum> _LOOKUP 
		= new HashMap<String,SecurityEnum>();

	static {
	    for(SecurityEnum s : EnumSet.allOf(SecurityEnum.class)) {
	         _LOOKUP.put(s.getCode(), s);
	    }
	}

	//=========================================================================
	// VARIABLES
	//=========================================================================
	private String _code = null;
	private String _label = null;
	
	//=========================================================================
	// CONSTRUCTORS
	//=========================================================================
	/**
	 * @param label
	 */
	SecurityEnum(final String code, final String label) {
		_code = code;
		_label = label;
	}
	
	//=========================================================================
	// PUBLIC METHODS
	//=========================================================================
	/**
	 * @return
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * @return
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * @param hemisphere
	 * @return
	 */
	public static SecurityEnum getFromString(final String code) {
		SecurityEnum result = null;

		if (code != null && code.length() != 0) {
			char value = code.charAt(0);
			result = _LOOKUP.get(new Character(value).toString());
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

