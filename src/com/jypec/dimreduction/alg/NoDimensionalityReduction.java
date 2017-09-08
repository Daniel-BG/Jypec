package com.jypec.dimreduction.alg;

import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImage;
import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * @author Daniel
 * Implementation of {@link DimensionalityReduction} that does nothing, leaving
 * the reduced exactly as the original
 */
public class NoDimensionalityReduction implements DimensionalityReduction{
	
	private int numComponents;

	@Override
	public void train(HyperspectralImage source, int targetDimension) {
		//targetDimension can be negative for all we care, we won't use it
		this.numComponents = source.getNumberOfBands();
	}

	@Override
	public double[][][] reduce(HyperspectralImage src) {
		double [][][] res = new double[src.getNumberOfBands()][src.getNumberOfLines()][src.getNumberOfSamples()];
		for (int i = 0; i < src.getNumberOfBands(); i++) {
			for (int j = 0; j < src.getNumberOfLines(); j++) {
				for (int k = 0; k < src.getNumberOfSamples(); k++) {
					res[i][j][k] = src.getValueAt(i, j, k);
				}
			}
		}
		return res;
	}

	@Override
	public void boost(double[][][] src, HyperspectralImage dst) {
		for (int i = 0; i < dst.getNumberOfBands(); i++) {
			for (int j = 0; j < dst.getNumberOfLines(); j++) {
				for (int k = 0; k < dst.getNumberOfSamples(); k++) {
					dst.setValueAt(src[i][j][k], i, j, k);
				}
			}
		}
	}

	@Override
	public void saveTo(BitStreamDataReaderWriter bw) {
		//do nothing
	}

	@Override
	public void loadFrom(BitStreamDataReaderWriter bw, ComParameters cp) {
		this.numComponents = cp.bands;
	}

	@Override
	public int getNumComponents() {
		return this.numComponents;
	}

	@Override
	public double getMaxValue(HyperspectralImage img) {
		return img.getDataType().getMaxValue();
	}

	@Override
	public double getMinValue(HyperspectralImage img) {
		return img.getDataType().getMinValue();
	}

}
