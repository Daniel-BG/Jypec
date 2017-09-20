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

/**
 * Class to read hyperspectral images (header + data)
 * Either compressed or uncompressed
 * @author Daniel
 */
public class HyperspectralImageReader {
	
	
	/**
	 * @param path the path where the header metadata 
	 * + image data is stored
	 * @return the read image, containing both header and data
	 * @throws IOException 
	 */
	public static HyperspectralImage read(String path) throws IOException {
		return HyperspectralImageReader.read(path, null);
	}

	
	/**
	 * @param dataPath where the image data is stored
	 * @param headerPath where the image metadata is stored
	 * @return the read image
	 * @throws IOException 
	 */
	public static HyperspectralImage read(String dataPath, String headerPath) throws IOException {
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
			data = d.decompress(header, bis);
		} else {						//load uncompressed
			int bands = (int) header.get(HeaderConstants.HEADER_BANDS);
			int lines = (int) header.get(HeaderConstants.HEADER_LINES);
			int samples = (int) header.get(HeaderConstants.HEADER_SAMPLES);
			ImageDataType type = ImageDataType.fromHeaderCode((byte) header.get(HeaderConstants.HEADER_DATA_TYPE));
			data = new HyperspectralImageData(null, type, bands, lines, samples);
			HyperspectralImageDataReader.readImageData(dataPath, offset, data);
		}
		
		/** Return the hyperspectral image */
		return new HyperspectralImage(data, header);
	}
	
}
