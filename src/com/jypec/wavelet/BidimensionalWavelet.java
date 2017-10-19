package com.jypec.wavelet;

import org.ejml.data.FMatrixRMaj;

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
	public void forwardTransform(FMatrixRMaj s, int height, int width);

	/**
	 * Reverts the process made by {@link #forwardTransform(FMatrixRMaj, int, int)}
	 * @param s
	 * @param width
	 * @param height
	 */
	public void reverseTransform(FMatrixRMaj s, int height, int width);
	
}
