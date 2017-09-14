package com.jypec.comdec;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Used for saving / loading parameters from a bitstream in an easy manner.
 * Initialize the public variables and then save, or load and then use the
 * public variables
 * @author Daniel
 */
public class ComParameters {
	/** Number of passes of the wavelet transform */
	public int wavePasses;
	/** Number of bits eliminated from each bitplane */
	public int bitReduction;

	/**
	 * Saves this parameters to the given BitStreamDataReaderWriter
	 * @param bw
	 */
	public void saveTo(BitStreamDataReaderWriter bw) {
		bw.writeNBitNumber(this.wavePasses, ComDecConstants.WAVE_PASSES_BITS);
		bw.writeNBitNumber(this.bitReduction, ComDecConstants.REDUCTION_BITS_BITS);
	}
	
	
	/**
	 * Loads this class from the given BitStreamDataReaderWriter, initializing all parameters
	 * @param bw
	 */
	public void loadFrom(BitStreamDataReaderWriter bw) {
		this.wavePasses = bw.readNBitNumber(ComDecConstants.WAVE_PASSES_BITS);
		this.bitReduction = bw.readNBitNumber(ComDecConstants.REDUCTION_BITS_BITS);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ComParameters)) {
			return false;
		}
		ComParameters other = (ComParameters) obj;
		
		return this.wavePasses == other.wavePasses &&
				this.bitReduction == other.bitReduction;
	}

}
