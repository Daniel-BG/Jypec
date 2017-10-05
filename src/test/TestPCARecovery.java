package test;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.ejml.data.DMatrixRMaj;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.dimreduction.ProjectingDimensionalityReduction.Precision;
import com.jypec.dimreduction.alg.PrincipalComponentAnalysis;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

import test.generic.TestHelpers;

/**
 * @author Daniel
 * Test the PrincipalComponentAnalysis class, as well as its recovery
 */
@RunWith(Parameterized.class)
public class TestPCARecovery {
	
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
	public TestPCARecovery(int numSamples, int eigenSize, int sampleSize, long seed) {
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
		pca.setPrecision(Precision.DOUBLE);
		pca.setNumComponents(eigenSize);
		
		double[] inputData = new double[sampleSize*numSamples];
		TestHelpers.randomGaussianFillArray(inputData, sampleSize*numSamples, r, 1000, 0);
		DMatrixRMaj mat = new DMatrixRMaj(sampleSize, numSamples);
		mat.setData(inputData);
		
		pca.train(mat);
		
		BitOutputStreamTree bost = new BitOutputStreamTree();
		BitInputStream input;
		
		PrincipalComponentAnalysis pcaRec = new PrincipalComponentAnalysis();
		pcaRec.setPrecision(Precision.DOUBLE);
		
		try {
			pca.saveTo(bost);
			bost.paddingFlush();
			input = bost.getBis();
			pcaRec = (PrincipalComponentAnalysis) DimensionalityReduction.loadFrom(input);
			
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		for (int i = 0; i < pca.getNumComponents(); i++) {
			DMatrixRMaj orig = pca.getUnProjectionMatrix();
			DMatrixRMaj rec = pcaRec.getUnProjectionMatrix();
			
			assertArrayEquals(orig.data, rec.data, 0.0);
		}
		
	}
	
}
