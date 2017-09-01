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
	
	
	
	/**
	 * @param src source of the data
	 * @param dst where the data is to be copied
	 * @param rows number of rows to be copied
	 * @param cols number of columns to be copied
	 */
	public static void copy(double[][] src, double[][] dst, int rows, int cols) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				dst[i][j] = src[i][j];
			}
		}
	}
	
}
