package com.jypec.util;

import com.jypec.ebc.SubBand;

/**
 * Class that stores a single coding plane from a coding block
 * @see CodingBlock
 * 
 * @author Daniel
 */
public class CodingPlane {
	private int[][] data;
	private int rows, columns;
	private SubBand band;
	private int bitMask;
	private int signMask;
	private boolean[][] codingStatus;
	
	/**
	 * Build a coding plane from the planeOffset-th bit of the given data
	 * @param data: it is assumed that this is the data on the encompassing code block,
	 * 		and thus the sign bit is stored in the MSB of the data
	 * @param planeOffset: offset (from the LSB) that this plane refers to
	 * @param band: which band this plane belongs to (needed for coding)
	 */
	public CodingPlane(int[][] data, int width, int height, int planeOffset, SubBand band, int signMask) {
		assert(planeOffset >= 0 && planeOffset <= 30);
		
		this.data = data;
		this.rows = height;
		this.columns = width;
		this.band = band;
		this.bitMask = 0x1 << planeOffset;
		this.signMask = signMask;
		
		this.codingStatus = new boolean[this.rows][this.columns]; //defaulted to false
	}
	
	
	/**
	 * Get the number of 4-high strips within this plane
	 * @return
	 */
	public int getFullStripsNumber() {
		return this.rows >> 2;
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
		return Bit.fromInteger(this.data[row][column] & this.bitMask);
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
		return (this.data[row][column] & this.signMask) != 0;
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

	/**
	 * Sets the plane symbol at the given position (1 or 0).
	 * @param row
	 * @param column
	 * @param magnitude
	 */
	public void setSymbolAt(int row, int column, Bit magnitude) {
		this.setBitAt(row, column, magnitude, this.bitMask);
	}

	/**
	 * Sets the sign at the given position (following the sign magnitude
	 * convention)
	 * @param row
	 * @param column
	 * @param sign
	 */
	public void setSignAt(int row, int column, Bit sign) {
		this.setBitAt(row, column, sign, this.signMask);
	}

	/**
	 * If the given bit is 1, ORs the mask in the given position.
	 * If the bit is 0, ANDs the inverse mask in the given position.
	 * @param row
	 * @param column
	 * @param bit
	 * @param mask
	 */
	private void setBitAt(int row, int column, Bit bit, int mask) {
		if (bit == Bit.BIT_ONE) {
			this.data[row][column] |= mask;
		} else {
			this.data[row][column] &= ~mask;
		}
	}
	
	/**
	 * @param row
	 * @param column
	 * @return true if the strip starting at the given position is fully uncoded.
	 */
	public boolean isStripUncoded(int row, int column) {
		for (int j = 0; j < 4; j++) {
			if (this.isCoded(j + row, column)) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * @param plane
	 * @param row
	 * @param column
	 * @return the index of the first non zero bit in the 4-sample strip starting
	 * at the given position for the given plane, or -1 if all are zero.
	 */
	public int stripFirstNonZeroBitAt(int row, int column) {
		for (int j = 0; j < 4; j++) {
			if (this.getSymbolAt(j + row, column) != Bit.BIT_ZERO) {
				return j;
			}
		}
		return -1;
	}
	
}
