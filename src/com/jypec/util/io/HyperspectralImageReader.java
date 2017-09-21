package com.jypec.util.io;

import java.io.FileInputStream;
import java.io.IOException;

import com.jypec.comdec.Decompressor;
import com.jypec.img.HeaderConstants;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageDataType;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.io.headerio.ImageHeaderReaderWriter;
import com.jypec.util.io.headerio.enums.BandOrdering;
import com.jypec.util.io.headerio.enums.ByteOrdering;

/**
 * Class to read hyperspectral images (header + data)
 * Either compressed or uncompressed
 * @author Daniel
 */
public class HyperspectralImageReader {
	
	
	/**
	 * @param path the path where the header metadata 
	 * + image data is stored
	 * @param verbose if info is to be output when decompressing
	 * @return the read image, containing both header and data
	 * @throws IOException 
	 */
	public static HyperspectralImage read(String path, boolean verbose) throws IOException {
		return HyperspectralImageReader.read(path, null, verbose);
	}

	
	/**
	 * @param dataPath where the image data is stored
	 * @param headerPath where the image metadata is stored
	 * @param verbose if info is to be output when decompressing
	 * @return the read image
	 * @throws IOException 
	 */
	public static HyperspectralImage read(String dataPath, String headerPath, boolean verbose) throws IOException {
		/** Load header */
		ImageHeaderData header = new ImageHeaderData();
		BitInputStream bis;
		if (headerPath != null) {
			bis = new BitInputStream(new FileInputStream(headerPath));
		} else {
			bis = new BitInputStream(new FileInputStream(dataPath));
		}
		int offset = ImageHeaderReaderWriter.loadFromStream(bis, header);
		if (headerPath != null) {
			offset = 0;
		}
		
		/** Load image data */
		HyperspectralImageData data;
		if (header.wasCompressed()) {	//load compressed
			Decompressor d = new Decompressor();
			d.setVerbose(verbose);
			data = d.decompress(header, bis);
		} else {						//load uncompressed
			int bands = (int) header.get(HeaderConstants.HEADER_BANDS);
			int lines = (int) header.get(HeaderConstants.HEADER_LINES);
			int samples = (int) header.get(HeaderConstants.HEADER_SAMPLES);
			ImageDataType type = ImageDataType.fromHeaderCode((byte) header.get(HeaderConstants.HEADER_DATA_TYPE));
			data = new HyperspectralImageData(null, type, bands, lines, samples);
			HyperspectralImageDataReader.readImageData(dataPath, offset, data,
					(BandOrdering) header.get(HeaderConstants.HEADER_INTERLEAVE), 
					(ByteOrdering) header.get(HeaderConstants.HEADER_BYTE_ORDER));
		}
		
		/** Return the hyperspectral image */
		return new HyperspectralImage(data, header);
	}
	
}
