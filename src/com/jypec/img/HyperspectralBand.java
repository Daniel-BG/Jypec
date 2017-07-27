package com.jypec.img;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.util.data.IntegerMatrix;

/**
 * Class that stores one hyperspectral band and can be used for extracting blocks for coding and such
 * @author Daniel
 *
 */
public class HyperspectralBand implements IntegerMatrix {

	private HyperspectralImage hyimg;
	private int band;
	private int depth;
	private int lines;
	private int samples;
	
	
	public HyperspectralBand (HyperspectralImage hi, int band, int depth, int lines, int samples) {
		this.hyimg = hi;
		this.band = band;
		this.depth = depth;
		this.lines = lines;
		this.samples = samples;
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
	private double getValueAt(int line, int sample) {
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
	 * @return
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

	@Override
	public int[][] extractInnerMatrix() {
		return this.hyimg.getDataReferenceToBand(this.band);
	}




}
