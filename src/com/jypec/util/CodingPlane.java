package com.jypec.util;

import com.jypec.mq.SubBand;

/**
 * Class that stores a single coding plane from a coding block
 * @see CodingBlock
 * 
 * @author Daniel
 */
public class CodingPlane {
	
	private static final int SIGN_MASK = 0x80000000;
	
	private int[][] data;
	private int rows, columns;
	private SubBand band;
	private int mask;
	private boolean[][] codingStatus;
	
	/**
	 * Build a coding plane from the planeOffset-th bit of the given data
	 * @param data: it is assumed that this is the data on the encompassing code block,
	 * 		and thus the sign bit is stored in the MSB of the data
	 * @param planeOffset
	 * @param band: which band this plane belongs to (needed for coding)
	 */
	public CodingPlane(int[][] data, int planeOffset, SubBand band) {
		assert(planeOffset >= 0 && planeOffset <= 30);
		
		this.data = data;
		this.rows = this.data.length;
		this.columns = this.data[0].length;
		this.band = band;
		this.mask = 0x1 << planeOffset;
		
		this.codingStatus = new boolean[this.rows][this.columns]; //defaulted to false
	}
	
	
	/**
	 * Get the number of 4-high strips within this plane
	 * @return
	 */
	public int getFullStripsNumber() {
		return this.rows / 4;
	}

	/**
	 * Get the width of this plane
	 * @return
	 */
	public int getWidth() {
		return this.columns;
	}

	/**
	 * Get the number of dangling rows at the end that don't make a full strip (0-3)
	 * @return
	 */
	public int getLastStripHeight() {
		return this.rows % 4;
	}

	/**
	 * Get the SubBand this plane belongs to
	 * @return
	 */
	public SubBand getSubBand() {
		return this.band;
	}

	/**
	 * @param column
	 * @param row
	 * @return 	The symbol at the given position
	 */
	public Bit getSymbolAt(int row, int column) {
		return Bit.fromInteger(this.data[row][column] & this.mask);
	}

	/**
	 * @return the offset at which the last dangling rows start 
	 * 		(note that there could be zero rows)
	 */
	public int getLastStripOffset() {
		return this.getFullStripsNumber() * 4;
	}

	/**
	 * @param column
	 * @param row
	 * @return true if the bit at the given position corresponds to a negative sample
	 * 		of the encompassing codeblock
	 */
	public boolean isNegativeAt(int row, int column) {
		return (this.data[row][column] & CodingPlane.SIGN_MASK) != 0;
	}

	/**
	 * Marks the bit at the given position as already coded
	 * @param column
	 * @param row
	 */
	public void setCoded(int row, int column) {
		this.codingStatus[row][column] = true;
	}

	/**
	 * @param i
	 * @param j
	 * @return true if the bit at the given position has already been set as coded
	 * @see CodingPlane.setCoded
	 */
	public boolean isCoded(int row, int column) {
		return this.codingStatus[row][column];
	}

}
