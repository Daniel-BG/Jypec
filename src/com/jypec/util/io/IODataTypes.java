package com.jypec.util.io;

/**
 * Store a couple data types for mainly image I/O
 * @author Daniel
 *
 */
public class IODataTypes {
	/**
	 * Defines the order of the image samples in the image file
	 * @author Daniel
	 */
	public enum ImageOrdering {
		/** Band Sequential. Band -> Line -> Sample */
		BSQ, 
		/** Band Interleaved by Pixel. Line -> Sample -> Band */
		BIP, 
		/** Band Interleaved by Line. Line -> Band -> Sample */
		BIL
	};
	
	/**
	 * Ordering of the bytes in the image file. 
	 * Only applies to byte-width data types
	 * @author Daniel
	 *
	 */
	public enum ByteOrdering {
		/** Big endian. Start with the most significant byte in the lowest memory address */
		BIG_ENDIAN, 
		/** Little endian. Start with the least significant byte in the lowest memory address */
		LITTLE_ENDIAN
	};
}
