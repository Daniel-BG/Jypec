package com.jypec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.ejml.data.DMatrixRMaj;

import com.jypec.cli.InputArguments;
import com.jypec.comdec.ComParameters;
import com.jypec.comdec.Compressor;
import com.jypec.distortion.ImageComparisons;
import com.jypec.img.HyperspectralImage;
import com.jypec.util.JypecException;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.bits.BitOutputStreamTree;
import com.jypec.util.debug.Logger;
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
		
		/** create the compressor */
		Compressor c = new Compressor(cp);
		
		/** Create the output stream and save the compressed result */
		BitOutputStreamTree bstn = new BitOutputStreamTree("root", args.showTree);
		ImageHeaderReaderWriter.saveToCompressedStream(hi.getHeader(), bstn.addChild("header"), args.essentialHeader);
		c.compress(hi.getData(), bstn.addChild("body"));
		if (args.showTree) {
			String res = bstn.layoutTreeStructure(null);
			PrintWriter out = new PrintWriter(args.output + ".tree");
			out.println(res);
			out.flush();
			out.close();
		}
		BitOutputStream output = new BitOutputStream(new FileOutputStream(new File(args.output)));
		bstn.dumpInBitOutputStream(output);
		bstn.close();
		
		
		/** Show some stats */
		if (args.showCompressionStats && args.verbose) {
			int orsize = hi.getData().getBitSize();
			int redsize = output.getBitsOutput();
			Logger.getLogger().log("Original size is: " + orsize);
			Logger.getLogger().log("Compressed size is: " + redsize);
			Logger.getLogger().log("Compression rate: " + (double) orsize / (double) redsize);
			Logger.getLogger().log("Bpppb: " + redsize / (double) (hi.getData().getTotalNumberOfSamples()));
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
	 * @param args
	 * @throws IOException 
	 */
	public static void compare(InputArguments args) throws IOException {
		//read both images
		HyperspectralImage first = HyperspectralImageReader.read(args.input, args.inputHeader);
		HyperspectralImage second = HyperspectralImageReader.read(args.output, args.outputHeader);
		
		//check that they are the same size and stuff
		if (!first.getData().sizeAndTypeEquals(second.getData())) {
			System.out.println("Images are of different sizes and/or data types. Cannot compare");
			return;
		}
		
		//output metrics
		DMatrixRMaj fdm = first.getData().toDoubleMatrix();
		DMatrixRMaj sdm = second.getData().toDoubleMatrix();
		double dynRange = first.getData().getDataType().getDynamicRange();
		Logger.getLogger().log("RAW PSNR is: " + ImageComparisons.rawPSNR(fdm, sdm, dynRange));
		Logger.getLogger().log("Normalized PSNR is: " + ImageComparisons.normalizedPSNR(fdm, sdm));
		Logger.getLogger().log("SNR is: " + ImageComparisons.SNR(fdm, sdm));
		Logger.getLogger().log("MSE is: " + ImageComparisons.MSE(fdm, sdm));
		Logger.getLogger().log("maxSE is: " + ImageComparisons.maxSE(fdm, sdm));
		Logger.getLogger().log("MSR is: " + ImageComparisons.MSR(fdm, sdm));
		Logger.getLogger().log("SSIM is: " + ImageComparisons.SSIM(fdm, sdm, dynRange));
	}
	
}
