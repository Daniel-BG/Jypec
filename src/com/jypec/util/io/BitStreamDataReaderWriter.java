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
		this.writeNBitNumber(Float.floatToIntBits(f), 32);
	}
	
	/**
	 * @return the next 32 bits of the inner BitStream interpreted as a floating-point number
	 */
	public float readFloat() {
		int floatBits = this.readNBitNumber(32);
		return Float.intBitsToFloat(floatBits);
	}
	
	/**
	 * @param d double to be written into the inner BitStream
	 */
	public void writeDouble(double d) {
		long bits = Double.doubleToLongBits(d);
		int leftBits = (int) (bits >>> 32);
		int rightBits = (int) (bits & 0xffffffffl);
		this.writeNBitNumber(leftBits, 32);
		this.writeNBitNumber(rightBits, 32);
	}
	
	/**
	 * @return the next 64 bits of the inner BitStream as a Double precision number
	 */
	public double readDouble() {
		int leftBits = this.readNBitNumber(32);
		int rightBits = this.readNBitNumber(32);
		return Double.longBitsToDouble((((long) leftBits) << 32) | (((long) rightBits) & 0xffffffffl));
	}
	
	/**
	 * writes the given integer in the output stream
	 * @param i
	 */
	public void writeInt(int i) {
		this.writeNBitNumber(i, Integer.SIZE);
	}
	
	/**
	 * @return the next 4-byte integer in the stream 
	 */
	public int readInt() {
		return this.readNBitNumber(Integer.SIZE);
	}
	
	/**
	 * @return a 16-bit short number
	 */
	public short readShort() {
		return (short) this.readNBitNumber(Short.SIZE);
	}
	
	/**
	 * @param i an integer containing an unsigned short in the 16 less significant
	 * bits
	 */
	public void writeShort(short i) {
		this.writeNBitNumber(i, Short.SIZE);
	}
	
	/**
	 * @return an 8-bit byte number
	 */
	public byte readByte() {
		return (byte) this.readNBitNumber(Byte.SIZE); 
	}
	
	/**
	 * @param i the byte to be written
	 */
	public void writeByte(byte i) {
		this.writeNBitNumber(i, Byte.SIZE);
	}
	
	/**
	 * @param bits
	 * @return the specified number of bits inside of an integer
	 */
	public int readNBitNumber(int bits) {
		return this.stream.getBits(bits, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * writes the specified number of bits from the given binary int
	 * @param i
	 * @param bits
	 */
	public void writeNBitNumber(int i, int bits) {
		this.stream.putBits(i, bits, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	
	/**
	 * Writes a boolean to the inner stream, using 1 bit
	 * @param b
	 */
	public void writeBoolean(boolean b) {
		this.writeNBitNumber(b ? 1 : 0, 1);
	}
	
	/**
	 * @return the next bit in the inner stream, parsed as a boolean
	 */
	public boolean readBoolean() {
		return this.readNBitNumber(1) != 0;
	}
	
	/**
	 * Write the given number of elements from the given array
	 * @param array
	 * @param length
	 */
	public void writeDoubleArray(double[] array, int length) {
		for (int i = 0; i < length; i++) {
			this.writeDouble(array[i]);
		}
	}
	
	/**
	 * @param length
	 * @return an array contaning the specified number of elements, read from the inner stream
	 */
	public double[] readDoubleArray(int length) {
		double[] res = new double[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readDouble();
		}
		return res;
	}
	
	/**
	 * Write the given number of elements from the given array
	 * @param array
	 * @param length
	 */
	public void writeFloatArray(float[] array, int length) {
		for (int i = 0; i < length; i++) {
			this.writeFloat(array[i]);
		}
	}
	
	/**
	 * @param length
	 * @return an array contaning the specified number of elements, read from the inner stream
	 */
	public float[] readFloatArray(int length) {
		float[] res = new float[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readFloat();
		}
		return res;
	}


}
