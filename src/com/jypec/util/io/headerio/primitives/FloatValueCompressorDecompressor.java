package com.jypec.util.io.headerio.primitives;

import java.io.IOException;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;


/**
 * Compresses/Decompresses floats
 * @author Daniel
 * @see {@link ValueCompressorDecompressor}
 */
public class FloatValueCompressorDecompressor extends SingleValueCompressorDecompressor {

	private Float value;

	@Override
	public void uncompress(BitInputStream brw) throws IOException {
		this.value = new Float(brw.readFloat());
	}

	@Override
	public void parse(Object obj) {
		this.value = Float.parseFloat(obj.toString());
	}

	@Override
	public Object getObject() {
		return this.value;
	}

	@Override
	public void compress(BitOutputStream brw) throws IOException {
		brw.writeFloat(this.value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (Float) obj;
	}

}
