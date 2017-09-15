package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Compresses/uncompresses a byte (8 bit) value
 * @author Daniel
 *
 */
public class ByteValueCompressorDecompressor extends SingleValueCompressorDecompressor {
	
	private Byte value;

	@Override
	public void uncompress(BitStreamDataReaderWriter brw) {
		this.value = new Byte(brw.readByte());
	}

	@Override
	public void parse(Object obj) {
		this.value = (byte) Integer.parseInt(obj.toString());
	}

	@Override
	public Object getObject() {
		return this.value;
	}

	@Override
	public int compress(BitStreamDataReaderWriter brw) {
		return brw.writeByte(this.value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (Byte) obj;
	}

}
