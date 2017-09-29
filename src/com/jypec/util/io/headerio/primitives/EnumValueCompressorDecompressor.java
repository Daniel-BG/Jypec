package com.jypec.util.io.headerio.primitives;

import java.io.IOException;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Class to compress/decompress header fields that are enums
 * @author Daniel
 * @param <T> enum class
 */
public class EnumValueCompressorDecompressor<T extends Enum<T>> extends SingleValueCompressorDecompressor {

	private T value;
	private final Class<T> type;
	
	/**
	 * Create a value compressor decompressor for the given enum.
	 * @param type the class of the parametrized type
	 */
	public EnumValueCompressorDecompressor(Class<T> type) {
		this.type = type;
	}
	
	
	@Override
	public void parse(Object obj) {
		this.value = T.valueOf(type, obj.toString().toUpperCase().trim());
		
	}

	@Override
	public Object getObject() {
		return this.value;
	}

	@Override
	public void setObject(Object obj) {
		//toString then parse to avoid type impossible type checking with
		//this.value = (T) obj;
		this.parse(obj);
	}

	@Override
	public void compress(BitOutputStream brw) throws IOException {
		brw.writeEnum(type, value, true);
	}

	@Override
	public void uncompress(BitInputStream brw) throws IOException {
		this.value = type.cast(brw.readEnum(type, true));
	}
	
	


}
