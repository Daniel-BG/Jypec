package com.jypec.util.io.imagewriting;

import java.nio.ByteBuffer;

import com.jypec.img.HyperspectralImage;
import com.jypec.util.io.DataMatrixWriter;

/**
 * Interface for writing hyperspectral images using {@link DataMatrixWriter}
 * @author Daniel
 *
 */
public interface ImageWriter {

	/**
	 * Writes a hyperspectralimage to a buffer, padding with zeros 
	 * at the end if the image bit size is not a multiple of 8
	 * @param hi
	 * @param bb
	 */
	public void writeToBuffer(HyperspectralImage hi, ByteBuffer bb);
	
	
}
