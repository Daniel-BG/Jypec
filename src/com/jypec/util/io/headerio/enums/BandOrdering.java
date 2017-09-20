package com.jypec.util.io.headerio.enums;

/**
 * Defines the order of the image samples in the image file
 * @author Daniel
 */
public enum BandOrdering {
	/** Band Sequential. Band -> Line -> Sample */
	BSQ, 
	/** Band Interleaved by Pixel. Line -> Sample -> Band */
	BIP, 
	/** Band Interleaved by Line. Line -> Band -> Sample */
	BIL
};
