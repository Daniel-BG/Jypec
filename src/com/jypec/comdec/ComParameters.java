package com.jypec.comdec;

import java.io.IOException;
import java.util.Map.Entry;

import com.jypec.cli.InputArguments;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitStreamTreeNode;
import com.jypec.util.datastructures.LowKeyHashMap;

/**
 * Used for saving / loading parameters from a bitstream in an easy manner.
 * Initialize the public variables and then save, or load and then use the
 * public variables
 * @author Daniel
 */
public class ComParameters {
	private static final int DEFAULT_WAVE_PASSES = 0;
	private static final int DEFAULT_BITS = 16;
	
	/** Number of passes of the wavelet transform */
	public int wavePasses = DEFAULT_WAVE_PASSES;
	/** Number of bits that occupy each bitplane */
	public int bits = DEFAULT_BITS;
	/** Number of bits shaved from each band */
	public LowKeyHashMap<Integer, Integer> shaveMap;
	/** Dimensionality reduction algorithm */
	public DimensionalityReduction dr;

	/**
	 * @param args read the compression parameters from the input arguments 
	 */
	public ComParameters(InputArguments args) {
		if (args.requestWavelet) {
			this.wavePasses = args.passes;
		}
		if (args.requestBits) {
			this.bits = args.bits;
		}
		this.shaveMap = args.shaves;
		this.dr = DimensionalityReduction.loadFrom(args);
	}
	
	/** Create empty parameters to be loaded from {@link #loadFrom(BitInputStream)}*/
	public ComParameters() {}

	/**
	 * Saves this parameters to the given BitStreamDataReaderWriter
	 * @param bw
	 * @throws IOException 
	 */
	public void saveTo(BitStreamTreeNode bw) throws IOException {
		bw.addChild("wave passes").bos.writeNBitNumber(this.wavePasses, ComDecConstants.WAVE_PASSES_BITS);
		bw.addChild("red bits").bos.writeNBitNumber(this.bits, ComDecConstants.REDUCTION_BITS_BITS);
		BitStreamTreeNode cbstn = bw.addChild("shave map");
		cbstn.bos.writeByte((byte) shaveMap.size());
		for (Entry<Integer, Integer> e: shaveMap.entrySet()) {
			cbstn.bos.writeByte((byte) (int)e.getKey());
			cbstn.bos.writeByte((byte) (int)e.getValue());
		}
		dr.saveTo(bw.addChild("dim red"));
	}
	
	
	/**
	 * Loads this class from the given BitStreamDataReaderWriter, initializing all parameters
	 * @param bw
	 * @throws IOException 
	 */
	public void loadFrom(BitInputStream bw) throws IOException {
		this.wavePasses = bw.readNBitNumber(ComDecConstants.WAVE_PASSES_BITS) & 0xff;
		this.bits = bw.readNBitNumber(ComDecConstants.REDUCTION_BITS_BITS) & 0xff;
		
		int entries = bw.readByte();
		this.shaveMap = new LowKeyHashMap<Integer, Integer>();
		for (int i = 0; i < entries; i++) {
			this.shaveMap.put((int) bw.readByte(), (int) bw.readByte());
		}
		this.dr = DimensionalityReduction.loadFrom(bw);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ComParameters)) {
			return false;
		}
		ComParameters other = (ComParameters) obj;


		return this.wavePasses == other.wavePasses &&
				this.bits == other.bits &&
				this.shaveMap.entrySet().equals(other.shaveMap.entrySet());
	}

}
