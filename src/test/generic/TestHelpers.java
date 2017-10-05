package test.generic;

import java.util.Random;

import com.jypec.util.datastructures.IntegerMatrix;

/**
 * @author Daniel
 * Helper functions that can be used across various tests
 */
public class TestHelpers {

	
	/**
	 * @param dst matrix to be filled
	 * @param rLim row limit
	 * @param cLim column limit
	 * @param r random generator
	 * @param std standard deviation for the gaussian function
	 * @param mean mean for the gaussian function
	 */
	public static void randomGaussianFillMatrix(float[][] dst, int rLim, int cLim, Random r, float std, float mean) {
		for (int k = 0; k < rLim; k++) {
			for (int j = 0; j < cLim; j++) {
				dst[k][j] = (float) r.nextGaussian() * std + mean;
			}
		}
	}
	
	
	/**
	 * @param dst array to be filled
	 * @param dim dimension of the array
	 * @param r random generator
	 * @param std standard deviation
	 * @param mean mean for the gaussian function
	 */
	public static void randomGaussianFillArray(float[] dst, int dim, Random r, float std, float mean) {
		for (int k = 0; k < dim; k++) {
			dst[k] = (float) r.nextGaussian() * std + mean;
		}
	}
	
	
	/**
	 * Fills the matrix with the given value
	 * @param data
	 * @param width
	 * @param height
	 * @param value
	 */
	public static void fillDataWithValue(IntegerMatrix data, int width, int height, int value) {
		for (int i = 0; i < height; i++) { 
			for (int j = 0; j < width; j++) {
				data.setDataAt(value, i, j);
			}
		}
	}
	
	
	/**
	 * Fill the matrix with random data following a gaussian distribution with mean 0 and std of 2^depth.
	 * Values over the limit of 2^depth are clamped to the inteval [-2^depth + 1, 2^depth - 1]. 
	 * Returned values are in sign-magnitude format, with one sign bit followed by (depth - 1) magnitude bits
	 * Can also be interpreted as unsigned values
	 * @param data
	 * @param width
	 * @param height
	 * @param depth does not include the sign bit!
	 * @param r
	 */
	public static void randomizeMatrix(Random r, IntegerMatrix data, int width, int height, int depth) {
		int magnitudeLimit = (0x1 << (depth - 1)) - 1;
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				float val = (float) r.nextGaussian() * magnitudeLimit;
				int res = (int) val;
				if (res < -magnitudeLimit)
					res = -magnitudeLimit;
				if (res > magnitudeLimit)
					res = magnitudeLimit;
				
				int sign = res < 0 ? 1 : 0;
				int magnitude = res < 0 ? -res : res;
				
				data.setDataAt((sign << (depth - 1)) + magnitude, i, j);
			}
		}
	}
	
}
