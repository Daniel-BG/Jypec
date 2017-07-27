package com.jypec.util.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;

/**
 * Wrapper for reading uniform data of arbitrary (1-32) bit lengths as integers
 * @author Daniel
 *
 */
public class BufferedDataReader extends FilterInputStream {
	private int depth;
	private int dataMask;
	private long buffer;
	private int bufferLen;
	
	/**
	 * Create a buffered image reader that will read the input file assuming it has even
	 * sized data of depth bits. Each call to {@link #next()} will return the next value
	 * as an integer. 
	 * @param file
	 * @param depth
	 * @throws FileNotFoundException
	 */
	public BufferedDataReader(String file, int depth) throws FileNotFoundException {
		super(new BufferedInputStream(new FileInputStream(new File(file))));
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
		this.buffer <<= 8;
		this.buffer += next;
		this.bufferLen += 8;
	}
	
	
	@Override
	public int read() throws IOException {
		while (bufferLen < depth) {
			this.fillBuffer();
		}
		return this.removeFromBuffer();
	}
	
}
