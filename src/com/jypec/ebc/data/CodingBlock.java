package com.jypec.ebc.data;

import com.jypec.ebc.SubBand;

/**
 * Class for the storage and manipulation of coding blocks that are coded
 * by the MQ-coder
 * @author Daniel
 *
 */
public class CodingBlock {
	
	private int[][] data;
	private int rows, columns;
	private int rowOffset = 0, columnOffset = 0;
	private int magnitudeBitPlanes;
	private SubBand band;
	
	/**
	 * Construct a coding block from the given data. It is assumed that 
	 * data is a non-null rectangular matrix. Otherwise behaviour
	 * is undefined.
	 * @param data: the data for this block
	 * @param depth: number of bitplanes in the data. (SIGN PLANE INCLUDED) 
	 * assuming the least significant bit plane is stored int the LSB
	 * @param band: the subBand this block belongs to
	 * @see SubBand
	 * @note MSB: most significant bit LSB: least significant bit
	 */
	public CodingBlock(int[][] data, int height, int width, int rowOffset, int columnOffset, int depth, SubBand band) {
		this.setUp(data, height, width, rowOffset, columnOffset, depth, band);
	}

	/**
	 * Creates an empty codeblock to be filled when decoding
	 * @param height
	 * @param width
	 * @param magnitudeBitPlanes
	 * @param band
	 */
	public CodingBlock(int height, int width, int depth, SubBand band) {
		this.setUp(new int[height][width], height, width, 0, 0, depth, band);
	}
	
	
	private void setUp(int[][] data, int height, int width, int rowOffset, int columnOffset, int depth, SubBand band) {
		//check validity of arguments
		if (depth < 2 || depth > 32) {
			throw new IllegalArgumentException("Number of bitplanes must be between 1 and 31 (both inclusive)");
		}
		
		//assign internal variables
		this.data = data;
		this.rows = height;
		this.columns = width;
		this.rowOffset = rowOffset;
		this.columnOffset = columnOffset;
		this.magnitudeBitPlanes = depth - 1;
		this.band = band;
	}

	/**
	 * @return the width, or number of columns, in this coding block
	 */
	public int getWidth() {
		return this.columns;
	}
	
	/**
	 * @return the height, or number of rows, in this coding block
	 */
	public int getHeight() {
		return this.rows;
	}

	/**
	 * @return the number of bitplanes that this block has (excluding the sign bitplane)
	 */
	public int getMagnitudeBitPlaneNumber() {
		return this.magnitudeBitPlanes;
	}

	/**
	 * Gets the ith bitPlane from within this codeBlock
	 * @param i
	 * @return
	 */
	public CodingPlane getBitPlane(int i) {
		if (i < 0 || i >= this.magnitudeBitPlanes) {
			throw new IllegalArgumentException("Requested plane does not exist. Available: [0," + (this.magnitudeBitPlanes - 1) + "]");
		}
		return new CodingPlane(this, i);
	}
	
	/**
	 * @return the mask to be used with the internal data to extract the sign bit
	 */
	public int getSignMask() {
		return 0x1 << this.magnitudeBitPlanes;
	}
	
	/**
	 * @param row
	 * @param column
	 * @return the data at the specified position
	 */
	public int getDataAt(int row, int column) {
		return this.data[row + rowOffset][column + columnOffset];
	}
	
	/**
	 * Sets the value given at the given position, overwriting existing data!
	 * @param value
	 * @param row
	 * @param column
	 */
	public void setDataAt(int value, int row, int column) {
		this.data[row + rowOffset][column + columnOffset] = value;
	}

	/**
	 * @return the subBand this block belongs to
	 */
	public SubBand getSubBand() {
		return this.band;
	}
	
}
