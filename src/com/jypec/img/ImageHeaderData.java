package com.jypec.img;

import java.util.EnumMap;

/**
 * Stores image header data
 * @author Daniel
 */
public class ImageHeaderData extends EnumMap<HeaderConstants, Object> {
	private static final long serialVersionUID = 2630007419932543631L;
	
	/**
	 * Create a new header data
	 */
	public ImageHeaderData() {
		super(HeaderConstants.class);
	}
	
}
