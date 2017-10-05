package com.jypec.util.arrays;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.simple.SimpleMatrix;

/**
 * Some operations over matrices
 * @author Daniel
 */
public class MatrixOperations {
	
	/**
	 * @param source
	 * @return the min and max values found in the source
	 */
	public static float[] minMax(float[][] source) {
		float[] minMax = new float[2];
		int rows = source.length;
		int cols = source[0].length;
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int j = 0; j < rows; j++) {
			for (int k = 0; k < cols; k++) {
				float sample = source[j][k];
				if (minMax[0] > sample) {
					minMax[0] = sample;
				}
				if (minMax[1] < sample) {
					minMax[1] = sample;
				}
			}
		}
		return minMax;
	}
	
	
	/**
	 * @param rows
	 * @param cols
	 * @return a matrix of the specified shape filled with ones
	 */
	public static FMatrixRMaj ones(int rows, int cols) {
		SimpleMatrix m = SimpleMatrix.wrap(new FMatrixRMaj(rows, cols));
		m.set(1);
		return m.matrix_F32();
	}
	
	
	/**
	 * Generates the covariance matrix of the given data, and a bunch more stuff. 
	 * Only the necessary calculations are performed, so don't worry about performance.
	 * @param data the data from which to generate the cov matrix. Its 
	 * shape is supposed to be nxm, with n being the sample size, and m the number of samples.
	 * @param cov if not null, return the covariance here. Matrix is modified
	 * @param summ if not null, return the summation here. Matrix is modified
	 * @param mean if not null, return the mean here. Matrix is modified
	 */
	public static void generateCovarianceMatrix(FMatrixRMaj data, FMatrixRMaj cov, FMatrixRMaj summ, FMatrixRMaj mean) {
		if (cov == null && summ == null && mean == null) {
			return;
		}
		
		int dim = data.getNumRows();
		int samples = data.getNumCols();
		
		/** Summ is always required */
		if (summ == null) {
			summ = new FMatrixRMaj(dim);
		}	
		summ.reshape(dim, 1);
        CommonOps_FDRM.mult(data, MatrixOperations.ones(samples, 1), summ);
        
        /** Mean is required if mean or cov are required */
        if (mean == null) {
        	if (cov == null) {
        		return;
        	}
        	mean = new FMatrixRMaj(dim);
        }
        mean.set(summ);
        CommonOps_FDRM.divide(mean, (float) data.getNumCols());
        
        /** Calculate cov if asked for */
        if (cov == null) {
        	return;
        }
        cov.reshape(dim, dim);
        CommonOps_FDRM.multTransB(data, data, cov);
        FMatrixRMaj s2 = new FMatrixRMaj(dim, dim);
        CommonOps_FDRM.multTransB(mean, summ, s2);
        CommonOps_FDRM.subtract(cov, s2, cov);
	}

}
