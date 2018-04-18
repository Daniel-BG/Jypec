package com.jypec.dimreduction.alg;

import java.io.IOException;

import org.ejml.data.FMatrixRMaj;

import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

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
	public FMatrixRMaj preprocess(FMatrixRMaj source) {
		return source;
	}

	@Override
	public boolean doTrain(FMatrixRMaj source) {
		//no training needed. If unset just preserve dimension
		if (dimProj == -1)
			this.dimProj = source.getNumRows();
		
		return true;
	}

	@Override
	public FMatrixRMaj reduce(FMatrixRMaj src) {
		FMatrixRMaj res = new FMatrixRMaj(this.dimProj, src.getNumCols());
		
		for (int i = 0; i < this.dimProj; i++) {
			for (int j = 0; j < src.getNumCols(); j++) {
				res.set(i, j, src.get(i, j));
			}
			
		}
		return res;
	}

	@Override
	public FMatrixRMaj boost(FMatrixRMaj src) {
		FMatrixRMaj dst = new FMatrixRMaj(this.dimProj, src.getNumCols());
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
	public void doSaveTo(BitOutputStreamTree bw) throws IOException {
		bw.writeInt(this.dimProj);
	}

	@Override
	public DimensionalityReduction doLoadFrom(String[] args) {
		if (args.length > 0) {
			this.dimProj = Integer.parseInt(args[0]);
		}
		return this;
	}

}
