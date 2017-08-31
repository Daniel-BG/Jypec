package com.jypec.comdec;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.pca.PrincipalComponentAnalysis;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.BitStream;
import com.jypec.util.FIFOBitStream;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.compositeTransforms.OneDimensionalWaveletExtender;
import com.jypec.wavelet.compositeTransforms.RecursiveBidimensionalWavelet;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

/**
 * @author Daniel
 * Class that wraps around everything else (ebc, wavelet, pca...) to perform compression
 */
public class Compressor {

	private int pcaDim;
	private int wavePasses;

	public Compressor(int pcaDim, int wavePasses) {
		this.pcaDim = pcaDim;
		this.wavePasses = wavePasses;
	}
	
	/**
	 * Compress a hyperspectral image with this compressor's settings
	 * @param srcImg the source hyperspectral image
	 * @param output where to put the compressed image
	 */
	public void compress(HyperspectralImage srcImg, BitStream output) {
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
		//TODO will need to save PCA values here to be later recovered!
		
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
		for (int i = 0; i < pcaDim; i++) {
			/** Apply the wavelet transform */
			HyperspectralBand hb = reduced.getBand(i);
			BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), this.wavePasses);
			double[][] waveForm = hb.toWave(0, 0, lines, samples);
			bdw.forwardTransform(waveForm, lines, samples);
			
			/** quantize the transform and save the quantization over the old image */
			MatrixQuantizer mq = new MatrixQuantizer(newDataType.getBitDepth() - 1, 0, 1, -newMaxVal, newMaxVal, 0.5);
			mq.quantize(waveForm, hb, 0, 0, lines, samples);
			
			/** Now divide into blocks and encode it*/
			Blocker blocker = new Blocker(hb, this.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			for (CodingBlock block: blocker) {
				block.setDepth(newDataType.getBitDepth()); //depth adjusted since there might be more bits
				EBCoder coder = new EBCoder();
				coder.code(block, output);
			}
		}
		
		
	}
	
}
