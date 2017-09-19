package com.jypec.comdec;

import java.io.IOException;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Used for saving / loading parameters from a bitstream in an easy manner.
 * Initialize the public variables and then save, or load and then use the
 * public variables
 * @author Daniel
 */
public class ComParameters {
	private static final int DEFAULT_WAVE_PASSES = 0;
	private static final int DEFAULT_BIT_REDUCTION = 0;
	
	/** Number of passes of the wavelet transform */
	public int wavePasses = DEFAULT_WAVE_PASSES;
	/** Number of bits eliminated from each bitplane */
	public int bitReduction = DEFAULT_BIT_REDUCTION;

	/**
	 * Saves this parameters to the given BitStreamDataReaderWriter
	 * @param bw
	 * @throws IOException 
	 */
	public void saveTo(BitOutputStream bw) throws IOException {
		bw.writeNBitNumber(this.wavePasses, ComDecConstants.WAVE_PASSES_BITS);
		bw.writeNBitNumber(this.bitReduction, ComDecConstants.REDUCTION_BITS_BITS);
	}
	
	
	/**
	 * Loads this class from the given BitStreamDataReaderWriter, initializing all parameters
	 * @param bw
	 * @throws IOException 
	 */
	public void loadFrom(BitInputStream bw) throws IOException {
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
