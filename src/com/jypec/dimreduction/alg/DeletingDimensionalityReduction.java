package com.jypec.dimreduction.alg;

import java.io.IOException;

import org.ejml.data.DMatrixRMaj;

import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * @author Daniel
 * Implementation of {@link DimensionalityReduction} that just removes dimensions
 * if required, leaving the set number of components untouched
 */
public class DeletingDimensionalityReduction extends DimensionalityReduction {
	
	/** Default constructor */
	public DeletingDimensionalityReduction() {
		super(DimensionalityReductionAlgorithm.DRA_DELETING_DIMENSIONALITY_REDUCTION);
	}

	private int numComponents = -1;

	@Override
	public void train(DMatrixRMaj source) {
		//no training needed. If unset just preserve dimension
		if (numComponents == -1)
			this.numComponents = source.getNumRows();
	}

	@Override
	public DMatrixRMaj reduce(DMatrixRMaj src) {
		DMatrixRMaj res = new DMatrixRMaj(this.numComponents, src.getNumCols());
		
		for (int i = 0; i < this.numComponents; i++) {
			for (int j = 0; j < src.getNumCols(); j++) {
				res.set(i, j, src.get(i, j));
			}
			
		}
		return res;
	}

	@Override
	public void boost(DMatrixRMaj src, DMatrixRMaj dst) {
		for (int i = 0; i < this.numComponents; i++) {
			for (int j = 0; j < dst.getNumCols(); j++) {
				dst.set(i, j, src.get(i, j));
			}
		}
	}

	@Override
	public void doLoadFrom(BitInputStream bw) throws IOException {
		this.numComponents = bw.readInt();
	}

	@Override
	public int getNumComponents() {
		return this.numComponents;
	}

	@Override
	public void setNumComponents(int numComponents) {
		this.numComponents = numComponents;
	}

	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
		bw.writeInt(this.numComponents);
	}

}
