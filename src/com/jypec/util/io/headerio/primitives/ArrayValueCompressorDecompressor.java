package com.jypec.util.io.headerio.primitives;

import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Compress/Decompress arrays of data. Assumes a "{data1, data2, ...}" format. <br>
 * The inner data types cannot be arrays //TODO eventually add nested arrays
 * @author Daniel
 */
public class ArrayValueCompressorDecompressor extends ValueCompressorDecompressor {

	SingleValueCompressorDecompressor childComDec;
	
	/**
	 * Create an array value decompressor which stores values of the given type
	 * @param childComDec
	 */
	public ArrayValueCompressorDecompressor(SingleValueCompressorDecompressor childComDec) {
		this.childComDec = childComDec;
	}
	
	@Override
	public void compress(Object obj, BitStreamDataReaderWriter brw) {
		String str = obj.toString();
		//remove initial and ending braces, then split 
		str = str.substring(1, str.length() - 1);
		String[] values = str.split(",");
		//indicate array length
		brw.writeInt(values.length);
		for (String val: values) {
			childComDec.compress(val, brw);
		}
	}

	@Override
	public Object uncompress(BitStreamDataReaderWriter brw) {
		int len = brw.readInt();
		Object[] arr = new Object[len];
		for (int i = 0; i < len; i++) {
			arr[i] = childComDec.uncompress(brw);
		}
		return arr;
	}

}
