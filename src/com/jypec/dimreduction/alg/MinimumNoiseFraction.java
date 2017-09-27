package com.jypec.dimreduction.alg;

import java.io.IOException;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.arrays.MatrixTransforms;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Implements the Minimum noise fraction algorithm for dimensionality reduction
 * <br>
 * Taken from "Real-Time Noise Removal for Line-Scanning Hyperspectral
 * Devices Using a Minimum Noise Fraction-Based Approach"
 * by Asgeir Bjorgan and Lise Lyngsnes Randeberg
 * <br>
 * (<a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4367363/pdf/sensors-15-03362.pdf">Visited 2017-09-25</a>)
 * <br><br>
 * @author Daniel
 */
public class MinimumNoiseFraction extends DimensionalityReduction {

	private int numComponents;
	private int sampleSize;
	private DMatrixRMaj projectionMatrix;
	private DMatrixRMaj unprojectionMatrix;
    private double mean[];
	
	
	/**
	 * Build a MNF algorithm
	 */
	public MinimumNoiseFraction() {
		super(DimensionalityReductionAlgorithm.DRA_MNF);
	}
	
	/**
	 * Extract the noise from the data. the formula used is: <br>
	 * noise(i,j) = (data(i,j) - data(i,j+1))/2 <br>
	 * except for the last value where: <br>
	 * noise(i,j) = (data(i,j) - data(i,j-1))/2 <br>
	 * @param data
	 * @return
	 */
	private static DMatrixRMaj extractNoise(DMatrixRMaj data) {
		//assume pushbroom sensor and only extract horizontal noise
		DMatrixRMaj res = new DMatrixRMaj(data.getNumRows(), data.getNumCols());
		for (int i = 0; i < data.getNumRows(); i++) {
			for (int j = 0; j < data.getNumCols(); j++) {
				double val = data.get(i, j);
				if (j < data.getNumCols() - 1) {
					val -= data.get(i, j+1);
				} else {
					val -= data.get(i, j-1);
				}
				res.set(i, j, val / 2.0);
			}
		}
		return res;
	}

	//https://www.researchgate.net/profile/Angelo_Palombo/publication/224354550_Experimental_Approach_to_the_Selection_of_the_Components_in_the_Minimum_Noise_Fraction/links/02bfe51064486871c4000000.pdf
	@Override
	public void train(HyperspectralImageData source) {
		//initialize values
		sampleSize = source.getNumberOfBands();
		int samples = source.getNumberOfLines() * source.getNumberOfSamples();
		this.mean = new double[sampleSize];
		//find out data and noise. The data is NOT zero-meaned,
		//while the noise is assumed to be
		DMatrixRMaj data = source.toDoubleMatrix();
		DMatrixRMaj noise = extractNoise(data);
		//CommonOps_DDRM.subtract(data, noise, data);
		
		/**Create data covariance matrix */
        //compute the summation of all the samples
        DMatrixRMaj ones = new DMatrixRMaj(samples, 1);
        for (int i = 0; i < ones.getNumElements(); i++) {
        	ones.set(i, 1);
        }
        DMatrixRMaj summ = new DMatrixRMaj(sampleSize, 1);
        CommonOps_DDRM.mult(data, ones, summ);
		
		//compute the mean of all samples
        DMatrixRMaj meann = new DMatrixRMaj(sampleSize, 1);
        for( int j = 0; j < sampleSize; j++ ) {
        	mean[j] = summ.get(j) / (double) data.getNumCols();
        	meann.set(j, mean[j]);
        }
        
        //create covariance matrix
        DMatrixRMaj sigma = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransB(data, data, sigma);
        DMatrixRMaj sigmaHelper = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransB(meann, summ, sigmaHelper);
        CommonOps_DDRM.subtract(sigma, sigmaHelper, sigma);
		/*********************************/
        
        /**Create noise covariance matrix */
        DMatrixRMaj sigmaNoise = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransB(noise, noise, sigmaNoise);
        /**********************************/
        
        //decompose sigma noise as noise = U*W*U^t
        SingularValueDecomposition_F64<DMatrixRMaj> svd = DecompositionFactory_DDRM.svd(sampleSize, sampleSize, true, false, false);
        this.say("Decomposition yielded: " + svd.decompose(sigmaNoise));
        DMatrixRMaj B = svd.getU(null, false);
        DMatrixRMaj lambda = svd.getW(null);
        
        DMatrixRMaj A = new DMatrixRMaj(sampleSize, sampleSize);
        MatrixTransforms.inverseSquareRoot(lambda);
        CommonOps_DDRM.mult(B, lambda, A);
        
        
        /** Check if A^t*Sn*A = Identity (seems that way)
        DMatrixRMaj tmp = new DMatrixRMaj(sampleSize, sampleSize);
        DMatrixRMaj tmp2 = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransA(A, sigmaNoise, tmp);
        CommonOps_DDRM.mult(tmp, A, tmp2);
        */
       
        DMatrixRMaj sigmaTemp = new DMatrixRMaj(sampleSize, sampleSize);
        DMatrixRMaj sigmaTransformed = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransA(A, sigma, sigmaTemp);
        CommonOps_DDRM.mult(sigmaTemp, A, sigmaTransformed);
        
        //decompose sigma temp as noise = U*W*U^t
        svd = DecompositionFactory_DDRM.svd(sampleSize, sampleSize, true, true, false);
        this.say("Decomposition yielded: " + svd.decompose(sigmaTransformed));
        DMatrixRMaj D = svd.getU(null, false);
        
        this.projectionMatrix = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.mult(A, D, this.projectionMatrix);
        CommonOps_DDRM.transpose(this.projectionMatrix);
        
        this.unprojectionMatrix = new DMatrixRMaj(this.projectionMatrix);
        CommonOps_DDRM.invert(this.unprojectionMatrix);
        //CommonOps_DDRM.transpose(this.unprojectionMatrix);
        
        //CommonOps_DDRM.transpose(this.projectionMatrix);      
        this.projectionMatrix.reshape(numComponents, sampleSize, true);
        CommonOps_DDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix.reshape(numComponents, sampleSize, true);
        CommonOps_DDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix = new DMatrixRMaj(this.unprojectionMatrix); //ensure internal buffer size is the right shape
        this.say("Finished");
	}

	
	@Override
	public DMatrixRMaj reduce(HyperspectralImageData source) {
		DMatrixRMaj img = source.toDoubleMatrix();
		for (int i = 0; i < img.getNumRows(); i++) {
			for (int j = 0; j < img.getNumCols(); j++) {
				img.minus(img.getIndex(i, j), this.mean[i]);
			}
		}
		DMatrixRMaj res = new DMatrixRMaj(this.numComponents, img.getNumCols());
		CommonOps_DDRM.mult(projectionMatrix, img, res);
		return res;
	}

	@Override
	public void boost(DMatrixRMaj src, HyperspectralImageData dst) {
		this.sayLn("Boosting samples from reduced space to the original...");
		DMatrixRMaj res = new DMatrixRMaj(this.sampleSize, src.getNumCols());
		CommonOps_DDRM.mult(unprojectionMatrix, src, res);
		for (int i = 0; i < this.sampleSize; i++) {
			for (int j = 0; j < res.getNumCols(); j++) {
				res.plus(res.getIndex(i, j), this.mean[i]);
			}
		}
		dst.copyDataFrom(res);
	}

	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
    	//write the number of dimensions in the original space
    	bw.writeInt(this.sampleSize);
    	//write the number of dimensions in the reduced space
    	bw.writeInt(numComponents);
    	//write the mean
    	bw.writeDoubleArray(mean, this.sampleSize);
    	//write the matrix
    	bw.writeDoubleArray(unprojectionMatrix.getData(), this.sampleSize * numComponents);
	}

	@Override
	public void doLoadFrom(BitInputStream bw, ComParameters cp, ImageHeaderData ihd) throws IOException {
    	//read the number of dimensions in the original space
    	this.sampleSize = bw.readInt();
    	//read the number of dimensions in the reduced space
    	this.numComponents = bw.readInt();
    	//read the mean
    	this.mean = bw.readDoubleArray(sampleSize);
    	//read the projection matrix
    	unprojectionMatrix = new DMatrixRMaj();
    	unprojectionMatrix.setData(bw.readDoubleArray(this.sampleSize * this.numComponents));
    	unprojectionMatrix.reshape(this.sampleSize,this.numComponents,true);
	}

	@Override
	public int getNumComponents() {
		return this.numComponents;
	}

	@Override
	public void setNumComponents(int numComponents) {
		this.numComponents = numComponents;
	}

	@Override
	public double getMaxValue(HyperspectralImageData img) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMinValue(HyperspectralImageData img) {
		throw new UnsupportedOperationException();
	}

}
