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

	@Override
	public void train(DMatrixRMaj source) {
		//no training needed. If unset just preserve dimension
		if (dimProj == -1)
			this.dimProj = source.getNumRows();
	}

	@Override
	public DMatrixRMaj reduce(DMatrixRMaj src) {
		DMatrixRMaj res = new DMatrixRMaj(this.dimProj, src.getNumCols());
		
		for (int i = 0; i < this.dimProj; i++) {
			for (int j = 0; j < src.getNumCols(); j++) {
				res.set(i, j, src.get(i, j));
			}
			
		}
		return res;
	}

	@Override
	public DMatrixRMaj boost(DMatrixRMaj src) {
		DMatrixRMaj dst = new DMatrixRMaj(this.dimProj, src.getNumCols());
		for (int i = 0; i < this.dimProj; i++) {
			for (int j = 0; j < src.getNumCols(); j++) {
				dst.set(i, j, src.get(i, j));
			}
		}
		return dst;
	}

	@Override
	public void doLoadFrom(BitInputStream bw) throws IOException {
		this.dimProj = bw.readInt();
	}

	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
		bw.writeInt(this.dimProj);
	}

	@Override
	protected void doLoadFrom(String[] args) {
		if (args.length > 0) {
			this.dimProj = Integer.parseInt(args[0]);
		}
	}

}
