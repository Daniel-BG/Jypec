package com.jypec.util.io;

import com.jypec.util.BitStream;
import com.jypec.util.BitStream.BitStreamConstants;

/**
 * @author Daniel
 * Utility class for reading/storing numeric values from/to a BitStream object
 */
public class BitStreamDataReaderWriter {
	
	private BitStream stream;
	
	/**
	 * @param stream the stream to be used with this IO object
	 */
	public void setStream(BitStream stream) {
		this.stream = stream;
	}
	
	/**
	 * @param f float to be written into the inner BitStream
	 */
	public void writeFloat(float f) {
		this.stream.putBits(Float.floatToIntBits(f), 32, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * @return the next 32 bits of the inner BitStream interpreted as a floating-point number
	 */
	public float readFloat() {
		int floatBits = this.stream.getBits(32, BitStreamConstants.ORDERING_RIGHTMOST_FIRST);
		return Float.intBitsToFloat(floatBits);
	}
	
	/**
	 * @param d double to be written into the inner BitStream
	 */
	public void writeDouble(double d) {
		long bits = Double.doubleToLongBits(d);
		int leftBits = (int) (bits >>> 32);
		int rightBits = (int) (bits & 0xffffffffl);
		this.stream.putBits(leftBits, 32, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
		this.stream.putBits(rightBits, 32, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * @return the next 64 bits of the inner BitStream as a Double precision number
	 */
	public double readDouble() {
		int leftBits = this.stream.getBits(32, BitStreamConstants.ORDERING_RIGHTMOST_FIRST);
		int rightBits = this.stream.getBits(32, BitStreamConstants.ORDERING_RIGHTMOST_FIRST);
		return Double.longBitsToDouble((((long) leftBits) << 32) | (((long) rightBits) & 0xffffffffl));
	}
	
	/**
	 * writes the given integer in the output stream
	 * @param i
	 */
	public void writeInt(int i) {
		this.stream.putBits(i, 32, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * @return the next 4-byte integer in the stream 
	 */
	public int readInt() {
		return this.stream.getBits(32, BitStreamConstants.ORDERING_RIGHTMOST_FIRST);
	}

}
