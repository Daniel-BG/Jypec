package com.jypec.img;

import java.util.EnumMap;

/**
 * Stores image header data
 * @author Daniel
 */
public class ImageHeaderData extends EnumMap<HeaderConstants, Object> {
	private static final long serialVersionUID = 2630007419932543631L;
	
	private boolean wasCompressed = false;
	
	/**
	 * Create a new header data
	 */
	public ImageHeaderData() {
		super(HeaderConstants.class);
	}

	/**
	 * @return true if this header was compressed in its input file
	 */
	public boolean wasCompressed() {
		return wasCompressed;
	}

	/**
	 * @param wasCompressed pass as true to indicate that this header was compressed 
	 * in the input file
	 */
	public void setWasCompressed(boolean wasCompressed) {
		this.wasCompressed = wasCompressed;
	}
	
	
	
}
