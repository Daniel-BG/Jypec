package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Class to transform strings to values (uncompressed to compressed) <br>
 * or values to strings (compressed to uncompressed)
 * @author Daniel
 */
public abstract class ValueCompressorDecompressor {
	
	/**
	 * Interpret the given object, and then compress into the stream
	 * @param obj
	 * @param brw
	 */
	public abstract void compress(Object obj, BitStreamDataReaderWriter brw);
	
	/**
	 * Read the value from the given stream and return the representing string
	 * @param brw
	 * @return the uncompressed object
	 */
	public abstract Object uncompress(BitStreamDataReaderWriter brw);

}
