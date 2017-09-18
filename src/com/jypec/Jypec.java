package com.jypec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.jypec.cli.InputArguments;
import com.jypec.comdec.ComParameters;
import com.jypec.comdec.Compressor;
import com.jypec.comdec.Decompressor;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.util.bits.BitStream;
import com.jypec.util.bits.BitStreamDataReaderWriter;
import com.jypec.util.bits.FIFOBitStream;
import com.jypec.util.io.HyperspectralImageReader;
import com.jypec.util.io.HyperspectralImageWriter;
import com.jypec.util.io.headerio.HeaderConstants;
import com.jypec.util.io.headerio.ImageHeaderData;

/**
 * Entry point for algorithms
 * @author Daniel
 */
public class Jypec {

	/**
	 * Compresses whatever the args tell it to
	 * @param args
	 */
	public static void compress(InputArguments args) {
		//select header source
		FileInputStream fis;
		try {
			if (args.metadata != null) { //read metadata from outside
				fis = new FileInputStream(args.metadata);
			} else { //unless we do not have outside, then read from input
				fis = new FileInputStream(args.input);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		//reade header
		int headerOffset = 0;
		ImageHeaderData ihd = new ImageHeaderData();
		try {
			//update header offset for later
			headerOffset = ihd.loadFromUncompressedStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//load image metadata from header
		int bands = (int) ihd.getData(HeaderConstants.HEADER_BANDS);
		int lines = (int) ihd.getData(HeaderConstants.HEADER_LINES);
		int samples = (int) ihd.getData(HeaderConstants.HEADER_SAMPLES);
		ImageDataType type = ImageDataType.fromHeaderCode((byte) ihd.getData(HeaderConstants.HEADER_DATA_TYPE));
		
		//load compression parameters
		ComParameters cp = new ComParameters();
		if (args.requestWavelet) {
			cp.wavePasses = args.passes;
		}
		if (args.requestShave) {
			cp.bitReduction = args.shave;
		}
		Compressor c = new Compressor(cp);
		DimensionalityReduction dr = DimensionalityReduction.loadFrom(args);
		
		HyperspectralImage hi = new HyperspectralImage(null, type, bands, lines, samples);
		HyperspectralImageReader.readImage(args.input, headerOffset, hi);

		BitStream output = new FIFOBitStream();

		ihd.saveToCompressedStream(new BitStreamDataReaderWriter(output));
		c.compress(hi, output, dr);
		
		if (args.showCompressionStats) {
			int orsize = bands * lines * samples * type.getBitDepth();
			int redsize = output.getNumberOfBits();
			System.out.println("Original size is: " + orsize);
			System.out.println("Compressed size is: " + redsize);
			System.out.println("Compression rate: " + (double) orsize / (double) redsize);
			System.out.println("Bpppb: " + redsize / (double) (bands * lines * samples));
		}
		

		
		BitStreamDataReaderWriter brw = new BitStreamDataReaderWriter(output);
		byte[] bytes = brw.readByteArray(output.getNumberOfBits() >> 3);
		
		
		File f = new File(args.output);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			fos.write(bytes);
			if (output.getNumberOfBits() > 0) {
				throw new IllegalStateException("Padding was not added properly");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Decompresses whatever the args tell it to
	 * @param args arguments in a readable form
	 */
	public static void decompress(InputArguments args) {
		//select header source. can be embedded or not, compressed or not, so many things to check for
		/*FileInputStream fis;
		try {
			if (args.metadata != null) { //read metadata from outside
				fis = new FileInputStream(args.metadata);
			} else { //unless we do not have outside, then read from input
				fis = new FileInputStream(args.input);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}*/
		
		
		//read image data (potentially header as well but we don't know that yet
		BitStream bs = new FIFOBitStream();
		BitStreamDataReaderWriter brw = new BitStreamDataReaderWriter(bs);
		byte[] data;
		try {
			data = Files.readAllBytes(Paths.get(args.input));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		brw.writeByteArray(data, data.length);
		
		//read header and data
		ImageHeaderData ihd = new ImageHeaderData();
		try {
			//if header is embedded read from same stream
			if (args.embed) {
				ihd.loadFromCompressedStream(brw);
			} else { 
				//assume header is uncompressed somewhere else
				if (args.metadata != null) {
					FileInputStream fis;
					fis = new FileInputStream(args.metadata);
					ihd.loadFromUncompressedStream(fis);
				}
			}		
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		Decompressor d = new Decompressor();
		HyperspectralImage res = d.decompress(ihd, bs);
		
		HyperspectralImageWriter.writeBSQ(res, args.output);
		
		if (args.showCompressionStats) {
			System.out.println("Image decompressed succesfully with: " + bs.getNumberOfBits() + " bits remaining (should be zero)");
		}
		
	}
	
}
