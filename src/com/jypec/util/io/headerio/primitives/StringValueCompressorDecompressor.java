package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Com/Dec for Strings
 * @author Daniel
 *
 */
public class StringValueCompressorDecompressor extends SingleValueCompressorDecompressor {

	private String value;

	@Override
	public void uncompress(BitStreamDataReaderWriter brw) {
		int len = brw.readInt();
		value =  brw.readString(len);
	}

	@Override
	public void parse(Object obj) {
		value = obj.toString().trim();
	}

	@Override
	public Object getObject() {
		return value;
	}

	@Override
	public void compress(BitStreamDataReaderWriter brw) {
		brw.writeInt(value.length());
		brw.writeString(value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (String) obj;
	}

}
