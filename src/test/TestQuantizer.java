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
		Quantizer q = new Quantizer(16, 0, 1, 0.0f, 65536.0f, 0.5f);
		float error = 1.0f;
		
		float[] valuesToTest = new float[]{0.0f, 1.0f, 2.0f, 4.0f, 8.0f, 16.0f, 32.0f, 64.0f, 128.0f, 256.0f, 512.0f, 1024.0f, 2048.0f, 4096.0f, 8192.0f, 16384.0f, 32768.0f, 65536.0f};
		
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
			float upperLimit = r.nextFloat() * 65536;
			float lowerLimit = - r.nextFloat() * 65536;
			float offset = r.nextFloat();
			int e = r.nextInt(29) + 1;
			int m = r.nextInt(0x1 << 11);
			Quantizer q = new Quantizer(e, m, 1, lowerLimit, upperLimit, offset);
			
			int bits = q.getNecessaryBitPlanes();
			int max = 0x1 << bits;
			
			//maximum expected error
			float distance = upperLimit - lowerLimit;
			float step = 2f * distance / (float) max;
			float expectedError = step * (float) Math.pow(10, Math.log10(distance) - 3); //adjust to float prec

			for (int i = 0; i < 1000; i++) {
				float test = r.nextFloat() * (upperLimit - lowerLimit) + lowerLimit;
				assertEquals("Failed with ([ll,lh], e, m, test) -> ([" + lowerLimit + "," +  upperLimit+ "]," + e + "," + m + "," + test + ") (expected error = " + expectedError + ")", 
						test, q.deQuantizeAndDenormalize(q.normalizeAndQuantize(test)), expectedError);
			}
		}
	}
}
