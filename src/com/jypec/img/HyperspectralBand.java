package com.jypec.img;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;

public class HyperspectralBand {

	private int[][] data;
	private int depth;
	private int lines;
	private int samples;
	
	
	public HyperspectralBand (int[][] data, int depth, int lines, int samples) {
		if (data == null) {
			this.data = new int[lines][samples];
		} else {
			this.data = data;
		}
		this.depth = depth;
		this.lines = lines;
		this.samples = samples;
	}
	
	/**
	 * @param line
	 * @param sample
	 * @return the data at the given position
	 */
	public int getDataAt(int line, int sample) {
		return this.data[line][sample];
	}
	
	/**
	 * Sets the given data at the given position
	 * @param data
	 * @param line
	 * @param sample
	 */
	public void setDataAt(int data, int line, int sample) {
		this.data[line][sample] = data;
	}
	
	/**
	 * @return the number of lines this band has
	 */
	public int getNumberOfLines() {
		return this.lines;
	}
	
	/**
	 * @return the number of samples this band has
	 */
	public int getNumberOfSamples() {
		return this.samples;
	}
	
	/**
	 * Extract a block that references the internal data (that is, can MODIFY it, be careful!)
	 * @param rowOffset
	 * @param colOffset
	 * @param height
	 * @param width
	 * @param band
	 * @return
	 */
	public CodingBlock extractBlock(int rowOffset, int colOffset, int height, int width, SubBand band) {
		if (rowOffset < 0 || colOffset < 0 || height < 0 || width < 0) {
			throw new IllegalArgumentException("Arguments cannot be negative");
		}
		if (rowOffset + height >= this.lines || colOffset + width >= this.samples) {
			throw new IllegalArgumentException("The block you are trying to create would have samples out of bounds");
		}
		return new CodingBlock(this.data, height, width, rowOffset, colOffset, this.depth, band);
	}
}
