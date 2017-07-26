package com.jypec;

import java.util.Random;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.util.BitStream;
import com.jypec.util.FIFOBitStream;
import test.TestEBCodec;

/**
 * Tests go here
 * @author Daniel
 *
 */
public class Main {

	public static void main(String[] args) {
		int size = 32;
		int[][] data = new int[size][size];
		TestEBCodec.randomizeData(data, size, size, 16, new Random(2));
		
		//Code it
		CodingBlock block = new CodingBlock(data, size, size, 16, SubBand.HH);
		BitStream output = new FIFOBitStream();
		EBCoder coder = new EBCoder();
		coder.code(block, output);
		
		System.out.println(output.dumpHex());

		
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
