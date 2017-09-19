package com.jypec.util.io.headerio.primitives;

import java.io.IOException;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * R/W for integers
 * @author Daniel
 */
public class IntegerValueCompressorDecompressor extends SingleValueCompressorDecompressor {

	private Integer value;

	@Override
	public void uncompress(BitInputStream brw) throws IOException {
		this.value = brw.readInt();
	}

	@Override
	public void parse(Object obj) {
		value = Integer.parseInt(obj.toString());
	}

	@Override
	public Object getObject() {
		return value;
	}

	@Override
	public void compress(BitOutputStream brw) throws IOException {
		brw.writeInt(value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (Integer) obj;
	}

}
