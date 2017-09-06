package com.jypec.comdec;

import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.pca.PrincipalComponentAnalysis;
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
	 * @return the resulting image from decompressing the given stream
	 */
	public HyperspectralImage decompress(BitStream input) {
		/** Create a wrapper for the stream to easily read/write it */
		BitStreamDataReaderWriter bw = new BitStreamDataReaderWriter();
		bw.setStream(input);
		
		/** Need to know the image dimensions and some other values */
		ComParameters cp = new ComParameters();
		cp.loadFrom(bw);
		
		/** Recover PCA setup */
		PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
		pca.restoreFromBitStream(bw);
		
		/** Uncompress the data stream */
		ImageDataType redDT = new ImageDataType(cp.redBitDepth, true);
		HyperspectralImage reduced = new HyperspectralImage(null, redDT, pca.getNumComponents(), cp.lines, cp.samples);
		EBDecoder decoder = new EBDecoder();
		MatrixQuantizer mq = new MatrixQuantizer(redDT.getBitDepth() - 1, 0, 1, -cp.newMaxVal, cp.newMaxVal, 0.5);
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		
		/** Proceed to uncompress the reduced image band by band */
		for (int i = 0; i < pca.getNumComponents(); i++) {
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
		
		pca.imageToSampleSpace(reduced, srcImg);
		
		
		//image is decompressed now
		return srcImg;
	}
}
