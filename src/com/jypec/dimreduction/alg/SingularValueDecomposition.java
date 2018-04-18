package com.jypec.dimreduction.alg;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;

import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.ArraySortingIndexComparator;
import com.jypec.util.arrays.EJMLExtensions;
import com.jypec.util.debug.Logger;

/**
 * Overrides the {@link #train(FMatrixRMaj)} method in {@link PrincipalComponentAnalysis} 
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
	public boolean doTrain(FMatrixRMaj data) {
		Logger.getLogger().log("Taking samples...");
		dimOrig = data.getNumRows();
		
		/** substract mean from data and normalize with the square root of the number of samples */
		FMatrixRMaj newData = new FMatrixRMaj(data);
		adjustment = new FMatrixRMaj(dimOrig, 1);
		if (center) {
			Logger.getLogger().log("Centering data...");
			EJMLExtensions.generateCovarianceMatrix(data, null, null, adjustment);
			for (int i = 0; i < data.getNumRows(); i++) {
				for (int j = 0; j < data.getNumRows(); j++) {
					float val = newData.get(i, j) - adjustment.get(i);
					val /= data.getNumCols(); //normalize
					newData.set(i, j, val);
				}
			}
		}
		
		
		/** apply SVD and get the V matrix. We do not compute W
		 * (and probably can't since it is of size samples * samples) */
		Logger.getLogger().log("Applying SVD...");
		CommonOps_FDRM.transpose(newData);
		SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(newData.getNumRows(), newData.getNumCols(), false, true, false);
		boolean result = svd.decompose(newData);
		if (!result) {
			Logger.getLogger().log("Could not decompose");
			return false;
		}
		
		FMatrixRMaj V = svd.getV(null, false);
		FMatrixRMaj W = svd.getW(null);
		
		/** extract projection and unprojection matrices */
		Logger.getLogger().log("Extracting projection matrix...");
		projectionMatrix = new FMatrixRMaj(V);
		CommonOps_FDRM.transpose(projectionMatrix);
		descendingOrder(W, projectionMatrix);
		projectionMatrix.reshape(dimProj, dimOrig);
		unprojectionMatrix = new FMatrixRMaj(projectionMatrix);
		CommonOps_FDRM.transpose(unprojectionMatrix);
		
		return true;
	}

	
	@Override
	public DimensionalityReduction doLoadFrom(String[] args) {
		int dimensions = Integer.parseInt(args[0]);
		this.setNumComponents(dimensions);
		this.setCenter(Boolean.parseBoolean(args[1]));
		return this;
	}
	
	
	/**
	 * @param center true if the data is to be centered
	 */
	public void setCenter(boolean center) {
		this.center = center;
	}
	
	
	private static void descendingOrder(FMatrixRMaj W, FMatrixRMaj V) {
		Float[] diag = new Float[W.getNumCols()];
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
					float viidx = V.get(iidx, j);
					float vnidx = V.get(nidx, j);
					V.set(iidx, j, vnidx);
					V.set(nidx, j, viidx);
				}
				//back index up to retry on changed row O(2*n)
				i--;
			}
		}	
	}
	
}
