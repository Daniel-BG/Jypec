package com.jypec.quantization;

/**
 * Matrix quantizer
 * @see Quantizer
 * @author Daniel
 *
 */
public class MatrixQuantizer {

	private Quantizer quantizer;
	
	/**
	 * Create a matrix quantizer with an underlying Quantizer for the quantizing process.
	 * @see Quantizer
	 */
	public MatrixQuantizer(int exponent, int mantissa, int guard, double sampleLowerLimit, double sampleUpperLimit, double reconstructionOffset) {
		this.quantizer = new Quantizer(exponent, mantissa, guard, sampleLowerLimit, sampleUpperLimit, reconstructionOffset); 
	}
	
	/**
	 * Quantizes the input 
	 * @param input
	 * @param output: where to put the result
	 * @param height first dimension length of the matrix
	 * @param width second dimension length of the matrix
	 */
	public void quantize(double[][] input, int[][] output, int height, int width) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				output[i][j] = this.quantizer.normalizeAndQuantize(input[i][j]);
			}
		}
	}
	
	/**
	 * Dequantizes the input
	 * @param input
	 * @param output: where the dequantized result is output
	 * @param height first dimension length of the matrix
	 * @param width second dimension length of the matrix
	 * @return the dequantized input
	 */
	public void dequantize(int[][] input, double[][] output, int height, int width) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				output[i][j] = this.quantizer.deQuantizeAndDenormalize(input[i][j]);
			}
		}
	}
	
	
}
