package com.jypec.dimreduction.alg;

import java.util.Random;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.NormOps_FDRM;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.debug.Logger;

/**
 * Implementation of the Vertex Component Analysis 
 * described <a href="http://www.lx.it.pt/~bioucas/files/ieeegrsVca04.pdf">here</a>
 * @author Daniel
 *
 */
public class VertexComponentAnalysis extends ProjectingDimensionalityReduction {
	
	private long seed = 2;
	
	/**
	 * Build a VCA dimensionality reductor
	 */
	public VertexComponentAnalysis() {
		super(DimensionalityReductionAlgorithm.DRA_VCA);
	}

	@Override
	public void doTrain(FMatrixRMaj source) {
		/** get metadata */
		dimOrig = source.getNumRows();

		/** apply SVD onto the input data, and projective project onto the projected mean */
		//do the svd and project onto its space
		Logger.getLogger().log("Applying SVD...");
		SingularValueDecomposition svd = new SingularValueDecomposition();
		svd.setNumComponents(dimProj);
		svd.setCenter(false);
		FMatrixRMaj x = svd.trainReduce(source);	//get projected values
		FMatrixRMaj ud = svd.getProjectionMatrix(); //get projection matrix
		
		/** Projective project onto the mean of the subspace */
		//y(:, j) = x(:, j)/(x(:, j)^tu)
		Logger.getLogger().log("Getting mean...");
		FMatrixRMaj u = new FMatrixRMaj(dimProj, 1);
		MatrixOperations.generateCovarianceMatrix(x, null, null, u);
		
		Logger.getLogger().log("Projective projection onto mean");
		FMatrixRMaj y = new FMatrixRMaj(x);			//y is the result of projecting onto the mean
		FMatrixRMaj projs = new FMatrixRMaj(x.getNumCols(), 1);
		CommonOps_FDRM.multTransA(x, u, projs);
		for (int i = 0; i < y.getNumRows(); i++) {
			for (int j = 0; j < y.getNumCols(); j++) {
				float data = y.get(i, j);
				data /= projs.get(j);
				y.set(i, j, data);
			}
		}
			
		/** initialize variables for endmember extraction */
		FMatrixRMaj a = new FMatrixRMaj(dimProj, dimProj);	//a is used for finding extreme points
		a.set(dimProj - 1, 0, 1);
		FMatrixRMaj id = CommonOps_FDRM.identity(dimProj);
		int[] indices = new int[dimProj];					//indices of endmembers
		Random r = new Random(seed);						//depending on the seed gets slightly different results
		
		/** compute each endmember */
		for (int i = 0; i < dimProj; i++) {
			Logger.getLogger().log("Computing vector: " + i + "...");
			//create w = randn(0, Ip) (see http://ftp//ftp.dca.fee.unicamp.br/pub/docs/vonzuben/ia013_2s09/material_de_apoio/gen_rand_multivar.pdf for why this works)
			FMatrixRMaj w = new FMatrixRMaj(dimProj, 1); 
			for (int j = 0; j < dimProj; j++) {
				w.set(j, (float) r.nextGaussian());
			}
			//create f (orthonormal to subspace spanned by a) 
			//f = ((I - AA#)w)/(||(I - AA#)w||)
			FMatrixRMaj inva = new FMatrixRMaj(dimProj, dimProj);
			CommonOps_FDRM.pinv(a, inva);
			FMatrixRMaj aia = new FMatrixRMaj(dimProj, dimProj);
			CommonOps_FDRM.mult(a, inva, aia);
			FMatrixRMaj tmp = new FMatrixRMaj(dimProj, dimProj);
			CommonOps_FDRM.subtract(id, aia, tmp);
			FMatrixRMaj f = new FMatrixRMaj(dimProj, 1);
			CommonOps_FDRM.mult(tmp, w, f);
			NormOps_FDRM.normalizeF(f);
			//create v: projection of y onto f
			//v = f^ty
			FMatrixRMaj v = new FMatrixRMaj(1, y.getNumCols());
			CommonOps_FDRM.multTransA(f, y, v);
			//get the max value of those projections. this is the endmember
			//k = argmax(|v[j]|) for all j=0...N
			int k = -1;
			float kmax = 0;
			for (int j = 0; j < v.getNumElements(); j++) {
				float val = Math.abs(v.get(j));
				if (val > kmax) {
					kmax = val;
					k = j;
				}
			}
			indices[i] = k;
			
			//update a matrix with the newly found endmember from the projective projection
			//a(:, i) = y(:, k)
			for (int j = 0; j < a.getNumRows(); j++) {
				a.set(j, i, y.get(j, k));
			}
		}
		
		/** extract endmembers */
		Logger.getLogger().log("Extracting endmembers...");
		FMatrixRMaj xSubSet = new FMatrixRMaj(dimProj, dimProj);
		for (int i = 0; i < dimProj; i++) {
			for (int j = 0; j < dimProj; j++) {
				xSubSet.set(j, i, x.get(j, indices[i]));
			}
		}
		
		/** get mixing matrix, which is the reverse projection matrix */
		Logger.getLogger().log("Computing mixing / unmixing matrices...");
		FMatrixRMaj m = new FMatrixRMaj(dimOrig, dimProj);
		CommonOps_FDRM.multTransA(ud, xSubSet, m);
		this.unprojectionMatrix = m;
		
		/** the projection matrix is the pseudoinverse */
		this.projectionMatrix = new FMatrixRMaj(unprojectionMatrix);
		CommonOps_FDRM.transpose(this.projectionMatrix);
		CommonOps_FDRM.pinv(this.unprojectionMatrix, this.projectionMatrix);
		
		/** adjustment is zero */
		this.adjustment = new FMatrixRMaj(dimOrig, 1);
	}
	
	
	@Override
	protected void doLoadFrom(String[] args) {
		int dimensions = Integer.parseInt(args[0]);
		this.setNumComponents(dimensions);
		if (args.length > 1) { // set seed
			this.seed = Long.parseLong(args[1]);
		}
	}

}