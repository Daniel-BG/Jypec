package com.jypec.dimreduction.alg;

import java.util.Random;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.MatrixOperations;

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
	public void train(DMatrixRMaj source) {
		/** get metadata */
		dimOrig = source.getNumRows();

		/** apply SVD onto the input data, and projective project onto the projected mean */
		//do the svd and project onto its space
		this.sayLn("Applying SVD...");
		SingularValueDecomposition svd = new SingularValueDecomposition();
		svd.setParentVerboseable(this);
		svd.setNumComponents(dimProj);
		svd.setCenter(false);
		DMatrixRMaj x = svd.trainReduce(source);	//get projected values
		DMatrixRMaj ud = svd.getProjectionMatrix(); //get projection matrix
		
		/** Projective project onto the mean of the subspace */
		//y(:, j) = x(:, j)/(x(:, j)^tu)
		this.sayLn("Getting mean...");
		DMatrixRMaj u = new DMatrixRMaj(dimProj, 1);
		MatrixOperations.generateCovarianceMatrix(x, null, null, u);
		
		this.sayLn("Projective projection onto mean...");
		DMatrixRMaj y = new DMatrixRMaj(x);			//y is the result of projecting onto the mean
		DMatrixRMaj projs = new DMatrixRMaj(x.getNumCols(), 1);
		CommonOps_DDRM.multTransA(x, u, projs);
		for (int i = 0; i < y.getNumRows(); i++) {
			for (int j = 0; j < y.getNumCols(); j++) {
				double data = y.get(i, j);
				data /= projs.get(j);
				y.set(i, j, data);
			}
		}
			
		/** initialize variables for endmember extraction */
		DMatrixRMaj a = new DMatrixRMaj(dimProj, dimProj);	//a is used for finding extreme points
		a.set(dimProj - 1, 0, 1);
		DMatrixRMaj id = CommonOps_DDRM.identity(dimProj);
		int[] indices = new int[dimProj];					//indices of endmembers
		Random r = new Random(seed);						//depending on the seed gets slightly different results
		
		/** compute each endmember */
		for (int i = 0; i < dimProj; i++) {
			this.sayLn("Computing vector: " + i + "...");
			//create w = randn(0, Ip) (see http://ftp//ftp.dca.fee.unicamp.br/pub/docs/vonzuben/ia013_2s09/material_de_apoio/gen_rand_multivar.pdf for why this works)
			DMatrixRMaj w = new DMatrixRMaj(dimProj, 1); 
			for (int j = 0; j < dimProj; j++) {
				w.set(j, r.nextGaussian());
			}
			//create f (orthonormal to subspace spanned by a) 
			//f = ((I - AA#)w)/(||(I - AA#)w||)
			DMatrixRMaj inva = new DMatrixRMaj(dimProj, dimProj);
			CommonOps_DDRM.pinv(a, inva);
			DMatrixRMaj aia = new DMatrixRMaj(dimProj, dimProj);
			CommonOps_DDRM.mult(a, inva, aia);
			DMatrixRMaj tmp = new DMatrixRMaj(dimProj, dimProj);
			CommonOps_DDRM.subtract(id, aia, tmp);
			DMatrixRMaj f = new DMatrixRMaj(dimProj, 1);
			CommonOps_DDRM.mult(tmp, w, f);
			NormOps_DDRM.normalizeF(f);
			//create v: projection of y onto f
			//v = f^ty
			DMatrixRMaj v = new DMatrixRMaj(1, y.getNumCols());
			CommonOps_DDRM.multTransA(f, y, v);
			//get the max value of those projections. this is the endmember
			//k = argmax(|v[j]|) for all j=0...N
			int k = -1;
			double kmax = 0;
			for (int j = 0; j < v.getNumElements(); j++) {
				double val = Math.abs(v.get(j));
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
		DMatrixRMaj xSubSet = new DMatrixRMaj(dimProj, dimProj);
		for (int i = 0; i < dimProj; i++) {
			for (int j = 0; j < dimProj; j++) {
				xSubSet.set(j, i, x.get(j, indices[i]));
			}
		}
		
		/** get mixing matrix, which is the reverse projection matrix */
		DMatrixRMaj m = new DMatrixRMaj(dimOrig, dimProj);
		CommonOps_DDRM.multTransA(ud, xSubSet, m);
		this.unprojectionMatrix = m;
		
		/** the projection matrix is the pseudoinverse */
		this.projectionMatrix = new DMatrixRMaj(unprojectionMatrix);
		CommonOps_DDRM.transpose(this.projectionMatrix);
		CommonOps_DDRM.pinv(this.unprojectionMatrix, this.projectionMatrix);
		
		/** adjustment is zero */
		this.adjustment = new DMatrixRMaj(dimOrig, 1);
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