package test;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.jypec.pca.PrincipalComponentAnalysis;
import com.jypec.util.FIFOBitStream;
import com.jypec.util.io.BitStreamDataReaderWriter;
import test.generic.TestHelpers;

/**
 * @author Daniel
 * Test the PrincipalComponentAnalysis class, as well as its recovery
 */
@RunWith(Parameterized.class)
public class PCARecoveryTest {
	
	/**
	 * @return a list of wavelets to be tested
	 */
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{5, 4, 4, 0}, {7, 7, 7, 0}, {15, 3, 6, 0}, {100, 20, 20, 0}, {150, 1, 150, 0}, {1000, 20, 25, 0}});
    }

	/** Number of samples under test */
	public int numSamples;
	/** Number of eigenvalues under test */
	public int eigenSize;
	/** Size of each sample */
	public int sampleSize;
	/** Random generating test values */
	public Random r;
	
	/**
	 * @param numSamples samples to test
	 * @param eigenSize number of eigenvalues
	 * @param sampleSize size of the samples
	 * @param seed seed for the RNG
	 * @param wavelet wavelet to test
	 */
	public PCARecoveryTest(int numSamples, int eigenSize, int sampleSize, long seed) {
		this.numSamples = numSamples;
		this.eigenSize = eigenSize;
		this.sampleSize = sampleSize;
		this.r = new Random(seed);
	}
	
	/**
	 * Test if the PCA can recover itself from a bitstream
	 */
	@Test
	public void testPCARecovery() {
		PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
		
		pca.setup(numSamples, sampleSize);
		double[] inputData = new double[sampleSize];
		
		for (int i = 0; i < numSamples; i++) {
			TestHelpers.randomGaussianFillArray(inputData, sampleSize, r, 1000, 0);
			pca.addSample(inputData);
		}
		
		pca.computeBasis(eigenSize);
		
		FIFOBitStream bs = new FIFOBitStream();
		BitStreamDataReaderWriter bw = new BitStreamDataReaderWriter();
		bw.setStream(bs);
		
		pca.saveToBitStream(bw);
		
		PrincipalComponentAnalysis pcaRec = new PrincipalComponentAnalysis();
		
		pcaRec.restoreFromBitStream(bw);
		
		
		for (int i = 0; i < pca.getNumComponents(); i++) {
			assertArrayEquals(pca.getBasisVector(i), pcaRec.getBasisVector(i), 0.0);
		}
		
	}
	
}
