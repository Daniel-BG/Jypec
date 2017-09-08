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

	private ComParameters cp;
	
	/**
	 * @param cp compressor parameters that will dictate how this compressor behaves
	 */
	public Compressor(ComParameters cp) {
		this.cp = cp;
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
		cp.feedFrom(srcImg);
		
		/** Project all image values onto the reduced space */
		dr.train(srcImg);
		double[][][] reduced = dr.reduce(srcImg);
		
		/** create the wavelet transform, and coder we'll be using, which won't change over the bands */
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		EBCoder coder = new EBCoder();
		
		/** Save metadata before compressing the image */
		cp.saveTo(bw);
		dr.saveTo(bw);
		
		/** Proceed to compress the reduced image */
		for (int i = 0; i < dr.getNumComponents(); i++) {
			/** Apply the wavelet transform */
			double[][] waveForm = reduced[i];

			bdw.forwardTransform(waveForm, cp.lines, cp.samples);
			double[] minMax = MatrixOperations.minMax(waveForm);
			/** get max and min from the resulting transform, and create the best data type possible */
			ImageDataType targetType = ImageDataType.findBest(minMax[0], minMax[1], 0);
			targetType.mutatePrecision(-cp.bitReduction);
			/** custom quantizer for this band */
			MatrixQuantizer mq = new MatrixQuantizer(targetType.getBitDepth() - 1, 0, 0, minMax[0], minMax[1], 0.375);
			
			bw.writeDouble(minMax[0]);
			bw.writeDouble(minMax[1]);
			
			
			/** quantize the transform and save the quantization over the current band */
			HyperspectralBand hb = HyperspectralBand.generateRogueBand(targetType, cp.lines, cp.samples);
			mq.quantize(waveForm, hb, 0, 0, cp.lines, cp.samples);
			
			/** Now divide into blocks and encode it*/
			Blocker blocker = new Blocker(hb, cp.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			for (CodingBlock block: blocker) {
				block.setDepth(targetType.getBitDepth()); //depth adjusted since there might be more bits
				coder.code(block, output);
			}
		
		}
		
	}
	
}
