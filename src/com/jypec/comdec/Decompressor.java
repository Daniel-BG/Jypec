package com.jypec.comdec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.FMatrixRMaj;

import com.jypec.comdec.refinement.Refinements;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HeaderConstants;
import com.jypec.img.HyperspectralBandData;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.HyperspectralImageFloatData;
import com.jypec.img.ImageDataType;
import com.jypec.img.ImageHeaderData;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.Pair;
import com.jypec.util.arrays.MatrixTransforms;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.debug.Logger;
import com.jypec.util.debug.Profiler;
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
		Profiler.getProfiler().profileStart();
		/** Need to know the image dimensions and some other values */
		Logger.getLogger().log("Extracting image metadata...");
		int lines = (int) ihd.getOnce(HeaderConstants.HEADER_LINES);
		int bands = (int) ihd.getOnce(HeaderConstants.HEADER_BANDS);
		int samples = (int) ihd.getOnce(HeaderConstants.HEADER_SAMPLES);
		ImageDataType idt = ImageDataType.fromHeaderCode((byte) ihd.getOnce(HeaderConstants.HEADER_DATA_TYPE));
		
		/** Recover compression parameter setup */
		Logger.getLogger().log("Loading decompression parameters...");
		ComParameters cp = new ComParameters();
		cp.loadFrom(input);
		
		/** Uncompress the data stream */
		float[][][] reduced = new float[cp.dr.getNumComponents()][lines][samples];

		EBDecoder decoder = new EBDecoder();
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		
		/** Proceed to uncompress the reduced image band by band */
		for (int i = 0; i < cp.dr.getNumComponents(); i++) {
			Logger.getLogger().log("Extracting compressed band [" + (i+1) + "/" + cp.dr.getNumComponents() + "]");
			/** Get the clamped values if present */
			List<Pair<Float, Pair<Integer, Integer>>> outliers = null;
			if (cp.percentOutliers > 0) {
				Logger.getLogger().log("\tGetting outliers...");
				outliers = new ArrayList<Pair<Float, Pair<Integer, Integer>>>();
				int size = input.readInt();
				for (int j = 0; j < size; j++) {
					outliers.add(new Pair<Float, Pair<Integer, Integer>>(
							input.readFloat(), 
							new Pair<Integer, Integer>(
									input.readVLPInt(), 
									input.readVLPInt())));
				}
			}
				
			/** Get this band's max and min values, and use that to create the quantizer */
			Logger.getLogger().log("\tLoading dequantizer...");
			float bandMin = input.readFloat();
			float bandMax = input.readFloat();
			ImageDataType targetType = new ImageDataType(cp.bits, true);
			if (cp.shaveMap.hasMappingForKey(i)) {
				targetType.mutatePrecision(-cp.shaveMap.get(i));
			}
			
			HyperspectralBandData hb = HyperspectralBandData.generateRogueBand(targetType, lines, samples);
			/** Now divide into blocks and decode it*/
			Blocker blocker = new Blocker(hb, cp.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			Logger.getLogger().log("\tDecoding " + blocker.size() + "blocks");
			for (CodingBlock block: blocker) {			
				block.setDepth(targetType.getBitDepth()); //depth adjusted since there might be more bits
				decoder.decode(input, block);
			}
			
			/** dequantize the wave */
			Logger.getLogger().log("\tDequantizing...");
			float[][] waveForm = reduced[i];
			MatrixQuantizer mq = new MatrixQuantizer(targetType.getBitDepth() - 1, 0, 0, bandMin, bandMax, 0.375f);
			mq.dequantize(hb, waveForm, 0, 0, lines, samples);
			
			/** add outliers back */
			if (cp.percentOutliers > 0) {
				Logger.getLogger().log("\tSetting outliers back...");
				Refinements.addOutliersBack(outliers, waveForm);
			}
			
			/** Apply the reverse wavelet transform */
			Logger.getLogger().log("Reversing wavelet...");
			bdw.reverseTransform(waveForm, samples, lines);
		}
		
		
		/** Undo PCA dimensionality reduction */
		ImageDataType srcDT = new ImageDataType(idt.getBitDepth(), idt.isSigned());
		Logger.getLogger().log("Projecting back into original dimension...");
		FMatrixRMaj result = cp.dr.boost(MatrixTransforms.getMatrix(reduced, cp.dr.getNumComponents(), lines, samples));
		
		HyperspectralImageData srcImg = new HyperspectralImageFloatData(result, srcDT, bands, lines, samples);
		
		
		
		//image is decompressed now
		Profiler.getProfiler().profileEnd();
		return srcImg;
	}
}
