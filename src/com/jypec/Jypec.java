package com.jypec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.jypec.cli.InputArguments;
import com.jypec.comdec.ComParameters;
import com.jypec.comdec.Compressor;
import com.jypec.comdec.Decompressor;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.util.JypecException;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;
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
	 * @throws IOException 
	 * @throws JypecException 
	 */
	public static void compress(InputArguments args) throws IOException, JypecException {
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

		//check for coherent args
		if (args.outputHeader != null) {
			throw new JypecException("The compressed header must be packed with the data. You specified a route for the output header");
		}
		if (args.dontOutputHeader) {
			throw new JypecException("The header must be packed with the data. You specified the option for no header output");
		}
		BitOutputStream output = new BitOutputStream(new FileOutputStream(new File(args.output)));
		
		//save header
		ihd.saveToCompressedStream(output);
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
	 * @throws IOException 
	 * @throws JypecException 
	 */
	public static void decompress(InputArguments args) throws IOException, JypecException {
		if (args.inputHeader != null) {
			throw new JypecException("The compressed header must be packed with the data. You specified a route for the input header");
		}
		
		//create input stream
		BitInputStream bis = new BitInputStream(new FileInputStream(args.input));
		
		//read header and data
		ImageHeaderData ihd = new ImageHeaderData();
		ihd.loadFromStream(bis);
		Decompressor d = new Decompressor();
		HyperspectralImage res = d.decompress(ihd, bis);
		
		BitOutputStream bos;
		int byteOffset = 0;
		
		if (!args.dontOutputHeader) {
			//create the output
			if (args.outputHeader != null) {
				bos = new BitOutputStream(new FileOutputStream(args.outputHeader));
			} else {
				bos = new BitOutputStream(new FileOutputStream(args.output));
			}
			ihd.saveToUncompressedStream(bos);
			//if header is separate, byteoffset is too
			if (args.outputHeader != null) {
				byteOffset = 0;
			}
		}
		
		HyperspectralImageWriter.writeBSQ(res, byteOffset ,args.output);
		
		if (args.showCompressionStats) {
			System.out.println("Image decompressed succesfully with: " + bis.available() + " bits remaining (should be zero)");
		}
		
	}

	/**
	 * Compares two images, one given in the input arguments and the other in the output arguments
	 * @param iArgs
	 */
	public static void compare(InputArguments iArgs) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
}
