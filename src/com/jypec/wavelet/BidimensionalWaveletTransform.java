package com.jypec.wavelet;

public class BidimensionalWaveletTransform {

	
	
	
	
	/**
	 * Applies a wavelet transform along both axes of the given matrix.
	 * First along the rows, then along the columns.
	 * A new copy is returned, and the memory originally pointed by s is modified
	 * @param s
	 * @param width
	 * @param height
	 */
	public static void forwardTransform(double[][] s, int width, int height) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			WaveletTransform.forwardTransform(s[i], width);
		}
		//transpose and transform along the other axis
		double[][] tmp = MatrixTransforms.transpose(s, width, height);
		for (int j = 0; j < width; j++) {
			WaveletTransform.forwardTransform(tmp[j], height);
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
	public static void reverseTransform(double[][] s, int width, int height) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			WaveletTransform.reverseTransform(s[i], width);
		}
		//transpose and transform along the other axis
		double[][] tmp = MatrixTransforms.transpose(s, width, height);
		for (int j = 0; j < width; j++) {
			WaveletTransform.reverseTransform(tmp[j], height);
		}
		//retranspose and return
		s = MatrixTransforms.transpose(tmp, height, width);
	}
	
	
}
