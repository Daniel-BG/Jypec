package com.jypec.img;

import org.ejml.data.FMatrixRMaj;

import com.jypec.util.arrays.MatrixTransforms;

/**
 * Class to give a little more functionality to a 3-dimensional matrix representation of a hyperspectral image
 * @author Daniel
 *
 */
public class HyperspectralImageData {

	private int[][][] data;
	private ImageDataType dataType;
	private int depth;
	private int bands;
	private int lines;
	private int samples;
	
	
	/**
	 * Create a hyperspectral image with the given data, or an empty image if data is null
	 * @param data the data that comprises the image, or null if it shall be initialized to zero
	 * @param dataType the type of data that forms the image
	 * @param bands number of bands in the image (spectral dimension)
	 * @param lines number of lines in a band (height of the spatial dimension (vertical number of samples))
	 * @param samples number of samples in a line (width of the spatial dimension (horizontal number of samples))
	 */
	public HyperspectralImageData (int[][][] data, ImageDataType dataType, int bands, int lines, int samples) {
		if (data == null) {
			this.data = new int[bands][lines][samples];
		} else {
			this.data = data;
		}
		this.dataType = dataType;
		this.depth = dataType.getBitDepth();
		this.bands = bands;
		this.lines = lines;
		this.samples = samples;
	}
	
	/**
	 * @param band
	 * @param line
	 * @param sample
	 * @return the data at the specified position, no questions asked
	 */
	public int getDataAt(int band, int line, int sample) {
		return this.data[band][line][sample];
	}
	
	/**
	 * @param band
	 * @param line
	 * @param sample
	 * @return the value that the inner data represents at that position
	 */
	public int getValueAt(int band, int line, int sample) {
		return this.dataType.dataToValue(this.getDataAt(band, line, sample));
	}
	
	/**
	 * Set the given value at the given position
	 * @param value value to be set
	 * @param band
	 * @param line
	 * @param sample
	 */
	public void setDataAt(int value, int band, int line, int sample) {
		this.data[band][line][sample] = value; 
	}
	
	/**
	 * Sets the value given in the given position, restricting it to this image's range
	 * and coding it in this image's data type
	 * @param value the new value to set
	 * @param band 
	 * @param line
	 * @param sample
	 */
	public void setValueAt(float value, int band, int line, int sample) {
		this.data[band][line][sample] = this.dataType.valueToData(value);
	}
	
	/**
	 * return a band of this image. Modifications to this band will affect the original data, since it is referenced, not copied
	 * @param band the returned band's index
	 * @return the requested band
	 */
	public HyperspectralBandData getBand(int band) {
		return new HyperspectralBandData(this, band, this.depth, this.lines, this.samples);
	}
	
	/**
	 * @return the number of bands in this image
	 */
	public int getNumberOfBands() {
		return this.bands;
	}
	
	/**
	 * @return the number of lines in each band of the image
	 */
	public int getNumberOfLines() {
		return this.lines;
	}
	
	/**
	 * @return the number of samples in each line of the image
	 */
	public int getNumberOfSamples() {
		return this.samples;
	}

	/**
	 * @param band
	 * @return the pointer to the raw data of the given band
	 */
	protected int[][] getDataReferenceToBand(int band) {
		return this.data[band];
	}

	/**
	 * @param line
	 * @param sample
	 * @return a pixel of the image at the given spatial position, with all spectral components
	 */
	public float[] getPixel(int line, int sample) {
		float[] res = new float[this.getNumberOfBands()];
		for (int i = 0; i < this.getNumberOfBands(); i++) {
			res[i] = this.getValueAt(i, line, sample);
		}
		return res;
	}

	/**
	 * @return the data type of this image
	 */
	public ImageDataType getDataType() {
		return this.dataType;
	}

	/**
	 * Set a whole sample (pixel) for this image
	 * @param values
	 * @param line
	 * @param sample
	 */
	public void setPixel(float[] values, int line, int sample) {
		for (int i = 0; i < this.getNumberOfBands(); i++) {
			this.setValueAt(values[i], i, line, sample);
		}
	}
	
	/**
	 * @param other
	 * @return true if this image's size and type is equal to other's size and type
	 */
	public boolean sizeAndTypeEquals(HyperspectralImageData other) {
		return this.getNumberOfBands() == other.getNumberOfBands() 
				&& this.getNumberOfLines() == other.getNumberOfLines() 
				&& this.getNumberOfSamples() == other.getNumberOfSamples()
				&& this.getDataType().equals(other.getDataType());
	}
	
	/**
	 * @param source where to copy data from
	 */
	public void copyDataFrom(HyperspectralImageData source) {
		if (!this.sizeAndTypeEquals(source)) {
			throw new IllegalArgumentException("The image to copy from must have the same type and dimensions as this one");
		}
		
		MatrixTransforms.copy(source.data, this.data, this.bands, this.lines, this.samples);
	}
	
	/**
	 * @param source where to copy data from
	 */
	public void copyDataFrom(FMatrixRMaj source) {
		for (int i = 0; i < this.getNumberOfBands(); i++) {
			for (int j = 0; j < this.getNumberOfLines(); j++) {
				for (int k = 0; k < this.getNumberOfSamples(); k++) {
					this.setValueAt(source.get(i, j*this.getNumberOfSamples() + k), i, j, k);
				}
			}
		}
	}

	/**
	 * @return the size in bits of this data
	 */
	public int getBitSize() {
		return bands * lines * samples * depth;
	}

	/**
	 * @return the total number of samples within the image, that is:<br>
	 * bands * lines * samples
	 */
	public int getTotalNumberOfSamples() {
		return bands * lines * samples;
	}
	
	/**
	 * @return the hyperspectral image data as a float matrix for better numerical processing
	 */
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


}
