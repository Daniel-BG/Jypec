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
	 * @param height first dimension length of the matrix
	 * @param width second dimension length of the matrix
	 * @return the quantized input
	 */
	public int[][] quantize(double[][] input, int height, int width) {
		int[][] result = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				result[i][j] = this.quantizer.normalizeAndQuantize(input[i][j]);
			}
		}
		return result;
	}
	
	/**
	 * Dequantizes the input
	 * @param input
	 * @param height first dimension length of the matrix
	 * @param width second dimension length of the matrix
	 * @return the dequantized input
	 */
	public double[][] dequantize(int[][] input, int height, int width) {
		double[][] result = new double[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				result[i][j] = this.quantizer.deQuantizeAndDenormalize(input[i][j]);
			}
		}
		return result;
	}
	
	
}
