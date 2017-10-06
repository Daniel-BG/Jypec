package com.jypec.util.arrays;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.simple.SimpleMatrix;

/**
 * Useful operations not native to EJML
 * @author Daniel
 *
 */
public class EJMLExtensions {
	
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
        CommonOps_FDRM.mult(data, EJMLExtensions.ones(samples, 1), summ);
        
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

	/**
	 * Does the inverse square root of all the elements in the diagonal. Use only with diagonal matrices
	 * @param source
	 */
	public static void inverseSquareRoot(FMatrixRMaj source) {
		if (source.getNumCols() != source.getNumRows()) {
			throw new IllegalArgumentException("Only works on square matrices");
		}
		
		for (int i = 0; i < source.getNumCols(); i++) {
			float val = source.get(i, i);
			if (Math.abs(val) < 0.1e-30) {
				source.set(i, i, 0);
			} else  {
				source.set(i, i, (float) (1.0 / Math.sqrt(val)));
			}
		}
		
	}
	
	
	/**
	 * Gets a subset of the given matrix
	 * @param source from where to take samples
	 * @param probability probability that one sample from source will make it to the 
	 * returned dataset. e.g: if probability = 0.1 -> 1 out of 10 samples will be output
	 * @return the subset of the input, which could be empty if <code>probability</code> 
	 * is too low
	 */
	public static FMatrixRMaj getSubSet(FMatrixRMaj source, double probability) {
		if (probability < 0 || probability > 1) {
			throw new IllegalArgumentException("Probability must be between 0 and 1");
		}
		if (probability == 1) {
			return source;
		}
		int samples = (int) (source.getNumCols() * probability);
		FMatrixRMaj result = new FMatrixRMaj(source.getNumRows(), samples);
		for (int i = 0; i < samples; i++) {
			double which = ((double) i) * source.getNumCols() / (double) samples;
			int index = (int) Math.round(which); //should be between 0 and source.numcols - 1
			for (int j = 0; j < source.getNumRows(); j++) {
				result.set(j, i, source.get(j, index));
			}
		}
		
		return result;
	}
	
	/**
	 * Substract the given <code>vector</code> from every column of the given <code>matrix</code>
	 * @param matrix
	 * @param vector
	 */
	public static void addColumnVector(FMatrixRMaj matrix, FMatrixRMaj vector) {
		if (vector.numCols != 1) {
			throw new IllegalArgumentException("the vector must have one column");
		}
		if (vector.numRows != matrix.numRows) {
			throw new IllegalArgumentException("the vector and matrix must have the same number of rows");
		}
		
		for (int i = 0; i < matrix.numRows; i++) {
			for (int j = 0; j < matrix.numCols; j++) {
				matrix.data[i*matrix.numCols + j] += vector.data[i];
			}
		}
	}
	
	
	/**
	 * Substract the given <code>vector</code> from every column of the given <code>matrix</code>
	 * @param matrix
	 * @param vector
	 */
	public static void subColumnVector(FMatrixRMaj matrix, FMatrixRMaj vector) {
		if (vector.numCols != 1) {
			throw new IllegalArgumentException("the vector must have one column");
		}
		if (vector.numRows != matrix.numRows) {
			throw new IllegalArgumentException("the vector and matrix must have the same number of rows");
		}
		
		for (int i = 0; i < matrix.numRows; i++) {
			for (int j = 0; j < matrix.numCols; j++) {
				matrix.data[i*matrix.numCols + j] -= vector.data[i];
			}
		}
	}
	
}
