package com.jypec.util.arrays;

import org.ejml.data.DMatrixRMaj;

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
	 * Extract the given band from the given image (as DMatrixRMaj)
	 * @param source
	 * @param band
	 * @param lines
	 * @param samples
	 * @return the extracted band
	 */
	public static double[][] extractBand(DMatrixRMaj source, int band, int lines, int samples) {
		double[][] res = new double[lines][samples];
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
	 * @return the parameter "src" as a DMatrixRMaj
	 */
	public static DMatrixRMaj getMatrix(double[][][] src, int bands, int lines, int samples) {
		DMatrixRMaj res = new DMatrixRMaj(bands, lines*samples);
		for (int i = 0; i < bands; i++) {
			for (int j = 0; j < lines; j++) {
				for (int k = 0; k < samples; k++) {
					res.set(i, j*samples+k, src[i][j][k]);
				}
			}
		}
		return res;
	}
	
	/**
	 * Does the inverse square root of all the elements in the diagonal. Use only with diagonal matrices
	 * @param source
	 */
	public static void inverseSquareRoot(DMatrixRMaj source) {
		if (source.getNumCols() != source.getNumRows()) {
			throw new IllegalArgumentException("Only works on square matrices");
		}
		
		for (int i = 0; i < source.getNumCols(); i++) {
			double val = source.get(i, i);
			if (Math.abs(val) < 0.1e-30) {
				source.set(i, i, 0);
			} else  {
				source.set(i, i, 1.0 / Math.sqrt(val));
			}
		}
		
	}
	
	
}
