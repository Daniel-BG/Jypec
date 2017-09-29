package com.jypec.dimreduction;

import java.io.IOException;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import com.jypec.util.arrays.ArrayTransforms;
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
	/** Quality of saved matrix */
	public enum Precision {
		/** 64 bit double precision*/
		DOUBLE, 
		/** 32 bit float precision */
		FLOAT
	};
	private Precision precision = Precision.FLOAT;
	
	
	
	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
    	//write the number of dimensions in the original space
    	bw.writeInt(dimOrig);
    	//write the number of dimensions in the reduced space
    	bw.writeInt(dimProj);
    	//write the precision used
    	bw.writeEnum(Precision.class, precision, true);
    	//write the mean and unprojection matrix
    	if (this.precision == Precision.DOUBLE) {
    		bw.writeDoubleArray(adjustment.getData(), dimOrig);
    		bw.writeDoubleArray(unprojectionMatrix.getData(), dimOrig * dimProj);
    	} else {
    		bw.writeFloatArray(ArrayTransforms.changeType(adjustment.getData()), dimOrig);
    		bw.writeFloatArray(ArrayTransforms.changeType(unprojectionMatrix.getData()), dimOrig * dimProj);
    	}
	}

	@Override
	public void doLoadFrom(BitInputStream bw) throws IOException {
    	//read the number of dimensions in the original space
		dimOrig = bw.readInt();
    	//read the number of dimensions in the reduced space
		dimProj = bw.readInt();
		//read the precision used
		this.precision = Precision.class.cast(bw.readEnum(Precision.class, true));
    	//read the mean and unprojection matrix
		double[] dataAdjustment, dataUnprojection;
		if (this.precision == Precision.DOUBLE) {
			dataAdjustment = bw.readDoubleArray(dimOrig);
			dataUnprojection = bw.readDoubleArray(dimOrig * dimProj);
		} else {
			dataAdjustment = ArrayTransforms.changeType(bw.readFloatArray(dimOrig));
			dataUnprojection = ArrayTransforms.changeType(bw.readFloatArray(dimOrig * dimProj));
		}
		//set the matrice's data
    	adjustment = new DMatrixRMaj();
    	adjustment.setData(dataAdjustment);
    	adjustment.reshape(dimOrig, 1, true);
    	unprojectionMatrix = new DMatrixRMaj();
    	unprojectionMatrix.setData(dataUnprojection);
    	unprojectionMatrix.reshape(dimOrig, dimProj, true);
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
	public DMatrixRMaj boost(DMatrixRMaj src) {
		this.sayLn("Boosting samples from reduced space to the original...");
		DMatrixRMaj res = new DMatrixRMaj(dimOrig, src.getNumCols());
		CommonOps_DDRM.mult(unprojectionMatrix, src, res);
		DMatrixRMaj ones = MatrixOperations.ones(1, res.getNumCols());
		DMatrixRMaj add = new DMatrixRMaj(res.getNumRows(), res.getNumCols());
		CommonOps_DDRM.mult(adjustment, ones, add);
		CommonOps_DDRM.add(res, add, res);
		return res;
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

	/**
	 * Set the precision for the values that are SAVED. Internal operations are done
	 * with max precision even if this is set to lower values
	 * @param precision
	 */
	public void setPrecision(Precision precision) {
		this.precision = precision;
	}
	
	@Override
	protected void doLoadFrom(String[] args) {
		int dimensions = Integer.parseInt(args[0]);
		this.setNumComponents(dimensions);
	}
}
