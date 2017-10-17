package com.jypec.quantization;

import com.jypec.util.datastructures.IntegerMatrix;
import com.jypec.util.debug.Profiler;

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
	 * @param exponent 
	 * @param mantissa 
	 * @param guard 
	 * @param sampleLowerLimit 
	 * @param sampleUpperLimit 
	 * @param reconstructionOffset 
	 * @see {@link Quantizer #Quantizer(int, int, int, float, float, float)}
	 */
	public MatrixQuantizer(int exponent, int mantissa, int guard, float sampleLowerLimit, float sampleUpperLimit, float reconstructionOffset) {
		this.quantizer = new Quantizer(exponent, mantissa, guard, sampleLowerLimit, sampleUpperLimit, reconstructionOffset); 
	}
	
	/**
	 * Quantizes the input
	 * @param input
	 * @param output where to put the result
	 * @param rowOffset
	 * @param colOffset
	 * @param rows first dimension length of the matrix
	 * @param cols second dimension length of the matrix
	 */
	public void quantize(float[][] input, IntegerMatrix output, int rowOffset, int colOffset, int rows, int cols) {
		Profiler.getProfiler().profileStart();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				output.setDataAt(this.quantizer.normalizeAndQuantize(input[rowOffset + i][colOffset + j]), rowOffset + i, colOffset + j);
			}
		}
		Profiler.getProfiler().profileEnd();
	}
	
	/**
	 * Dequantizes the input
	 * @param input
	 * @param output where the dequantized result is output
	 * @param rowOffset first dimension length of the matrix
	 * @param colOffset second dimension length of the matrix
	 * @param rows
	 * @param cols
	 */
	public void dequantize(IntegerMatrix input, float[][] output, int rowOffset, int colOffset, int rows, int cols) {
		Profiler.getProfiler().profileStart();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				output[rowOffset + i][colOffset + j] = this.quantizer.deQuantizeAndDenormalize(input.getDataAt(rowOffset + i, colOffset + j));
			}
		}
		Profiler.getProfiler().profileEnd();
	}
	
	
}
