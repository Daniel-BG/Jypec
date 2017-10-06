package com.jypec.img;

import org.ejml.data.FMatrixRMaj;

/**
 * Float implementation of {@link HyperspectralImageData}, which internally
 * stores its contents in a {@link FMatrixRMaj}. Useful to avoid the memory
 * overhead of {@link #tofloatMatrix()}, since it returns the internal
 * pointer instead of creating a new matrix as {@link HyperspectralImageIntegerData} 
 * does
 * @author Daniel
 */
public class HyperspectralImageFloatData extends HyperspectralImageData {

	private FMatrixRMaj data;
	
	/**
	 * Create a {@link HyperspectralImageData} with floating point inner representation
	 * @param type
	 * @param bands
	 * @param lines
	 * @param samples
	 */
	public HyperspectralImageFloatData(ImageDataType type, int bands, int lines, int samples) {
		super(type, bands, lines, samples);
		this.data = new FMatrixRMaj(bands, this.bandElements);
	}

	@Override
	public int getDataAt(int band, int line, int sample) {
		return this.dataType.valueToData(data.get(band*this.bandElements + line*this.samples + sample));
	}

	@Override
	public int getValueAt(int band, int line, int sample) {
		return (int) data.get(band*this.bandElements + line*this.samples + sample);
	}

	@Override
	public void setDataAt(int value, int band, int line, int sample) {
		data.set(band*this.bandElements + line*this.samples + sample, this.dataType.dataToValue(value));
	}

	@Override
	public void setValueAt(float value, int band, int line, int sample) {
		data.set(band*this.bandElements + line*this.samples + sample, value);
	}

	@Override
	public float[] getPixel(int line, int sample) {
		float[] pixel = new float[this.bands];
		for (int i = 0; i < this.bands; i++) {
			pixel[i] = this.getValueAt(i, line, sample);
		}
		return pixel;
	}

	@Override
	public void setPixel(float[] pixel, int line, int sample) {
		for (int i = 0; i < this.bands; i++) {
			this.setValueAt(pixel[i], i, line, sample);
		}
	}

	@Override
	/**
	 * Will just update the inner pointer to the given matrix
	 */
	public void copyDataFrom(FMatrixRMaj source) {
		if (this.data.numCols != source.numCols || this.data.numRows != source.numRows) {
			throw new IllegalArgumentException("Dimensions do not match");
		}
		this.data = source;
	}

	@Override
	/**
	 * Will just return the inner matrix
	 */
	public FMatrixRMaj tofloatMatrix() {
		return this.data;
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub
		
	}

}
