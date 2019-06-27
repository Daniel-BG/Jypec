package com.jypec.img;

import org.ejml.data.FMatrixRMaj;

/**
 * Parent class for hyperspectral image data.
 * Goves a little more functionality to a representation of a hyperspectral image.
 * Subclass to store the data in different formats, which might suit some
 * needs better than others.
 * @author Daniel
 */
public abstract class HyperspectralImageData {

	protected int bands;
	protected int lines;
	protected int samples;
	protected int bandElements;
	protected ImageDataType dataType;
	protected int depth;
	
	/**
	 * Create hyperspectral image data
	 * @param type 
	 * @param bands
	 * @param lines
	 * @param samples
	 */
	public HyperspectralImageData(ImageDataType type, int bands, int lines, int samples) {
		this.bands = bands;
		this.lines = lines;
		this.samples = samples;
		this.bandElements = this.lines * this.samples;
		this.dataType = type;
		this.depth = dataType.getBitDepth();
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
	 * @return the data type of this image
	 */
	public ImageDataType getDataType() {
		return this.dataType;
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
	 * @return the size in bits of this data. Note that this might not be the number of
	 * bits it occupies in memory
	 */
	public long getBitSize() {
		return (long) bands * (long) lines * (long) samples * (long) depth;
	}

	/**
	 * @return the total number of samples within the image, that is:<br>
	 * bands * lines * samples
	 */
	public int getTotalNumberOfSamples() {
		return bands * lines * samples;
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
	 * @param band
	 * @param line
	 * @param sample
	 * @return the data at the specified position, no questions asked
	 */
	public abstract int getDataAt(int band, int line, int sample);
	
	
	/**
	 * @param band
	 * @param line
	 * @param sample
	 * @return the value that the inner data represents at that position
	 */
	public abstract int getValueAt(int band, int line, int sample);
	
	/**
	 * Set the given value at the given position
	 * @param value value to be set
	 * @param band
	 * @param line
	 * @param sample
	 */
	public abstract void setDataAt(int value, int band, int line, int sample);
	
	/**
	 * Sets the value given in the given position, restricting it to this image's range
	 * and coding it in this image's data type
	 * @param value the new value to set
	 * @param band 
	 * @param line
	 * @param sample
	 */
	public abstract void setValueAt(float value, int band, int line, int sample);
	
	/**
	 * @param line
	 * @param sample
	 * @return a pixel of the image at the given spatial position, with all spectral components
	 */
	public abstract float[] getPixel(int line, int sample);
	
	/**
	 * Set a whole sample (pixel) for this image
	 * @param values
	 * @param line
	 * @param sample
	 */
	public abstract void setPixel(float[] values, int line, int sample);
	
	/**
	 * @param source where to copy data from
	 */
	public abstract void copyDataFrom(FMatrixRMaj source);
	
	/**
	 * Generate a NEW {@link HyperspectralImageData} object with the specific new size.
	 * If it is smaller than the current size then the original is cropped, otherwise it 
	 * is filled with zeroes
	 * @param bands
	 * @param lines
	 * @param samples
	 * @return
	 */
	public abstract HyperspectralImageData resize(int bands, int lines, int samples);
	
	/**
	 * @return the hyperspectral image data as a float matrix for better numerical processing.
	 * Note that, depending on implementation, modifications to the matrix returned might 
	 * reflect on this object
	 */
	public abstract FMatrixRMaj tofloatMatrix();
	
	/**
	 * WARNING: this deletes the internal data to free up memory. Be use you know what you are doing
	 * before using this method
	 */
	public abstract void free();
}
