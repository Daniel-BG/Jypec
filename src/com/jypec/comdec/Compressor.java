package com.jypec.comdec;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.pca.PrincipalComponentAnalysis;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.MathOperations;
import com.jypec.util.bits.BitStream;
import com.jypec.util.bits.BitStreamDataReaderWriter;
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
	private int guardBits;

	/**
	 * @param pcaDim value of the reduced PCA dimension
	 * @param wavePasses passes that the wavelet transform does over each band before compressing
	 * @param guardBits guard bits used when quantizing
	 */
	public Compressor(int pcaDim, int wavePasses, int guardBits) {
		this.pcaDim = pcaDim;
		this.wavePasses = wavePasses;
		this.guardBits = guardBits;
	}
	
	/**
	 * Compress a hyperspectral image with this compressor's settings
	 * @param srcImg the source hyperspectral image
	 * @param output where to put the compressed image
	 */
	public void compress(HyperspectralImage srcImg, BitStream output) {
		/** We will need a wrapper around the output to make it easier to save numbers */
		BitStreamDataReaderWriter bw = new BitStreamDataReaderWriter();
		bw.setStream(output);
		
		/** First off, extract necessary information from the image and save to stream */
		int bands = srcImg.getNumberOfBands(), lines = srcImg.getNumberOfLines(), samples = srcImg.getNumberOfSamples();
		ComParameters cp = new ComParameters();
		cp.bands = bands;
		cp.lines = lines;
		cp.samples = samples;
		cp.srcSigned = srcImg.getDataType().isSigned();
		cp.srcBitDepth = srcImg.getDataType().getBitDepth();
		cp.wavePasses = this.wavePasses;
		cp.guardBits = this.guardBits;
		
		/** Now compute the max value that this newly created basis might have, and allocate an image with enough space for it */
		double newMaxVal = MathOperations.getMaximumDistance(srcImg.getDataType().getMagnitudeAbsoluteRange(), bands);
		ImageDataType newDataType = new ImageDataType((int) Math.ceil(MathOperations.logBase(newMaxVal, 2d)), true);
		HyperspectralImage reduced = new HyperspectralImage(null, newDataType, this.pcaDim, lines, samples);
		cp.newMaxVal = newMaxVal;
		cp.redBitDepth = newDataType.getBitDepth();
		
		/** Project all image values onto the reduced space */
		PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
		pca.computeBasisFrom(srcImg, this.pcaDim);
		pca.imageToEigenSpace(srcImg, reduced);
		
		/** create the wavelet transform, quantizer, and coder we'll be using */
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), this.wavePasses);
		MatrixQuantizer mq = new MatrixQuantizer(newDataType.getBitDepth() - 1, 0, cp.guardBits, -newMaxVal, newMaxVal, 0.5);
		EBCoder coder = new EBCoder();
		
		/** Save metadata before compressing the image */
		cp.saveTo(bw);
		pca.saveToBitStream(bw);
		
		/** Proceed to compress the reduced image */
		for (int i = 0; i < pcaDim; i++) {
			/** Apply the wavelet transform */
			HyperspectralBand hb = reduced.getBand(i);
			double[][] waveForm = hb.toWave(0, 0, lines, samples);
			bdw.forwardTransform(waveForm, lines, samples);
			
			/** quantize the transform and save the quantization over the old image */
			mq.quantize(waveForm, hb, 0, 0, lines, samples);
			
			/** Now divide into blocks and encode it*/
			Blocker blocker = new Blocker(hb, this.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			for (CodingBlock block: blocker) {
				block.setDepth(newDataType.getBitDepth()); //depth adjusted since there might be more bits
				coder.code(block, output);
			}
		}
		
		
	}
	
}
