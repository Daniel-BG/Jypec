package com.jypec.comdec;

import java.io.IOException;

import org.ejml.data.DMatrixRMaj;

import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HeaderConstants;
import com.jypec.img.HyperspectralBandData;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageDataType;
import com.jypec.img.ImageHeaderData;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.arrays.MatrixTransforms;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.debug.Logger;
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
	 * @param ihd the image header metadata, for now reading the appropiate number of bits
	 * @param input
	 * @return the resulting image from decompressing the given stream
	 * @throws IOException 
	 */
	public HyperspectralImageData decompress(ImageHeaderData ihd, BitInputStream input) throws IOException {
		/** Need to know the image dimensions and some other values */
		Logger.getLogger().log("Extracting image metadata...");
		int lines = (int) ihd.get(HeaderConstants.HEADER_LINES);
		int bands = (int) ihd.get(HeaderConstants.HEADER_BANDS);
		int samples = (int) ihd.get(HeaderConstants.HEADER_SAMPLES);
		ImageDataType idt = ImageDataType.fromHeaderCode((byte) ihd.get(HeaderConstants.HEADER_DATA_TYPE));
		
		/** Recover compression parameter setup */
		Logger.getLogger().log("Loading decompression parameters...");
		ComParameters cp = new ComParameters();
		cp.loadFrom(input);
		
		/** Uncompress the data stream */
		double[][][] reduced = new double[cp.dr.getNumComponents()][lines][samples];

		EBDecoder decoder = new EBDecoder();
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		
		/** Proceed to uncompress the reduced image band by band */
		for (int i = 0; i < cp.dr.getNumComponents(); i++) {
			Logger.getLogger().log("Extracting compressed band [" + (i+1) + "/" + cp.dr.getNumComponents() + "]");
			/** Get this band's max and min values, and use that to create the quantizer */
			Logger.getLogger().log("Loading dequantizer...");
			double bandMin = input.readDouble();
			double bandMax = input.readDouble();
			ImageDataType targetType = new ImageDataType(cp.bits, true);
			if (cp.shaveMap.hasMappingForKey(i)) {
				targetType.mutatePrecision(-cp.shaveMap.get(i));
			}
			
			HyperspectralBandData hb = HyperspectralBandData.generateRogueBand(targetType, lines, samples);
			/** Now divide into blocks and decode it*/
			Blocker blocker = new Blocker(hb, cp.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			Logger.getLogger().log("Decoding " + blocker.size() + "blocks");
			for (CodingBlock block: blocker) {			
				block.setDepth(targetType.getBitDepth()); //depth adjusted since there might be more bits
				decoder.decode(input, block);
			}
			Logger.getLogger().log("");
			
			/** dequantize the wave */
			Logger.getLogger().log("Dequantizing...");
			double[][] waveForm = reduced[i];
			MatrixQuantizer mq = new MatrixQuantizer(targetType.getBitDepth() - 1, 0, 0, bandMin, bandMax, 0.375);
			mq.dequantize(hb, waveForm, 0, 0, lines, samples);
			
			/** Apply the reverse wavelet transform */
			Logger.getLogger().log("Reversing wavelet...");
			bdw.reverseTransform(waveForm, lines, samples);
		}
		
		
		/** Undo PCA dimensionality reduction */
		ImageDataType srcDT = new ImageDataType(idt.getBitDepth(), idt.isSigned());
		HyperspectralImageData srcImg = new HyperspectralImageData(null, srcDT, bands, lines, samples);
		Logger.getLogger().log("Projecting back into original dimension...");
		DMatrixRMaj result = cp.dr.boost(MatrixTransforms.getMatrix(reduced, cp.dr.getNumComponents(), lines, samples));
		
		srcImg.copyDataFrom(result);
		
		
		
		//image is decompressed now
		return srcImg;
	}
}
