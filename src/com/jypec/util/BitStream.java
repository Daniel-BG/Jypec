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
	public int getBitAsInt();
	
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
