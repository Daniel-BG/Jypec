package com.jypec.util.bits;

/** Useful constants for bit manipulation */
public enum BitStreamConstants {
	/** If the leftmost bit is the first to be processed, 
	 * either for reading the integer (reads from left to right) 
	 * or writing (writes left then shifts right) */
	ORDERING_LEFTMOST_FIRST, 
	/** If the rightmost bit is the first to be processed, 
	 * either for reading the integer (reads from right to left) 
	 * or writing (writes the rightmost bit then shifts left) */
	ORDERING_RIGHTMOST_FIRST;
	
	/** Mask to extract leftmost bit of a byte */
	public static final int BYTE_LEFT_BIT_MASK = 0x80;
	/** Mask to extract rightmost bit of a byte */
	public static final int BYTE_RIGHT_BIT_MASK = 0x1;
	
	/** Mask to extract rightmost bit of an int */
	public static final int INT_LEFT_BIT_MASK = 0x1 << (Integer.SIZE - 1);
	/** Mask to extract rightmost bit of an int */
	public static final int INT_RIGHT_BIT_MASK = 0x1;
	
	/** Mask to extract rightmost bit of a long */
	public static final long LONG_LEFT_BIT_MASK = 0x1l << (Long.SIZE - 1);
	/** Mask to extract rightmost bit of a long */
	public static final long LONG_RIGHT_BIT_MASK = 0x1l;
}