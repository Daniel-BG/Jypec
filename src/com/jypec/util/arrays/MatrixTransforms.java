package com.jypec.util.arrays;

import java.util.List;

import org.ejml.data.FMatrixRMaj;

/**
 * @author Daniel
 * Useful matrix transforms are written here
 */
public class MatrixTransforms {
	
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
	public static FMatrixRMaj extractBand(FMatrixRMaj source, int band, int lines, int samples) {
		FMatrixRMaj res = new FMatrixRMaj(lines, samples);
		for (int i = 0; i < lines; i++) {
			for (int j = 0; j < samples; j++) {
				res.unsafe_set(i, j, source.get(band, i*samples + j));
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
	public static FMatrixRMaj getMatrix(List<FMatrixRMaj> src, int bands, int lines, int samples) {
		FMatrixRMaj res = new FMatrixRMaj(bands, lines*samples);
		for (int i = 0; i < bands; i++) {
			FMatrixRMaj curr = src.get(i);
			for (int j = 0; j < lines; j++) {
				for (int k = 0; k < samples; k++) {
					res.unsafe_set(i, j*samples+k, curr.unsafe_get(j, k));
				}
			}
		}
		return res;
	}
	
	
	
	/**
	 * Normalizes the input matrix in place so that 
	 * the old minimum and maximum values are now the targets given,
	 * and all other samples lie between them, maintaining their
	 * relative differences
	 * @param source the matrix to be normalized
	 * @param min the minimum value found in source. Cannot be NaN or Inf
	 * @param max the maximum value found in source. Cannot be NaN or Inf
	 * @param targetMin the desired minimum value
	 * @param targetMax the desired maximum value
	 * @return the given matrix modified in place
	 */
	public static FMatrixRMaj normalize(FMatrixRMaj source, float min, float max, float targetMin, float targetMax) {
		if (min >= max || targetMin >= targetMax) {
			throw new IllegalArgumentException("Ranges when normalizing a matrix must be non-empty intervals!");
		}
		//generate ranges
		float initialRange = max - min;
		float targetRange = targetMax - targetMin;
		//recalculate every sample
		for (int i = 0; i < source.getNumRows(); i++) {
			for (int j = 0; j < source.getNumCols(); j++) {
				float value = source.get(i, j);
				value = ((value - min) / initialRange);		//value is in [0, 1]
				value = value * targetRange + targetMin;	//value is in targetMin, targetMax
				source.set(i, j, value);
			}
		}
		
		return source;
	}
	
}
