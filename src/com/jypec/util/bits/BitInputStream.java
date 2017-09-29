package com.jypec.util.bits;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps around an InputStream to provide bit-wise functionality
 * @author Daniel
 * @see BitOutputStream
 */
public class BitInputStream extends InputStream {	

	@Override
	public int available() throws IOException {
		return this.source.available();
	}

	@Override
	public void close() throws IOException {
		this.source.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		this.source.mark(readlimit);
		this.markedLastBitsRead = this.lastBitsRead;
		this.markedAvailableBits = this.availableBits;
		this.markedBuffer = this.buffer;
		this.markedBitsInput = this.bitsInput;
	}

	@Override
	public synchronized void reset() throws IOException {
		this.availableBits = this.markedAvailableBits;
		this.buffer = this.markedBuffer;
		this.lastBitsRead = this.markedLastBitsRead;
		this.bitsInput = this.markedBitsInput;
		this.source.reset();
	}

	@Override
	public boolean markSupported() {
		return this.source.markSupported();
	}
	
	@Override
	public int read() throws IOException {
		return ((int) this.readByte()) & 0xff;
	}
	
	

	private InputStream source;
	private int buffer, markedBuffer;
	private int availableBits, markedAvailableBits;
	private int lastBitsRead, markedLastBitsRead;
	private int bitsInput, markedBitsInput;
	
	
	/**
	 * @param source the parent stream from which this takes information
	 */
	public BitInputStream(InputStream source) {
		this.source = source;
		this.availableBits = 0;
		this.buffer = 0;
		this.lastBitsRead = 0;
		this.bitsInput = 0;
	}
	
	/**
	 * @return the next bit from the input stream
	 * @throws IOException 
	 */
	public Bit getBit() throws IOException {
		return Bit.fromInteger(this.readBitAsInt());
	}
	
	/**
	 * @return the next bit from the input stream as an integer (a zero means the bit ZERO, anything else is a ONE)
	 * @throws IOException 
	 */
	public int readBitAsInt() throws IOException {
		if (availableBits == 0) {
			this.buffer = this.source.read();
			if (this.buffer == -1) {
				throw new IOException("More bytes were requested than available!");
			}
			this.availableBits = 8;
		}
		int result = Bit.normalize(this.buffer & BitStreamConstants.BYTE_LEFT_BIT_MASK);
		this.lastBitsRead <<= 1;
		this.lastBitsRead |= result;
		this.buffer <<= 1;
		this.availableBits--;
		this.bitsInput++;
		return result;	 
	}
	
	/**
	 * @param quantity
	 * @param ordering
	 * @return the specified number of bits
	 * @throws IOException 
	 */
	public int readBits(int quantity, BitStreamConstants ordering) throws IOException {
		int result = 0;
		switch (ordering) {
		case ORDERING_RIGHTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				result >>= 1;
				result &= BitStreamConstants.INT_LEFT_BIT_MASK;
				result |= readBitAsInt() << (Integer.SIZE - 1);
			}
			result >>>= (Integer.SIZE - quantity);
			break;
		case ORDERING_LEFTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				result <<= 1;
				result |= readBitAsInt();
			}
			break;
		}
		
		return result;
	}
	
	/**
	 * @param quantity
	 * @return the next n bit number using {@link BitStreamConstants#ORDERING_LEFTMOST_FIRST}
	 * @throws IOException
	 */
	public int readNBitNumber(int quantity) throws IOException {
		return this.readNBitNumber(quantity, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * @param quantity the quantity of bits to read
	 * @param ordering the ordering in which to read the bits {@link BitStreamConstants}
	 * @return the specified number of bits inside of an integer
	 * @throws IOException 
	 */
	public int readNBitNumber(int quantity, BitStreamConstants ordering) throws IOException {
		int result = 0;
		switch (ordering) {
		case ORDERING_RIGHTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				result >>= 1;
				result &= BitStreamConstants.INT_LEFT_BIT_MASK;
				result |= readBitAsInt() << (Integer.SIZE - 1);
			}
			result >>>= (Integer.SIZE - quantity);
			break;
		case ORDERING_LEFTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				result <<= 1;
				result |= readBitAsInt();
			}
			break;
		}
		
		return result;
	}
	
	/**
	 * @return the next 32 bits of the inner BitStream interpreted as a floating-point number
	 * @throws IOException 
	 */
	public float readFloat() throws IOException {
		int floatBits = this.readInt();
		return Float.intBitsToFloat(floatBits);
	}
	
	/**
	 * @return the next 64 bits of the inner BitStream as a Double precision number
	 * @throws IOException 
	 */
	public double readDouble() throws IOException {
		int leftBits = this.readInt();
		int rightBits = this.readInt();
		return Double.longBitsToDouble((((long) leftBits) << 32) | (((long) rightBits) & 0xffffffffl));
	}

	
	/**
	 * @return the next 4-byte integer in the stream 
	 * @throws IOException 
	 */
	public int readInt() throws IOException {
		return (this.read() << 24) | (this.read() << 16) | (this.read() << 8) | this.read();
	}
	
	/**
	 * @return a 16-bit short number
	 * @throws IOException 
	 */
	public short readShort() throws IOException {
		return (short) ((this.read() << 8) | this.read());
	}

	/**
	 * @return an 8-bit byte number
	 * @throws IOException 
	 */
	public byte readByte() throws IOException {
		//bypass bit by bit reading for faster computation
		if (this.availableBits == 0) {
			int res = this.source.read();
			if (res == -1) {
				throw new IOException("More bytes requested than available");
			}
			this.lastBitsRead <<= 8;
			this.lastBitsRead |= res;
			this.bitsInput += 8;
			return (byte) res;
		}
		return (byte) this.readNBitNumber(Byte.SIZE); 
	}

	
	/**
	 * @return a character
	 * @throws IOException 
	 */
	private char readChar() throws IOException {
		return (char) ((this.read() << 8) | this.read());
	}

	/**
	 * @return the next bit in the inner stream, parsed as a boolean
	 * @throws IOException 
	 */
	public boolean readBoolean() throws IOException {
		return this.readNBitNumber(1) != 0;
	}
	
	/**
	 * @param length
	 * @return an array contaning the specified number of elements, read from the inner stream
	 * @throws IOException 
	 */
	public double[] readDoubleArray(int length) throws IOException {
		double[] res = new double[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readDouble();
		}
		return res;
	}
	
	/**
	 * @param length
	 * @return an array contaning the specified number of elements, read from the inner stream
	 * @throws IOException 
	 */
	public float[] readFloatArray(int length) throws IOException {
		float[] res = new float[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readFloat();
		}
		return res;
	}
	
	/**
	 * Reads an array of the specified length
	 * @param length
	 * @return the read array
	 * @throws IOException 
	 */
	public byte[] readByteArray(int length) throws IOException {
		byte[] res = new byte[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readByte();
		}
		return res;
	}
	
	/**
	 * Reads an integer array of the specified length
	 * @param length
	 * @return the array
	 * @throws IOException
	 */
	public int[] readIntArray(int length) throws IOException {
		int[] res = new int[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readInt();
		}
		return res;
	}
	
	
	/**
	 * Reads an integer array of the specified length
	 * @param bits the number of bits of each (unsigned) integer to be read
	 * @param length
	 * @return the array
	 * @throws IOException
	 */
	public int[] readNBitNumberArray(int bits, int length) throws IOException {
		int[] res = new int[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readNBitNumber(bits);
		}
		return res;
	}
	
	/**
	 * Reads an array of the specified length
	 * @param length
	 * @return the read array
	 * @throws IOException 
	 */
	public char[] readCharArray(int length) throws IOException {
		char[] res = new char[length];
		for (int i = 0; i < length; i++) {
			res[i] = this.readChar();
		}
		return res;
	}
	
	/**
	 * Reads a String of the specified length
	 * @param length
	 * @return the read string
	 * @throws IOException 
	 */
	public String readString(int length) throws IOException {
		byte[] chars = this.readByteArray(length);
		return new String(chars, StandardCharsets.US_ASCII);
	}
	
	/**
	 * @param clazz the enum class for which a value is to be read
	 * @param byteSize if <code>true</code>, the written enum occupies a size
	 * multiple of a byte in the stream. if <code>false</code>, it is assumed that 
	 * the minimum possible bit size was used to encode it
	 * @return the read enum
	 * @throws IOException 
	 */
	public Enum<?> readEnum(Class<?> clazz, boolean byteSize) throws IOException {
		if (clazz.getEnumConstants() == null) {
			throw new IllegalArgumentException("The given class is not an Enum");
		}
		
		int numberOfEnums = clazz.getEnumConstants().length;
		int bitSize = BitTwiddling.bitsOf(numberOfEnums);
		if (byteSize && bitSize % 8 != 0) {
			bitSize += (8 - (bitSize % 8));
		}
		int index = this.readNBitNumber(bitSize);
		return (Enum<?>) clazz.getEnumConstants()[index];		
	}
	
	/**
	 * @return the number of bits read so far
	 */
	public int getBitsInput() {
		return this.bitsInput;
	}

	/**
	 * @return the last bits read
	 */
	public int getLastReadBits() {
		return this.lastBitsRead;
	}




}
