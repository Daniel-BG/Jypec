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
	private int byteSize;
	
	
	/**
	 * Create a value compressor decompressor for the given enum.
	 * @param type the class of the parametrized type
	 */
	public EnumValueCompressorDecompressor(Class<T> type) {
		this.type = type;
		int numberOfEnums = type.getEnumConstants().length;
		if (numberOfEnums <= 255) {
			byteSize = 1;
		} else if (numberOfEnums <= 65535) {
			byteSize = 2;
		} else {
			byteSize = 4;
		}
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
		int ordinal = this.value.ordinal();
		if (byteSize == 1) {
			brw.writeByte((byte) ordinal);	
		} else if (byteSize == 2) {
			brw.writeShort((short) ordinal);
		} else {
			brw.writeInt(ordinal);
		}
	}

	@Override
	public void uncompress(BitInputStream brw) throws IOException {
		int index;
		if (byteSize == 1) {
			index = brw.readByte() & 0xff;	
		} else if (byteSize == 2) {
			index = brw.readShort() & 0xffff;
		} else {
			index = brw.readInt();
		}
		this.value = type.getEnumConstants()[index];
	}
	
	


}
