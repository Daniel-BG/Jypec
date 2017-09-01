package com.jypec.comdec;

import com.jypec.img.HyperspectralImage;
import com.jypec.util.BitStream;
import com.jypec.util.io.BitStreamDataReaderWriter;

/**
 * @author Daniel
 * Decompresses an input BitStream into a hyperspectral image
 */
public class Decompressor {

	
	/**
	 * @param input
	 * @return the resulting image from decompressing the given stream
	 */
	public HyperspectralImage decompress(BitStream input) {
		/** Create a wrapper for the stream to easily read/write it */
		BitStreamDataReaderWriter bw = new BitStreamDataReaderWriter();
		bw.setStream(input);
		
		/** Need to know the image dimensions */
		short bands = bw.readShort();
		short lines = bw.readShort();
		short samples = bw.readShort();
		
		
		throw new UnsupportedOperationException();
	}
}
