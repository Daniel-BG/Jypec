package com.jypec;

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
import com.jypec.util.data.BidimensionalArrayIntegerMatrix;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.kernelTransforms.cdf97.KernelCdf97WaveletTransform;
import test.TestEBCodec;

/**
 * Tests go here
 * @author Daniel
 *
 */
public class Main {

	public static void main(String[] args) {
		int size = 32;
		int codingDepth = 18;
		int[][][] data = new int[size][size][size];
		for (int i = 0; i < size; i++) {
			TestEBCodec.fillDataWithValue(new BidimensionalArrayIntegerMatrix(data[i], size, size), size, size, 4);
			/*TestEBCodec.randomizeMatrix(new Random(i), 
					new BidimensionalArrayIntegerMatrix(data[i], size, size), 
					size, size, 16);*/
		}
		HyperspectralImage hi = new HyperspectralImage(data, ImageDataTypes.UNSIGNED_TWO_BYTE, 16, size, size, size);
		
		for (int i = 0; i < size; i++) {

			
			//CODE IT
			HyperspectralBand hb = hi.getBand(i);
			
			
			System.out.println("");
			for (int ii = 0; ii < size; ii++) {
				System.out.print(hi.getDataAt(i, 0, ii) + ",");
			}
			System.out.println("");
			//transform to double and wave it
			double[][] waveForm = hb.toWave();

			
			BidimensionalWavelet bdw = new BidimensionalWavelet(new KernelCdf97WaveletTransform());
			bdw.forwardTransform(waveForm, size, size);
			

			//quantize
			MatrixQuantizer mq = new MatrixQuantizer(codingDepth - 1, 0, 1, - (0x1 << 16), 0x1 << 16, 0.5);
			mq.quantize(waveForm, hb, size, size);
			//code it
			CodingBlock block = hi.getBand(i).extractBlock(0, 0, size, size, SubBand.HH);
			block.setDepth(codingDepth); //depth adjusted since there is one bit more now!
			BitStream output = new FIFOBitStream();
			EBCoder coder = new EBCoder();
			coder.code(block, output);
			//clear the block and output results
			block.clear(); 
			
			double compressionRate = (double) output.getNumberOfBits() / ((double) 16 * size * size);
			System.out.println("Compression rate is: " + compressionRate);
			
			
			//DECODE IT into the original block
			EBDecoder decoder = new EBDecoder();
			decoder.decode(output, block);
			//dequantize
			mq.dequantize(hb, waveForm, size, size);
			
			//reverse transform and back into integer form
			bdw.reverseTransform(waveForm, size, size);
			

			
			hb.fromWave(waveForm);
			
			for (int ii = 0; ii < size; ii++) {
				System.out.print(hi.getDataAt(i, 0, ii) + ",");
			}
			System.out.println("");
			

		}
	}

}
