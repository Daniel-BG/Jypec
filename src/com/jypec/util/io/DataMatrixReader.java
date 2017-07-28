package com.jypec.util.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataTypes;

/**
 * Generic reader for hyperspectral image data
 * @author Daniel
 *
 */
public class DataMatrixReader {

	
	/**
	 * Reads a hyperspectral image from the given file. The dimensions of the image must be specified, as well as the format.
	 * A flag indicating if the data is in Little endian format ensures that a wrapper is placed around the reader in order
	 * to invert the bit-order of the values
	 * @param file
	 * @param bands
	 * @param lines
	 * @param samples
	 * @param format
	 * @param isLittleEndian
	 * @return
	 * @throws FileNotFoundException
	 */
	public static final HyperspectralImage read(String file, int bands, int lines, int samples, ImageDataTypes format, boolean isLittleEndian) throws FileNotFoundException {
		HyperspectralImage hi = new HyperspectralImage(null, format, bands, lines, samples);
		InputStream is = new FileInputStream(new File(file));	//read a file
		is = new BufferedInputStream(is);						//buffer it
		if (isLittleEndian) {
			is = new EndiannessChangerReader(is, format.getByteDepth());
		}
		
		DataReader bdr = new DataReader(is, format.getBitDepth());
		
		for (int i = 0; i < bands; i++) {
			for (int j = 0; j < lines; j++) {
				for (int k = 0; k < samples; k++) {
					try {
						hi.setDataAt(bdr.read(), i, j, k);
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
