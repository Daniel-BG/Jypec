package com.jypec.comdec;

import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.ebc.EBCoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.arrays.MatrixOperations;
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
	private int bitReduction;

	/**
	 * @param pcaDim value of the reduced PCA dimension
	 * @param wavePasses passes that the wavelet transform does over each band before compressing
	 * @param bitReduction bits reduced when quantizing
	 */
	public Compressor(int pcaDim, int wavePasses, int bitReduction) {
		this.pcaDim = pcaDim;
		this.wavePasses = wavePasses;
		this.bitReduction = bitReduction;
	}
	
	/**
	 * Compress a hyperspectral image with this compressor's settings
	 * @param srcImg the source hyperspectral image
	 * @param output where to put the compressed image
	 * @param dr dimensionality reduction algorithm that is to be applied
	 */
	public void compress(HyperspectralImage srcImg, BitStream output, DimensionalityReduction dr) {
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
		cp.bitReduction = this.bitReduction;
		
		/** Now compute the max value that this newly created basis might have, and allocate an image with enough space for it */
		double newMaxVal = dr.getMaxValue(srcImg);
		double newMinVal = dr.getMinValue(srcImg);
		//ImageDataType newDataType = ImageDataType.findBest(newMinVal, newMaxVal);

		cp.newMaxVal = newMaxVal;
		cp.newMinVal = newMinVal;
		//cp.redBitDepth = newDataType.getBitDepth();
		
		/** Project all image values onto the reduced space */
		dr.train(srcImg, this.pcaDim);
		double[][][] reduced = dr.reduce(srcImg);
		
		/** create the wavelet transform, and coder we'll be using, which won't change over the bands */
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), this.wavePasses);
		EBCoder coder = new EBCoder();
		
		/** Save metadata before compressing the image */
		cp.saveTo(bw);
		dr.saveTo(bw);
		
		/** Proceed to compress the reduced image */
		for (int i = 0; i < pcaDim; i++) {
			/** Apply the wavelet transform */
			double[][] waveForm = reduced[i];

			bdw.forwardTransform(waveForm, lines, samples);
			double[] minMax = MatrixOperations.minMax(waveForm);
			/** get max and min from the resulting transform, and create the best data type possible */
			ImageDataType targetType = ImageDataType.findBest(minMax[0], minMax[1], 0);
			targetType.mutatePrecision(-cp.bitReduction);
			/** custom quantizer for this band */
			MatrixQuantizer mq = new MatrixQuantizer(targetType.getBitDepth() - 1, 0, 0, minMax[0], minMax[1], 0.375);
			
			bw.writeDouble(minMax[0]);
			bw.writeDouble(minMax[1]);
			
			
			/** quantize the transform and save the quantization over the current band */
			HyperspectralBand hb = HyperspectralBand.generateRogueBand(targetType, lines, samples);
			mq.quantize(waveForm, hb, 0, 0, lines, samples);
			
			/** Now divide into blocks and encode it*/
			Blocker blocker = new Blocker(hb, this.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			for (CodingBlock block: blocker) {
				block.setDepth(targetType.getBitDepth()); //depth adjusted since there might be more bits
				coder.code(block, output);
			}
		
		}
		
	}
	
}
