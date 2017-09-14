package com.jypec.comdec;

import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.bits.BitStream;
import com.jypec.util.bits.BitStreamDataReaderWriter;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.compositeTransforms.OneDimensionalWaveletExtender;
import com.jypec.wavelet.compositeTransforms.RecursiveBidimensionalWavelet;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

/**
 * @author Daniel
 * Decompresses an input BitStream into a hyperspectral image
 */
public class Decompressor {

	
	/**
	 * @param input
	 * @param dr dimensionality reduction algorithm to be employed
	 * @return the resulting image from decompressing the given stream
	 */
	public HyperspectralImage decompress(BitStream input, DimensionalityReduction dr) {
		/** Create a wrapper for the stream to easily read/write it */
		BitStreamDataReaderWriter bw = new BitStreamDataReaderWriter(input);
		
		/** Need to know the image dimensions and some other values */
		ComParameters cp = new ComParameters();
		cp.loadFrom(bw);
		
		/** Recover PCA setup */
		dr.loadFrom(bw, cp);
		
		/** Uncompress the data stream */
		//ImageDataType redDT = ImageDataType.findBest(cp.newMinVal, cp.newMaxVal);//new ImageDataType(cp.redBitDepth, true);
		double[][][] reduced = new double[dr.getNumComponents()][cp.lines][cp.samples];

		EBDecoder decoder = new EBDecoder();
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		
		/** Proceed to uncompress the reduced image band by band */
		for (int i = 0; i < dr.getNumComponents(); i++) {
			/** Get this band's max and min values, and use that to create the quantizer */
			double bandMin = bw.readDouble();
			double bandMax = bw.readDouble();
			ImageDataType targetType = ImageDataType.findBest(bandMin, bandMax, 0);
			targetType.mutatePrecision(-cp.bitReduction);
			HyperspectralBand hb = HyperspectralBand.generateRogueBand(targetType, cp.lines, cp.samples);
			/** Now divide into blocks and decode it*/
			Blocker blocker = new Blocker(hb, cp.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			for (CodingBlock block: blocker) {			
				block.setDepth(targetType.getBitDepth()); //depth adjusted since there might be more bits
				decoder.decode(input, block);
			}
			
			/** dequantize the wave */
			double[][] waveForm = reduced[i];
			MatrixQuantizer mq = new MatrixQuantizer(targetType.getBitDepth() - 1, 0, 0, bandMin, bandMax, 0.375);

			mq.dequantize(hb, waveForm, 0, 0, cp.lines, cp.samples);
			
			/** Apply the reverse wavelet transform */
			bdw.reverseTransform(waveForm, cp.lines, cp.samples);
		}
		
		
		/** Undo PCA dimensionality reduction */
		ImageDataType srcDT = new ImageDataType(cp.srcBitDepth, cp.srcSigned);
		HyperspectralImage srcImg = new HyperspectralImage(null, srcDT, cp.bands, cp.lines, cp.samples);
		
		dr.boost(reduced, srcImg);
		
		
		
		//image is decompressed now
		return srcImg;
	}
}
