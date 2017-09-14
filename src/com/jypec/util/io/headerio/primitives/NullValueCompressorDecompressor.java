package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Dummy class for storing things that are flags in the compressed stream
 * @author Daniel
 */
public class NullValueCompressorDecompressor extends ValueCompressorDecompressor {

	@Override
	public void parse(Object obj) {}

	@Override
	public Object getObject() {
		return null;
	}

	@Override
	public void setObject(Object obj) {}

	@Override
	public void compress(BitStreamDataReaderWriter brw) {}

	@Override
	public void uncompress(BitStreamDataReaderWriter brw) {}

	@Override
	public String unParse() {
		return null;
	}

}
