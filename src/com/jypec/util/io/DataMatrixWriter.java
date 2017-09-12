package com.jypec.util.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.jypec.img.HyperspectralImage;

/**
 * Class for storing data matrices (mostly hyperspectral images)
 * 
 * @author Daniel
 *
 */
public class DataMatrixWriter {

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
			for (int i = 0; i < hi.getNumberOfBands(); i++) {
				for (int j = 0; j < hi.getNumberOfLines(); j++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						short val = (short) hi.getDataAt(i, j, k);
						buf.putShort((short) (((val << 8) & 0xff00) | ((val >> 8) & 0xff)));
					}
				}
			}
			file.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			safeClose(out);
		}
	}

	/**
	 * Close taking into account a null pointer possibility
	 * @param out
	 */
	private static void safeClose(RandomAccessFile out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			// do nothing
		}
	}

}
