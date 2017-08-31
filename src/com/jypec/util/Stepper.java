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

}
