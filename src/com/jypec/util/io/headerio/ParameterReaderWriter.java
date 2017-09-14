package com.jypec.util.io.headerio;

import com.jypec.util.bits.BitStreamDataReaderWriter;
import com.jypec.util.io.headerio.primitives.ValueCompressorDecompressor;

/**
 * Reads and/or writes envi header parameters from compressed to uncompressed
 * form and vice versa
 * @author Daniel
 */
public class ParameterReaderWriter {


	private HeaderConstants headerConstant;
	private ValueCompressorDecompressor comDec;
	
	/**
	 * Creates a parameter reader/writer for the given parameter. 
	 * For a list of parameters go to {@link HeaderConstants}
	 * @param parameter
	 * @param comDec 
	 */
	public ParameterReaderWriter(String parameter) {
		this.headerConstant = HeaderConstants.fromString(parameter);
		this.setUp();
	}
	
	/**
	 * Create a parameter reader/writer for the specified header constant
	 * @param hc
	 */
	public ParameterReaderWriter(HeaderConstants hc) {
		this.headerConstant = hc;
		this.setUp();
	}
	
	
	private void setUp() {
		this.comDec = this.headerConstant.getValueComDec();
	}
	
	
	/**
	 * Compresses this parameter into the given stream. Will add the parameter code (a byte)
	 * plus the parameter argument.
	 * @param argument
	 * @param brw
	 */
	public final void compress(BitStreamDataReaderWriter brw) {
		this.compressParameterType(brw);
		this.compressData(brw);
	}
	
	private final void compressParameterType(BitStreamDataReaderWriter brw) {
		brw.writeByte(headerConstant.getCode());
	}
	
	/**
	 * Reads a byte from the bitstream, creating the ParameterReaderWriter
	 * for that code. Then it reads the parameter. The Stream is left at the 
	 * end of the reading
	 * @param brw
	 * @return the parameterReaderWriter for the next parameter, with the parameter read
	 */
	public static ParameterReaderWriter readNextCompressedParameter(BitStreamDataReaderWriter brw) {
		int type = brw.readByte();
		if (type < 0 || type > HeaderConstants.values().length) {
			throw new IllegalStateException("I do not recognize the parameter code");
		}
		ParameterReaderWriter prw = new ParameterReaderWriter(HeaderConstants.values()[type]);
		prw.decompressData(brw);
		return prw;
	}
	
	/**
	 * Parses the data but does not compress it
	 * @param data to be parsed
	 */
	public void parseData(String data) {
		this.comDec.parse(data);
	}
	
	/**
	 * Compresses the data parsed with {@link #parseData(String)}
	 * @param argument
	 * @param brw
	 */
	private void compressData(BitStreamDataReaderWriter brw) {
		this.comDec.compress(brw);
	}
	
	
	/**
	 * Decompresses this parameter's data, which should start right where the
	 * {@link BitStreamDataReaderWriter} pointer is at. The parameter code should 
	 * already be read
	 * @param brw
	 */
	public void decompressData(BitStreamDataReaderWriter brw) {
		this.comDec.uncompress(brw);
	}
	
	/**
	 * @return the data processed with {@link #decompressData(BitStreamDataReaderWriter)} 
	 * or {@link #parseData(String)}
	 */
	public Object getData() {
		return this.comDec.getObject();
	}
	
	/**
	 * Sets the data to be written
	 * @param data
	 */
	public void setData(Object data) {
		this.comDec.setObject(data);
	}
	
	/**
	 * @return the type of header constant this ParameterReaderWriter reads/writes
	 */
	public HeaderConstants getHeaderConstant() {
		return this.headerConstant;
	}
	
	@Override
	public String toString() {
		return this.headerConstant.toString() + " = " + this.comDec.unParse();
	}


	
}
