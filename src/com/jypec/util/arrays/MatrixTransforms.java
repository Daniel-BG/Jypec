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
	
	/**
	 * Does the inverse square root of all the elements in the diagonal. Use only with diagonal matrices
	 * @param source
	 */
	public static void inverseSquareRoot(FMatrixRMaj source) {
		if (source.getNumCols() != source.getNumRows()) {
			throw new IllegalArgumentException("Only works on square matrices");
		}
		
		for (int i = 0; i < source.getNumCols(); i++) {
			float val = source.get(i, i);
			if (Math.abs(val) < 0.1e-30) {
				source.set(i, i, 0);
			} else  {
				source.set(i, i, (float) (1.0 / Math.sqrt(val)));
			}
		}
		
	}
	
	
	/**
	 * Gets a subset of the given matrix
	 * @param source from where to take samples
	 * @param probability probability that one sample from source will make it to the 
	 * returned dataset. e.g: if probability = 0.1 -> 1 out of 10 samples will be output
	 * @return the subset of the input, which could be empty if <code>probability</code> 
	 * is too low
	 */
	public static FMatrixRMaj getSubSet(FMatrixRMaj source, double probability) {
		if (probability < 0 || probability > 1) {
			throw new IllegalArgumentException("Probability must be between 0 and 1");
		}
		if (probability == 1) {
			return source;
		}
		int samples = (int) (source.getNumCols() * probability);
		FMatrixRMaj result = new FMatrixRMaj(source.getNumRows(), samples);
		for (int i = 0; i < samples; i++) {
			double which = ((double) i) * source.getNumCols() / (double) samples;
			int index = (int) Math.round(which); //should be between 0 and source.numcols - 1
			for (int j = 0; j < source.getNumRows(); j++) {
				result.set(j, i, source.get(j, index));
			}
		}
		
		return result;
	}
	
	
}
