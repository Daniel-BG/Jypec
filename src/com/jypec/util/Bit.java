package com.jypec.util;

/**
 * Enum class for making it easy to work with bits and transform other types to/from them
 * @author Daniel
 *
 */
public enum Bit {
	BIT_ZERO, BIT_ONE;
	
	/**
	 * Converts the bit to a one or a zero in integer form
	 * @return
	 */
	public int toInteger() {
		return this == BIT_ZERO ? 0 : 1;
	}
	
	/**
	 * Creates a Bit class object from an integer (if zero then BIT_ZERO, else BIT_ONE)
	 * @param integer
	 * @return
	 */
	public static Bit fromInteger(int integer) {
		return integer == 0 ? BIT_ZERO : BIT_ONE;
	}
	
	/**
	 * Normalizes an integer to a bit. That is, if integer == 0 return 0, else return 1.
	 * @param integer
	 * @return
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
	 * Creates a bit from a boolean. one if true, zero is false
	 * @param isPositive
	 * @return
	 */
	public static Bit fromBoolean(boolean isNegative) {
		return isNegative ? BIT_ONE : BIT_ZERO;
	}
}
