package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.jypec.ebc.EBCoder;
import com.jypec.ebc.EBDecoder;
import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.datastructures.BidimensionalArrayIntegerMatrix;
import com.jypec.util.datastructures.IntegerMatrix;
import com.jypec.util.debug.Logger;

import test.generic.TestHelpers;

/**
 * @author Daniel
 * Test for the embedded block coder
 */
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
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(bais);
		
		
		EBCoder coder = new EBCoder();
		try {
			coder.code(block, output);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		//decode it
		CodingBlock blockOut = new CodingBlock(height, width, depth, band);
		EBDecoder decoder = new EBDecoder();
		
		BitInputStream input = new BitInputStream(new ByteArrayInputStream(bais.toByteArray()));
		try {
			decoder.decode(input, blockOut);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

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
	 * Test if the EBCoder works for different depths (2 to 32)
	 */
	@Test
	public void testDifferentDepths() {
		Random r = new Random();
		
		//test all depths
		int width = 64, height = 64, depth = 1;
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(height, width);
		for (depth = 2; depth <= 32; depth++) {
			TestHelpers.fillDataWithValue(data, width, height, 0);
			assertTrue("Failed testing with zeroes at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
			TestHelpers.fillDataWithValue(data, width, height, (-1) & (0xffffffff >>> (32 - depth)));
			assertTrue("Failed testing with ones at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
			TestHelpers.randomizeMatrix(r, data, width, height, depth);
			assertTrue("Failed testing with random at depth: " + depth, this.testEncoding(data, width, height, depth, SubBand.HH));
		}
	}
	
	/**
	 * Test if the EBCoder works for different matrix sizes
	 */
	@Test
	public void testDifferentSizes() {
		Random r = new Random();

		//test all regular resulutions
		int depth = 16;
		int MAX_SIZE = 64;
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(MAX_SIZE, MAX_SIZE);
		for (int size = 1; size <= 64; size++) {
			TestHelpers.fillDataWithValue(data, size, size, 0);
			assertTrue("Failed testing with zeroes at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
			TestHelpers.fillDataWithValue(data, size, size, (-1) & (0xffffffff >>> (32 - depth)));
			assertTrue("Failed testing with ones at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
			TestHelpers.randomizeMatrix(r, data, size, size, depth);
			assertTrue("Failed testing with random at depth: " + depth, this.testEncoding(data, size, size, depth, SubBand.HH));
		}
	}
	
	/**
	 * Test if the EBCoder works for different subband types
	 */
	@Test
	public void testDifferentBands() {
		Random r = new Random();
		
		//test all subband predictions
		SubBand[] subBands = SubBand.values();
		int width = 64, height = 64, depth = 16;
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(height, width);
		for (int i = 0; i < subBands.length; i++) {
			TestHelpers.fillDataWithValue(data, width, height, 0);
			this.testEncoding(data, width, height, depth, subBands[i]);
			TestHelpers.fillDataWithValue(data, width, height, (-1) & (0xffffffff >>> (32 - depth)));
			this.testEncoding(data, width, height, depth, subBands[i]);
			TestHelpers.randomizeMatrix(r, data, width, height, depth);
			this.testEncoding(data, width, height, depth, subBands[i]);
		}
	}
	
	/**
	 * Test the EBCoder in a few different random scenarios to see if it works for non regular cases too
	 */
	@Test
	public void testRandomConfigurations() {
		Random r = new Random();
		
		//test irregular sizes
		int[] widths = {64, 1, 64, 4, 50, 59, 52, 20, 46, 12, 36, 44, 51, 19};
		int[] heights = {1, 64, 4, 64, 60, 2, 28, 4, 10, 59, 7, 33, 29, 16};
		int[] depths = {4, 4, 4, 4, 2, 25, 21, 3, 12, 7, 16, 3, 3, 6};
		IntegerMatrix data = BidimensionalArrayIntegerMatrix.newMatrix(64, 64);
		for (int i = 0; i < widths.length; i++) {
			TestHelpers.fillDataWithValue(data, widths[i], heights[i], 0);
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
			TestHelpers.fillDataWithValue(data, widths[i], heights[i], (-1) & (0xffffffff >>> (32 - depths[i])));
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
			TestHelpers.randomizeMatrix(r, data, widths[i], heights[i], depths[i]);
			this.testEncoding(data, widths[i], heights[i], depths[i], SubBand.HH);
		}

	}
	


}
