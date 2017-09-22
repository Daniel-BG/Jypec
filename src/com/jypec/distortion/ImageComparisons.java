package com.jypec.distortion;

import com.jypec.img.HyperspectralBandData;
import com.jypec.img.HyperspectralImageData;

/**
 * @author Daniel
 * Utilities for image comparison
 */
public class ImageComparisons {

	
	/**
	 * Compares both images, returning the PSNR (Peak Signal Noise Ratio) value.
	 * @param h1
	 * @param h2
	 * @return the PSNR between both images
	 */
	public static double rawPSNR (HyperspectralImageData h1, HyperspectralImageData h2) {
		double mse = MSE(h1, h2);
		double maxVal = h1.getDataType().getDynamicRange();
		
		return PSNR(mse, maxVal);
	}
	
	/**
	 * Compares both bands, returning the PSNR (Peak Signal Noise Ratio) value.
	 * @param h1
	 * @param h2
	 * @return the PSNR between both bands
	 */
	public static double rawPSNR (HyperspectralBandData h1, HyperspectralBandData h2) {
		double mse = MSE(h1, h2);
		double maxVal = h1.getDataType().getDynamicRange();
		
		return PSNR(mse, maxVal);
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the normalized PSNR, calculated using the dynamic range of the image instead of
	 * the fixed maximum value range that pixels can have
	 */
	public static double normalizedPSNR(HyperspectralImageData h1, HyperspectralImageData h2) {
		double mse = MSE(h1, h2);
		int[] minMax = ImageOperations.minMaxVal(h1);
		double maxVal = minMax[1] - minMax[0];
		
		return PSNR(mse, maxVal);
	}

	/**
	 * @param h1
	 * @param h2
	 * @return the normalized PSNR, calculated using the dynamic range of the image instead of
	 * the fixed maximum value range that pixels can have
	 */
	public static double normalizedPSNR(HyperspectralBandData h1, HyperspectralBandData h2) {
		double mse = MSE(h1, h2);
		int[] minMax = ImageOperations.minMaxVal(h1);
		double maxVal = minMax[1] - minMax[0];
		
		return PSNR(mse, maxVal);
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the signal noise ratio between the given images
	 */
	public static double SNR(HyperspectralImageData h1, HyperspectralImageData h2) {
		double var = ImageOperations.variance(h1);
		double mse = MSE(h1, h2);
		return 10 * Math.log10(var / mse);
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the signal noise ratio between the given images
	 */
	public static double SNR(HyperspectralBandData h1, HyperspectralBandData h2) {
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
	public static double MSE (HyperspectralImageData h1, HyperspectralImageData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double acc = 0;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					double val = h1.getValueAt(i, j, k) - h2.getValueAt(i, j, k);
					acc += val * val;
				}
			}
		}
		//do the mean and return it
		acc /= h1.getNumberOfBands() * h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc;
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the Mean Squared Error (mean of all "differences between pixels squared")
	 */
	public static double MSE (HyperspectralBandData h1, HyperspectralBandData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double acc = 0;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				int val = h1.getValueAt(j, k) - h2.getValueAt(j, k);
				acc += val * val;
			}
		}
		//do the mean and return it
		acc /= h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc;
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the maximum squared error (difference between samples squared)
	 */
	public static double maxSE (HyperspectralImageData h1, HyperspectralImageData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double acc = 0;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					double val = h1.getValueAt(i, j, k) - h2.getValueAt(i, j, k);
					val *= val;
					if (val > acc)
						acc = val;
				}
			}
		}
		return acc;
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the maximum squared error (difference between samples squared)
	 */
	public static double maxSE (HyperspectralBandData h1, HyperspectralBandData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double acc = 0;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				double val = h1.getValueAt(j, k) - h2.getValueAt(j, k);
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
	public static double MSR (HyperspectralImageData h1, HyperspectralImageData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double mean = 0;
		double std = 0;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					double v1 = h1.getValueAt(i, j, k);
					double v2 = h2.getValueAt(i, j, k);
					mean += v1;
					std += (v1 - v2) * (v1 - v2);
				}
			}
		}
		mean /= h1.getTotalNumberOfSamples();
		std /= h1.getTotalNumberOfSamples();
		std = Math.sqrt(std);
		
		return mean / std;
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the mean to noise standard deviation ratio
	 */
	public static double MSR (HyperspectralBandData h1, HyperspectralBandData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double mean = 0;
		double std = 0;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				double v1 = h1.getValueAt(j, k);
				double v2 = h2.getValueAt(j, k);
				mean += v1;
				std += (v1 - v2) * (v1 - v2);
			}
		}
		mean /= h1.getTotalNumberOfSamples();
		std /= h1.getTotalNumberOfSamples();
		std = Math.sqrt(std);
		
		return mean / std;
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the mean to noise standard deviation ratio
	 */
	public static double SSIM (HyperspectralImageData h1, HyperspectralImageData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double mu1 = ImageOperations.averageValue(h1);
		double mu2 = ImageOperations.averageValue(h2);
		double v1 = ImageOperations.variance(h1);
		double v2 = ImageOperations.variance(h2);
		double sigma = ImageOperations.covariance(h1, h2);
		double L = h1.getDataType().getDynamicRange();
		
		final double k1 = 0.01;
		final double k2 = 0.03;
		
		double c1 = k1*k1*L*L;
		double c2 = k2*k2*L*L;
		
		return 
				((2*mu1*mu2 + c1) * (2*sigma + c2)) 
				/ //-------------------------------- Beautiful formatting
				((mu1*mu1 + mu2*mu2 + c1) * (v1 + v2 + c2));
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the mean to noise standard deviation ratio
	 */
	public static double SSIM (HyperspectralBandData h1, HyperspectralBandData h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double mu1 = ImageOperations.averageValue(h1);
		double mu2 = ImageOperations.averageValue(h2);
		double v1 = ImageOperations.variance(h1);
		double v2 = ImageOperations.variance(h2);
		double sigma = ImageOperations.covariance(h1, h2);
		double L = h1.getDataType().getDynamicRange();
		
		final double k1 = 0.01;
		final double k2 = 0.03;
		
		double c1 = k1*k1*L*L;
		double c2 = k2*k2*L*L;
		
		return 
				((2*mu1*mu2 + c1) * (2*sigma + c2)) 
				/ //-------------------------------- Beautiful formatting
				((mu1*mu1 + mu2*mu2 + c1) * (v1*v1 + v2*v2 + c2));
	}
	
	
		
	
}
