package com.jypec.util.io.headerio;

import com.jypec.util.bits.BitStreamDataReaderWriter;
import com.jypec.util.io.headerio.primitives.ValueCompressorDecompressor;

/**
 * Reads and/or writes envi header parameters from compressed to uncompressed
 * form and vice versa
 * @author Daniel
 */
public class ParameterReaderWriter {


	private HeaderConstants parameterType;
	private ValueCompressorDecompressor comDec;
	private Object data;
	
	/**
	 * Creates a parameter reader/writer for the given parameter. 
	 * For a list of parameters go to {@link HeaderConstants}
	 * @param parameter
	 * @param comDec 
	 */
	public ParameterReaderWriter(String parameter) {
		this.parameterType = HeaderConstants.valueOf(parameter);
		this.comDec = this.parameterType.getValueComDec();
	}
	
	
	/**
	 * Compresses this parameter into the given stream. Will add the parameter code (a byte)
	 * plus the parameter argument.
	 * @param argument
	 * @param brw
	 */
	public final void compress(String argument, BitStreamDataReaderWriter brw) {
		this.compressParameterType(brw);
		this.compressData(argument, brw);
	}
	
	private final void compressParameterType(BitStreamDataReaderWriter brw) {
		brw.writeByte(parameterType.getCode());
	}
	
	
	/**
	 * Compresses the data of this parameter (given in the string variable)
	 * into the reader writer
	 * @param argument
	 * @param brw
	 */
	public void compressData(String argument, BitStreamDataReaderWriter brw) {
		this.comDec.compress(argument, brw);
	}
	
	
	/**
	 * Decompresses this parameter's data, which should start right where the
	 * {@link BitStreamDataReaderWriter} pointer is at. The parameter code should 
	 * already be read
	 * @param brw
	 */
	public void decompressData(BitStreamDataReaderWriter brw) {
		this.data = this.comDec.uncompress(brw);
	}
	
	/**
	 * @return the data decompressed with {@link #decompressData(BitStreamDataReaderWriter)}
	 */
	public Object getDecompressedData() {
		return this.data;
	}
	
	
	@Override
	public String toString() {
		return this.parameterType.toString() + " = " + this.data;
	}
	
}
