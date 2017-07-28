package com.jypec.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Change the endianess of an input stream
 * @author Daniel
 *
 */
public class EndiannessChangerReader extends FilterInputStream {


	private int byteLength;
	private int[] buffer;
	private int bufferPointer;
	
	/**
	 * @param in stream to be filtered
	 * @param byteLength length (in bytes) of the words. When reading, packets
	 * of this length are read and then inverted and delivered via read() with 
	 * the opposite endianness
	 */
	protected EndiannessChangerReader(InputStream in, int byteLength) {
		super(in);
		this.byteLength = byteLength;
		this.buffer = new int[this.byteLength];
		this.bufferPointer = -1;
	}
	
	
	@Override
	public int read() throws IOException {
		if (bufferPointer == -1) {
			this.fillBuffer();
		}
		return this.popNextValueInBuffer();
		
	}

	/**
	 * Returns the current value in the buffer and leaves the pointer pointing to the next
	 * @return the current value in the buffer.
	 */
	private int popNextValueInBuffer() {
		int val = this.buffer[this.bufferPointer];
		this.bufferPointer--;
		return val;
	}

	/**
	 * Fills the internal buffer
	 * @throws IOException
	 */
	private void fillBuffer() throws IOException {
		for (int i = 0; i < this.byteLength; i++) {
			this.buffer[i] = this.in.read();
		}
		this.bufferPointer = this.byteLength - 1;
	}
	

}
