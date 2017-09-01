package com.jypec.comdec;

/**
 * @author Daniel
 * Constants for the Compressor/Decompressor
 */
public class ComDecConstants {
	/** Bits used to store the number of bands in the compressed file */
	public static final int BAND_BITS = 16;
	/** Bits used to store the number of samples in the compressed file */
	public static final int SAMPLE_BITS = 16;
	/** Bits used to store the number of lines in the compressed file */
	public static final int LINE_BITS = 16;
	/** Bit Depth of the samples in the reduced image */
	public static final int IMAGE_BIT_DEPTH_BITS = 8;
	/** Bit depth of the number of passes of the wavelet transform */
	public static final int WAVE_PASSES_BITS = 8;
}
