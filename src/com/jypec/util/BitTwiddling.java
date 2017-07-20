package com.jypec.util;

/**
 * Bitwise operations go here
 * @author Daniel
 *
 */
public class BitTwiddling {

	/**
	 * @param input
	 * @param mask
	 * @param shift
	 * @return the input and mask ANDed together then arithmetically shifted to the right
	 */
	public static int maskAndShift(int input, int mask, int shift) {
		return (input & mask) >> shift;
	}
	
}
