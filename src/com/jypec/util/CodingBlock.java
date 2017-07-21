package com.jypec.util;

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
	private int bitPlanes;
	private SubBand band;
	
	/**
	 * Construct a coding block from the given data. It is assumed that 
	 * data is a non-null rectangular matrix. Otherwise behaviour
	 * is undefined.
	 * The sign plane is taken from the MSB, and thus the number of bitplanes
	 * is limited to 31
	 * @param data: the data for this block
	 * @param bitPlanes: number of bitplanes in the data. assuming the 
	 * 		least significant bit plane is stored int the LSB
	 * @param band: the subBand this block belongs to
	 * @see SubBand
	 * @note MSB: most significant bit LSB: least significant bit
	 */
	public CodingBlock(int[][] data, int bitPlanes,SubBand band) {
		//check validity of arguments
		assert (bitPlanes > 0 && bitPlanes <= 31);
		
		//assign internal variables
		this.data = data;
		this.rows = this.data.length;
		this.columns = this.data[0].length;
		this.bitPlanes = bitPlanes;
		this.band = band;
	}

	/**
	 * Creates an empty codeblock to be filled when decoding
	 * @param height
	 * @param width
	 * @param bitPlanes
	 * @param band
	 */
	public CodingBlock(int height, int width, int bitPlanes, SubBand band) {
		this.data = new int[height][width];
		this.rows = height;
		this.columns = width;
		this.bitPlanes = bitPlanes;
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
	public int getBitPlaneNumber() {
		return this.bitPlanes;
	}

	/**
	 * Gets the ith bitPlane from within this codeBlock
	 * @param i
	 * @return
	 */
	public CodingPlane getBitPlane(int i) {
		assert (i < this.bitPlanes);
		return new CodingPlane(data, i, this.band, 0x2 << this.bitPlanes);
	}
	
	/**
	 * @return the data matrix inside this coding block. Note that if this data is modified, 
	 * the block is modified too
	 */
	public int[][] getData() {
		return this.data;
	}
}
