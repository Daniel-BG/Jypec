package com.jypec.comdec;

import com.jypec.img.HyperspectralImage;
import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Used for saving / loading parameters from a bitstream in an easy manner.
 * Initialize the public variables and then save, or load and then use the
 * public variables
 * @author Daniel
 */
public class ComParameters {
	//public variables initialized to random values for testing
	/** Number of bands in the image */
	public int bands;
	/** Number of lines in the image  */
	public int lines;
	/** Number of samples in the image */
	public int samples;
	/** Number of passes of the wavelet transform */
	public int wavePasses;
	/** Bit depth of the source image */
	public int srcBitDepth;
	/** Flag indicating if the source image's samples are signed (the reduced one is always signed) */
	public boolean srcSigned;
	/** Number of bits eliminated from each bitplane */
	public int bitReduction;

	
	
	
	/**
	 * Saves this parameters to the given BitStreamDataReaderWriter
	 * @param bw
	 */
	public void saveTo(BitStreamDataReaderWriter bw) {
		bw.writeNBitNumber(this.bands, ComDecConstants.BAND_BITS);
		bw.writeNBitNumber(this.lines, ComDecConstants.LINE_BITS);
		bw.writeNBitNumber(this.samples, ComDecConstants.SAMPLE_BITS);
		bw.writeNBitNumber(this.wavePasses, ComDecConstants.WAVE_PASSES_BITS);
		bw.writeNBitNumber(this.srcBitDepth, ComDecConstants.IMAGE_BIT_DEPTH_BITS);
		bw.writeBoolean(this.srcSigned);
		bw.writeNBitNumber(this.bitReduction, ComDecConstants.REDUCTION_BITS_BITS);
	}
	
	
	/**
	 * Loads this class from the given BitStreamDataReaderWriter, initializing all parameters
	 * @param bw
	 */
	public void loadFrom(BitStreamDataReaderWriter bw) {
		this.bands = bw.readNBitNumber(ComDecConstants.BAND_BITS);
		this.lines = bw.readNBitNumber(ComDecConstants.LINE_BITS);
		this.samples = bw.readNBitNumber(ComDecConstants.SAMPLE_BITS);
		this.wavePasses = bw.readNBitNumber(ComDecConstants.WAVE_PASSES_BITS);
		this.srcBitDepth = bw.readNBitNumber(ComDecConstants.IMAGE_BIT_DEPTH_BITS);
		this.srcSigned = bw.readBoolean();
		this.bitReduction = bw.readNBitNumber(ComDecConstants.REDUCTION_BITS_BITS);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ComParameters)) {
			return false;
		}
		ComParameters other = (ComParameters) obj;
		
		return this.bands == other.bands &&
				this.lines == other.lines &&
				this.samples == other.samples &&
				this.wavePasses == other.wavePasses &&
				this.srcBitDepth == other.srcBitDepth &&
				this.srcSigned == other.srcSigned &&
				this.bitReduction == other.bitReduction;
	}


	/**
	 * Take the dimensions and type from the given image and save
	 * in this compression parameter object
	 * @param srcImg 
	 */
	public void feedFrom(HyperspectralImage srcImg) {
		this.bands = srcImg.getNumberOfBands();
		this.lines = srcImg.getNumberOfLines();
		this.samples = srcImg.getNumberOfSamples();
		this.srcSigned = srcImg.getDataType().isSigned();
		this.srcBitDepth = srcImg.getDataType().getBitDepth();
	}


	

}
