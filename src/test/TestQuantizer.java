package test;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import com.jypec.quantization.Quantizer;

public class TestQuantizer {

	/**
	 * Test the corner cases with the quantizer we'll probably use
	 */
	@Test
	public void testQuantizerCornerCases() {
		Quantizer q = new Quantizer(16, 0, 1, 0.0, 65536.0, 0.5);
		double error = 1.0;
		
		double[] valuesToTest = new double[]{0.0, 1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 64.0, 128.0, 256.0, 512.0, 1024.0, 2048.0, 4096.0, 8192.0, 16384.0, 32768.0, 65536.0};
		
		for (double d: valuesToTest) {
			assertEquals(d, q.deQuantizeAndDenormalize(q.normalizeAndQuantize(d)), error);	
		}
	}
	
	@Test
	public void testQuantizerBruteForce() {
		Random r = new Random(1);
		for (int j = 0; j < 400; j++) {
			double upperLimit = r.nextDouble() * 65536;
			double offset = r.nextDouble();
			int e = r.nextInt(29) + 1;
			int m = r.nextInt(0x1 << 11);
			Quantizer q = new Quantizer(e, m, 1, 0, upperLimit, offset);
			
			int bits = q.getNecessaryBitPlanes();
			int max = 0x1 << bits;
			
			//maximum expected error
			double expectedError = 2 * upperLimit / (double) max;

			for (int i = 0; i < 1000; i++) {
				double test = r.nextDouble() * upperLimit;
				assertEquals("Failed with (limit, e, m, test) -> (" + upperLimit+ "," + e + "," + m + "," + test + ") (expected error = " + expectedError + ")", 
						test, q.deQuantizeAndDenormalize(q.normalizeAndQuantize(test)), expectedError);
			}
		}
	}
}
