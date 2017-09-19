package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

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
	public void compress(BitOutputStream brw) {}

	@Override
	public void uncompress(BitInputStream brw) {}

	@Override
	public String unParse() {
		return null;
	}

}
