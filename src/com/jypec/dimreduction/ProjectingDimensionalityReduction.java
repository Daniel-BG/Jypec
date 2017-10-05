package com.jypec.dimreduction;

import java.io.IOException;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;

import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Parent class for all your projecting dimensionality needs
 * @author Daniel
 */
public abstract class ProjectingDimensionalityReduction extends DimensionalityReduction {

	protected ProjectingDimensionalityReduction(DimensionalityReductionAlgorithm dra) {
		super(dra);
	}
	
	/** Dimension of the original space */
	protected int dimOrig;
	/** Matrix used to go from original to projected space */
	protected FMatrixRMaj projectionMatrix;
	/** Matrix used to go from projected to original space */
	protected FMatrixRMaj unprojectionMatrix;
	/** Substracted before projection, added after unprojection. <br>
	 * This is usually the sample mean that centers in zero. <br>
	 * If null it is not applied */
	protected FMatrixRMaj adjustment;
	
	@Override
	public void doSaveTo(BitOutputStreamTree bw) throws IOException {
    	//write the number of dimensions in the original space
    	bw.addChild("original dimension").writeInt(dimOrig);
    	//write the number of dimensions in the reduced space
    	bw.addChild("projected dimension").writeInt(dimProj);
    	//write the mean and unprojection matrix
		bw.addChild("mean").writeFloatArray(adjustment.getData(), dimOrig);
		bw.addChild("projection matrix").writeFloatArray(unprojectionMatrix.getData(), dimOrig * dimProj);
	}

	@Override
	public void doLoadFrom(BitInputStream bw) throws IOException {
    	//read the number of dimensions in the original space
		dimOrig = bw.readInt();
    	//read the number of dimensions in the reduced space
		dimProj = bw.readInt();
    	//read the mean and unprojection matrix
		float[] dataAdjustment, dataUnprojection;
		dataAdjustment = bw.readFloatArray(dimOrig);
		dataUnprojection = bw.readFloatArray(dimOrig * dimProj);
		//set the matrice's data
    	adjustment = new FMatrixRMaj();
    	adjustment.setData(dataAdjustment);
    	adjustment.reshape(dimOrig, 1, true);
    	unprojectionMatrix = new FMatrixRMaj();
    	unprojectionMatrix.setData(dataUnprojection);
    	unprojectionMatrix.reshape(dimOrig, dimProj, true);
	}
	
	@Override
	public FMatrixRMaj reduce(FMatrixRMaj img) {
		FMatrixRMaj ones = MatrixOperations.ones(1, img.getNumCols());
		FMatrixRMaj sub = new FMatrixRMaj(img.getNumRows(), img.getNumCols());
		CommonOps_FDRM.mult(adjustment, ones, sub);
		CommonOps_FDRM.subtract(img, sub, img);
		FMatrixRMaj res = new FMatrixRMaj(dimProj, img.getNumCols());
		CommonOps_FDRM.mult(projectionMatrix, img, res);
		return res;
	}

	@Override
	public FMatrixRMaj boost(FMatrixRMaj src) {
		FMatrixRMaj res = new FMatrixRMaj(dimOrig, src.getNumCols());
		CommonOps_FDRM.mult(unprojectionMatrix, src, res);
		FMatrixRMaj ones = MatrixOperations.ones(1, res.getNumCols());
		FMatrixRMaj add = new FMatrixRMaj(res.getNumRows(), res.getNumCols());
		CommonOps_FDRM.mult(adjustment, ones, add);
		CommonOps_FDRM.add(res, add, res);
		return res;
	}
	
	/**
	 * @return a copy of this {@link ProjectingDimensionalityReduction} projection matrix.
	 */
	public FMatrixRMaj getProjectionMatrix() {
		return this.projectionMatrix.copy();
	}
	
	/**
	 * @return a copy of this {@link ProjectingDimensionalityReduction} unprojection matrix.
	 */
	public FMatrixRMaj getUnProjectionMatrix() {
		return this.unprojectionMatrix.copy();
	}
	
	/**
	 * @return a copy of this {@link ProjectingDimensionalityReduction} adjustment vector
	 * (which is usually the mean)
	 */
	public FMatrixRMaj getAdjustment() {
		return this.adjustment.copy();
	}
	
	@Override
	protected void doLoadFrom(String[] args) {
		int dimensions = Integer.parseInt(args[0]);
		this.setNumComponents(dimensions);
	}
}
