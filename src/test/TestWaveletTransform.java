package test;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

import java.util.Random;

import com.jypec.wavelet.WaveletTransform;

public class TestWaveletTransform {

	@Test
	public void testIndexOutOfBounds() {
		for (int i = 1; i < 100; i++) {
			double[] s = new double[i];
			WaveletTransform.forwardTransform(s);
			WaveletTransform.reverseTransform(s);
		}
	}
	
	
	@Test
	public void testSignalRecovered() {
		Random r = new Random();
		for (int i = 1; i < 200; i++) {
			double[] s = new double[i];
			double[] res = new double[i];
			for (int j = 0; j < s.length; j++) {
				s[j] = r.nextGaussian() * 1000;
				res[j] = s[j];
			}
			
			WaveletTransform.forwardTransform(s);
			WaveletTransform.reverseTransform(s);
			
			assertArrayEquals(s, res, 0.000001);
		}
	}
	
}
