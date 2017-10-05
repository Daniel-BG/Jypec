package com.jypec.img;

import com.jypec.util.MathOperations;

/**
 * Types of data that can be contained in the images and functions to treat them properly
 * @author Daniel
 *
 */
public class ImageDataType {
	/** represents a unsigned byte data type (from 0 to 255) */
	public static final ImageDataType UNSIGNED_BYTE = new ImageDataType(8, false); 
	/** represents a signed byte data type in sign-magnitude form (from -127 to 127) */
	public static final ImageDataType SIGNED_BYTE = new ImageDataType(8, true);
	/** represents a unsigned two-byte data type (from 0-65535) */
	public static final ImageDataType UNSIGNED_TWO_BYTE = new ImageDataType(16, false); 
	/** represents a signed two-byte data type in sign-magnitude form (from -32767 to 32767) */
	public static final ImageDataType SIGNED_TWO_BYTE = new ImageDataType(16, true); 
	/** signed 4 byte */
	public static final ImageDataType SIGNED_FOUR_BYTE = new ImageDataType(32, true);
	/** unsigned 4 byte */
	public static final ImageDataType UNSIGNED_FOUR_BYTE = new ImageDataType(32, true);
	
	
	private int bitDepth;
	private int magnitudeDepth;
	private int magnitudeLimit;
	private int signMask;
	private int signBit;
	private boolean signed;
	
	
	/**
	 * Build a data type of the specified bit depth, indicating if it is signed or not.
	 * These data types are on sign-magnitude form, and the sign bit is COUNTED in the
	 * bitDepth parameter. <br>
	 * E.g: bitDepth=8, signed=true means 1 sign bit + 7 magnitude bits
	 * @param bitDepth
	 * @param signed
	 */
	public ImageDataType(int bitDepth, boolean signed) {
		this.setup(bitDepth, signed);
	}
	
	private void setup(int bitDepth, boolean signed) {
		if (bitDepth == 0 || bitDepth > 32) {
			throw new IllegalArgumentException("Data type not valid. Bit depth is between 1 and 32 both inclusive. (Tried with: " + bitDepth + ")");
		}
		this.bitDepth = bitDepth;
		this.signed = signed;
		this.magnitudeDepth = this.bitDepth - (this.signed ? 1 : 0);
		this.magnitudeLimit = (0x1 << this.magnitudeDepth) - 1;
		this.signBit = this.signed ? (0x1 << this.magnitudeDepth) : 0;
		this.signMask = 0xffffffff << this.magnitudeDepth;
	}
	
	
	/**
	 * @return this type's bit depth. Sign is included here. So bitDepth of 8 signed is 7 magnitude + 1 sign
	 */
	public int getBitDepth() {
		return this.bitDepth;
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
		if (this.signed) {
			return signMaskToValue(data, this.signMask);
		} else {
			return data;
		}
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
	public int valueToData(float value) {
		int val = (int) value;
		
		if (this.signed) {
			return this.signedClampToRange(val, this.magnitudeLimit, this.signBit); 	
		} else {
			return this.clampToInterval(val, 0, this.magnitudeLimit);
		}

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
	
	/**
	 * @return the number of different values this image data type can have
	 */
	public int getDynamicRange() {
		if (this.signed) {
			return this.magnitudeLimit * 2 + 1;
		} else {
			return this.magnitudeLimit;
		}
	}
	
	/**
	 * @return true if this data type is signed, i.e. it accepts negative numbers
	 */
	public boolean isSigned() {
		return this.signed;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ImageDataType) {
			ImageDataType other = (ImageDataType) obj;
			return this.bitDepth == other.bitDepth && this.signed == other.signed;
		}
		return false;
	}


	/**
	 * @return the maximum value this data type can store (positive or negative)
	 */
	public int getAbsoluteMaxValue() {
		return this.magnitudeLimit;
	}
	
	/**
	 * @return the maximum value this data type can have
	 */
	public int getMaxValue() {
		return this.magnitudeLimit;
	}

	/**
	 * @return the minimum value this data type can have
	 */
	public int getMinValue() {
		return this.isSigned() ? -this.magnitudeLimit : 0;
	}


	/**
	 * @param newMinVal
	 * @param newMaxVal
	 * @param extra extra bits to add over the minimum detected
	 * @return a data type that can fit the whole given range in the least bits possible
	 */
	public static ImageDataType findBest(int newMinVal, int newMaxVal, int extra) {
		/*if (newMinVal > 0 || newMaxVal < 0) {
			System.err.println("@ImageDataType.findBest: These values will probably work but are not ideal. Maybe fix this to shift the values to the [0, max-min] range");
		}*/
		
		int absMax = Math.max(Math.abs(newMaxVal), Math.abs(newMinVal));
		boolean signed = newMinVal < 0;
		int bits = extra + (int) Math.ceil(MathOperations.logBase(absMax, 2f));

		return new ImageDataType(bits, signed);
	}
	
	
	/**
	 * Change the precision of this type
	 * @param precisionChange the change in bit depth precision
	 */
	public void mutatePrecision(int precisionChange) {
		this.setup(this.bitDepth + precisionChange, this.signed);
	}

	
	@Override
	public String toString() {
		return this.bitDepth + "bit " + (this.signed ? "signed" : "unsigned");
	}
	
	
	/**
	 * @param code
	 * @return the image data type for the given header code
	 */
	public static ImageDataType fromHeaderCode(byte code) {
		switch((int)code) {
		case 1:
			return ImageDataType.UNSIGNED_BYTE;
		case 2:
			return ImageDataType.SIGNED_TWO_BYTE;
		case 3: 
			return ImageDataType.SIGNED_FOUR_BYTE;
		case 12:
			return ImageDataType.UNSIGNED_TWO_BYTE;
		case 13:
			return ImageDataType.UNSIGNED_FOUR_BYTE;
			
		case 4: //float 32 bit
		case 5: //float 64 bit
		case 6: //complex pair of 32 bits
		case 9: //complex pair of 64 bits
		case 14: //signed 64 bit long
		case 15: //unsigned 64 bit long
		default:
			throw new UnsupportedOperationException("This data type is not yet supported: " + code);
		}
	}
}
