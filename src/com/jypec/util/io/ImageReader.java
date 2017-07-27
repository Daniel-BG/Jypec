package com.jypec.util.io;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataTypes;

public class ImageReader {

	
	
	public static final HyperspectralImage read(String file, int depth, int bands, int lines, int samples) throws FileNotFoundException {
		HyperspectralImage hi = new HyperspectralImage(null, ImageDataTypes.UNSIGNED_TWO_BYTE, depth, bands, lines, samples);
		BufferedDataReader bdr = new BufferedDataReader(file, depth);
		
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
