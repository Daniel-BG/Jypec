package com.jypec.comdec;

import java.io.IOException;

import org.ejml.data.DMatrixRMaj;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBandData;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageDataType;
import com.jypec.img.ImageHeaderData;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.DefaultVerboseable;
import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.arrays.MatrixTransforms;
import com.jypec.util.bits.BitStreamTreeNode;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.compositeTransforms.OneDimensionalWaveletExtender;
import com.jypec.wavelet.compositeTransforms.RecursiveBidimensionalWavelet;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

/**
 * @author Daniel
 * Class that wraps around everything else (ebc, wavelet, pca...) to perform compression
 */
public class Compressor extends DefaultVerboseable {

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
	public void compress(HyperspectralImageData srcImg, BitStreamTreeNode output) throws IOException {
		/** Project all image values onto the reduced space */
		this.sayLn("Applying dimensionality reduction");
		cp.dr.setParentVerboseable(this);
		DMatrixRMaj reduced = cp.dr.trainReduce(srcImg.toDoubleMatrix());
		
		/** create the wavelet transform, and coder we'll be using, which won't change over the bands */
		BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), cp.wavePasses);
		EBCoder coder = new EBCoder();
		
		/** Save metadata before compressing the image */
		this.say("Saving compression parameters... ");
		this.cp.saveTo(output.addChild("compression parameters"));
		this.sayLn("(" + output.getTreeBits() + " bits)");
		
		/** Proceed to compress the reduced image */
		int lastBits = 0;
		for (int i = 0; i < cp.dr.getNumComponents(); i++) {
			BitStreamTreeNode banditree = output.addChild("code for band " + i);
			this.sayLn("Compressing band [" + (i+1) + "/" + cp.dr.getNumComponents() + "]: ");
			
			/** Apply the wavelet transform */
			this.sayLn("\tApplying wavelet... ");
			double[][] waveForm = MatrixTransforms.extractBand(reduced, i, srcImg.getNumberOfLines(), srcImg.getNumberOfSamples());
			bdw.forwardTransform(waveForm, srcImg.getNumberOfLines(), srcImg.getNumberOfSamples());
			double[] minMax = MatrixOperations.minMax(waveForm);
			
			/** get max and min from the resulting transform, and create the best data type possible */
			ImageDataType targetType = new ImageDataType(cp.bits, true);
			if (cp.shaveMap.hasMappingForKey(i)) {
				targetType.mutatePrecision(-cp.shaveMap.get(i));
			}
			
			//targetType.mutatePrecision(-cp.bitReduction);
			this.sayLn("\tApplying quantization to type: " + targetType + "...");
			
			/** custom quantizer for this band */
			MatrixQuantizer mq = new MatrixQuantizer(targetType.getBitDepth() - 1, 0, 0, minMax[0], minMax[1], 0.375);
			
			banditree.bos.writeDouble(minMax[0]);
			banditree.bos.writeDouble(minMax[1]);
			
			
			/** quantize the transform and save the quantization over the current band */
			HyperspectralBandData hb = HyperspectralBandData.generateRogueBand(targetType, srcImg.getNumberOfLines(), srcImg.getNumberOfSamples());
			mq.quantize(waveForm, hb, 0, 0, srcImg.getNumberOfLines(), srcImg.getNumberOfSamples());
			
			/** Now divide into blocks and encode it*/
			Blocker blocker = new Blocker(hb, cp.wavePasses, Blocker.DEFAULT_EXPECTED_DIM, Blocker.DEFAULT_MAX_BLOCK_DIM);
			this.say("\tEncoding in " + blocker.size() + " blocks");
			for (CodingBlock block: blocker) {
				block.setDepth(targetType.getBitDepth()); //depth adjusted since there might be more bits
				coder.code(block, banditree.addChild(block.toString()));
				this.say(".");
			}
			this.sayLn("");
			this.sayLn("\tCurrent size: " + banditree.getTreeBits() + " bits (+" + (banditree.getTreeBits() - lastBits) + ")");
			lastBits = banditree.getTreeBits();
		}
	}
	
}
