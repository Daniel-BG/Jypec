package com.jypec;

import java.util.Random;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;
import com.jypec.quantization.MatrixQuantizer;
import com.jypec.util.BitStream;
import com.jypec.util.FIFOBitStream;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

import test.TestEBCodec;

/**
 * Tests go here
 * @author Daniel
 *
 */
public class Main {

	public static void main(String[] args) {
		int size = 32;
		int[][][] data = new int[size][size][size];
		for (int i = 0; i < size; i++) {
			TestEBCodec.randomizeData(data[i], size, size, 16, new Random(i));
		}
		HyperspectralImage hi = new HyperspectralImage(data, 16, size, size, size);
		
		for (int i = 0; i < size; i++) {
			//Code it
			HyperspectralBand hb = hi.getBand(i, true);
			double[][] waveForm = hb.toWave();
			BidimensionalWavelet bdw = new BidimensionalWavelet(new LiftingCdf97WaveletTransform());
			bdw.forwardTransform(waveForm, size, size);
			MatrixQuantizer mq = new MatrixQuantizer(16, 0, 1, 0, 0x1 << 16, 0.5);
			mq.quantize(waveForm, hb.getDataReference(), size, size);
			CodingBlock block = hi.getBand(i, true).extractBlock(0, 0, size, size, SubBand.HH, true);
			BitStream output = new FIFOBitStream();
			EBCoder coder = new EBCoder();
			coder.code(block, output);
			
			System.out.println(output.dumpHex());
		}
		
		
		
		//decode it
		/*CodingBlock blockOut = new CodingBlock(size, size, 16, SubBand.HH);
		EBDecoder decoder = new EBDecoder();
		decoder.decode(output, blockOut);

		boolean right = true;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (data[i][j] != blockOut.getData()[i][j]) {
					right = false;
					System.out.println("Failed at " + i + "," + j);
				}
			}
		}

		
		System.out.println("It is " + right);*/
	}

}
