package com.jypec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.ejml.data.FMatrixRMaj;

import com.jypec.analysis.Analyzer;
import com.jypec.cli.InputArguments;
import com.jypec.comdec.ComParameters;
import com.jypec.comdec.Compressor;
import com.jypec.distortion.ImageComparisons;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.HyperspectralImageData;
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
		HyperspectralImage hi = HyperspectralImageReader.read(args.input, args.inputHeader, true);
		ComParameters cp = new ComParameters(args);
		
		/** create the compressor */
		Compressor c = new Compressor(cp);
		
		/** Create the output stream and save the compressed result */
		BitOutputStreamTree bstn;
		if (args.showTree) {
			bstn = new BitOutputStreamTree("root", args.showTree);
		} else { //in this case dump while compressing to avoid memory overhead
			bstn = new BitOutputStreamTree(new FileOutputStream(new File(args.output)));
		}
		ImageHeaderReaderWriter.saveToCompressedStream(hi.getHeader(), bstn.addChild("header"), args.essentialHeader);
		c.compress(hi.getData(), bstn.addChild("body"));
		if (args.showTree) {
			String res = bstn.layoutTreeStructure(null);
			PrintWriter out = new PrintWriter(args.output + ".tree");
			out.println(res);
			out.flush();
			out.close();
		}		
		
		/** Show some stats */
		if (args.showCompressionStats) {
			long orsize = hi.getData().getBitSize();
			long redsize = bstn.getTreeBits(); // output.getBitsOutput();
			System.out.println("Original size is: " + orsize);
			System.out.println("Compressed size is: " + redsize);
			System.out.println("Compression rate: " + (float) orsize / (float) redsize);
			System.out.println("Bpppb: " + redsize / (float) (hi.getData().getTotalNumberOfSamples()));
		}
		
		/** close output streams and dump data if needed */
		if (args.showTree) { //if the tree was shown we need to dump the results now
			Logger.getLogger().log("Saving compressed data...");
			BitOutputStream output = new BitOutputStream(new FileOutputStream(new File(args.output)));
			bstn.dumpInBitOutputStream(output);
		}
		bstn.close();
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
		HyperspectralImage hi = HyperspectralImageReader.read(args.input, false);
		
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
		HyperspectralImage first = HyperspectralImageReader.read(args.input, args.inputHeader, true);
		HyperspectralImage second = HyperspectralImageReader.read(args.output, args.outputHeader, true);
		
		//check that they are the same size and stuff
		if (!first.getData().sizeAndTypeEquals(second.getData())) {
			System.out.println("Images are of different sizes and/or data types. Cannot compare");
			return;
		}
		
		//output metrics
		FMatrixRMaj fdm = first.getData().tofloatMatrix();
		float dynRange = first.getData().getDataType().getDynamicRange();
		first = null; //allow garbage collector to work here
		FMatrixRMaj sdm = second.getData().tofloatMatrix();
		Logger.getLogger().log("RAW PSNR is: " + ImageComparisons.rawPSNR(fdm, sdm, dynRange));
		Logger.getLogger().log("Normalized PSNR is: " + ImageComparisons.normalizedPSNR(fdm, sdm));
		Logger.getLogger().log("powerSNR is: " + ImageComparisons.powerSNR(fdm, sdm));
		Logger.getLogger().log("SNR is: " + ImageComparisons.SNR(fdm, sdm));
		Logger.getLogger().log("MSE is: " + ImageComparisons.MSE(fdm, sdm));
		Logger.getLogger().log("maxSE is: " + ImageComparisons.maxSE(fdm, sdm));
		Logger.getLogger().log("MSR is: " + ImageComparisons.MSR(fdm, sdm));
		Logger.getLogger().log("SSIM is: " + ImageComparisons.SSIM(fdm, sdm, dynRange));
	}

	/**
	 * Analyze the input image
	 * @param args program arguments
	 * @throws IOException 
	 */
	public static void analyze(InputArguments args) throws IOException {
		HyperspectralImage image = HyperspectralImageReader.read(args.input, args.inputHeader, true);
		HyperspectralImageData hid = image.getData();
		
		int blacks = 0;
		int aberrated = 0;
		double totalAberration = 0;
		
		Logger.getLogger().log("Analyzing image...");
		@SuppressWarnings("unused")
		float[] cp = null, pp = null, np = null; //current previous and next pixel
		for (int i = 0; i < hid.getNumberOfLines(); i++) {
			if (i % (hid.getNumberOfLines() / 10) == 0 && i > 0) {
				Logger.getLogger().log(10 * i / (hid.getNumberOfLines() / 10) + "% done");
			}
			
			
			for (int j = 0; j < hid.getNumberOfSamples(); j++) {
				if (j == 0) {
					cp = hid.getPixel(i, j);
					np = hid.getPixel(i, j+1);
					pp = null;
				} else if (j == hid.getNumberOfSamples() - 1) {
					pp = cp;
					cp = np;
					np = null;
				} else {
					pp = cp;
					cp = np;
					np = hid.getPixel(i,  j+1);
				}
				
				if (Analyzer.isBlack(cp)) {
					blacks++;
				}
				
				double aberration = Analyzer.hasAberration(cp);
				if (aberration > 0) {
					aberrated++;
					totalAberration += aberration;
				}

			}
		}
		
		int total = hid.getNumberOfLines() * hid.getNumberOfSamples();
		float percentBlack = (float) blacks / (float) total * 100;
		float percentAberr = (float) aberrated / (float) total * 100;
		
		Logger.getLogger().log("Number of black pixels: " + blacks + "/" + total + " [" + percentBlack + "%]");
		Logger.getLogger().log("Number of aberrations: " + aberrated + "/" + total + " [" + percentAberr + "%]");
		Logger.getLogger().log("Total aberration: " + totalAberration);
	}

}
