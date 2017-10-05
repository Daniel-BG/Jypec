package com.jypec.distortion;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.simple.SimpleMatrix;

/**
 * @author Daniel
 * Mathematical operations over hyperspectral images
 */
public class ImageOperations {

	/**
	 * @param h1
	 * @return a pair of integers, the firs one being the minimum value within the image, 
	 * the second one being the maximum. 
	 */
	public static float[] minMaxVal(FMatrixRMaj h1) {
		float[] minMax = new float[2];
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int i = 0; i < h1.getNumRows(); i++) {
			for (int j = 0; j < h1.getNumCols(); j++) {
				float sample = h1.get(i, j);
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
	 * @param h1
	 * @return the average value of the samples in h1
	 */
	public static float averageValue(FMatrixRMaj h1) {
		SimpleMatrix m = SimpleMatrix.wrap(h1);
		return (float) (m.elementSum() / (float) m.getNumElements());
	}
	
	/**
	 * @param h1
	 * @param avg precalculated average for faster computation (if null will calculate)
	 * @return the variance of the samples of the given image
	 */
	public static float variance(FMatrixRMaj h1, Float avg) {
		if (avg == null) {
			avg = averageValue(h1);
		}
		float acc = 0;
		for (int j = 0; j < h1.getNumRows(); j++) {
			for (int k = 0; k < h1.getNumCols(); k++) {
				float val = (float) h1.get(j, k) - avg;
				acc += val * val;
			}
		}
		return acc / (float) h1.getNumElements();
	}
	
	/**
	 * @param h1
	 * @param h2 
	 * @param avg1 average of h1 used for speed up. If not available send null
	 * @param avg2 average of h2 used for speed up. If not available send null
	 * @return the covariance of both images
	 */
	public static float covariance(FMatrixRMaj h1, FMatrixRMaj h2, Float avg1, Float avg2) {
		ImageComparisons.checkDimensions(h1, h2);
		if (avg1 == null) {
			avg1 = averageValue(h1);
		}
		if (avg2 == null) {
			avg2 = averageValue(h2);
		}
		float acc = 0;
		for (int i = 0; i < h1.getNumRows(); i++) {
			for (int j = 0; j < h1.getNumCols(); j++) {
				acc += (h1.get(i, j) - avg1) * (h2.get(i, j) - avg2);
			}
		}

		return acc / (float) h1.getNumElements();
	}
	
	/**
	 * @param h1
	 * @param avg average of h1, used to speed up calculations. if null, it will be calculated
	 * @return the std of the samples of the given image
	 */
	public static float std(FMatrixRMaj h1, float avg) {
		return (float) Math.sqrt(variance(h1, avg));
	}
	
	/**
	 * Calculate the mean difference of each row
	 * @param h1
	 * @param h2
	 * @return a matrix of size (h1.rows, 1) containing the mean diff of each row of h1 and h2
	 */
	public static FMatrixRMaj meanDiff(FMatrixRMaj h1, FMatrixRMaj h2) {
		ImageComparisons.checkDimensions(h1, h2);
		
		FMatrixRMaj res = new FMatrixRMaj(h1.getNumRows(), 1);
		
		for (int i = 0; i < h1.getNumRows(); i++) {
			for (int j = 0; j < h1.getNumCols(); j++) {
				res.add(i, 0, h1.get(i, j) - h2.get(i, j));
			}
		}
		
		CommonOps_FDRM.divide(res, h1.getNumCols());
		
		return res;
	}
	
	
	/**
	 * @param h1
	 * @return the power of the image (mean of the squared values)
	 */
	public static float power(FMatrixRMaj h1) {
		float acc = 0;
		for (int i = 0; i < h1.getNumRows(); i++) {
			for (int j = 0; j < h1.getNumCols(); j++) {
				acc += h1.get(i, j) * h1.get(i, j);
			}
		}
		acc /= h1.getNumElements();
		return acc;
	}

}
