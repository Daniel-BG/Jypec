package com.jypec;

import java.io.FileNotFoundException;
import com.jypec.ebc.EBCoder;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataTypes;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.BitStream;
import com.jypec.util.FIFOBitStream;
import com.jypec.util.io.DataMatrixReader;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

/**
 * Tests go here
 * @author Daniel
 *
 */
public class Main {
	
	
	

	public static void main(String[] args) {
		testFullProcess();
		
		
	}

	
	private static void testFullProcess() {
		int bands = 188, lines = 350, samples = 350;
		ImageDataTypes type = ImageDataTypes.UNSIGNED_TWO_BYTE;
		
		HyperspectralImage hi;
		try {
			hi = DataMatrixReader.read("C:/Users/Daniel/Hiperspectral images/cupriteBSQ/Cuprite", bands, lines, samples, type, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		for (int i = 0; i < bands; i++) {
			//things we'll need
			BidimensionalWavelet bdw = new BidimensionalWavelet(new LiftingCdf97WaveletTransform());
			MatrixQuantizer mq = new MatrixQuantizer(type.getBitDepth() - 1, 0, 1, - (0x1 << 16), 0x1 << 17, 0.5);
			
			int stl = lines / 2, sts = samples / 2;
			int ll = lines - lines / 2, sl = samples - samples / 2;
			
			CodingBlock block = hi.getBand(i).extractBlock(stl, sts, ll, sl, SubBand.HH);
			
			///////CODING
			HyperspectralBand hb = hi.getBand(i);
			//transform to double and wave it
			double[][] waveForm = hb.toWave(0, 0, lines, samples);
			bdw.forwardTransform(waveForm, lines, samples);
			//quantize
			mq.quantize(waveForm, hb, stl, sts, ll, sl);
			//code it
			block.setDepth(type.getBitDepth()); //depth adjusted since there might be more bits
			BitStream output = new FIFOBitStream();
			EBCoder coder = new EBCoder();
			coder.code(block, output);
			//////END CODING
			
			//output results
			double compressionRate = (double) output.getNumberOfBits() / ((double) 16 * ll * sl);
			System.out.println("Compression rate is: " + compressionRate);
			
			
			///////DECODE IT into the original block
			//clear the block since we need it at zero
			block.clear(); 
			//decode the block from the stream
			EBDecoder decoder = new EBDecoder();
			decoder.decode(output, block);
			//dequantize
			mq.dequantize(hb, waveForm, stl, sts, ll, sl);
			//reverse transform and back into integer form
			bdw.reverseTransform(waveForm, ll, sl);
			hb.fromWave(waveForm, stl, sts, ll, sl);
			///////END DECODING
		}
	}
}
