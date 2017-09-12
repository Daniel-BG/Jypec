package com.jypec.util.io.imagereading;

import java.nio.ByteBuffer;

import com.jypec.img.HyperspectralImage;

/**
 * Interface to define image readers
 * @author Daniel
 */
public interface ImageReader {
	
	/**
	 * Reads a hyperspectral image from a buffer
	 * @param bb
	 * @param hi
	 */
	public void readFromBuffer(ByteBuffer bb, HyperspectralImage hi);

}
