package com.jypec.quantization;

import org.ejml.data.FMatrixRMaj;

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
	 */
	public void quantize(FMatrixRMaj input, IntegerMatrix output) {
		Profiler.getProfiler().profileStart();
		for (int i = 0; i < input.getNumRows(); i++) {
			for (int j = 0; j < input.getNumCols(); j++) {
				output.setDataAt(this.quantizer.normalizeAndQuantize(input.unsafe_get(i, j)), i, j);
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
	public void dequantize(IntegerMatrix input, FMatrixRMaj output) {
		Profiler.getProfiler().profileStart();
		for (int i = 0; i < output.getNumRows(); i++) {
			for (int j = 0; j < output.getNumCols(); j++) {
				output.unsafe_set(i, j, this.quantizer.deQuantizeAndDenormalize(input.getDataAt(i, j)));
			}
		}
		Profiler.getProfiler().profileEnd();
	}
	
	
}
