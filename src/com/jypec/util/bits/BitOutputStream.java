package com.jypec.util.bits;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps around an output stream providing bit-wise functionality
 * @author Daniel
 */
public class BitOutputStream extends OutputStream {

	@Override
	/**
	 * Flush fully formed bytes to the output. 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		this.stream.flush();
	}
	
	/**
	 * Adds padding to the remaining bits (if present) to make a multiple of a byte, 
	 * then flushes the underlying stream
	 * @throws IOException
	 */
	public void paddingFlush() throws IOException {
		//flush remaining bits padding with zeroes
		if (this.bufferSize > 0) {
			this.writeNBitNumber(0, 8 - this.bufferSize);
		}
		this.flush();
	}

	@Override
	public void close() throws IOException {
		this.stream.close();
	}

	@Override
	public void write(int b) throws IOException {
		this.writeByte((byte) b);
	}
	
	
	private OutputStream stream;
	private int buffer;
	private int bufferSize;
	private int bitsOutput;
	
	/**
	 * Create a bit output wrapper around the given outputstream
	 * @param stream
	 */
	public BitOutputStream(OutputStream stream) {
		this.stream = stream;
		this.buffer = 0;
		this.bufferSize = 0;
		this.bitsOutput = 0;
	}
	
	

	/**
	 * Adds a single bit to this stream. A zero is Bit.BIT_ZERO and a one is Bit.BIT_ONE
	 * @param bit
	 * @throws IOException 
	 */
	public void putBit(int bit) throws IOException {
		if (bit != 0 && bit != 1) {
			throw new IllegalStateException("Whoops @FIFOBitStream.putBit");
		}
		bit = Bit.normalize(bit);
		
		this.buffer <<= 1;
		this.buffer |= bit;
		this.bufferSize ++;
		if (this.bufferSize == 8) {
			this.stream.write(this.buffer);
			this.buffer = 0;
			this.bufferSize = 0;
		}
		this.bitsOutput++;
	}


	/**
	 * Adds a Bit object to the stream
	 * @param bit
	 * @throws IOException 
	 */
	public void putBit(Bit bit) throws IOException {
		this.putBit(bit.toInteger());
	}


	/**
	 * @param bits
	 * @param quantity
	 * @param ordering
	 * @throws IOException 
	 */
	public void putBits(int bits, int quantity, BitStreamConstants ordering) throws IOException {
		switch (ordering) {
		case ORDERING_LEFTMOST_FIRST:
			//adjust the bits so that the first one is in the leftmost position
			bits <<= Integer.SIZE - quantity;
			for (int i = 0; i < quantity; i++) {
				putBit(Bit.fromInteger(bits & BitStreamConstants.INT_LEFT_BIT_MASK));
				bits <<= 1;
			}
			break;
		case ORDERING_RIGHTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				putBit(Bit.fromInteger(bits & BitStreamConstants.INT_RIGHT_BIT_MASK));
				bits >>= 1;
			}
			break;
		}
	}
	
	
	/**
	 * writes the specified number of bits from the given binary int
	 * @param i
	 * @param bits
	 * @throws IOException 
	 */
	public void writeNBitNumber(int i, int bits) throws IOException {
		this.putBits(i, bits, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * @param i the byte to be written
	 * @throws IOException 
	 */
	public void writeByte(byte i) throws IOException {
		if (this.bufferSize == 0) {
			this.stream.write(i);
			this.bitsOutput += 8;
		} else {
			this.writeNBitNumber(i, Byte.SIZE);
		}
	}
	
	/**
	 * @param f float to be written into the inner BitStream
	 * @throws IOException 
	 */
	public void writeFloat(float f) throws IOException {
		this.writeInt(Float.floatToIntBits(f));
	}
	
	/**
	 * @param d double to be written into the inner BitStream
	 * @throws IOException 
	 */
	public void writeDouble(double d) throws IOException {
		long bits = Double.doubleToLongBits(d);
		int leftBits = (int) (bits >>> 32);
		int rightBits = (int) (bits & 0xffffffffl);
		this.writeInt(leftBits);
		this.writeInt(rightBits);
	}
	
	/**
	 * writes the given integer in the output stream,
	 * @param i
	 * @throws IOException 
	 */
	public void writeInt(int i) throws IOException {
		this.writeByte((byte) (i >> 24));
		this.writeByte((byte) (i >> 16));
		this.writeByte((byte) (i >> 8));
		this.writeByte((byte) i);
	}
	
	/**
	 * @param i an integer containing an unsigned short in the 16 less significant
	 * bits
	 * @throws IOException 
	 */
	public void writeShort(short i) throws IOException {
		this.writeByte((byte) (i >> 8));
		this.writeByte((byte) i);
	}
	
	/**
	 * @param c the character to be written
	 * @throws IOException 
	 */
	private void writeChar(char c) throws IOException {
		this.writeByte((byte) (c >> 8));
		this.writeByte((byte) c);
	}
	
	/**
	 * Writes a boolean to the inner stream, using 1 bit
	 * @param b
	 * @throws IOException 
	 */
	public void writeBoolean(boolean b) throws IOException {
		this.writeNBitNumber(b ? 1 : 0, 1);
	}
	
	/**
	 * Write the given number of elements from the given array
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeDoubleArray(double[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeDouble(array[i]);
		}
	}

	/**
	 * Write the given number of elements from the given array
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeFloatArray(float[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeFloat(array[i]);
		}
	}

	/**
	 * Writes the given array up to the given position
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeByteArray(byte[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeByte(array[i]);
		}
	}
	
	/**
	 * Writes the given array up to the given position
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeCharArray(char[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeChar(array[i]);
		}
	}

	/**
	 * Writes the given string
	 * @param val
	 * @throws IOException 
	 */
	public void writeString(String val) throws IOException {
		byte[] chars = val.getBytes(StandardCharsets.US_ASCII);
		this.writeByteArray(chars, chars.length);
	}
	

	/**
	 * @return the number of bits output so far
	 */
	public int getBitsOutput() {
		return this.bitsOutput;
	}
	

}
