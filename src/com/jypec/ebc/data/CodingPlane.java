package com.jypec.ebc.data;

import com.jypec.ebc.SubBand;
import com.jypec.util.bits.Bit;

/**
 * Class that stores a single coding plane from a coding block
 * @see CodingBlock
 * 
 * @author Daniel
 */
public class CodingPlane {
	private CodingBlock block;
	
	
	private SubBand subBand;
	private int bitMask;
	private int signMask;
	private boolean[][] codingStatus;
	
	private int fullStripNumber, lastStripHeight, columns;
	
	/**
	 * Build a coding plane from the planeOffset-th bit of the given data
	 * @param block the encompassing block of this plane. Used to operate over the
	 * data and to precalculate values to avoid excessive calling afterwards
	 * @param planeOffset offset (from the LSB) that this plane refers to
	 */
	public CodingPlane(CodingBlock block, int planeOffset) {
		if (planeOffset < 0 && planeOffset > 30) {
			throw new IllegalArgumentException("Planeoffset out of range");
		}
		
		this.block = block;
		int rows = this.block.getHeight();
		this.fullStripNumber = rows >> 2;
		this.lastStripHeight = rows % 4;
		this.columns = this.block.getWidth();
		this.subBand = this.block.getSubBand();
		this.bitMask = 0x1 << planeOffset;
		this.signMask = this.block.getSignMask();
		
		this.codingStatus = new boolean[rows][columns]; //defaulted to false
	}
	
	
	/**
	 * @return the number of 4-high strips within this plane
	 */
	public int getFullStripsNumber() {
		return this.fullStripNumber;
	}

	/**
	 * @return the width of this plane
	 */
	public int getWidth() {
		return this.columns;
	}

	/**
	 * @return the number of dangling rows at the end that don't make a full strip (0-3)
	 */
	public int getLastStripHeight() {
		return this.lastStripHeight;
	}

	/**
	 * @return the SubBand this plane belongs to
	 */
	public SubBand getSubBand() {
		return this.subBand;
	}

	/**
	 * @param column
	 * @param row
	 * @return The symbol at the given position
	 */
	public Bit getSymbolAt(int row, int column) {
		return Bit.fromInteger(this.block.getDataAt(row, column) & this.bitMask);
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
		return (this.block.getDataAt(row, column) & this.signMask) != 0;
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
	 * @param row 
	 * @param column 
	 * @return true if the given position is already coded
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
		int data = this.block.getDataAt(row, column);
		if (bit == Bit.BIT_ONE) {
			data |= mask;
		} else {
			data &= ~mask;
		}
		this.block.setDataAt(data, row, column);
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
