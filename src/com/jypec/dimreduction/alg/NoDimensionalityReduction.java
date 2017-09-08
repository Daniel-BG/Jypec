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
	public void reduce(HyperspectralImage source, HyperspectralImage dst) {
		dst.copyDataFrom(source);
	}

	@Override
	public void boost(HyperspectralImage source, HyperspectralImage dst) {
		dst.copyDataFrom(source);
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
