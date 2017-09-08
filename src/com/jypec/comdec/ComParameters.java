package com.jypec.comdec;

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
	/** Bit depth of the reduced image (after dimensionality reduction) */
	public int redBitDepth;
	/** Bit depth of the source image */
	public int srcBitDepth;
	/** Flag indicating if the source image's samples are signed (the reduced one is always signed) */
	public boolean srcSigned;
	/** Max value of reduced image */
	public double newMaxVal;
	/** Min value of reduced image*/
	public double newMinVal;
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
		bw.writeNBitNumber(this.redBitDepth, ComDecConstants.IMAGE_BIT_DEPTH_BITS);
		bw.writeNBitNumber(this.srcBitDepth, ComDecConstants.IMAGE_BIT_DEPTH_BITS);
		bw.writeBoolean(this.srcSigned);
		bw.writeDouble(this.newMaxVal);
		bw.writeDouble(this.newMinVal);
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
		this.redBitDepth = bw.readNBitNumber(ComDecConstants.IMAGE_BIT_DEPTH_BITS);
		this.srcBitDepth = bw.readNBitNumber(ComDecConstants.IMAGE_BIT_DEPTH_BITS);
		this.srcSigned = bw.readBoolean();
		this.newMaxVal = bw.readDouble();
		this.newMinVal = bw.readDouble();
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
				this.redBitDepth == other.redBitDepth &&
				this.srcBitDepth == other.srcBitDepth &&
				this.srcSigned == other.srcSigned &&
				this.newMaxVal == other.newMaxVal &&
				this.newMinVal == other.newMinVal &&
				this.bitReduction == other.bitReduction;
	}


	

}
