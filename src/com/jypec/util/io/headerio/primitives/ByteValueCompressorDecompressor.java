package com.jypec.util.io.headerio.primitives;

import java.io.IOException;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Compresses/uncompresses a byte (8 bit) value
 * @author Daniel
 *
 */
public class ByteValueCompressorDecompressor extends SingleValueCompressorDecompressor {
	
	private Byte value;

	@Override
	public void uncompress(BitInputStream brw) throws IOException {
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
	public void compress(BitOutputStream brw) throws IOException {
		brw.writeByte(this.value);
	}

	@Override
	public void setObject(Object obj) {
		this.value = (Byte) obj;
	}

}
