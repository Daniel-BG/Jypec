package com.jypec.util.arrays;

/**
 * Some operations over matrices
 * @author Daniel
 */
public class MatrixOperations {
	
	/**
	 * @param source
	 * @return the min and max values found in the source
	 */
	public static double[] minMax(double[][] source) {
		double[] minMax = new double[2];
		int rows = source.length;
		int cols = source[0].length;
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int j = 0; j < rows; j++) {
			for (int k = 0; k < cols; k++) {
				double sample = source[j][k];
				if (minMax[0] > sample) {
					minMax[0] = sample;
				}
				if (minMax[1] < sample) {
					minMax[1] = sample;
				}
			}
		}
		return minMax;
	}

}
