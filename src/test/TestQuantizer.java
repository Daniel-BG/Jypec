package test;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import com.jypec.quantization.Quantizer;

/**
 * @author Daniel
 * Test the quantizer
 */
public class TestQuantizer {

	/**
	 * Test the corner cases with the quantizer we'll probably use
	 */
	@Test
	public void testQuantizerCornerCases() {
		Quantizer q = new Quantizer(16, 0, 1, 0.0, 65536.0, 0.5);
		float error = 1.0;
		
		float[] valuesToTest = new float[]{0.0, 1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 64.0, 128.0, 256.0, 512.0, 1024.0, 2048.0, 4096.0, 8192.0, 16384.0, 32768.0, 65536.0};
		
		for (float d: valuesToTest) {
			assertEquals(d, q.deQuantizeAndDenormalize(q.normalizeAndQuantize(d)), error);	
		}
	}
	
	/**
	 * Test various random values for the quantizer, ensuring the recovered unquantized values fall within the expected error range
	 */
	@Test
	public void testQuantizerBruteForce() {
		Random r = new Random(1);
		for (int j = 0; j < 400; j++) {
			float upperLimit = r.nextfloat() * 65536;
			float lowerLimit = - r.nextfloat() * 65536;
			float offset = r.nextfloat();
			int e = r.nextInt(29) + 1;
			int m = r.nextInt(0x1 << 11);
			Quantizer q = new Quantizer(e, m, 1, lowerLimit, upperLimit, offset);
			
			int bits = q.getNecessaryBitPlanes();
			int max = 0x1 << bits;
			
			//maximum expected error
			float expectedError = 2 * (upperLimit - lowerLimit) / (float) max;

			for (int i = 0; i < 1000; i++) {
				float test = r.nextfloat() * (upperLimit - lowerLimit) + lowerLimit;
				assertEquals("Failed with ([ll,lh], e, m, test) -> ([" + lowerLimit + "," +  upperLimit+ "]," + e + "," + m + "," + test + ") (expected error = " + expectedError + ")", 
						test, q.deQuantizeAndDenormalize(q.normalizeAndQuantize(test)), expectedError);
			}
		}
	}
}
