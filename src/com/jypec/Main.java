package com.jypec;

import java.util.Random;

import com.jypec.mq.MQCoder;
import com.jypec.mq.SubBand;
import com.jypec.util.BitStream;
import com.jypec.util.CodingBlock;
import com.jypec.util.FIFOBitStream;
import com.jypec.util.debug.Logger;
import com.jypec.util.debug.Logger.LoggerParameter;
import com.jypec.util.debug.Logger.SeverityScale;

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
		
		
		int[][] data = new int[64][64];
		Random r = new Random(1);
		
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				double val = r.nextGaussian() * 16;
				int res = (int) val;
				if (res < -128)
					res = -128;
				if (res > 127)
					res = 127;
				data[i][j] = res;
			}
		}
		
		CodingBlock block = new CodingBlock(data, 7, SubBand.HH);
		BitStream output = new FIFOBitStream();
		MQCoder coder = new MQCoder();
		Logger.logger().log("Created block, output and coder! Coding...", SeverityScale.INFO);
		
		coder.code(block, output);
		Logger.logger().log("Coding completed! Creating statistics and dumping...", SeverityScale.INFO);
		
		int bitscoded = output.getNumberOfBits();
		int originalBits = 64*64*8;
		double rate = (double) bitscoded / (double) originalBits;
				
		Logger.logger().log(output.dumpHex(), SeverityScale.INFO);
		Logger.logger().log("Dumped!", SeverityScale.INFO);
		Logger.logger().log("\tThe compression rate is: " + rate + " bps", SeverityScale.INFO);		
		
	}

}
