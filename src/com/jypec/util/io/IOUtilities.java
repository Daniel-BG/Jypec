package com.jypec.util.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.jypec.util.io.headerio.enums.ByteOrdering;

/**
 * Utilities for io that don't fit anywhere else
 * @author Daniel
 *
 */
public class IOUtilities {

	/**
	 * Close taking into account a null pointer possibility
	 * @param out
	 */
	public static void safeClose(Closeable out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			// do nothing
		}
	}
	
	/**
	 * Puts the specified number of bytes from the value into the buffer, taking into account 
	 * the ordering requested
	 * @param val
	 * @param byteOrdering
	 * @param dataBytes
	 * @param bb
	 */
	public static void putBytes(int val, ByteOrdering byteOrdering, int dataBytes, ByteBuffer bb) {
		if (byteOrdering == ByteOrdering.LITTLE_ENDIAN) {
			val = IOUtilities.flipBytes(val, dataBytes);
		} 
		IOUtilities.putRightBytes(val, dataBytes, bb);
	}
	
	/**
	 * Gets the specified number of bytes and returns them assuming the given ordering
	 * @param byteOrdering
	 * @param dataBytes
	 * @param bb
	 * @return the newly read bytes
	 */
	public static int getBytes(ByteOrdering byteOrdering, int dataBytes, ByteBuffer bb) {
		int val = IOUtilities.getRightBytes(dataBytes, bb);
		if (byteOrdering == ByteOrdering.LITTLE_ENDIAN) {
			val = IOUtilities.flipBytes(val, dataBytes);
		} 
		return val;
	}
	
	/**
	 * Put the leftmost bytes in the buffer (from left to right)
	 * @param val where to take the bytes from
	 * @param bytes number of bytes to take 
	 * @param bb where to put the bytes
	 * @see {@link #putRightBytes(int, int, ByteBuffer)}
	 */
	public static void putLeftBytes(int val, int bytes, ByteBuffer bb) {
		switch (bytes) {
		case 1:
			bb.put((byte) (val >> 24));
			break;
		case 2:
			bb.putShort((short) (val >> 16));
			break;
		case 3:
			bb.put((byte) (val >> 24));
			bb.putShort((short) (val >> 16));
			break;
		case 4:
			bb.putInt(val);
			break;
		default:
			throw new IllegalArgumentException("Too many or too little bytes to put");
		}
	}
	
	
	/**
	 * Gets the next 'bytes' bytes from bb and returns them in the leftmost position
	 * @param bytes
	 * @param bb
	 * @return the read bytes
	 */
	public static int getLeftBytes(int bytes, ByteBuffer bb) {
		switch(bytes) {
		case 1:
			return bb.get() << 24;
		case 2:
			return bb.getShort() << 16;
		case 3:
			return (bb.get() << 24) | (bb.getShort() << 8);
		case 4:
			return bb.getInt();
		default:
			throw new IllegalArgumentException("Too many or too little bytes to get");
		}
	}
	
	/**
	 * Put the rightmost bytes in the buffer (from left to right)
	 * @param val where to take the bytes from
	 * @param bytes number of bytes to take 
	 * @param bb where to put the bytes
	 * @see {@link #putLeftBytes(int, int, ByteBuffer)}
	 */
	public static void putRightBytes(int val, int bytes, ByteBuffer bb) {
		switch (bytes) {
		case 1:
			bb.put((byte) val);
			break;
		case 2:
			bb.putShort((short) val);
			break;
		case 3:
			bb.put((byte) (val >> 16));
			bb.putShort((short) val);
			break;
		case 4:
			bb.putInt(val);
			break;
		default:
			throw new IllegalArgumentException("Too many or too little bytes to put");
		}
	}
	
	/**
	 * Gets the next 'bytes' bytes from bb and returns them in the leftmost position
	 * @param bytes
	 * @param bb
	 * @return the read bytes
	 */
	public static int getRightBytes(int bytes, ByteBuffer bb) {
		switch(bytes) {
		case 1:
			return bb.get() & 0xff;
		case 2:
			return bb.getShort() & 0xffff;
		case 3:
			return ((bb.get() << 16) | bb.getShort()) & 0xffffff;
		case 4:
			return bb.getInt();
		default:
			throw new IllegalArgumentException("Too many or too little bytes to get");
		}
	}
	
	/**
	 * flip the given number of bytes (basically change from lil endian to big endian
	 * and vice versa
	 * @param source integer to be flipped
	 * @param bytes number of bytes to be flipped
	 * @return the flipped integer
	 */
	public static int flipBytes(int source, int bytes) {
		switch(bytes) {
		case 1:
			return source;
		case 2:
			return ((source >> 8) & 0xff) | ((source & 0xff) << 8);
		case 3:
			return (source & 0xff00) | ((source >> 16) & 0xff) | ((source & 0xff) << 16);
		case 4:
			return ((source & 0xff) << 24) | ((source & 0xff00) << 8) | ((source & 0xff0000) >> 8) | ((source >> 24) & 0xff);
		}
		throw new IllegalArgumentException("Cannot work with that size");
	}
	
	
	/**
	 * @param stream
	 * @return the fully read stream in String form
	 * @throws IOException
	 * @see "https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string"
	 */
	public static String fullyReadStream(InputStream stream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = stream.read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}

		return result.toString(StandardCharsets.UTF_8.name());
	}

}
