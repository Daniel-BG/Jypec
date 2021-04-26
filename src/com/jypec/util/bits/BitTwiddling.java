package com.jypec.util.bits;

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
	
	
	/**
	 * @param number
	 * @return true if number is a positive power of two
	 */
	public static boolean powerOfTwo(int number) {
		return (number > 0) && ((number & (number - 1)) == 0);
	}
	
	/**
	 * Calculate the number of bits needed to store the given number, which is assumed unsigned.
	 * This function is not efficient so change it if you plan on intensive calling
	 * @param number
	 * @return the number of bits needed to represent the given number
	 */
	public static int bitsOf(int number) {
		int result = 0;
		while (number != 0) {
			result++;
			number >>>= 1;
		}
		return result;
	}
	
	/**
	 * @param number
	 * @return the number of trailing zeros
	 */
	public static int trailingZerosOf(int number) {
		int result = 0;
		for (int i = 0; i < 32; i++) {
			if ((number & 0x1) == 0) {
				result ++;
			} else {
				break;
			}
			number >>= 1;
		}
		return result;
	}
	
	/**
	 * @param number
	 * @return the number of trailing ones
	 */
	public static int trailingOnesOf(int number) {
		int result = 0;
		for (int i = 0; i < 32; i++) {
			if ((number & 0x1) == 1) {
				result ++;
			} else {
				break;
			}
			number >>= 1;
		}
		return result;
	}
	
	/**
	 * Reverses the least 'quant' significant bits in 'bits'
	 * @param bits
	 * @param quant
	 * @return
	 */
	public static int reverseBits(int bits, int quant) {
		int b = 0;
		while (quant --> 0) {
			b <<= 1;
			b |= (bits & 0x1);
			bits >>>= 1;
		}
		return b;
	}
	
}
