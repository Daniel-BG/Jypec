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
import com.jypec.util.data.BidimensionalArrayIntegerMatrix;
import com.jypec.util.data.IntegerMatrix;
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
	private boolean testEncoding(IntegerMatrix data, int width, int height, int depth, SubBand band) {
		Logger.logger().log(this, "Testing: " + height + "x" + width + "x" + depth + " (" + band.toString() + ")");
		
		//Code it
		CodingBlock block = new CodingBlock(data, height, width, 0, 0, depth, band);
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
				assertEquals("Failed @" + i + "," + j, data.getDataAt(i, j), blockOut.getDataAt(i, j));
				if (data.getDataAt(i, j) != blockOut.getDataAt(i, j)) {
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
	public static void fillDataWithValue(IntegerMatrix data, int width, int height, int value) {
		for (int i = 0; i < height; i++) { 
			for (int j = 0; j < width; j++) {
				data.setDataAt(value, i, j);
			}
		}
	}
	
	
	/**
	 * Fill the matrix with random data following a gaussian distribution with mean 0 and std of 2^depth.
	 * Values over the limit of 2^depth are clamped to the inteval [-2^depth + 1, 2^depth - 1]. 
	 * Returned values are in sign-magnitude format, with one sign bit followed by (depth - 1) magnitude bits
	 * Can also be interpreted as unsigned values
	 * @param data
	 * @param width
	 * @param height
	 * @param depth: does not include the sign bit!
	 * @param r
	 */
	public static void randomizeMatrix(Random r, IntegerMatrix data, int width, int height, int depth) {
		int magnitudeLimit = (0x1 << (depth - 1)) - 1;
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				double val = r.nextGaussian() * magnitudeLimit;
				int res = (int) val;
				if (res < -magnitudeLimit)
					res = -magnitudeLimit;
				if (res > magnitudeLimit)
					res = magnitudeLimit;
				
				int sign = res < 0 ? 1 : 0;
				int magnitude = res < 0 ? -res : res;
				
				data.setDataAt((sign << (depth - 1)) + magnitude, i, j);
			}
		}
	}


	@Test
	public void testDifferentDepths() {
		Random r = new Random();
		
		//test all depths
		int width = 64, height = 64, depth = 1;
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(height, width);
		for (depth = 2; depth <= 32; depth++) {
			fillDataWithValue(data, width, height, 0);
			assertTrue("Failed testing with zeroes at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
			fillDataWithValue(data, width, height, (-1) & (0xffffffff >>> (32 - depth)));
			assertTrue("Failed testing with ones at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
			randomizeMatrix(r, data, width, height, depth);
			assertTrue("Failed testing with random at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
		}
	}
	
	@Test
	public void testDifferentSizes() {
		Random r = new Random();

		//test all regular resulutions
		int depth = 16;
		int MAX_SIZE = 64;
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(MAX_SIZE, MAX_SIZE);
		for (int size = 1; size <= 64; size++) {
			fillDataWithValue(data, size, size, 0);
			assertTrue("Failed testing with zeroes at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
			fillDataWithValue(data, size, size, (-1) & (0xffffffff >>> (32 - depth)));
			assertTrue("Failed testing with ones at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
			randomizeMatrix(r, data, size, size, depth);
			assertTrue("Failed testing with random at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
		}
	}
	
	@Test
	public void testDifferentBands() {
		Random r = new Random();
		
		//test all subband predictions
		SubBand[] subBands = SubBand.values();
		int width = 64, height = 64, depth = 16;
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(height, width);
		for (int i = 0; i < subBands.length; i++) {
			fillDataWithValue(data, width, height, 0);
			this.testEncoding(data, width, height, depth, subBands[i]);
			fillDataWithValue(data, width, height, (-1) & (0xffffffff >>> (32 - depth)));
			this.testEncoding(data, width, height, depth, subBands[i]);
			randomizeMatrix(r, data, width, height, depth);
			this.testEncoding(data, width, height, depth, subBands[i]);
		}
	}
	
	@Test
	public void testRandomConfigurations() {
		Random r = new Random();
		
		//test irregular sizes
		int[] widths = {64, 1, 64, 4, 50, 59, 52, 20, 46, 12, 36, 44, 51, 19};
		int[] heights = {1, 64, 4, 64, 60, 2, 28, 4, 10, 59, 7, 33, 29, 16};
		int[] depths = {4, 4, 4, 4, 2, 25, 21, 3, 12, 7, 16, 3, 3, 6};
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(64, 64);
		for (int i = 0; i < widths.length; i++) {
			fillDataWithValue(data, widths[i], heights[i], 0);
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
			fillDataWithValue(data, widths[i], heights[i], (-1) & (0xffffffff >>> (32 - depths[i])));
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
			randomizeMatrix(r, data, widths[i], heights[i], depths[i]);
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
		}

	}
	


}
