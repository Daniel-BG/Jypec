package com.jypec.wavelet;

/**
 * @author Daniel
 * Specifies a bidimensional wavelet interface
 */
public interface BidimensionalWavelet {

	
	/**
	 * Applies a wavelet transform along both axes of the given matrix.
	 * First along the rows, then along the columns.
	 * S is modified to include the results
	 * @param s
	 * @param width
	 * @param height
	 */
	public void forwardTransform(double[][] s, int width, int height);

	/**
	 * Reverts the process made by {@link #forwardTransform(double[][], int, int)}
	 * @param s
	 * @param width
	 * @param height
	 */
	public void reverseTransform(double[][] s, int width, int height);
	
}
