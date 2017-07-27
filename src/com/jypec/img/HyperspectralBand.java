package com.jypec.img;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;

/**
 * Class that stores one hyperspectral band and can be used for extracting blocks for coding and such
 * @author Daniel
 *
 */
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
	 * @return the data at the given position (in binary form inside an integer)
	 */
	public int getDataAt(int line, int sample) {
		return this.data[line][sample];
	}
	
	/**
	 * @param line
	 * @param sample
	 * @return the VALUE of the DATA at the given position. This could be different from {@link #getDataAt(int, int)}
	 */
	private double getValueAt(int line, int sample) {
		return (double) this.getDataAt(line, sample);
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
	public CodingBlock extractBlock(int rowOffset, int colOffset, int height, int width, SubBand band, boolean referenceOriginal) {
		if (rowOffset < 0 || colOffset < 0 || height < 0 || width < 0) {
			throw new IllegalArgumentException("Arguments cannot be negative");
		}
		if (rowOffset + height > this.lines || colOffset + width > this.samples) {
			throw new IllegalArgumentException("The block you are trying to create would have samples out of bounds");
		}
		if (referenceOriginal) {
			return new CodingBlock(this.data, height, width, rowOffset, colOffset, this.depth, band);
		} else {
			int[][] newData = new int[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					newData[i][j] = this.getDataAt(i + rowOffset, j + colOffset);
				}
			}
			return new CodingBlock(newData, height, width, 0, 0, this.depth, band);
		}
	}
	
	/**
	 * @return the internal information in double precision format for wave analysis
	 */
	public double[][] toWave() {
		double[][] wave = new double[this.lines][this.samples];
		
		for (int i = 0; i < this.lines; i++) {
			for (int j = 0; j  < this.lines; j++) {
				wave[i][j] = this.getValueAt(i, j);
			}
		}
		
		return wave;
	}

	/**
	 * @return a pointer to the internal data. Useful for changing it
	 * TODO maybe remove this to make it a lil bit cleaner
	 */
	public int[][] getDataReference() {
		return this.data;
	}


}
