package test;

import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.util.BitStream;
import com.jypec.util.FIFOBitStream;
import com.jypec.util.debug.Logger;

public class TestEBCodec {

	/**
	 * Tests if the encoding and then decoding of the given data produces the same result back
	 * @param data
	 * @param width
	 * @param height
	 * @param depth
	 * @param band
	 * @return true if the test was passed
	 */
	private boolean testEncoding(int[][] data, int width, int height, int depth, SubBand band) {
		Logger.logger().log(this, "Testing: " + height + "x" + width + "x" + depth + " (" + band.toString() + ")");
		
		//Code it
		CodingBlock block = new CodingBlock(data, height, width, depth, band);
		BitStream output = new FIFOBitStream();
		EBCoder coder = new EBCoder();
		coder.code(block, output);
		
		//decode it
		CodingBlock blockOut = new CodingBlock(height, width, depth, band);
		EBDecoder decoder = new EBDecoder();
		decoder.decode(output, blockOut);

		boolean right = true;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				assertEquals("Failed @" + i + "," + j, data[i][j], blockOut.getData()[i][j]);
				if (data[i][j] != blockOut.getData()[i][j]) {
					right = false;
				}
			}
		}
		
		return right;
	}
	
	
	/**
	 * Fills the matrix with the given value
	 * @param data
	 * @param width
	 * @param height
	 * @param value
	 */
	private void fillDataWithValue(int[][] data, int width, int height, int value) {
		for (int i = 0; i < height; i++) { 
			for (int j = 0; j < width; j++) {
				data[i][j] = value;
			}
		}
	}
	
	
	/**
	 * Fill the matrix with random data following a gaussian distribution with mean 0 and std of 2^depth.
	 * Values over the limit of 2^depth are clamped to the inteval [-2^depth + 1, 2^depth - 1]
	 * @param data
	 * @param width
	 * @param height
	 * @param depth
	 * @param r
	 */
	private void randomizeData(int[][] data, int width, int height, int depth, Random r) {
		int limit = 0x1 << (depth - 1);
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				double val = r.nextGaussian() * limit;
				int res = (int) val;
				if (res < -limit)
					res = -limit;
				if (res > limit)
					res = limit;
				
				int sign = res < 0 ? 1 : 0;
				int magnitude = res < 0 ? -res : res;
				
				data[i][j] = (sign << depth) + magnitude;
			}
		}
	}


	@Test
	public void testDifferentDepths() {
		Random r = new Random();
		
		//test all depths
		int width = 64, height = 64, depth = 1;
		int[][] data = new int[height][width];
		for (depth = 1; depth < 32; depth++) {
			this.fillDataWithValue(data, width, height, 0);
			assertTrue("Failed testing with zeroes at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
			this.fillDataWithValue(data, width, height, (-1) & (0xffffffff >>> (31 - depth)));
			assertTrue("Failed testing with ones at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
			this.randomizeData(data, width, height, depth, r);
			assertTrue("Failed testing with random at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
		}
	}
	
	@Test
	public void testDifferentSizes() {
		Random r = new Random();

		//test all regular resulutions
		int depth = 16;
		int MAX_SIZE = 64;
		int [][] data = new int[MAX_SIZE][MAX_SIZE];
		for (int size = 1; size <= 64; size++) {
			this.fillDataWithValue(data, size, size, 0);
			assertTrue("Failed testing with zeroes at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
			this.fillDataWithValue(data, size, size, (-1) & (0xffffffff >>> (31 - depth)));
			assertTrue("Failed testing with ones at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
			this.randomizeData(data, size, size, depth, r);
			assertTrue("Failed testing with random at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
		}
	}
	
	@Test
	public void testDifferentBands() {
		Random r = new Random();
		
		//test all subband predictions
		SubBand[] subBands = SubBand.values();
		int width = 64, height = 64, depth = 16;
		int [][] data = new int[height][width];
		for (int i = 0; i < subBands.length; i++) {
			this.fillDataWithValue(data, width, height, 0);
			this.testEncoding(data, width, height, depth, subBands[i]);
			this.fillDataWithValue(data, width, height, (-1) & (0xffffffff >>> (31 - depth)));
			this.testEncoding(data, width, height, depth, subBands[i]);
			this.randomizeData(data, width, height, depth, r);
			this.testEncoding(data, width, height, depth, subBands[i]);
		}
	}
	
	@Test
	public void testRandomConfigurations() {
		Random r = new Random();
		
		//test irregular sizes
		int[] widths = {64, 1, 64, 4, 50, 59, 52, 20, 46, 12, 36, 44, 51, 19};
		int[] heights = {1, 64, 4, 64, 60, 2, 28, 4, 10, 59, 7, 33, 29, 16};
		int[] depths = {4, 4, 4, 4, 1, 25, 21, 3, 12, 7, 16, 3, 3, 6};
		int [][] data = new int[64][64];
		for (int i = 0; i < widths.length; i++) {
			this.fillDataWithValue(data, widths[i], heights[i], 0);
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
			this.fillDataWithValue(data, widths[i], heights[i], (-1) & (0xffffffff >>> (31 - depths[i])));
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
			this.randomizeData(data, widths[i], heights[i], depths[i], r);
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
		}

	}
	


}
