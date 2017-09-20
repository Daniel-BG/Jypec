package com.jypec.util.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.io.IODataTypes.ByteOrdering;
import com.jypec.util.io.IODataTypes.ImageOrdering;
import com.jypec.util.io.imagereading.ImageReaderFactory;

/**
 * Generic reader for hyperspectral image data
 * @author Daniel
 *
 */
public class HyperspectralImageReader {

	
	/**
	 * Reads an image from the specified file. If headers are present, the offset must
	 * be indicated via the parameter. It can be found with 
	 * {@link ImageHeaderData#loadFromUncompressedStream(InputStream)}
	 * @param fileName name of the file
	 * @param offset where the image data starts within the file
	 * @param image where the data is sent to
	 */
	public static void readImage(String fileName, int offset, HyperspectralImage image) {
		FileInputStream in = null;
		try {
			File f = new File(fileName);
			in = new FileInputStream(f); 
			FileChannel file = in.getChannel();
			ByteBuffer buf = file.map(FileChannel.MapMode.READ_ONLY, offset, f.length());
			
			ImageReaderFactory.getReader(ImageOrdering.BSQ, ByteOrdering.LITTLE_ENDIAN, image.getDataType()).readFromBuffer(buf, image);

			file.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtilities.safeClose(in);
		}	
	}
	
	/**
	 * Same as {@link #readImage(String, int, HyperspectralImage)} with an offset of zero
	 * @param fileName
	 * @param image
	 */
	public static void readImage(String fileName, HyperspectralImage image) {
		readImage(fileName, 0, image);
	}
	

	

	
	
	/**
	 * @param file
	 * @param bands bands in file
	 * @param lines lines in file
	 * @param samples samples in file
	 * @param reqB bands to read
	 * @param reqL lines to read
	 * @param reqS samples to read
	 * @param format format of the samples 
	 * @param isLittleEndian if the format is lil endian
	 * @return the image
	 * @throws FileNotFoundException
	 */
	public static final HyperspectralImage readSkippingBIP(String file, int bands, int lines, int samples, int reqB, int reqL, int reqS, ImageDataType format, boolean isLittleEndian) throws FileNotFoundException {
		HyperspectralImage hi = new HyperspectralImage(null, format, reqB, reqL, reqS);
		InputStream is = new FileInputStream(new File(file));	//read a file
		is = new BufferedInputStream(is);						//buffer it
		if (isLittleEndian) {
			is = new EndiannessChangerReader(is, format.getByteDepth());
		}
		
		IntegerReader bdr = new IntegerReader(is, format.getBitDepth());
		
		
		for (int j = 0; j < lines; j++) {
			for (int k = 0; k < samples; k++) {
				for (int i = 0; i < bands; i++) {
					try {
						int data = bdr.read();
						if (i < reqB && j < reqL && k < reqS) {
							hi.setDataAt(data, i, j, k);
						}
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		}
		
		try {
			bdr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return hi;
	}
	
}