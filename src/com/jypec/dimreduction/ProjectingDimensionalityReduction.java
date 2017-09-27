package com.jypec.dimreduction;

import java.io.IOException;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Parent class for all your projecting dimensionality needs
 * @author Daniel
 */
public abstract class ProjectingDimensionalityReduction extends DimensionalityReduction {

	protected ProjectingDimensionalityReduction(DimensionalityReductionAlgorithm dra) {
		super(dra);
	}
	
	/** Dimension of the projected space */
	protected int dimProj = -1;
	/** Dimension of the original space */
	protected int dimOrig;
	/** Matrix used to go from original to projected space */
	protected DMatrixRMaj projectionMatrix;
	/** Matrix used to go from projected to original space */
	protected DMatrixRMaj unprojectionMatrix;
	/** Substracted before projection, added after unprojection. <br>
	 * This is usually the sample mean that centers in zero. <br>
	 * If null it is not applied */
	protected DMatrixRMaj adjustment;
	
	
	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
    	//write the number of dimensions in the original space
    	bw.writeInt(dimOrig);
    	//write the number of dimensions in the reduced space
    	bw.writeInt(dimProj);
    	//write the mean
    	bw.writeDoubleArray(adjustment.getData(), dimOrig);
    	//write the matrix
    	bw.writeDoubleArray(unprojectionMatrix.getData(), dimOrig * dimProj);
	}

	@Override
	public void doLoadFrom(BitInputStream bw) throws IOException {
    	//read the number of dimensions in the original space
		dimOrig = bw.readInt();
    	//read the number of dimensions in the reduced space
		dimProj = bw.readInt();
    	//read the mean
		double[] data = bw.readDoubleArray(dimOrig);
    	adjustment = new DMatrixRMaj(dimOrig, 1);
    	adjustment.setData(data);
    	//read the projection matrix
    	unprojectionMatrix = new DMatrixRMaj();
    	unprojectionMatrix.setData(bw.readDoubleArray(dimOrig * dimProj));
    	unprojectionMatrix.reshape(dimOrig, dimProj, true);
	}

	@Override
	public int getNumComponents() {
		return dimProj;
	}

	@Override
	public void setNumComponents(int dimProj) {
		this.dimProj = dimProj;
	}
	
	@Override
	public DMatrixRMaj reduce(DMatrixRMaj img) {
		DMatrixRMaj ones = MatrixOperations.ones(1, img.getNumCols());
		DMatrixRMaj sub = new DMatrixRMaj(img.getNumRows(), img.getNumCols());
		CommonOps_DDRM.mult(adjustment, ones, sub);
		CommonOps_DDRM.subtract(img, sub, img);
		DMatrixRMaj res = new DMatrixRMaj(dimProj, img.getNumCols());
		CommonOps_DDRM.mult(projectionMatrix, img, res);
		return res;
	}

	@Override
	public void boost(DMatrixRMaj src, DMatrixRMaj dst) {
		this.sayLn("Boosting samples from reduced space to the original...");
		DMatrixRMaj res = new DMatrixRMaj(dimOrig, src.getNumCols());
		CommonOps_DDRM.mult(unprojectionMatrix, src, res);
		DMatrixRMaj ones = MatrixOperations.ones(1, res.getNumCols());
		DMatrixRMaj add = new DMatrixRMaj(res.getNumRows(), res.getNumCols());
		CommonOps_DDRM.mult(adjustment, ones, add);
		CommonOps_DDRM.add(res, add, res);
		dst.set(res);
	}
	
	/**
	 * @return a copy of this {@link ProjectingDimensionalityReduction} projection matrix.
	 */
	public DMatrixRMaj getProjectionMatrix() {
		return this.projectionMatrix.copy();
	}
	
	/**
	 * @return a copy of this {@link ProjectingDimensionalityReduction} unprojection matrix.
	 */
	public DMatrixRMaj getUnProjectionMatrix() {
		return this.unprojectionMatrix.copy();
	}
	
}
