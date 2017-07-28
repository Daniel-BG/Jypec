package com.jypec.img;

/**
 * Types of data that can be contained in the images and functions to treat them properly
 * @author Daniel
 *
 */
public enum ImageDataTypes {
	UNSIGNED_BYTE, SIGNED_BYTE, UNSIGNED_TWO_BYTE, SIGNED_TWO_BYTE;
	
	private static final int SIGNED_BYTE_MASK = 0xffffff80;
	private static final int SIGNED_BYTE_SIGN_BIT = 0x80;
	private static final int SIGNED_TWO_BYTE_MASK = 0xffff8000;
	private static final int SIGNED_TWO_BYTE_SIGN_BIT = 0x8000;
	private static final int UNSIGNED_BYTE_LIMIT = 0xff;
	private static final int UNSIGNED_TWO_BYTE_LIMIT = 0xffff;
	
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
	
	/**
	 * Clamp to the given interval (both ends inclusive)
	 * @param val
	 * @param lowerLim
	 * @param upperLim
	 * @return
	 */
	private int clampToInterval(int val, int lowerLim, int upperLim) {
		if (val < lowerLim) {
			val = lowerLim;
		} 
		if (val > upperLim) {
			val = upperLim;
		}
		return val;
	}
	
	/**
	 * Clamp the value to the range [-absoluteMax, absoluteMax]
	 * If the sign is negative, OR the result with the signmask
	 * @param val
	 * @param absoluteMax
	 * @param signMask
	 * @return the clamped value
	 */
	private int signedClampToRange(int val, int absoluteMax, int signMask) {
		int sign = 0;
		if (val < 0) {
			sign = signMask;
			val *= -1;
		}
		int magnitude = this.clampToInterval(val, 0, absoluteMax);
		return magnitude | sign;
	}

	/**
	 * Convert the given value to its data representation. If the given value falls outside of
	 * this data type's interval, it is clamped to the allowed interal
	 * @param value
	 * @return the bit representation of the given value according to this data Type
	 */
	public int valueToData(double value) {
		int val = (int) value;
		
		switch(this) {
		case UNSIGNED_BYTE:
			return this.clampToInterval(val, 0, UNSIGNED_BYTE_LIMIT);
		case UNSIGNED_TWO_BYTE:
			return this.clampToInterval(val, 0, UNSIGNED_TWO_BYTE_LIMIT);
		case SIGNED_BYTE:
			return this.signedClampToRange(val, SIGNED_BYTE_SIGN_BIT - 1, SIGNED_BYTE_SIGN_BIT);
		case SIGNED_TWO_BYTE:
			return this.signedClampToRange(val, SIGNED_BYTE_SIGN_BIT - 1, SIGNED_TWO_BYTE_SIGN_BIT);
		}
		
		return val;
	}

	/**
	 * @return the byte depth of this type, or -1 if its depth is not multiple of 8
	 */
	public int getByteDepth() {
		if (this.getBitDepth() % 8 != 0) {
			return -1;
		}
		
		return this.getBitDepth() / 8;
	}
	
	
}
