package com.jypec.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.io.headerio.enums.BandOrdering;
import com.jypec.util.io.headerio.enums.ByteOrdering;
import com.jypec.util.io.imagedatareading.ImageDataReaderFactory;

/**
 * Generic reader for hyperspectral image data
 * @author Daniel
 *
 */
public class HyperspectralImageDataReader {

	
	/**
	 * Reads an image from the specified file. If headers are present, the offset must
	 * be indicated via the parameter. It can be found with 
	 * {@link ImageHeaderData#loadFromUncompressedStream(InputStream)}
	 * @param fileName name of the file
	 * @param offset where the image data starts within the file
	 * @param image where the data is sent to
	 * @param imageOrdering ordering of the image data (BSQ, BIP, BIL)
	 * @param byteOrdering lil endian or big endian (only useful for data types where the data width is multiple of 8 bits
	 */
	public static void readImageData(String fileName, int offset, HyperspectralImageData image, BandOrdering imageOrdering, ByteOrdering byteOrdering) {
		FileInputStream in = null;
		try {
			File f = new File(fileName);
			in = new FileInputStream(f); 
			FileChannel file = in.getChannel();
			ByteBuffer buf = file.map(FileChannel.MapMode.READ_ONLY, offset, f.length());
			
			ImageDataReaderFactory.getReader(imageOrdering, byteOrdering, image.getDataType()).readFromBuffer(buf, image);

			file.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtilities.safeClose(in);
		}	
	}
	
	/**
	 * Same as {@link #readImageData(String, int, HyperspectralImageData)} with an offset of zero
	 * @param fileName
	 * @param image
	 * @param imageOrdering 
	 * @param byteOrdering 
	 */
	public static void readImageData(String fileName, HyperspectralImageData image, BandOrdering imageOrdering, ByteOrdering byteOrdering) {
		readImageData(fileName, 0, image, imageOrdering, byteOrdering);
	}
	
}