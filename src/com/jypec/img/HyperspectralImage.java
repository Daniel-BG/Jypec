package com.jypec.img;

/**
 * Class to give a little more functionality to a 3-dimensional matrix representation of a hyperspectral image
 * @author Daniel
 *
 */
public class HyperspectralImage {

	private int[][][] data;
	private ImageDataTypes dataType;
	private int depth;
	private int bands;
	private int lines;
	private int samples;
	
	
	/**
	 * Create a hyperspectral image with the given data, or an empty image if data is null
	 * @param data: the data that comprises the image, or null if it shall be initialized to zero
	 * @param depth: the bitdepth of the samples that comprise the image
	 * @param bands: number of bands in the image (spectral dimension)
	 * @param lines: number of lines in a band (height of the spatial dimension (vertical number of samples))
	 * @param samples: number of samples in a line (width of the spatial dimension (horizontal number of samples))
	 */
	public HyperspectralImage (int[][][] data, ImageDataTypes dataType, int depth, int bands, int lines, int samples) {
		if (data == null) {
			data = new int[bands][lines][samples];
		} else {
			this.data = data;
		}
		this.dataType = dataType;
		this.depth = depth;
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
	public double getValueAt(int band, int line, int sample) {
		return this.dataType.dataToValue(this.getDataAt(band, line, sample));
	}
	
	/**
	 * Set the given value at the given position
	 * @param value: value to be set
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
	 * @param line
	 * @param sample
	 */
	public void setValueAt(double value, int band, int line, int sample) {
		this.data[band][line][sample] = this.dataType.valueToData(value);
	}
	
	/**
	 * return a band of this image
	 * @param band: the returned band's index
	 * @param referenceOriginal: if true, the returned band references the original and thus any changes to it
	 * will affet it
	 * @return the requested band
	 */
	public HyperspectralBand getBand(int band) {
		return new HyperspectralBand(this, band, this.depth, this.lines, this.samples);
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




	
}
