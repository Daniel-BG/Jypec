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

	private boolean useSVD = true;
	
	
	/**
	 * Build a VCA dimensionality reductor
	 */
	public VertexComponentAnalysis() {
		super(DimensionalityReductionAlgorithm.DRA_VCA);
	}

	@Override
	public void train(DMatrixRMaj source) {
		//this choice should be based on SNR but we do not know what the SNR is,
		//so it is based on parameter selection
		//snrth = 15 + 10log(this.dimProj)
		//if snr > snrth
		dimOrig = source.getNumRows();
		
		int d;
		DMatrixRMaj y;
		DMatrixRMaj ud;
		DMatrixRMaj x;
		DMatrixRMaj rbar = new DMatrixRMaj(dimOrig, 1);
		/** apply SVD onto the input data, and projective project onto the projected mean */
		if (useSVD) {
			d = this.dimProj;
			//do the svd and project onto its space
			this.sayLn("Applying SVD...");
			SingularValueDecomposition svd = new SingularValueDecomposition();
			svd.setNumComponents(dimProj);
			svd.setCenter(false);
			x = svd.trainReduce(source);
			ud = svd.getProjectionMatrix();
			//get the mean of the subspaced data
			this.sayLn("Getting mean...");
			DMatrixRMaj mean = new DMatrixRMaj(dimProj, 1);
			MatrixOperations.generateCovarianceMatrix(x, null, null, mean);
			//projective projection onto the mean vector
			this.sayLn("Projective projection");
			DMatrixRMaj projs = new DMatrixRMaj(x.getNumCols(), 1);
			CommonOps_DDRM.multTransA(x, mean, projs);
			for (int i = 0; i < x.getNumRows(); i++) {
				for (int j = 0; j < x.getNumCols(); j++) {
					double data = x.get(i, j);
					data /= projs.get(j);
					x.set(i, j, data);
				}
			}
			//assign y matrix
			y = new DMatrixRMaj(x);
		/** apply PCA onto the input data, and create a new row for the max norm of the projected vectors */
		} else { //use PCA on a lower dimension space
			d = this.dimProj - 1;
			//apply PCA and project
			this.sayLn("Applying PCA...");
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			pca.setNumComponents(dimProj);
			x = pca.trainReduce(source);
			ud = pca.getProjectionMatrix();
			//get the max norm of the projected vectors
			this.sayLn("Getting max norm");
			double argmaxval = 0;
			int argmax = -1;
			for (int j = 0; j < x.getNumCols(); j++) {
				double norm = 0;
				for (int i = 0; i < x.getNumRows(); i++) {
					double val = x.get(i, j);
					norm += val * val;
				}
				norm = Math.sqrt(norm);
				if (norm > argmaxval) {
					argmaxval = norm;
					argmax = j;
				}
			}
			//add a row to x and set all to argmax
			this.sayLn("Building y matrix");
			y = new DMatrixRMaj(x);
			y.reshape(dimProj, x.getNumRows(), true);
			for (int i = 0; i < x.getNumCols(); i++) {
				y.set(dimProj - 1, i, argmax);
			}
		}
		/** onto the next steps which are common */
		//create some matrices
		DMatrixRMaj a = new DMatrixRMaj(dimProj, dimProj);
		a.set(dimProj - 1, 0, 1);
		DMatrixRMaj id = CommonOps_DDRM.identity(dimProj);
		int[] indices = new int[dimProj];
		
		Random r = new Random(1);
		for (int i = 0; i < dimProj; i++) {
			this.sayLn("Computing vector: " + i + "...");
			//create w
			DMatrixRMaj w = new DMatrixRMaj(dimProj, 1); //TODO generate w = randn(0, Ip)
			for (int j = 0; j < dimProj; j++) {
				w.set(j, r.nextGaussian());
			}
			//create f
			DMatrixRMaj inva = new DMatrixRMaj(dimProj, dimProj);
			CommonOps_DDRM.pinv(a, inva);
			DMatrixRMaj aia = new DMatrixRMaj(dimProj, dimProj);
			CommonOps_DDRM.mult(a, inva, aia);
			DMatrixRMaj tmp = new DMatrixRMaj(dimProj, dimProj);
			CommonOps_DDRM.subtract(id, aia, tmp);
			DMatrixRMaj f = new DMatrixRMaj(dimProj, 1);
			CommonOps_DDRM.mult(tmp, w, f);
			NormOps_DDRM.normalizeF(f);
			//create v
			DMatrixRMaj v = new DMatrixRMaj(1, y.getNumCols());
			CommonOps_DDRM.multTransA(f, y, v);
			//get arg max of v
			int k = -1;
			double kmax = 0;
			for (int j = 0; j < v.getNumElements(); j++) {
				double val = Math.abs(v.get(j));
				if (val > kmax) {
					kmax = val;
					k = j;
				}
			}
			
			//update a matrix
			for (int j = 0; j < a.getNumRows(); j++) {
				a.set(j, i, y.get(j, k));
			}
		}
		
		//extract the x matrix subset
		DMatrixRMaj xSubSet = new DMatrixRMaj(d, dimProj);
		for (int i = 0; i < dimProj; i++) {
			for (int j = 0; j < d; j++) {
				xSubSet.set(j, i, x.get(j, indices[i]));
			}
		}
		
		//calculate M
		DMatrixRMaj m = new DMatrixRMaj(dimOrig, dimProj);
		CommonOps_DDRM.multTransA(ud, xSubSet, m);
		
		//add mean if pca was used
		if (!useSVD) {
			for (int i = 0; i < dimOrig; i++) {
				for (int j = 0; j < dimProj; j++) {
					double val = m.get(i, j);
					val += rbar.get(i);
					m.set(i, j, val);
				}
			}
		}
		
		this.projectionMatrix = m;
		this.unprojectionMatrix = new DMatrixRMaj(projectionMatrix);
		CommonOps_DDRM.transpose(this.projectionMatrix);
		
		this.adjustment = new DMatrixRMaj(dimOrig, 1);
	}

}