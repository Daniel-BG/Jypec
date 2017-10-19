package com.jypec.util.arrays;

/**
 * Some operations over matrices
 * @author Daniel
 */
public class MatrixOperations {
	
	/**
	 * @param source
	 * @return the min and max values found in the source
	 */
	public static float[] minMax(float[][] source) {
		float[] minMax = new float[2];
		int rows = source.length;
		int cols = source[0].length;
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int j = 0; j < rows; j++) {
			for (int k = 0; k < cols; k++) {
				float sample = source[j][k];
				if (minMax[0] > sample) {
					minMax[0] = sample;
				}
				if (minMax[1] < sample) {
					minMax[1] = sample;
				}
			}
		}
		return minMax;
	}
	
	
	/**
	 * @param source
	 * @return the average value of the given matrix (assumed to be rectangular)
	 */
	public static float avg(float[][] source) {
		double acc = 0;
		for (int j = 0; j < source.length; j++) {
			for (int k = 0; k < source[0].length; k++) {
				acc += (double) source[j][k];
			}
		}
		acc /= (double) (source.length * source[0].length);
		return (float) acc;
	}


	public static float std(float[][] waveForm) {
		float avg = avg(waveForm);
		double acc = 0;
		for (int j = 0; j < waveForm.length; j++) {
			for (int k = 0; k < waveForm[0].length; k++) {
				float val = (float) waveForm[j][k] - avg;
				acc += val * val;
			}
		}
		acc /= (float) (waveForm.length * waveForm[0].length);
		
		
		// TODO Auto-generated method stub
		return (float) Math.sqrt(acc);
	}

}
