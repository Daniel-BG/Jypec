package com.jypec.comdec;

import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.pca.PrincipalComponentAnalysis;

/**
 * @author Daniel
 * Class that wraps around everything else (ebc, wavelet, pca...) to perform compression
 */
public class Compressor {

	private int pcaDim = 30;

	public Compressor() {
		
	}
	
	/**
	 * Compress a hyperspectral image with this compressor's settings
	 * @param srcImg the source hyperspectral image
	 */
	public void compress(HyperspectralImage srcImg) {
		/** First off, extract necessary information from the image*/
		int bands = srcImg.getNumberOfBands(), lines = srcImg.getNumberOfLines(), samples = srcImg.getNumberOfSamples();
		
		/** Do the PCA */
		PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
		
		pca.setup(lines * samples, bands);
		for (int i = 0; i < lines; i++) {
			for (int j = 0; j < samples; j++) {
				pca.addSample(srcImg.getPixel(i, j));
			}
		}
		
		pca.computeBasis(this.pcaDim);
		
		/** Now compute the max value that this newly created basis might have, and allocate an image with enough space for it */
		double maxVal = (double) srcImg.getDataType().getMagnitudeAbsoluteRange();
		double valueIncrement = Math.sqrt((double) bands);
		double newMaxVal = maxVal * valueIncrement;
		double newBitDepth = Math.log10(newMaxVal) * Math.log10(2);
		
		ImageDataType newDataType = new ImageDataType((int) Math.ceil(newBitDepth), true);
		
		HyperspectralImage reduced = new HyperspectralImage(null, newDataType, this.pcaDim, lines, samples);
		
		/** Project all image values onto the reduced space */
		
		for (int i = 0; i < lines; i++) {
			for (int j = 0; j < samples; j++) {
				reduced.setPixel(pca.sampleToEigenSpace(srcImg.getPixel(i, j)), i, j);
			}
		}
		
		/** Proceed to compress the reduced image */
		
		
	}
	
}
