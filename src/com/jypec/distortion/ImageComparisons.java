package com.jypec.distortion;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.simple.SimpleMatrix;

/**
 * @author Daniel
 * Utilities for image comparison
 */
public class ImageComparisons {

	
	/**
	 * Compares both images, returning the PSNR (Peak Signal Noise Ratio) value.
	 * @param h1
	 * @param h2
	 * @param dynRange the dynamic range of the data
	 * @return the PSNR between both images
	 */
	public static double rawPSNR (DMatrixRMaj h1, DMatrixRMaj h2, double dynRange) {
		double mse = MSE(h1, h2);
		double maxVal = dynRange;
		
		return PSNR(mse, maxVal);
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the normalized PSNR, calculated using the dynamic range of the image instead of
	 * the fixed maximum value range that pixels can have
	 */
	public static double normalizedPSNR(DMatrixRMaj h1, DMatrixRMaj h2) {
		double mse = MSE(h1, h2);
		double[] minMax = ImageOperations.minMaxVal(h1);
		double maxVal = minMax[1] - minMax[0];
		
		return PSNR(mse, maxVal);
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the signal noise ratio between the given images
	 */
	public static double SNR(DMatrixRMaj h1, DMatrixRMaj h2) {
		double var = ImageOperations.variance(h1);
		double mse = MSE(h1, h2);
		return 10 * Math.log10(var / mse);
	}
	
	private static double PSNR (double mse, double max) {
		if (mse == 0d) {
			return Double.POSITIVE_INFINITY;
		} else {
			return 20 * Math.log10(max) - 10 * Math.log10(mse);
		}
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the Mean Squared Error (mean of all "differences between pixels squared")
	 */
	public static double MSE (DMatrixRMaj h1, DMatrixRMaj h2) {
		checkDimensions(h1, h2);
		SimpleMatrix m1 = SimpleMatrix.wrap(new DMatrixRMaj(h1));
		SimpleMatrix m2 = SimpleMatrix.wrap(new DMatrixRMaj(h2));
		SimpleMatrix m3 = m1.minus(m2);
		return m3.elementMult(m3).elementSum() / (double) m3.getNumElements();
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the maximum squared error (difference between samples squared)
	 */
	public static double maxSE (DMatrixRMaj h1, DMatrixRMaj h2) {
		checkDimensions(h1, h2);
		SimpleMatrix m1 = SimpleMatrix.wrap(new DMatrixRMaj(h1));
		SimpleMatrix m2 = SimpleMatrix.wrap(new DMatrixRMaj(h2));
		SimpleMatrix m3 = m1.minus(m2);
		return m3.elementMult(m3).elementMaxAbs();
	}	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the mean to noise standard deviation ratio
	 */
	public static double MSR (DMatrixRMaj h1, DMatrixRMaj h2) {
		checkDimensions(h1, h2);
		double mean = ImageOperations.averageValue(h1);
		double std = Math.sqrt(MSE(h1, h2));
		return mean / std;
	}
	
	private final static double k1 = 0.0; //usually 0.01?
	private final static double k2 = 0.0; //usually 0.03?
	
	/**
	 * @param h1
	 * @param h2
	 * @param dynRange dynamic range of the data
	 * @return the mean to noise standard deviation ratio
	 * @see <a href="https://ece.uwaterloo.ca/~z70wang/publications/ssim.pdf">Article</a>
	 */
	public static double SSIM (DMatrixRMaj h1, DMatrixRMaj h2, double dynRange) {
		checkDimensions(h1, h2);
		//add up all squared differences
		double mu1 = ImageOperations.averageValue(h1);
		double mu2 = ImageOperations.averageValue(h2);
		double v1 = ImageOperations.variance(h1);
		double v2 = ImageOperations.variance(h2);
		double sigma = ImageOperations.covariance(h1, h2);
		double L = dynRange;
		
		double c1 = k1*k1*L*L;
		double c2 = k2*k2*L*L;
		
		return 
				((2*mu1*mu2 + c1) * (2*sigma + c2)) 
				/ //-------------------------------- Beautiful formatting
				((mu1*mu1 + mu2*mu2 + c1) * (v1 + v2 + c2));
	}

	private static void checkDimensions(DMatrixRMaj h1, DMatrixRMaj h2) {
		if (h1.getNumCols() != h2.getNumCols() || h1.getNumRows() != h2.getNumRows())
			throw new IllegalArgumentException("Image sizes do not match!");
	}
	
}
