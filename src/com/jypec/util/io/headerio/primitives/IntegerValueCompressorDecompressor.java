package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * R/W for integers
 * @author Daniel
 */
public class IntegerValueCompressorDecompressor extends SingleValueCompressorDecompressor {

	@Override
	public void compress(Object obj, BitStreamDataReaderWriter brw) {
		brw.writeInt(Integer.parseInt(obj.toString()));
	}

	@Override
	public Object uncompress(BitStreamDataReaderWriter brw) {
		return brw.readInt();
	}

}
