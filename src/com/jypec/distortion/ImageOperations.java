package com.jypec.distortion;

import org.ejml.data.DMatrixRMaj;
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
	public static double[] minMaxVal(DMatrixRMaj h1) {
		double[] minMax = new double[2];
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int i = 0; i < h1.getNumRows(); i++) {
			for (int j = 0; j < h1.getNumCols(); j++) {
				double sample = h1.get(i, j);
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
	public static double averageValue(DMatrixRMaj h1) {
		SimpleMatrix m = SimpleMatrix.wrap(h1);
		return m.elementSum() / (double) m.getNumElements();
	}
	
	/**
	 * @param h1
	 * @return the variance of the samples of the given image
	 */
	public static double variance(DMatrixRMaj h1) {
		double avg = averageValue(h1);
		SimpleMatrix m = SimpleMatrix.wrap(new DMatrixRMaj(h1));
		m.plus(-avg);
		return m.elementMult(m).elementSum() / (double) m.getNumElements();
	}
	
	/**
	 * @param h1
	 * @param h2 
	 * @return the covariance of both images
	 */
	public static double covariance(DMatrixRMaj h1, DMatrixRMaj h2) {
		double avg1 = averageValue(h1);
		double avg2 = averageValue(h2);
		SimpleMatrix m1 = SimpleMatrix.wrap(new DMatrixRMaj(h1));
		SimpleMatrix m2 = SimpleMatrix.wrap(new DMatrixRMaj(h2));
		m1.plus(-avg1);
		m2.plus(-avg2);

		return m1.elementMult(m2).elementSum() / (double) m1.getNumElements();
	}
	
	/**
	 * @param h1
	 * @return the std of the samples of the given image
	 */
	public static double std(DMatrixRMaj h1) {
		return Math.sqrt(variance(h1));
	}

}
