package com.jypec;

import java.util.Random;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.SubBand;
import com.jypec.util.BitStream;
import com.jypec.util.CodingBlock;
import com.jypec.util.FIFOBitStream;
import com.jypec.util.debug.Logger;
import com.jypec.util.debug.Logger.LoggerParameter;

/**
 * Tests go here
 * @author Daniel
 *
 */
public class Main {

	public static void main(String[] args) {
		Logger.logger().set(LoggerParameter.SHOW_ERROR, 1);
		Logger.logger().set(LoggerParameter.SHOW_INFO, 1);
		Logger.logger().set(LoggerParameter.SHOW_WARNING, 1);
		
		int height = 8, width = 8, depth = 1;
		int limit = 0x1 << (depth - 1);
		
		////CREATE TEST DATA
		int[][] data = new int[height][width];
		Random r = new Random(1);
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				double val = r.nextGaussian() * 1;
				int res = (int) val;
				if (res < -limit)
					res = -limit;
				if (res > limit)
					res = limit;
				
				int sign = res < 0 ? 1 : 0;
				int magnitude = res < 0 ? -res : res;
				
				data[i][j] = (sign << depth) + magnitude;
				System.out.print(data[i][j]);
			}
			System.out.println("");
		}
		System.out.println("");
		////
		
		
		
		////CODE 
		CodingBlock block = new CodingBlock(data, depth, SubBand.HH);
		BitStream output = new FIFOBitStream();
		EBCoder coder = new EBCoder();
		Logger.logger().log("Created block, output and coder! Coding...");
		
		coder.code(block, output);
		Logger.logger().log("Coding completed! Creating statistics...");
		
		int bitscoded = output.getNumberOfBits();
		int originalBits = width*height*2;
		double rate = (double) bitscoded / (double) originalBits;
			
		Logger.logger().log("\tThe compression rate is: " + rate + " bps");
		////
		
		//Logger.logger().log(output.dumpHex());
		
		////DECODE
		CodingBlock blockOut = new CodingBlock(height, width, depth, SubBand.HH);
		EBDecoder decoder = new EBDecoder();
		Logger.logger().log("Created output block and decoder! Decoding...");
		
		decoder.decode(output, blockOut);
		Logger.logger().log("Decoding completed! Checking matching results...");
		////
		
		out: for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (data[i][j] != blockOut.getData()[i][j]) {
					Logger.logger().log("Failed when decoding @" + i + "," + j);
					break out;
				}
			}
		}
		
	}

}
