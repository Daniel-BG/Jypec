package com.jypec.util.bits;

/**
 * Enum class for making it easy to work with bits and transform other types to/from them
 * @author Daniel
 *
 */
public enum Bit {
	/** A constant representing the ZERO bit */
	BIT_ZERO, 
	/** A constant representing the ONE bit */ 
	BIT_ONE;
	
	/**
	 * @return this bit as an integer (0 or 1)
	 */
	public int toInteger() {
		return this == BIT_ZERO ? 0 : 1;
	}
	
	/** 
	 * @param integer
	 * @return a Bit class object from an integer (if zero then BIT_ZERO, else BIT_ONE)
	 */
	public static Bit fromInteger(int integer) {
		return integer == 0 ? BIT_ZERO : BIT_ONE;
	}
	
	/**
	 * @param integer
	 * @return if integer == 0 return 0, else return 1.
	 */
	public static int normalize(int integer) {
		return integer == 0 ? 0 : 1;
	}
	
	/**
	 * @return The inverse bit of this one
	 */
	public Bit getInverse() {
		if (this == BIT_ZERO)
			return BIT_ONE;
		else
			return BIT_ZERO;
	}

	/**
	 * Creates a bit from a boolean 
	 * @param value 
	 * @return one if true, zero is false
	 */
	public static Bit fromBoolean(boolean value) {
		return value ? BIT_ONE : BIT_ZERO;
	}

	/**
	 * @return true if this bit is a {@link #BIT_ONE}
	 */
	public boolean toBoolean() {
		return this == BIT_ONE;
	}
}
