package com.jypec.util.io;

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for reading uniform data of arbitrary (1-32) bit lengths as integers
 * @author Daniel
 *
 */
public class IntegerReader extends FilterInputStream {
	private int depth;
	private int dataMask;
	private long buffer;
	private int bufferLen;
	
	/**
	 * Create a buffered image reader that will read the input file assuming it has even
	 * sized data of depth bits. Each call to {@link #next()} will return the next value
	 * as an integer. 
	 * @param in the input stream from where data is read
	 * @param depth depth of the input data (in bits)
	 * @throws FileNotFoundException
	 */
	public IntegerReader(InputStream in, int depth) throws FileNotFoundException {
		super(in);
		if (depth < 1 || depth > 32) {
			throw new IllegalArgumentException("The depth is a number between 1 and 32 inclusive");
		}
		
		this.depth = depth;
		this.dataMask = (-1) >>> (32 - this.depth); 
		this.buffer = 0;
		this.bufferLen = 0;
	}
	
	
	/**
	 * @return the next value in the input sequence, with length {@link #depth} bits. also readjusts the inner buffer
	 */
	private int removeFromBuffer() {
		int shift = this.bufferLen - this.depth;
		long result = this.buffer >>> shift;
		this.bufferLen -= this.depth;
		return (int) (result & this.dataMask);
	}
	
	/**
	 * Reads one byte of the input and adds it to the buffer.
	 * This function assumes that the buffer has enough space for the new byte, and
	 * the behaviour is undefined otherwise
	 * @throws IOException if the input stream has run out
	 */
	private void fillBuffer() throws IOException {
		int next = this.in.read();
		if (next == -1) {
			throw new IOException("No more bytes available");
		}
		this.buffer <<= 8;
		this.buffer += next;
		this.bufferLen += 8;
	}
	
	/**
	 * Returns the next sample from the stream. It is NOT restricted to the range 0-255, but to the range 0-(2^depth-1)
	 */
	@Override
	public int read() throws IOException {
		while (bufferLen < depth) {
			this.fillBuffer();
		}
		return this.removeFromBuffer();
	}
	
}
