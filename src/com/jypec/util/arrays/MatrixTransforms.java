package com.jypec.util.arrays;

public class MatrixTransforms {

	/**
	 * Transpose the matrix s, returning a fresh new matrix as its transpose.
	 * s is not altered
	 * @param s
	 * @param width
	 * @param height
	 * @return
	 */
	public static double[][] transpose(double[][]s, int width, int height) {
		double[][] result = new double[width][height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				result[j][i] = s[i][j];
			}
		}
		return result;
	}
	
}
