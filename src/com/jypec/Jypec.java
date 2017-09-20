package com.jypec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.jypec.cli.InputArguments;
import com.jypec.comdec.ComParameters;
import com.jypec.comdec.Compressor;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.distortion.ImageComparisons;
import com.jypec.img.HyperspectralImage;
import com.jypec.util.JypecException;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.io.HyperspectralImageReader;
import com.jypec.util.io.HyperspectralImageWriter;
import com.jypec.util.io.headerio.ImageHeaderReaderWriter;

/**
 * Entry point for algorithms
 * @author Daniel
 */
public class Jypec {
	
	private static void checkCompressArguments(InputArguments args) throws JypecException {
		if (args.outputHeader != null) {
			throw new JypecException("The compressed header must be packed with the data. You specified a route for the output header");
		}
		if (args.dontOutputHeader) {
			throw new JypecException("The header must be packed with the data. You specified the option for no header output");
		}
	}

	/**
	 * Compresses whatever the args tell it to
	 * @param args
	 * @throws IOException 
	 * @throws JypecException 
	 */
	public static void compress(InputArguments args) throws IOException, JypecException {
		/** Check for coherent arguments */
		checkCompressArguments(args);
		
		/** load the image, and compression parameters */
		HyperspectralImage hi = HyperspectralImageReader.read(args.input, args.inputHeader);
		ComParameters cp = new ComParameters(args);
		DimensionalityReduction dr = DimensionalityReduction.loadFrom(args);
		
		/** create the compressor */
		Compressor c = new Compressor(cp);
		
		/** Create the output stream and save the compressed result */
		BitOutputStream output = new BitOutputStream(new FileOutputStream(new File(args.output)));
		ImageHeaderReaderWriter.saveToCompressedStream(hi.getHeader(), output, args.essentialHeader);
		c.compress(hi.getData(), output, dr);
		
		/** Show some stats */
		if (args.showCompressionStats) {
			int orsize = hi.getData().getBitSize();
			int redsize = output.getBitsOutput();
			System.out.println("Original size is: " + orsize);
			System.out.println("Compressed size is: " + redsize);
			System.out.println("Compression rate: " + (double) orsize / (double) redsize);
			System.out.println("Bpppb: " + redsize / (double) (hi.getData().getTotalNumberOfSamples()));
		}
	}

	
	private static void checkDecompressArguments(InputArguments args) throws JypecException {
		if (args.inputHeader != null) {
			throw new JypecException("The compressed header must be packed with the data. You specified a route for the input header");
		}
	}

	/**
	 * Decompresses whatever the args tell it to
	 * @param args arguments in a readable form
	 * @throws IOException 
	 * @throws JypecException 
	 */
	public static void decompress(InputArguments args) throws IOException, JypecException {
		/** Check arguments */
		checkDecompressArguments(args);
		
		/** Read input image, decompressing if compressed format is found */
		HyperspectralImage hi = HyperspectralImageReader.read(args.input);
		
		/** Save the result */
		HyperspectralImageWriter.write(hi, args);
	}



	/**
	 * Compares two images, one given in the input arguments and the other in the output arguments
	 * @param iArgs
	 * @throws IOException 
	 */
	public static void compare(InputArguments iArgs) throws IOException {
		//read both images
		HyperspectralImage first = HyperspectralImageReader.read(iArgs.input, iArgs.inputHeader);
		HyperspectralImage second = HyperspectralImageReader.read(iArgs.output, iArgs.outputHeader);
		
		//check that they are the same size and stuff
		if (first.getData().getNumberOfBands() != second.getData().getNumberOfBands()
				|| first.getData().getNumberOfLines() != second.getData().getNumberOfLines()
				|| first.getData().getNumberOfSamples() != second.getData().getNumberOfSamples()
				|| first.getData().getDataType().equals(second.getData().getDataType())) {
			System.out.println("Images are of different sizes and/or data types. Cannot compare");
			return;
		}
		
		//output metrics
		System.out.println("RAW PSNR is: " + ImageComparisons.rawPSNR(first.getData(), second.getData()));
		System.out.println("Normalized PSNR is: " + ImageComparisons.normalizedPSNR(first.getData(), second.getData()));
		System.out.println("SNR is: " + ImageComparisons.SNR(first.getData(), second.getData()));
		System.out.println("MSE is: " + ImageComparisons.MSE(first.getData(), second.getData()));
		System.out.println("maxSE is: " + ImageComparisons.maxSE(first.getData(), second.getData()));
	}
	
}
