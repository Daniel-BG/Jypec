package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * R/W for integers
 * @author Daniel
 */
public class IntegerValueCompressorDecompressor extends SingleValueCompressorDecompressor {

	private Integer value;

	@Override
	public void uncompress(BitStreamDataReaderWriter brw) {
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
	public int compress(BitStreamDataReaderWriter brw) {
		return brw.writeInt(value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (Integer) obj;
	}

}
