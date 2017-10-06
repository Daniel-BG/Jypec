package com.jypec.util.io;

import java.io.FileInputStream;
import java.io.IOException;

import com.jypec.comdec.Decompressor;
import com.jypec.img.HeaderConstants;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.HyperspectralImageFloatData;
import com.jypec.img.HyperspectralImageIntegerData;
import com.jypec.img.ImageDataType;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.debug.Logger;
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
	 * @param floatRep if true the matrix read will be stored as floats (may be useful for memory reasons)
	 * @param verbose if info is to be output when decompressing
	 * @return the read image, containing both header and data
	 * @throws IOException 
	 */
	public static HyperspectralImage read(String path, boolean floatRep) throws IOException {
		return HyperspectralImageReader.read(path, null, floatRep);
	}

	
	/**
	 * @param dataPath where the image data is stored
	 * @param headerPath where the image metadata is stored
	 * @param floatRep if true the matrix read will be stored as floats (may be useful for memory reasons)
	 * @param verbose if info is to be output when decompressing
	 * @return the read image
	 * @throws IOException 
	 */
	public static HyperspectralImage read(String dataPath, String headerPath, boolean floatRep) throws IOException {
		/** Load header */
		ImageHeaderData header = new ImageHeaderData();
		String realHeaderPath = headerPath != null ? headerPath : dataPath;
		BitInputStream bis = new BitInputStream(new FileInputStream(realHeaderPath)); 
		Logger.getLogger().log("Reading image header: " + realHeaderPath);
		int offset = ImageHeaderReaderWriter.loadFromStream(bis, header);
		if (headerPath != null) {
			offset = 0;
		}
		
		/** Load image data */
		HyperspectralImageData data;
		if (header.wasCompressed()) {	//load compressed
			Logger.getLogger().log("Image was compressed. Uncompressing from: " + dataPath);
			Decompressor d = new Decompressor();
			data = d.decompress(header, bis);
		} else {						//load uncompressed
			Logger.getLogger().log("Image was not compressed. Reading raw data: " + dataPath);
			int bands = (int) header.get(HeaderConstants.HEADER_BANDS);
			int lines = (int) header.get(HeaderConstants.HEADER_LINES);
			int samples = (int) header.get(HeaderConstants.HEADER_SAMPLES);
			ImageDataType type = ImageDataType.fromHeaderCode((byte) header.get(HeaderConstants.HEADER_DATA_TYPE));
			if (floatRep) {
				data = new HyperspectralImageFloatData(type, bands, lines, samples);
			} else {
				data = new HyperspectralImageIntegerData(type, bands, lines, samples);
			}
			HyperspectralImageDataReader.readImageData(dataPath, offset, data,
					(BandOrdering) header.get(HeaderConstants.HEADER_INTERLEAVE), 
					(ByteOrdering) header.get(HeaderConstants.HEADER_BYTE_ORDER));
		}
		
		/** Return the hyperspectral image */
		return new HyperspectralImage(data, header);
	}
	
}
