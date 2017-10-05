package com.jypec.dimreduction.alg;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.arrays.MatrixTransforms;
import com.jypec.util.debug.Logger;

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
	private static FMatrixRMaj extractNoise(FMatrixRMaj data) {
		//assume pushbroom sensor and only extract horizontal noise
		FMatrixRMaj res = new FMatrixRMaj(data.getNumRows(), data.getNumCols());
		for (int i = 0; i < data.getNumRows(); i++) {
			for (int j = 0; j < data.getNumCols(); j++) {
				float val = data.get(i, j);
				if (j < data.getNumCols() - 1) {
					val -= data.get(i, j+1);
				} else {
					val -= data.get(i, j-1);
				}
				res.set(i, j, val / 2.0f);
			}
		}
		return res;
	}

	//https://www.researchgate.net/profile/Angelo_Palombo/publication/224354550_Experimental_Approach_to_the_Selection_of_the_Components_in_the_Minimum_Noise_Fraction/links/02bfe51064486871c4000000.pdf
	@Override
	public void train(FMatrixRMaj data) {
		//initialize values
		dimOrig = data.getNumRows();
		//find out data and noise. The data is NOT zero-meaned,
		//while the noise is assumed to be
		FMatrixRMaj noise = extractNoise(data);
		//CommonOps_FDRM.subtract(data, noise, data);
		
		/**Create data covariance matrix */
		adjustment = new FMatrixRMaj(dimOrig, 1);
		FMatrixRMaj sigma = new FMatrixRMaj(dimOrig, dimOrig);
		MatrixOperations.generateCovarianceMatrix(data, sigma, null, adjustment);
		/*********************************/
        
        /**Create noise covariance matrix */
        FMatrixRMaj sigmaNoise = new FMatrixRMaj(dimOrig, dimOrig);
        CommonOps_FDRM.multTransB(noise, noise, sigmaNoise);
        /**********************************/
        
        //decompose sigma noise as noise = U*W*U^t
        SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(dimOrig, dimOrig, true, false, false);
        Logger.getLogger().log("Decomposition yielded: " + svd.decompose(sigmaNoise));
        FMatrixRMaj B = svd.getU(null, false);
        FMatrixRMaj lambda = svd.getW(null);
        
        FMatrixRMaj A = new FMatrixRMaj(dimOrig, dimOrig);
        MatrixTransforms.inverseSquareRoot(lambda);
        CommonOps_FDRM.mult(B, lambda, A);
        
        
        /** Check if A^t*Sn*A = Identity (seems that way)
        FMatrixRMaj tmp = new FMatrixRMaj(sampleSize, sampleSize);
        FMatrixRMaj tmp2 = new FMatrixRMaj(sampleSize, sampleSize);
        CommonOps_FDRM.multTransA(A, sigmaNoise, tmp);
        CommonOps_FDRM.mult(tmp, A, tmp2);
        */
       
        FMatrixRMaj sigmaTemp = new FMatrixRMaj(dimOrig, dimOrig);
        FMatrixRMaj sigmaTransformed = new FMatrixRMaj(dimOrig, dimOrig);
        CommonOps_FDRM.multTransA(A, sigma, sigmaTemp);
        CommonOps_FDRM.mult(sigmaTemp, A, sigmaTransformed);
        
        //decompose sigma temp as noise = U*W*U^t
        svd = DecompositionFactory_FDRM.svd(dimOrig, dimOrig, true, true, false);
        Logger.getLogger().log("Decomposition yielded: " + svd.decompose(sigmaTransformed));
        FMatrixRMaj D = svd.getU(null, false);
        
        this.projectionMatrix = new FMatrixRMaj(dimOrig, dimOrig);
        CommonOps_FDRM.mult(A, D, this.projectionMatrix);
        CommonOps_FDRM.transpose(this.projectionMatrix);
        
        this.unprojectionMatrix = new FMatrixRMaj(this.projectionMatrix);
        CommonOps_FDRM.invert(this.unprojectionMatrix);
        //CommonOps_FDRM.transpose(this.unprojectionMatrix);
        
        //CommonOps_FDRM.transpose(this.projectionMatrix);      
        this.projectionMatrix.reshape(dimProj, dimOrig, true);
        CommonOps_FDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix.reshape(dimProj, dimOrig, true);
        CommonOps_FDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix = new FMatrixRMaj(this.unprojectionMatrix); //ensure internal buffer size is the right shape
        Logger.getLogger().log("Finished");
	}



}
