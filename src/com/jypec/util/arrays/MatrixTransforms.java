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
	
}
