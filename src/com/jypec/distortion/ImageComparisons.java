package com.jypec.distortion;

import org.ejml.data.FMatrixRMaj;

import com.jypec.util.arrays.EJMLExtensions;

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
	public static float rawPSNR (FMatrixRMaj h1, FMatrixRMaj h2, float dynRange) {
		float mse = MSE(h1, h2);
		float maxVal = dynRange;
		
		return PSNR(mse, maxVal);
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the normalized PSNR, calculated using the dynamic range of the image instead of
	 * the fixed maximum value range that pixels can have
	 */
	public static float normalizedPSNR(FMatrixRMaj h1, FMatrixRMaj h2) {
		float mse = MSE(h1, h2);
		float[] minMax = EJMLExtensions.minMax(h1);
		float maxVal = minMax[1] - minMax[0];
		
		return PSNR(mse, maxVal);
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the signal noise ratio between the given images
	 */
	public static float SNR(FMatrixRMaj h1, FMatrixRMaj h2) {
		float var = EJMLExtensions.var(h1, null);
		float mse = MSE(h1, h2);
		return (float) (10 * Math.log10(var / mse));
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the signal noise ratio between the given images
	 */
	public static float powerSNR(FMatrixRMaj h1, FMatrixRMaj h2) {
		float pow = EJMLExtensions.power(h1);
		float mse = MSE(h1, h2);
		return (float) (10 * Math.log10(pow / mse));
	}
	
	private static float PSNR (float mse, float max) {
		if (mse == 0d) {
			return Float.POSITIVE_INFINITY;
		} else {
			return (float) (20 * Math.log10(max) - 10 * Math.log10(mse));
		}
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the Mean Squared Error (mean of all "differences between pixels squared")
	 */
	public static float MSE (FMatrixRMaj h1, FMatrixRMaj h2) {
		checkDimensions(h1, h2);
		float acc = 0;
		for (int i = 0; i < h1.getNumRows(); i++) {
			for (int j = 0; j < h2.getNumCols(); j++) {
				float val = h1.get(i, j) - h2.get(i, j);
				acc += val * val;
			}
		}
		return acc / (float) h1.getNumElements();
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the maximum squared error (difference between samples squared)
	 */
	public static float maxSE (FMatrixRMaj h1, FMatrixRMaj h2) {
		checkDimensions(h1, h2);
		float acc = Float.MIN_VALUE;
		for (int i = 0; i < h1.getNumRows(); i++) {
			for (int j = 0; j < h2.getNumCols(); j++) {
				float val = h1.get(i, j) - h2.get(i, j);
				val *= val;
				if (val > acc)
					acc = val;
			}
		}
		return acc;
	}	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the mean to noise standard deviation ratio
	 */
	public static float MSR (FMatrixRMaj h1, FMatrixRMaj h2) {
		checkDimensions(h1, h2);
		float mean = EJMLExtensions.avg(h1);
		float std = (float) Math.sqrt(MSE(h1, h2));
		return mean / std;
	}
	
	private final static float k1 = 0.01f; //usually 0.01?
	private final static float k2 = 0.03f; //usually 0.03?
	
	/**
	 * @param h1
	 * @param h2
	 * @param dynRange dynamic range of the data
	 * @return the mean to noise standard deviation ratio
	 * @see <a href="https://ece.uwaterloo.ca/~z70wang/publications/ssim.pdf">Article</a>
	 */
	public static float SSIM (FMatrixRMaj h1, FMatrixRMaj h2, float dynRange) {
		checkDimensions(h1, h2);
		//add up all squared differences
		float mu1 = EJMLExtensions.avg(h1);
		float mu2 = EJMLExtensions.avg(h2);
		float v1 = EJMLExtensions.var(h1, mu1);
		float v2 = EJMLExtensions.var(h2, mu2);
		float sigma = EJMLExtensions.cov(h1, h2, mu1, mu2);
		float L = dynRange;
		
		float c1 = k1*k1*L*L;
		float c2 = k2*k2*L*L;
		
		return 
				((2*mu1*mu2 + c1) * (2*sigma + c2)) 
				/ //-------------------------------- Beautiful formatting
				((mu1*mu1 + mu2*mu2 + c1) * (v1 + v2 + c2));
	}

	/**
	 * Check that both matrices are of the same shape. Will throw exception otherwise
	 * @param h1
	 * @param h2
	 */
	public static void checkDimensions(FMatrixRMaj h1, FMatrixRMaj h2) {
		if (h1.getNumCols() != h2.getNumCols() || h1.getNumRows() != h2.getNumRows())
			throw new IllegalArgumentException("Image sizes do not match!");
	}
	
}
