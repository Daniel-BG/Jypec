package com.jypec.wavelet;

import com.jypec.util.arrays.MatrixTransforms;

/**
 * Extend a 1-D wavelet to 2-D
 * @author Daniel
 *
 */
public class BidimensionalWavelet {

	private Wavelet baseWavelet;
	
	/**
	 * Create a Bidimensional wavelet
	 * @param baseWavelet: 1-D wavelet that is to be applied in both dimensions
	 */
	public BidimensionalWavelet(Wavelet baseWavelet) {
		this.baseWavelet = baseWavelet;
	}
	
	
	/**
	 * Applies a wavelet transform along both axes of the given matrix.
	 * First along the rows, then along the columns.
	 * A new copy is returned, and the memory originally pointed by s is modified
	 * @param s
	 * @param width
	 * @param height
	 */
	public void forwardTransform(double[][] s, int width, int height) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			this.baseWavelet.forwardTransform(s[i], width);
		}
		//transpose and transform along the other axis
		double[][] tmp = MatrixTransforms.transpose(s, width, height);
		for (int j = 0; j < width; j++) {
			this.baseWavelet.forwardTransform(tmp[j], height);
		}
		//retranspose and return
		s = MatrixTransforms.transpose(tmp, height, width);
	}

	/**
	 * Reverts the process made by {@link #forwardTransform(double[][], int, int)}
	 * @param s
	 * @param width
	 * @param height
	 */
	public void reverseTransform(double[][] s, int width, int height) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			this.baseWavelet.reverseTransform(s[i], width);
		}
		//transpose and transform along the other axis
		double[][] tmp = MatrixTransforms.transpose(s, width, height);
		for (int j = 0; j < width; j++) {
			this.baseWavelet.reverseTransform(tmp[j], height);
		}
		//retranspose and return
		s = MatrixTransforms.transpose(tmp, height, width);
	}
	
	
}
