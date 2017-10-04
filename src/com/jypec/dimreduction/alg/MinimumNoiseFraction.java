package com.jypec.dimreduction.alg;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.arrays.MatrixTransforms;

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
public class MinimumNoiseFraction extends ProjectingDimensionalityReduction {
	
	
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
	public void train(DMatrixRMaj data) {
		//initialize values
		dimOrig = data.getNumRows();
		//find out data and noise. The data is NOT zero-meaned,
		//while the noise is assumed to be
		DMatrixRMaj noise = extractNoise(data);
		//CommonOps_DDRM.subtract(data, noise, data);
		
		/**Create data covariance matrix */
		adjustment = new DMatrixRMaj(dimOrig, 1);
		DMatrixRMaj sigma = new DMatrixRMaj(dimOrig, dimOrig);
		MatrixOperations.generateCovarianceMatrix(data, sigma, null, adjustment);
		/*********************************/
        
        /**Create noise covariance matrix */
        DMatrixRMaj sigmaNoise = new DMatrixRMaj(dimOrig, dimOrig);
        CommonOps_DDRM.multTransB(noise, noise, sigmaNoise);
        /**********************************/
        
        //decompose sigma noise as noise = U*W*U^t
        SingularValueDecomposition_F64<DMatrixRMaj> svd = DecompositionFactory_DDRM.svd(dimOrig, dimOrig, true, false, false);
        this.say("Decomposition yielded: " + svd.decompose(sigmaNoise));
        DMatrixRMaj B = svd.getU(null, false);
        DMatrixRMaj lambda = svd.getW(null);
        
        DMatrixRMaj A = new DMatrixRMaj(dimOrig, dimOrig);
        MatrixTransforms.inverseSquareRoot(lambda);
        CommonOps_DDRM.mult(B, lambda, A);
        
        
        /** Check if A^t*Sn*A = Identity (seems that way)
        DMatrixRMaj tmp = new DMatrixRMaj(sampleSize, sampleSize);
        DMatrixRMaj tmp2 = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransA(A, sigmaNoise, tmp);
        CommonOps_DDRM.mult(tmp, A, tmp2);
        */
       
        DMatrixRMaj sigmaTemp = new DMatrixRMaj(dimOrig, dimOrig);
        DMatrixRMaj sigmaTransformed = new DMatrixRMaj(dimOrig, dimOrig);
        CommonOps_DDRM.multTransA(A, sigma, sigmaTemp);
        CommonOps_DDRM.mult(sigmaTemp, A, sigmaTransformed);
        
        //decompose sigma temp as noise = U*W*U^t
        svd = DecompositionFactory_DDRM.svd(dimOrig, dimOrig, true, true, false);
        this.say("Decomposition yielded: " + svd.decompose(sigmaTransformed));
        DMatrixRMaj D = svd.getU(null, false);
        
        this.projectionMatrix = new DMatrixRMaj(dimOrig, dimOrig);
        CommonOps_DDRM.mult(A, D, this.projectionMatrix);
        CommonOps_DDRM.transpose(this.projectionMatrix);
        
        this.unprojectionMatrix = new DMatrixRMaj(this.projectionMatrix);
        CommonOps_DDRM.invert(this.unprojectionMatrix);
        //CommonOps_DDRM.transpose(this.unprojectionMatrix);
        
        //CommonOps_DDRM.transpose(this.projectionMatrix);      
        this.projectionMatrix.reshape(dimProj, dimOrig, true);
        CommonOps_DDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix.reshape(dimProj, dimOrig, true);
        CommonOps_DDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix = new DMatrixRMaj(this.unprojectionMatrix); //ensure internal buffer size is the right shape
        this.say("Finished");
	}



}
