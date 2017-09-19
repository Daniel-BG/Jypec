package com.jypec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.jypec.cli.InputArguments;
import com.jypec.comdec.ComParameters;
import com.jypec.comdec.Compressor;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.io.HyperspectralImageReader;
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
	 * @throws IOException 
	 */
	public static void compress(InputArguments args) throws IOException {
		//select header source
		FileInputStream fis;
		try {
			if (args.inputHeader != null) { //read metadata from outside
				fis = new FileInputStream(args.inputHeader);
			} else { //unless we do not have outside, then read from input
				fis = new FileInputStream(args.input);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		//read header
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

		//output header		
		BitOutputStream output;
		if (args.outputHeader != null){
			output = new BitOutputStream(new FileOutputStream(new File(args.outputHeader)));
		} else {
			output = new BitOutputStream(new FileOutputStream(new File(args.output)));
		}
		//save header wherever it goes
		if (!args.dontOutputHeader) {
			if (args.compressHeader) {
				ihd.saveToCompressedStream(output);
			} else {
				ihd.saveToUncompressedStream(output);
			}
		}
		//if header goes separate, save and restart
		if (args.outputHeader != null) {
			output.paddingFlush();
			output.close();
			output = new BitOutputStream(new FileOutputStream(new File(args.output)));
		}
		
		//now dump the image data
		c.compress(hi, output, dr);
		
		if (args.showCompressionStats) {
			int orsize = bands * lines * samples * type.getBitDepth();
			int redsize = output.getBitsOutput();
			System.out.println("Original size is: " + orsize);
			System.out.println("Compressed size is: " + redsize);
			System.out.println("Compression rate: " + (double) orsize / (double) redsize);
			System.out.println("Bpppb: " + redsize / (double) (bands * lines * samples));
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
		/*BitInputStream bs = new BitInputStream(new FileInputStream(args.input));
		
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
		}*/
		
	}
	
}
