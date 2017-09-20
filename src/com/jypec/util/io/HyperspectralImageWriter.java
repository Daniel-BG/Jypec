package com.jypec.util.io;

import java.io.FileOutputStream;
import java.io.IOException;

import com.jypec.cli.InputArguments;
import com.jypec.img.HeaderConstants;
import com.jypec.img.HyperspectralImage;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.io.headerio.ImageHeaderReaderWriter;
import com.jypec.util.io.headerio.enums.BandOrdering;
import com.jypec.util.io.headerio.enums.ByteOrdering;

/**
 * Write hyperspectral images to files
 * @author Daniel
 *
 */
public class HyperspectralImageWriter {

	/**
	 * @param hi the image to write
	 * @param args the arguments with which to write it (output location and such)
	 * @throws IOException
	 */
	public static void write(HyperspectralImage hi, InputArguments args) throws IOException {
		BitOutputStream bos;
		int byteOffset = 0;
		
		if (!args.dontOutputHeader) {
			//create the output
			if (args.outputHeader != null) {
				bos = new BitOutputStream(new FileOutputStream(args.outputHeader));
			} else {
				bos = new BitOutputStream(new FileOutputStream(args.output));
			}
			ImageHeaderReaderWriter.saveToUncompressedStream(hi.getHeader(), bos);
			//if header is separate, byteoffset is too
			if (args.outputHeader != null) {
				byteOffset = 0;
			}
		}
		
		HyperspectralImageDataWriter.writeBSQ(hi.getData(), byteOffset, args.output, 
				(BandOrdering) hi.getHeader().get(HeaderConstants.HEADER_INTERLEAVE), 
				(ByteOrdering) hi.getHeader().get(HeaderConstants.HEADER_BYTE_ORDER));		
	}

}
