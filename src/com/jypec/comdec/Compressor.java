package com.jypec.comdec;

import java.io.IOException;
import java.util.List;

import org.ejml.data.FMatrixRMaj;

import com.jypec.comdec.refinement.Refinements;
import com.jypec.ebc.EBCoder;
import com.jypec.img.HyperspectralBandData;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageDataType;
import com.jypec.img.ImageHeaderData;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.Pair;
import com.jypec.util.arrays.EJMLExtensions;
import com.jypec.util.arrays.MatrixTransforms;
import com.jypec.util.bits.BitOutputStreamTree;
import com.jypec.util.debug.Logger;
import com.jypec.util.debug.Profiler;
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
	 * Compress a hyperspectral image with this compressor's settings. This only compresses
	 * the data, and the metadata needs to be compressed with {@link ImageHeaderData}
	 * @param srcImg the source hyperspectral image
	 * @param output where to put the compressed image
	 * @param dr dimensionality reduction algorithm that is to be applied
	 * @throws IOException 
	 */
	public void compress(HyperspectralImageData srcImg, BitOutputStreamTree output) throws IOException {
		Profiler.getProfiler().profileStart();
		/** Get some values we are gonna need */
		int numLines = srcImg.getNumberOfLines();
		int numSamples = srcImg.getNumberOfSamples();
		FMatrixRMaj srcImgDMRM = srcImg.tofloatMatrix();
		srcImg.free();
		
		/** Project all image values onto the reduced space */
		Logger.getLogger().log("Applying dimensionality reduction");
		cp.dr.setPercentTraining(cp.percentTraining);
		FMatrixRMaj reduced = cp.dr.trainReduce(srcImgDMRM);
		srcImgDMRM = null; //not needed anymore. allow GC to discard it
		
		/** create the wavelet transform, and coder we'll be using, which won't change over the bands */
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		EBCoder coder = new EBCoder();
		
		/** Save metadata before compressing the image */
		Logger.getLogger().log("Saving compression parameters... ");
		this.cp.saveTo(output.addChild("compression parameters"));
		Logger.getLogger().log("(" + output.getTreeBits() + " bits)");
		
		/** Proceed to compress the reduced image */
		long lastBits = output.getTreeBits();
		for (int i = 0; i < cp.dr.getNumComponents(); i++) {
			BitOutputStreamTree banditree = output.addChild("code for band " + i);
			Logger.getLogger().log("Compressing band [" + (i+1) + "/" + cp.dr.getNumComponents() + "]: ");
			
			FMatrixRMaj waveForm = MatrixTransforms.extractBand(reduced, i, numLines, numSamples);
			
			/** Shave the resulting limits and raw encode their values */
			if (cp.percentOutliers > 0) {
				BitOutputStreamTree outlierTree = banditree.addChild("outliers");
				Logger.getLogger().log("\tSaving outliers...");
				List<Pair<Float, Pair<Integer, Integer>>> outliers = Refinements.findOutliers(waveForm, cp.percentOutliers);
				outlierTree.writeInt(outliers.size());
				for (Pair<Float, Pair<Integer, Integer>> p: outliers) {
					outlierTree.writeFloat(p.first());
					outlierTree.writeVLPInt(p.second().first());  //x coordinate
					outlierTree.writeVLPInt(p.second().second()); //y coordinate
				}
				Refinements.clamp(waveForm, Refinements.getNonOutlierRange());
			}
			
			/** Apply the wavelet transform */
			Logger.getLogger().log("\tApplying wavelet... ");

			
			float[] minMax = EJMLExtensions.minMax(waveForm);
			banditree.writeFloat(minMax[0]);
			banditree.writeFloat(minMax[1]);
			MatrixTransforms.normalize(waveForm, minMax[0], minMax[1], -0.5f, 0.5f);
			minMax = EJMLExtensions.minMax(waveForm);
			bdw.forwardTransform(waveForm, numLines, numSamples);
			


			/** get the requested data type */
			ImageDataType targetType = new ImageDataType(cp.bits, true);
			if (cp.shaveMap.hasMappingForKey(i)) {
				targetType.mutatePrecision(-cp.shaveMap.get(i));
			}
			
			Logger.getLogger().log("\tApplying quantization to type: " + targetType + "...");
			/** custom quantizer for this band */
			cp.pt.train(waveForm);
			cp.pt.saveTo(banditree.addChild("PreQuantizationTransform"));
			cp.pt.forwardTransform(waveForm);
			
			//use one guard bit just in case for the wavelet transform
			MatrixQuantizer mq = new MatrixQuantizer(targetType.getBitDepth() - 1, 0, 1, -0.5f, 0.5f, 0.375f);
			
			
			/** quantize the transform and save the quantization over the current band */
			HyperspectralBandData hb = HyperspectralBandData.generateRogueBand(targetType, numLines, numSamples);
			mq.quantize(waveForm, hb);
			
			/** Now divide into blocks and encode it*/
			Blocker blocker = new Blocker(hb, cp.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			Logger.getLogger().log("\tEncoding in " + blocker.size() + " blocks");
			blocker.code(targetType, coder, banditree.addChild("Blocks"));
			Logger.getLogger().log("\tCurrent size: " + output.getTreeBits() + " bits (+" + (output.getTreeBits() - lastBits) + ")");
			lastBits = output.getTreeBits();
		}
		Profiler.getProfiler().profileEnd();
	}
	
}
