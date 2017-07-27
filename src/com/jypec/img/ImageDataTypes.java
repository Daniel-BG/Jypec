package com.jypec.img;

/**
 * Types of data that can be contained in the images and functions to treat them properly
 * @author Daniel
 *
 */
public enum ImageDataTypes {
	UNSIGNED_BYTE, SIGNED_BYTE, UNSIGNED_TWO_BYTE, SIGNED_TWO_BYTE;
	
	private static final int SIGNED_BYTE_MASK = 0xffffff80;
	private static final int SIGNED_TWO_BYTE_MASK = 0xffff8000;
	
	/**
	 * @return this type's bit depth
	 */
	public int getBitDepth() {
		switch(this) {
		case UNSIGNED_BYTE:
		case SIGNED_BYTE:
			return 8;
		case UNSIGNED_TWO_BYTE:
		case SIGNED_TWO_BYTE:
			return 16;
		}
		return 0;
	}
	
	/**
	 * @param data
	 * @param mask
	 * @return take data & mask as a the sign. data &~mask as the magnitude. Assemble it in a two's compliment integer
	 */
	public int signMaskToValue(int data, int mask) {
		int sign = data & mask;
		int magnitude = data & (~mask);
		return sign != 0 ? -magnitude : magnitude;
	}
	
	/**
	 * @param data
	 * @return the value of the given data if it is of this object's type
	 */
	public int dataToValue(int data) {
		switch(this) {
		case UNSIGNED_BYTE:
		case UNSIGNED_TWO_BYTE:
			return data;
		case SIGNED_BYTE:
			return signMaskToValue(data, SIGNED_BYTE_MASK);
		case SIGNED_TWO_BYTE:
			return signMaskToValue(data, SIGNED_TWO_BYTE_MASK);
		}
		return 0;
	}
	
	
}
