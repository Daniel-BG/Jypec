package com.jypec.util.io.headerio.primitives;

import java.io.IOException;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Compress/Decompress arrays of data. Assumes a "{data1, data2, ...}" format. <br>
 * The inner data types cannot be arrays //TODO eventually add nested arrays
 * @author Daniel
 */
public class ArrayValueCompressorDecompressor extends ValueCompressorDecompressor {

	SingleValueCompressorDecompressor childComDec;
	private Object[] values;
	
	/**
	 * Create an array value decompressor which stores values of the given type
	 * @param childComDec
	 */
	public ArrayValueCompressorDecompressor(SingleValueCompressorDecompressor childComDec) {
		this.childComDec = childComDec;
	}

	@Override
	public void uncompress(BitInputStream brw) throws IOException {
		int len = brw.readInt();
		this.values = new Object[len];
		for (int i = 0; i < len; i++) {
			childComDec.uncompress(brw);
			this.values[i] = childComDec.getObject();
		}
	}

	@Override
	public void parse(Object obj) {
		String str = obj.toString();
		//remove initial and ending braces, then split 
		str = str.substring(1, str.length() - 1).trim();
		String[] strings = str.split(",");
		
		this.values = new Object[strings.length];
		for (int i = 0; i < values.length; i++) {
			childComDec.parse(strings[i]);
			values[i] = childComDec.getObject();
		}
	}

	@Override
	public Object getObject() {
		return this.values;
	}

	@Override
	public void compress(BitOutputStreamTree brw) throws IOException {
		brw.addChild("len").writeInt(this.values.length);
		BitOutputStreamTree bstn = brw.addChild("array");
		for (int i = 0; i < this.values.length; i++) {
			childComDec.setObject(this.values[i]);
			childComDec.compress(bstn);
		}
	}

	@Override
	public void setObject(Object obj) {
		this.values = (Object[]) obj;
	}

	@Override
	public String unParse() {
		String res = "{";
		for (int i = 0; i < this.values.length; i++) {
			childComDec.setObject(values[i]);
			if (i < this.values.length - 1) {
				res += childComDec.unParse() + ",";
			} else {
				res += childComDec.unParse();
			}
		}
		
		return res + "}";
	}

}
