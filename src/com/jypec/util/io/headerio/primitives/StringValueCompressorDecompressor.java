package com.jypec.util.io.headerio.primitives;

import java.io.IOException;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Com/Dec for Strings
 * @author Daniel
 *
 */
public class StringValueCompressorDecompressor extends SingleValueCompressorDecompressor {

	private String value;

	@Override
	public void uncompress(BitInputStream brw) throws IOException {
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
	public void compress(BitOutputStream brw) throws IOException {
		brw.writeInt(value.length());
		brw.writeString(value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (String) obj;
	}

}
