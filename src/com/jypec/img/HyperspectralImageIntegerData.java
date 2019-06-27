package com.jypec.img;

import org.ejml.data.FMatrixRMaj;

/**
 * Integer implementation of {@link HyperspectralImageData}, stores the information
 * in raw data form, and converts to value when necessary
 * @author Daniel
 */
public class HyperspectralImageIntegerData extends HyperspectralImageData {

	private int[] data;
	
	/**
	 * Create a hyperspectral image with the given data, or an empty image if data is null
	 * @param data the data that comprises the image, or null if it shall be initialized to zero
	 * @param dataType the type of data that forms the image
	 * @param bands number of bands in the image (spectral dimension)
	 * @param lines number of lines in a band (height of the spatial dimension (vertical number of samples))
	 * @param samples number of samples in a line (width of the spatial dimension (horizontal number of samples))
	 */
	public HyperspectralImageIntegerData (ImageDataType dataType, int bands, int lines, int samples) {
		super(dataType, bands, lines, samples);
		this.data = new int[bands*lines*samples];
	}
	
	@Override
	public int getDataAt(int band, int line, int sample) {
		return this.data[band*bandElements + line*samples + sample];
	}
	
	@Override
	public int getValueAt(int band, int line, int sample) {
		return this.dataType.dataToValue(this.getDataAt(band, line, sample));
	}
	
	@Override
	public void setDataAt(int value, int band, int line, int sample) {
		this.data[band*bandElements + line*samples + sample] = value; 
	}
	
	@Override
	public void setValueAt(float value, int band, int line, int sample) {
		this.data[band*bandElements + line*samples + sample] = this.dataType.valueToData(value);
	}
	
	@Override
	public float[] getPixel(int line, int sample) {
		float[] res = new float[this.getNumberOfBands()];
		for (int i = 0; i < this.getNumberOfBands(); i++) {
			res[i] = this.getValueAt(i, line, sample);
		}
		return res;
	}

	@Override
	public void setPixel(float[] values, int line, int sample) {
		for (int i = 0; i < this.getNumberOfBands(); i++) {
			this.setValueAt(values[i], i, line, sample);
		}
	}
	
	@Override
	public void copyDataFrom(FMatrixRMaj source) {
		for (int i = 0; i < this.getNumberOfBands(); i++) {
			for (int j = 0; j < this.getNumberOfLines(); j++) {
				for (int k = 0; k < this.getNumberOfSamples(); k++) {
					this.setValueAt(source.get(i, j*this.getNumberOfSamples() + k), i, j, k);
				}
			}
		}
	}

	@Override
	public FMatrixRMaj tofloatMatrix() {
		FMatrixRMaj res = new FMatrixRMaj(this.bands, this.lines * this.samples);
		for (int i = 0; i < bands; i++) {
			for (int j = 0; j < lines; j++) {
				for (int k = 0; k < samples; k++) {
					res.set(i, j*samples + k, this.getValueAt(i, j, k));
				}
			}
		}
		return res;
	}

	@Override
	public void free() {
		this.data = null;
	}

	@Override
	public HyperspectralImageData resize(int bands, int lines, int samples) {
		HyperspectralImageData newImage = new HyperspectralImageIntegerData(
				new ImageDataType(this.getDataType().getBitDepth(), this.getDataType().isSigned()), 
				bands, lines, samples);
		
		int cBands = this.getNumberOfBands();
		int cLines = this.getNumberOfLines();
		int cSamples = this.getNumberOfSamples();
		
		for (int i = 0; i < bands; i++) {
			for (int j = 0; j < lines; j++) {
				for (int k = 0; k < samples; k++) {
					if (i < cBands && j < cLines && k < cSamples) {
						newImage.setDataAt(this.getDataAt(i, j, k), i, j, k);
					} else {
						newImage.setValueAt(0, i, j, k);
					}
				}
			}
		}

		return newImage;
	}


}
