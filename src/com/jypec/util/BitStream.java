package com.jypec.util;


/**
 * Interface for creating bitstreams for the coder
 * @author Daniel
 *
 */
public interface BitStream {
	/** Useful constants for this class */
	public enum BitStreamConstants {
		/** If the leftmost bit is the first to be processed, 
		 * either for reading the integer (reads from left to right) 
		 * or writing (writes left then shifts right) */
		ORDERING_LEFTMOST_FIRST, 
		/** If the rightmost bit is the first to be processed, 
		 * either for reading the integer (reads from right to left) 
		 * or writing (writes the rightmost bit then shifts left) */
		ORDERING_RIGHTMOST_FIRST
	}


	/**
	 * @return the next bit
	 */
	public Bit getBit();
	
	/**
	 * @return the next bit as an integer [0, 1]
	 */
	public int getBitAsInt();
	
	/**
	 * Get the given quantity of bits as an integer that packs them
	 * inserting from the MSB and shifting right or from the LSB
	 * and shifting left
	 * @param quantity
	 * @param ordering
	 * @return the specified number of bits as an integer
	 */
	public int getBits(int quantity, BitStreamConstants ordering);
	
	/**
	 * @return the last 32 bits read by calls to {@link #getBit()}, {@link #getBitAsInt()} and 
	 * {@link #getBits(int, BitStreamConstants)}. If less than 32 bits have been read in total, only 
	 * the bits read so far are returned, with zeroes filling the leftmost positions of the returned integer
	 */
	public int getLastReadBits();
	
	/**
	 * Puts a bit in the stream
	 * @param bit the bit to be put
	 */
	public void putBit(Bit bit);
	/**
	 * @param bit same as {@link #putBit(Bit)} but using an integer as input and converting to {@link Bit}
	 */
	public void putBit(int bit);
	
	/**
	 * Puts more than one bit if needed
	 * @param bits
	 * @param quantity
	 * @param ordering
	 */
	public void putBits(int bits, int quantity, BitStreamConstants ordering);
	
	/**
	 * @return the number of bits currently stored on this stream 
	 */
	public int getNumberOfBits();
	
	/**
	 * @return the size of this stream, given as the dimensions of
	 * the hypercube of bits that contains it
	 */
	public int[] getStreamDimensions();
	
	/**
	 * @return the contents of the stream in hex format. Zeros are appended if needed
	 */
	public String dumpHex();
	
}
