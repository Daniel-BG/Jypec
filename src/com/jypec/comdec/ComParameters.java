package com.jypec.comdec;

import java.io.IOException;
import java.util.Map.Entry;

import com.jypec.cli.InputArguments;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.quantization.PrequantizationTransformer;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;
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
	/** Prequantization algorithm */
	public PrequantizationTransformer pt;
	/** from 0-1, percent of samples used for training */
	public double percentTraining = 1;
	/** from 0-1, percent of outliers that are to be rawcoded */
	public double percentOutliers;

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
		if (args.requestTrainingReduction) {
			this.percentTraining = args.percentTraining;
		}
		if (args.requestOutliers) {
			this.percentOutliers = args.percentOutliers;
		}
		this.shaveMap = args.shaves;
		this.dr = DimensionalityReduction.loadFrom(args);
		this.pt = PrequantizationTransformer.loadFrom(args);
	}
	
	/** Create empty parameters to be loaded from {@link #loadFrom(BitInputStream)}*/
	public ComParameters() {}

	/**
	 * Saves this parameters to the given BitStreamDataReaderWriter
	 * @param bw
	 * @throws IOException 
	 */
	public void saveTo(BitOutputStreamTree bw) throws IOException {
		bw.addChild("wave passes").writeNBitNumber(this.wavePasses, ComDecConstants.WAVE_PASSES_BITS);
		bw.addChild("red bits").writeNBitNumber(this.bits, ComDecConstants.REDUCTION_BITS_BITS);
		bw.addChild("outliers").writeDouble(percentOutliers);
		BitOutputStreamTree cbstn = bw.addChild("shave map");
		cbstn.writeByte((byte) shaveMap.size());
		for (Entry<Integer, Integer> e: shaveMap.entrySet()) {
			cbstn.writeByte((byte) (int)e.getKey());
			cbstn.writeByte((byte) (int)e.getValue());
		}
		dr.saveTo(bw.addChild("dim red"));
		//pt is not saved since it is band-dependent
	}
	
	
	/**
	 * Loads this class from the given BitStreamDataReaderWriter, initializing all parameters
	 * @param bw
	 * @throws IOException 
	 */
	public void loadFrom(BitInputStream bw) throws IOException {
		this.wavePasses = bw.readNBitNumber(ComDecConstants.WAVE_PASSES_BITS) & 0xff;
		this.bits = bw.readNBitNumber(ComDecConstants.REDUCTION_BITS_BITS) & 0xff;
		this.percentOutliers = bw.readDouble();
		
		int entries = bw.readByte();
		this.shaveMap = new LowKeyHashMap<Integer, Integer>();
		for (int i = 0; i < entries; i++) {
			this.shaveMap.put((int) bw.readByte(), (int) bw.readByte());
		}
		this.dr = DimensionalityReduction.loadFrom(bw);
		//pt is not loaded since it is band-dependent
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
