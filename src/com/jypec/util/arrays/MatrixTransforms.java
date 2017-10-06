package com.jypec.util.arrays;

import org.ejml.data.FMatrixRMaj;

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
	public static void transpose(float[][]source, float[][]dest, int width, int height) {
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
	public static void copy(float[][] src, float[][] dst, int rows, int cols) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				dst[i][j] = src[i][j];
			}
		}
	}
	
	
	/**
	 * @param src source of the data
	 * @param dst where the data is to be copied
	 * @param planes number of planes to be copied
	 * @param rows number of rows to be copied
	 * @param cols number of columns to be copied
	 */
	public static void copy(int[][][] src, int[][][] dst, int planes, int rows, int cols) {
		for (int k = 0; k < planes; k++) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					dst[k][i][j] = src[k][i][j];
				}
			}
		}
	}
	
	/**
	 * @param src source of the data
	 * @param value what to multiply for
	 * @param planes number of planes to be copied
	 * @param rows number of rows to be copied
	 * @param cols number of columns to be copied
	 */
	public static void multiply(int[][][] src, int value, int planes, int rows, int cols) {
		for (int k = 0; k < planes; k++) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					src[k][i][j] *= value;
				}
			}
		}
	}
	
	
	/**
	 * Extract the given band from the given image (as FMatrixRMaj)
	 * @param source
	 * @param band
	 * @param lines
	 * @param samples
	 * @return the extracted band
	 */
	public static float[][] extractBand(FMatrixRMaj source, int band, int lines, int samples) {
		float[][] res = new float[lines][samples];
		for (int i = 0; i < lines; i++) {
			for (int j = 0; j < samples; j++) {
				res[i][j] = source.get(band, i*samples + j);
			}
		}
		return res;
	}
	
	/**
	 * @param src source of the data
	 * @param bands 
	 * @param lines 
	 * @param samples 
	 * @return the parameter "src" as a FMatrixRMaj
	 */
	public static FMatrixRMaj getMatrix(float[][][] src, int bands, int lines, int samples) {
		FMatrixRMaj res = new FMatrixRMaj(bands, lines*samples);
		for (int i = 0; i < bands; i++) {
			for (int j = 0; j < lines; j++) {
				for (int k = 0; k < samples; k++) {
					res.set(i, j*samples+k, src[i][j][k]);
				}
			}
		}
		return res;
	}
	
	
}
