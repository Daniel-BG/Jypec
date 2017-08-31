package com.jypec;

import java.io.FileNotFoundException;
import java.util.Locale;

import com.jypec.comdec.Blocker;
import com.jypec.ebc.EBCoder;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;
import com.jypec.pca.PrincipalComponentAnalysis;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.BitStream;
import com.jypec.util.FIFOBitStream;
import com.jypec.util.debug.ArrayPrinter;
import com.jypec.util.debug.Logger;
import com.jypec.util.debug.Logger.LoggerParameter;
import com.jypec.util.io.DataMatrixReader;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.compositeTransforms.OneDimensionalWaveletExtender;
import com.jypec.wavelet.compositeTransforms.RecursiveBidimensionalWavelet;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

/**
 * Tests go here
 * @author Daniel
 *
 */
public class Main {
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		Logger.logger().allowLogging(Main.class);
		Logger.logger().set(LoggerParameter.SHOW_INFO, 1);
		testFullProcess();
		//testPCA();
		
	}

	private static void testPCA() {
		
		int bands = 188, lines = 350, samples = 350;
		ImageDataType type = ImageDataType.UNSIGNED_TWO_BYTE;
		
		HyperspectralImage hi;
		try {
			hi = DataMatrixReader.read("C:/Users/Daniel/Hiperspectral images/cupriteBSQ/Cuprite", bands, lines, samples, type, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
		pca.setup(350*350, 188);
		for (int i = 0; i < 350; i++) {
			for (int j = 0; j < 350; j++) {
				pca.addSample(hi.getPixel(i, j));
			}
		}
		pca.computeBasis(30);
		
		System.out.println(ArrayPrinter.printDoubleArray(hi.getPixel(0, 0)));
		System.out.println(ArrayPrinter.printDoubleArray(pca.eigenToSampleSpace(pca.sampleToEigenSpace(hi.getPixel(0, 0)))));
		System.out.println(ArrayPrinter.printDoubleArray(pca.sampleToEigenSpace(hi.getPixel(0, 0))));
		
	}
	
	private static void testFullProcess() {
		int bands = 188, lines = 350, samples = 350;
		ImageDataType type = ImageDataType.UNSIGNED_TWO_BYTE;
		
		HyperspectralImage hi;
		try {
			hi = DataMatrixReader.read("C:/Users/Daniel/Hiperspectral images/cupriteBSQ/Cuprite", bands, lines, samples, type, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		for (int i = 0; i < bands; i++) {
			//things we'll need
			BidimensionalWavelet bdw = new RecursiveBidimensionalWavelet(new OneDimensionalWaveletExtender(new LiftingCdf97WaveletTransform()), 3);
			MatrixQuantizer mq = new MatrixQuantizer(type.getBitDepth() - 1, 0, 1, - (0x1 << 16), 0x1 << 17, 0.5);
			
			

			Blocker blocker = new Blocker(hi.getBand(i), 3, 64, 1024);
			//CodingBlock block = hi.getBand(i).extractBlock(stl, sts, ll, sl, SubBand.HH);
			
			///////CODING
			HyperspectralBand hb = hi.getBand(i);
			//transform to double and wave it
			double[][] waveForm = hb.toWave(0, 0, lines, samples);
			bdw.forwardTransform(waveForm, lines, samples);
			//quantize
			mq.quantize(waveForm, hb, 0, 0, lines, samples);
			//code it
			BitStream output = new FIFOBitStream();
			for (CodingBlock block: blocker) {
				
				block.setDepth(type.getBitDepth()); //depth adjusted since there might be more bits
				EBCoder coder = new EBCoder();
				coder.code(block, output);
				//Logger.logger().log(Main.class, "Coded block: " + block);
			}
			//////END CODING
			
			//output results
			double compressionRate = (double) output.getNumberOfBits() / ((double) 16 * lines * samples);
			System.out.println("Compression rate is: " + compressionRate);
			
			
			///////DECODE IT into the original block
			//clear the block since we need it at zero
			for (CodingBlock block: blocker) {
				block.clear(); 
				//decode the block from the stream
				EBDecoder decoder = new EBDecoder();
				decoder.decode(output, block);
				//Logger.logger().log(Main.class, "Decoded block: " + block);
				//Logger.logger().log(Main.class, "Bits remaining: " + output.getNumberOfBits());
			}

			//dequantize
			mq.dequantize(hb, waveForm, 0, 0, lines, samples);
			//reverse transform and back into integer form
			bdw.reverseTransform(waveForm, lines, samples);
			hb.fromWave(waveForm, 0, 0, lines, samples);
			///////END DECODING
		}
	}
}
