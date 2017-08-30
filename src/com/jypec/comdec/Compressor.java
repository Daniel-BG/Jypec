package com.jypec.comdec;

import javax.swing.text.Highlighter.HighlightPainter;

import com.jypec.img.HyperspectralImage;
import com.jypec.pca.PrincipalComponentAnalysis;
import com.jypec.util.debug.ArrayPrinter;

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
				pca.addSample(srcImg.getSample(i, j));
			}
		}
		
		pca.computeBasis(pcaDim);
		
		HyperspectralImage reduced;
		
		
		
		
		
		System.out.println(ArrayPrinter.printDoubleArray(srcImg.getSample(0, 0)));
		System.out.println(ArrayPrinter.printDoubleArray(pca.eigenToSampleSpace(pca.sampleToEigenSpace(srcImg.getSample(0, 0)))));
		System.out.println(ArrayPrinter.printDoubleArray(pca.sampleToEigenSpace(srcImg.getSample(0, 0))));
	}
	
}
