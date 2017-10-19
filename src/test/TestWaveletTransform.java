package test;

import org.ejml.data.FMatrixRMaj;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.jypec.util.arrays.ArrayTransforms;
import com.jypec.wavelet.Wavelet;
import com.jypec.wavelet.compositeTransforms.OneDimensionalWaveletExtender;
import com.jypec.wavelet.kernelTransforms.cdf97.KernelCdf97WaveletTransform;
import com.jypec.wavelet.liftingTransforms.LiftingCdf97WaveletTransform;

import test.generic.TestHelpers;

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
			float[] s = new float[i];
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
			float[] s = new float[i];
			float[] res = new float[i];
			TestHelpers.randomGaussianFillArray(s, s.length, r, 1000, 0);
			ArrayTransforms.copy(s, res, s.length);
			
			testWavelet.forwardTransform(s, i);
			testWavelet.reverseTransform(s, i);
			
			assertArrayEquals(s, res, 0.01f);
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
			FMatrixRMaj s = new FMatrixRMaj(i, i*2);
			TestHelpers.randomGaussianFillArray(s.data, i*i*2, r, 1000, 0);
			FMatrixRMaj res = new FMatrixRMaj(s);
			
			biTestWavelet.forwardTransform(res, i, i);
			biTestWavelet.reverseTransform(res, i, i);
			
			for (int k = 0; k < i; k++) {
				assertArrayEquals(s.data, res.data, 0.01f);
			}
		}
	}
	
	//TODO add tests for irregular matrices
	
}
