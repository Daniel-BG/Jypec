package com.jypec.util.debug;

/**
 * @author Daniel
 * Facilitates the printing of arrays for when debugging
 */
public class ArrayPrinter {

	/**
	 * @param array
	 * @return the string representing the given array
	 */
	public static String printfloatArray(float[] array) {
		String res = "(";
		for (int i = 0; i < array.length - 1; i++) 
			res += String.format("%6.3e",array[i]) + ", ";
		res += String.format("%6.3e", array[array.length - 1]) + ")";
		return res;
	}
}
