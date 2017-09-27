package com.jypec.dimreduction;

import java.io.IOException;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import com.jypec.comdec.ComParameters;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageHeaderData;
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
	protected int dimProj;
	/** Dimension of the original space */
	protected int dimOrig;
	/** Matrix used to go from original to projected space */
	protected DMatrixRMaj projectionMatrix;
	/** Matrix used to go from projected to original space */
	protected DMatrixRMaj unprojectionMatrix;
	/** Substracted before projection, added after unprojection. <br>
	 * This is usually the sample mean that centers in zero. <br>
	 * If null it is not applied */
	protected double adjustment[];
	
	
	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
    	//write the number of dimensions in the original space
    	bw.writeInt(dimOrig);
    	//write the number of dimensions in the reduced space
    	bw.writeInt(dimProj);
    	//write the mean
    	bw.writeDoubleArray(adjustment, dimOrig);
    	//write the matrix
    	bw.writeDoubleArray(unprojectionMatrix.getData(), dimOrig * dimProj);
	}

	@Override
	public void doLoadFrom(BitInputStream bw, ComParameters cp, ImageHeaderData ihd) throws IOException {
    	//read the number of dimensions in the original space
		dimOrig = bw.readInt();
    	//read the number of dimensions in the reduced space
		dimProj = bw.readInt();
    	//read the mean
    	adjustment = bw.readDoubleArray(dimOrig);
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
        if( dimProj > dimOrig )
            throw new IllegalArgumentException("More components requested that the data's length.");
		this.dimProj = dimProj;
	}
	
	@Override
	public DMatrixRMaj reduce(HyperspectralImageData source) {
		DMatrixRMaj img = source.toDoubleMatrix();
		for (int i = 0; i < img.getNumRows(); i++) {
			for (int j = 0; j < img.getNumCols(); j++) {
				img.minus(img.getIndex(i, j), adjustment[i]);
			}
		}
		DMatrixRMaj res = new DMatrixRMaj(dimProj, img.getNumCols());
		CommonOps_DDRM.mult(projectionMatrix, img, res);
		return res;
	}

	@Override
	public void boost(DMatrixRMaj src, HyperspectralImageData dst) {
		this.sayLn("Boosting samples from reduced space to the original...");
		DMatrixRMaj res = new DMatrixRMaj(dimOrig, src.getNumCols());
		CommonOps_DDRM.mult(unprojectionMatrix, src, res);
		for (int i = 0; i < dimOrig; i++) {
			for (int j = 0; j < res.getNumCols(); j++) {
				res.plus(res.getIndex(i, j), adjustment[i]);
			}
		}
		dst.copyDataFrom(res);
	}
	
}
