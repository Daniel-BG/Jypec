package com.jypec.util.bits;

import java.nio.charset.StandardCharsets;

import com.jypec.util.bits.BitStream.BitStreamConstants;

/**
 * @author Daniel
 * Utility class for reading/storing numeric values from/to a BitStream object
 */
public class BitStreamDataReaderWriter {
	
	private BitStream stream;
	

	
	/**
	 * Create a readerwriter over the given stream
	 * @param stream
	 */
	public BitStreamDataReaderWriter(BitStream stream) {
		this.setStream(stream);
	}
	
	/**
	 * @param stream the stream to be used with this IO object
	 */
	public void setStream(BitStream stream) {
		this.stream = stream;
	}
	
	/**
	 * @param f float to be written into the inner BitStream
	 * @return the number of bits used to write it
	 */
	public int writeFloat(float f) {
		this.writeNBitNumber(Float.floatToIntBits(f), Integer.SIZE);
		return Integer.SIZE;
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
	 * writes the given integer in the output stream,
	 * @param i
	 * @return the number of bits used
	 */
	public int writeInt(int i) {
		this.writeNBitNumber(i, Integer.SIZE);
		return Integer.SIZE;
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
	 * @return the number of bits used to write it
	 */
	public int writeByte(byte i) {
		this.writeNBitNumber(i, Byte.SIZE);
		return Byte.SIZE;
	}
	
	/**
	 * @return a character
	 */
	private char readChar() {
		return (char) this.readNBitNumber(Character.SIZE);
	}
	
	/**
	 * @param c the character to be written
	 */
	private void writeChar(char c) {
		this.writeNBitNumber(c, Character.SIZE);
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

	/**
	 * Writes the given array up to the given position
	 * @param array
	 * @param length
	 * @return the number of bits used to write the array
	 */
	public int writeByteArray(byte[] array, int length) {
		for (int i = 0; i < length; i++) {
			this.writeByte(array[i]);
		}
		return Byte.SIZE * length;
	}
	
	/**
	 * Reads an array of the specified length
	 * @param length
	 * @return the read array
	 */
	public byte[] readByteArray(int length) {
		byte[] res = new byte[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readByte();
		}
		return res;
	}
	
	/**
	 * Writes the given array up to the given position
	 * @param array
	 * @param length
	 */
	public void writeCharArray(char[] array, int length) {
		for (int i = 0; i < length; i++) {
			this.writeChar(array[i]);
		}
	}
	
	/**
	 * Reads an array of the specified length
	 * @param length
	 * @return the read array
	 */
	public char[] readCharArray(int length) {
		char[] res = new char[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readChar();
		}
		return res;
	}
	
	/**
	 * Writes the given string
	 * @param val
	 * @return the number of bits used to write the array
	 */
	public int writeString(String val) {
		byte[] chars = val.getBytes(StandardCharsets.US_ASCII);
		return this.writeByteArray(chars, chars.length);
	}
	
	/**
	 * Reads a String of the specified length
	 * @param length
	 * @return the read string
	 */
	public String readString(int length) {
		byte[] chars = this.readByteArray(length);
		return new String(chars, StandardCharsets.US_ASCII);
	}
	
	/**
	 * @return the number of bytes still available
	 */
	public int availableBytes() {
		return this.stream.getNumberOfBits() >> 3;
	}
	
	/**
	 * @return the number of bits still available
	 */
	public int availableBits() {
		return this.stream.getNumberOfBits();
	}
	
}
