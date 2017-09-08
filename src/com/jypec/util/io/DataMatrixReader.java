package com.jypec.util.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;

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
	 * @param bands number of bands in the image
	 * @param lines number of lines in the image
	 * @param samples number of samples in the image
	 * @param format format of the image
	 * @param isLittleEndian if the input data is in LittleEndian format (BigEndian otherwise)
	 * @return the read image
	 * @throws FileNotFoundException
	 */
	public static final HyperspectralImage read(String file, int bands, int lines, int samples, ImageDataType format, boolean isLittleEndian) throws FileNotFoundException {
		HyperspectralImage hi = new HyperspectralImage(null, format, bands, lines, samples);
		InputStream is = new FileInputStream(new File(file));	//read a file
		is = new BufferedInputStream(is);						//buffer it
		if (isLittleEndian) {
			is = new EndiannessChangerReader(is, format.getByteDepth());
		}
		
		IntegerReader bdr = new IntegerReader(is, format.getBitDepth());
		
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
