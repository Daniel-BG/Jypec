package com.jypec.dimreduction.alg;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.ArraySortingIndexComparator;
import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.debug.Logger;

/**
 * Overrides the {@link #train(DMatrixRMaj)} method in {@link PrincipalComponentAnalysis} 
 * to use singular value decomposition instead of generating the covariance matrix.
 * This method is slower (probably because of the SVD implementation) but in some cases
 * has better numerical convergence
 * @author Daniel
 *
 */
public class SingularValueDecomposition extends ProjectingDimensionalityReduction {

	boolean center;
	
	/**
	 * Construct a singular value decomposition reduction
	 */
	public SingularValueDecomposition() {
		super(DimensionalityReductionAlgorithm.DRA_PCASVD);
	}

	@Override
	public void train(DMatrixRMaj data) {
		Logger.getLogger().log("Taking samples...");
		dimOrig = data.getNumRows();
		
		/** substract mean from data and normalize with the square root of the number of samples */
		DMatrixRMaj newData = new DMatrixRMaj(data);
		adjustment = new DMatrixRMaj(dimOrig, 1);
		if (center) {
			MatrixOperations.generateCovarianceMatrix(data, null, null, adjustment);
			for (int i = 0; i < data.getNumRows(); i++) {
				for (int j = 0; j < data.getNumRows(); j++) {
					double val = newData.get(i, j) - adjustment.get(i);
					val /= data.getNumCols(); //normalize
					newData.set(i, j, val);
				}
			}
		}
		
		
		/** apply SVD and get the V matrix. We do not compute W
		 * (and probably can't since it is of size samples * samples) */
		CommonOps_DDRM.transpose(newData);
		SingularValueDecomposition_F64<DMatrixRMaj> svd = DecompositionFactory_DDRM.svd(newData.getNumRows(), newData.getNumCols(), false, true, false);
		Logger.getLogger().log("Decomposition yielded: " + svd.decompose(newData));
		DMatrixRMaj V = svd.getV(null, false);
		DMatrixRMaj W = svd.getW(null);
		
		/** extract projection and unprojection matrices */
		projectionMatrix = new DMatrixRMaj(V);
		CommonOps_DDRM.transpose(projectionMatrix);
		descendingOrder(W, projectionMatrix);
		projectionMatrix.reshape(dimProj, dimOrig);
		unprojectionMatrix = new DMatrixRMaj(projectionMatrix);
		CommonOps_DDRM.transpose(unprojectionMatrix);
	}

	
	@Override
	protected void doLoadFrom(String[] args) {
		int dimensions = Integer.parseInt(args[0]);
		this.setNumComponents(dimensions);
		this.setCenter(Boolean.parseBoolean(args[1]));
	}
	
	
	/**
	 * @param center true if the data is to be centered
	 */
	public void setCenter(boolean center) {
		this.center = center;
	}
	
	
	private static void descendingOrder(DMatrixRMaj W, DMatrixRMaj V) {
		Double[] diag = new Double[W.getNumCols()];
		for (int i = 0; i < W.getNumCols(); i++) {
			diag[i] = W.get(i, i);
		}
		Integer[] sortingArray = ArraySortingIndexComparator.createSortingArray(diag, true);
	
		for (int i = 0; i < sortingArray.length; i++) {
			if (sortingArray[i] != i) {
				//swap indices
				int iidx, nidx;
				iidx = sortingArray[i];
				nidx = sortingArray[iidx];
				sortingArray[i] = nidx;
				sortingArray[iidx] = iidx;
				
				//swap rows
				for (int j = 0; j < V.getNumCols(); j++) {
					double viidx = V.get(iidx, j);
					double vnidx = V.get(nidx, j);
					V.set(iidx, j, vnidx);
					V.set(nidx, j, viidx);
				}
				//back index up to retry on changed row O(2*n)
				i--;
			}
		}	
	}
	
}
