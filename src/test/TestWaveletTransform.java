package test;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

import java.util.Random;

import com.jypec.wavelet.BidimensionalWaveletTransform;
import com.jypec.wavelet.WaveletTransform;

public class TestWaveletTransform {

	@Test
	public void testIndexOutOfBounds() {
		for (int i = 1; i < 100; i++) {
			double[] s = new double[i];
			WaveletTransform.forwardTransform(s, i);
			WaveletTransform.reverseTransform(s, i);
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
			
			WaveletTransform.forwardTransform(s, i);
			WaveletTransform.reverseTransform(s, i);
			
			assertArrayEquals(s, res, 0.000001);
		}
	}
	
	
	@Test
	public void testSymetricBidimensionalRecovery() {
		Random r = new Random();
		for (int i = 1; i < 50; i++) {
			double[][] s = new double[i][i*2];
			double[][] res = new double[i][i*2];
			for (int k = 0; k < i; k++) {
				for (int j = 0; j < i*2; j++) {
					s[k][j] = r.nextGaussian() * 1000;
					res[k][j] = s[k][j];
				}
			}
			
			BidimensionalWaveletTransform.forwardTransform(res, i, i);
			BidimensionalWaveletTransform.reverseTransform(res, i, i);
			
			for (int k = 0; k < i; k++) {
				assertArrayEquals(s[k], res[k], 0.000001);
			}
		}
	}
	
}
