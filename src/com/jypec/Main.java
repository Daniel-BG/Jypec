package com.jypec;

import java.util.Random;

import com.jypec.ebc.EBCoder;
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
		int size = 8;
		int[][] data = new int[size][size];
		TestEBCodec.randomizeData(data, size, size, 16, new Random(2));
		
		//Code it
		CodingBlock block = new CodingBlock(data, size, size, 16, SubBand.HH);
		BitStream output = new FIFOBitStream();
		EBCoder coder = new EBCoder();
		coder.code(block, output);
		
		System.out.println(output.dumpHex());
	}

}
