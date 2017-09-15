package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Compresses/Decompresses floats
 * @author Daniel
 * @see {@link ValueCompressorDecompressor}
 */
public class FloatValueCompressorDecompressor extends SingleValueCompressorDecompressor {

	private Float value;

	@Override
	public void uncompress(BitStreamDataReaderWriter brw) {
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
	public int compress(BitStreamDataReaderWriter brw) {
		return brw.writeFloat(this.value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (Float) obj;
	}

}
