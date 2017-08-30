package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.jypec.wavelet.Wavelet;
import com.jypec.wavelet.compositeTransforms.OneDimensionalWaveletExtender;
import com.jypec.wavelet.kernelTransforms.cdf97.KernelCdf97WaveletTransform;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * @author Daniel
 * Test various wavelet transforms (can be specified inside in the @Parameters array)
 */
@RunWith(Parameterized.class)
public class TestWaveletTransform {
	
	/**
	 * @return a list of wavelets to be tested
	 */
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{new LiftingCdf97WaveletTransform()}, {new KernelCdf97WaveletTransform()}});
    }

	/** Wavelet under test */
	public Wavelet testWavelet;
	
	/**
	 * @param wavelet wavelet to test
	 */
	public TestWaveletTransform(Wavelet wavelet) {
		this.testWavelet = wavelet;
	}

	
	/**
	 * Test that the wavelet does not go out of bounds in small signals (ranging from one element onwards)
	 */
	@Test
	public void testIndexOutOfBounds() {
		for (int i = 1; i < 100; i++) {
			double[] s = new double[i];
			testWavelet.forwardTransform(s, i);
			testWavelet.reverseTransform(s, i);
		}
	}
	
	
	/**
	 * Test that after a forward and reverse transform, the original signal is recovered within a margin of 1 ten thousanth
	 */
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
			
			testWavelet.forwardTransform(s, i);
			testWavelet.reverseTransform(s, i);
			
			assertArrayEquals(s, res, 0.0001);
		}
	}
	
	
	/**
	 * Test that the filter works for recovering a signal  when made two dimensional by applying along both dimensions
	 */
	@Test
	public void testSymetricBidimensionalRecovery() {
		OneDimensionalWaveletExtender biTestWavelet = new OneDimensionalWaveletExtender(testWavelet);
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
			
			biTestWavelet.forwardTransform(res, i, i);
			biTestWavelet.reverseTransform(res, i, i);
			
			for (int k = 0; k < i; k++) {
				assertArrayEquals(s[k], res[k], 0.0001);
			}
		}
	}
	
	//TODO add tests for irregular matrices
	
}
