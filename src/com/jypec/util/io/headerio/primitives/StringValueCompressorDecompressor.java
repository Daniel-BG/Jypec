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
	public int compress(BitStreamDataReaderWriter brw) {
		int res = 0;
		res += brw.writeInt(value.length());
		res += brw.writeString(value);
		return res;
	}

	@Override
	public void setObject(Object obj) {
		this.value = (String) obj;
	}

}
