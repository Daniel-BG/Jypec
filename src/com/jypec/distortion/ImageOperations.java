package com.jypec.distortion;

import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;

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
	public static int[] minMaxVal(HyperspectralImage h1) {
		int[] minMax = new int[2];
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					int sample = h1.getValueAt(i, j, k);
					if (minMax[0] > sample) {
						minMax[0] = sample;
					}
					if (minMax[1] < sample) {
						minMax[1] = sample;
					}
				}
			}
		}
		return minMax;
	}

	
	/**
	 * @param h1
	 * @return a pair of integers, the firs one being the minimum value within the band, 
	 * the second one being the maximum. 
	 */
	public static int[] minMaxVal(HyperspectralBand h1) {
		int[] minMax = new int[2];
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				int sample = h1.getValueAt(j, k);
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
	public static double averageValue(HyperspectralImage h1) {
		double acc = 0;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					acc += (double) h1.getValueAt(i, j, k);
				}
			}
		}
		int dim = h1.getNumberOfBands() * h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc / (double) dim;
	}
	
	
	/**
	 * @param h1
	 * @return the average value of the samples in h1
	 */
	public static double averageValue(HyperspectralBand h1) {
		double acc = 0;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				acc += (double) h1.getValueAt(j, k);
			}
		}
		int dim = h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc / (double) dim;
	}
	
	
	/**
	 * @param h1
	 * @return the variance of the samples of the given image
	 */
	public static double variance(HyperspectralImage h1) {
		double avg = averageValue(h1);
		double acc = 0;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					double val = (double) h1.getValueAt(i, j, k) - avg;
					acc += val * val;
				}
			}
		}
		int dim = h1.getNumberOfBands() * h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc / (double) dim;
	}
	
	/**
	 * @param h1
	 * @return the variance of the samples of the given image
	 */
	public static double variance(HyperspectralBand h1) {
		double avg = averageValue(h1);
		double acc = 0;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				double val = (double) h1.getValueAt(j, k) - avg;
				acc += val * val;
			}
		}
		int dim = h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc / (double) dim;
	}
}
