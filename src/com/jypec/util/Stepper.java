package com.jypec.util;

/**
 * @author Daniel
 */
public class Stepper {
	
	/**
	 * @param size
	 * @param steps
	 * @return a list of the sizes resulting from halving an inteval of size "size" 
	 * a number of steps "steps", ordered from bigger (equal to "size") to smaller,
	 * for a total of steps + 1 integers
	 */
	public static int[] getStepSizes(int size, int steps) {
		int[] res = new int[steps + 1];
		for (int i = 0; i <= steps; i++) {
			res[i] = size;
			size += 1;
			size /= 2;
		}
		return res;
	}
	
	
	/**
	 * Same as {@link #getStepSizes(int, int)} but returns
	 * the list in the reverse order
	 * @param size
	 * @param steps
	 * @return a list of steps in increasing value
	 */
	public static int[] getReverseStepSizes(int size, int steps) {
		int[] res = getStepSizes(size, steps);
		int[] fixed = new int[res.length];
		for (int i = 0; i < fixed.length; i++) {
			fixed[i] = res[res.length - 1 - i];
		}
		return fixed;
	}

}
