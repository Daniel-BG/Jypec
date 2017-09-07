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
		BitStreamDataReaderWriter bw = new BitStreamDataReaderWriter();
		bw.setStream(input);
		
		/** Need to know the image dimensions and some other values */
		ComParameters cp = new ComParameters();
		cp.loadFrom(bw);
		
		/** Recover PCA setup */
		dr.loadFrom(bw, cp);
		
		/** Uncompress the data stream */
		ImageDataType redDT = dr.getNewDataType(cp.newMaxVal);//new ImageDataType(cp.redBitDepth, true);
		HyperspectralImage reduced = new HyperspectralImage(null, redDT, dr.getNumComponents(), cp.lines, cp.samples);
		EBDecoder decoder = new EBDecoder();
		MatrixQuantizer mq = new MatrixQuantizer(redDT.getBitDepth() - 1, 0, cp.guardBits, -cp.newMaxVal, cp.newMaxVal, 0.5);
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		
		/** Proceed to uncompress the reduced image band by band */
		for (int i = 0; i < dr.getNumComponents(); i++) {
			HyperspectralBand hb = reduced.getBand(i);
			/** Now divide into blocks and decode it*/
			Blocker blocker = new Blocker(hb, cp.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			for (CodingBlock block: blocker) {
				block.setDepth(redDT.getBitDepth()); //depth adjusted since there might be more bits
				decoder.decode(input, block);
			}
			
			/** dequantize the wave */
			double[][] waveForm = hb.toWave(0, 0, cp.lines, cp.samples);
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
