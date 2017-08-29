package com.jypec.util.arrays;

/**
 * @author Daniel
 * Useful matrix transforms are written here
 */
public class MatrixTransforms {

	/**
	 * Transpose the matrix source and leave it in dest
	 * @param source
	 * @param dest
	 * @param width
	 * @param height
	 */
	public static void transpose(double[][]source, double[][]dest, int width, int height) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				dest[j][i] = source[i][j];
			}
		}
	}
	
}
