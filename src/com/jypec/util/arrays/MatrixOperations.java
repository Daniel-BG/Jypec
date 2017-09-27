package com.jypec.util.arrays;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
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
	public static double[] minMax(double[][] source) {
		double[] minMax = new double[2];
		int rows = source.length;
		int cols = source[0].length;
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int j = 0; j < rows; j++) {
			for (int k = 0; k < cols; k++) {
				double sample = source[j][k];
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
	public static DMatrixRMaj ones(int rows, int cols) {
		SimpleMatrix m = SimpleMatrix.wrap(new DMatrixRMaj(rows, cols));
		m.set(1);
		return m.matrix_F64();
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
	public static void generateCovarianceMatrix(DMatrixRMaj data, DMatrixRMaj cov, DMatrixRMaj summ, DMatrixRMaj mean) {
		if (cov == null && summ == null && mean == null) {
			return;
		}
		
		int dim = data.getNumRows();
		int samples = data.getNumCols();
		
		/** Summ is always required */
		if (summ == null) {
			summ = new DMatrixRMaj(dim);
		}	
		summ.reshape(dim, 1);
        CommonOps_DDRM.mult(data, MatrixOperations.ones(samples, 1), summ);
        
        /** Mean is required if mean or cov are required */
        if (mean == null) {
        	if (cov == null) {
        		return;
        	}
        	mean = new DMatrixRMaj(dim);
        }
        mean.set(summ);
        CommonOps_DDRM.divide(mean, (double) data.getNumCols());
        
        /** Calculate cov if asked for */
        if (cov == null) {
        	return;
        }
        cov.reshape(dim, dim);
        CommonOps_DDRM.multTransB(data, data, cov);
        DMatrixRMaj s2 = new DMatrixRMaj(dim, dim);
        CommonOps_DDRM.multTransB(mean, summ, s2);
        CommonOps_DDRM.subtract(cov, s2, cov);
	}

}
