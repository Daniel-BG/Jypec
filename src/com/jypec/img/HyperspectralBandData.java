package com.jypec.img;


import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.util.datastructures.IntegerMatrix;
import com.jypec.util.debug.Logger;

/**
 * Class that stores one hyperspectral band and can be used for extracting blocks for coding and such
 * @author Daniel
 *
 */
public class HyperspectralBandData implements IntegerMatrix {


	private HyperspectralImageData hyimg;
	private int band;
	private int depth;
	private int lines;
	private int samples;
	
	
	/**
	 * Builds a hyperspectral band from the given hyperspectral image
	 * @param hi
	 * @param band
	 * @param depth
	 * @param lines 
	 * @param samples
	 */
	public HyperspectralBandData (HyperspectralImageData hi, int band, int depth, int lines, int samples) {
		this.hyimg = hi;
		this.band = band;
		this.depth = depth;
		this.lines = lines;
		this.samples = samples;
	}
	
	/**
	 * Builds a hyperspectral band with no image attached (one will be created underneath with 1 band)
	 * @param type 
	 * @param lines 
	 * @param samples
	 * @return a band with no image attached
	 */
	public static HyperspectralBandData generateRogueBand (ImageDataType type, int lines, int samples) {
		return new HyperspectralImageIntegerData(type, 1, lines, samples).getBand(0); //be integer since it is for coding
	}
	
	@Override
	public int getDataAt(int line, int sample) {
		return this.hyimg.getDataAt(this.band, line, sample);
	}
	
	/**
	 * @param line
	 * @param sample
	 * @return the VALUE of the DATA at the given position. This could be different from {@link #getDataAt(int, int)}
	 */
	public int getValueAt(int line, int sample) {
		return this.hyimg.getValueAt(this.band, line, sample);
	}
	
	@Override
	public void setDataAt(int data, int line, int sample) {
		this.hyimg.setDataAt(data, this.band, line, sample);
	}
	
	/**
	 * @return the number of lines this band has
	 */
	public int getNumberOfLines() {
		return this.lines;
	}
	@Override
	public int getRows() {
		return this.lines;
	}
	
	/**
	 * @return the number of samples this band has
	 */
	public int getNumberOfSamples() {
		return this.samples;
	}
	@Override
	public int getColumns() {
		return this.samples;
	}
	

	
	/**
	 * Extract a block that references the internal data (that is, can MODIFY it, be careful!)
	 * @param rowOffset
	 * @param colOffset
	 * @param height
	 * @param width
	 * @param band
	 * @return a Coding block Wrapper for the data in the requested position
	 */
	public CodingBlock extractBlock(int rowOffset, int colOffset, int height, int width, SubBand band) {
		if (rowOffset < 0 || colOffset < 0 || height < 0 || width < 0) {
			throw new IllegalArgumentException("Arguments cannot be negative");
		}
		if (rowOffset + height > this.lines || colOffset + width > this.samples) {
			throw new IllegalArgumentException("The block you are trying to create would have samples out of bounds");
		}
			
		return new CodingBlock(this, height, width, rowOffset, colOffset, this.depth, band);
	}
	
	/**
	 * @return the type of this band's data
	 */
	public ImageDataType getDataType() {
		return this.hyimg.getDataType();
	}

	/**
	 * @param other
	 * @return true if this image's size and type is equal to other's size and type
	 */
	public boolean sizeAndTypeEquals(HyperspectralBandData other) {
		return this.getNumberOfLines() == other.getNumberOfLines() 
				&& this.getNumberOfSamples() == other.getNumberOfSamples()
				&& this.getDataType().equals(other.getDataType());
	}

	/**
	 * @return the total number of data samples within the band
	 */
	public float getTotalNumberOfSamples() {
		return this.getNumberOfLines() * this.getNumberOfSamples();
	}
	
	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HyperspectralBandData) {
			HyperspectralBandData other = (HyperspectralBandData) obj;
			if (other.getNumberOfLines() != this.getNumberOfLines() || other.getNumberOfSamples() != this.getNumberOfSamples()) {
				return false;
			}
			for (int i = 0; i < this.getNumberOfLines(); i++) {
				for (int j = 0; j < this.getNumberOfSamples(); j++) {
					if (this.getValueAt(i, j) != other.getValueAt(i, j)) {
						Logger.getLogger().log("Not match @ (" + i + "," + j + "): " + this.getValueAt(i, j) + "<->" + other.getValueAt(i, j));
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return the image this band belongs to
	 */
	public HyperspectralImageData getImage() {
		return this.hyimg;
	}
}
