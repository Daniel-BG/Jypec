package com.jypec.dimreduction.alg;

import org.ejml.data.DMatrixRMaj;

import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HeaderConstants;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageHeaderData;
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
	public void train(HyperspectralImageData source) {
		//no training needed. If unset just preserve dimension
		if (numComponents == -1)
			this.numComponents = source.getNumberOfBands();
	}

	@Override
	public DMatrixRMaj reduce(HyperspectralImageData src) {
		DMatrixRMaj res = new DMatrixRMaj(this.numComponents, src.getNumberOfLines()*src.getNumberOfSamples());
		
		for (int i = 0; i < this.numComponents; i++) {
			for (int j = 0; j < src.getNumberOfLines(); j++) {
				for (int k = 0; k < src.getNumberOfSamples(); k++) {
					res.set(i, j*src.getNumberOfSamples() + k, src.getValueAt(i, j, k));
				}
			}
		}
		return res;
	}

	@Override
	public void boost(DMatrixRMaj src, HyperspectralImageData dst) {
		for (int i = 0; i < this.numComponents; i++) {
			for (int j = 0; j < dst.getNumberOfLines(); j++) {
				for (int k = 0; k < dst.getNumberOfSamples(); k++) {
					dst.setValueAt(src.get(i, j*dst.getNumberOfSamples() + k), i, j, k);
				}
			}
		}
	}

	@Override
	public void doLoadFrom(BitInputStream bw, ComParameters cp, ImageHeaderData ihd) {
		this.numComponents = (int) ihd.get(HeaderConstants.HEADER_BANDS);
	}

	@Override
	public int getNumComponents() {
		return this.numComponents;
	}

	@Override
	public double getMaxValue(HyperspectralImageData img) {
		return img.getDataType().getMaxValue();
	}

	@Override
	public double getMinValue(HyperspectralImageData img) {
		return img.getDataType().getMinValue();
	}

	@Override
	public void setNumComponents(int numComponents) {
		this.numComponents = numComponents;
	}

	@Override
	public void doSaveTo(BitOutputStream bw) {
		//nothing to do, saved already in the header metadata
	}

}
