package com.jypec.util;


/**
 * Interface for creating bitstreams for the coder
 * @author Daniel
 *
 */
public interface BitStream {
	//CONSTANTS
	public enum BitStreamConstants {
		ORDERING_LEFTMOST_FIRST, ORDERING_RIGHTMOST_FIRST
	}

	/**
	 * Gets a bit from the stream
	 * @return
	 * @throws EmptyStreamException
	 */
	public Bit getBit();
	
	/**
	 * Same as getBit but returning an integer in the interval [0, 1]
	 * @return
	 */
	public int getBitAsInt();
	
	/**
	 * Get the given quantity of bits as an integer that packs them
	 * inserting from the MSB and shifting right or from the LSB
	 * and shifting left
	 * @param quantity
	 * @param ordering
	 * @return
	 */
	public int getBits(int quantity, BitStreamConstants ordering);
	
	/**
	 * Puts a bit in the stream
	 */
	public void putBit(Bit bit);
	public void putBit(int bit);
	
	/**
	 * Puts more than one bit if needed
	 * @param bits
	 * @param quantity
	 * @param ordering
	 */
	public void putBits(int bits, int quantity, BitStreamConstants ordering);
	
	/**
	 * Gets the number of bits currently stored on this stream
	 * @return
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
