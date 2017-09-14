package com.jypec.util.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.jypec.img.HyperspectralImage;
import com.jypec.util.io.IODataTypes.ByteOrdering;
import com.jypec.util.io.IODataTypes.ImageOrdering;
import com.jypec.util.io.imagewriting.ImageWriterFactory;

/**
 * Class for storing data matrices (mostly hyperspectral images)
 * 
 * @author Daniel
 *
 */
public class HyperspectralImageWriter {

	/**
	 * From
	 * https://stackoverflow.com/questions/4358875/fastest-way-to-write-an-array-of-integers-to-a-file-in-java
	 * 
	 * @param hi
	 *            image to save
	 * @param fileName
	 *            name of file where to write image
	 */
	public static void writeBSQ(HyperspectralImage hi, String fileName) {
		RandomAccessFile out = null;
		try {
			out = new RandomAccessFile(fileName, "rw"); //need rw for the file.map function to work
			FileChannel file = out.getChannel();
			int expectedBytes = hi.getNumberOfBands() * hi.getNumberOfLines() * hi.getNumberOfSamples() * 2;
			
			ByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, expectedBytes);
			ImageWriterFactory.getWriter(ImageOrdering.BSQ, ByteOrdering.LITTLE_ENDIAN, hi.getDataType()).writeToBuffer(hi, buf);

			file.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtilities.safeClose(out);
		}
	}



}