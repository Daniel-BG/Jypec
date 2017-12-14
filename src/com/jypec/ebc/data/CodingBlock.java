package com.jypec.ebc.data;

import com.jypec.ebc.SubBand;
import com.jypec.util.bits.BitTwiddling;
import com.jypec.util.datastructures.BidimensionalArrayIntegerMatrix;
import com.jypec.util.datastructures.IntegerMatrix;

/**
 * Class for the storage and manipulation of coding blocks that are coded
 * by the MQ-coder
 * @author Daniel
 *
 */
public class CodingBlock {
	
	class CodingBlockDataView {
		private CodingBlock block;
		public CodingBlockDataView(CodingBlock block) {
			this.block = block;
		}
		
		@Override
		public String toString() {
			String res = "";
			for (int i = 0; i < block.getHeight(); i++) {
				for (int j = 0; j < block.getWidth(); j++) {
					res += block.getMagnitudeAt(i, j) + ",";
				}
				res += "\n";
			}
			return res;
		}
	}
	
	@Override
	public String toString() {
		return "(" + rows + "x" + columns + ") @ (" + rowOffset + "," + columnOffset + ") [" + band.name() + "]";
	}

	private CodingBlockDataView cbdv;
	private IntegerMatrix data;
	private int rows, columns;
	private int rowOffset = 0, columnOffset = 0;
	private int magnitudeBitPlanes;
	private SubBand band;
	
	/**
	 * Construct a coding block from the given data. It is assumed that 
	 * data is a non-null rectangular matrix. Otherwise behaviour
	 * is undefined.
	 * @param data the data for this block
	 * @param height of the block within the data
	 * @param width of the block within the data
	 * @param rowOffset how much to offset the index when accesing the block within the data
	 * @param columnOffset same as rowOffset but for columns
	 * @param depth number of bitplanes in the data. (SIGN PLANE INCLUDED) 
	 * assuming the least significant bit plane is stored int the LSB
	 * @param band the subBand this block belongs to
	 * @see SubBand
	 */
	public CodingBlock(IntegerMatrix data, int height, int width, int rowOffset, int columnOffset, int depth, SubBand band) {
		this.setUp(data, height, width, rowOffset, columnOffset, depth, band);
	}

	/**
	 * Creates an empty codeblock to be filled when decoding
	 * @param height height of this block
	 * @param width width of this block
	 * @param depth depth of this block (sign bit included)
	 * @param band which band this block corresponds to
	 */
	public CodingBlock(int height, int width, int depth, SubBand band) {
		this.setUp(BidimensionalArrayIntegerMatrix.newMatrix(height, width), height, width, 0, 0, depth, band);
	}
	
	
	private void setUp(IntegerMatrix data, int height, int width, int rowOffset, int columnOffset, int depth, SubBand band) {
		//check validity of arguments
		if (depth < 2 || depth > 32) {
			throw new IllegalArgumentException("Number of bitplanes must be between 2 and 31 (both inclusive). You are trying with " + depth);
		}
		
		//assign internal variables
		this.data = data;
		this.rows = height;
		this.columns = width;
		this.rowOffset = rowOffset;
		this.columnOffset = columnOffset;
		this.setDepth(depth);
		this.band = band;
		this.cbdv = new CodingBlockDataView(this);
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
	 * @return the row offset from the top-left position of the band it codes
	 */
	public int getRowOffset() {
		return this.rowOffset;
	}
	
	/**
	 * @return the column offset from the top-left position of the band it codes
	 */
	public int getColumnOffset() {
		return this.columnOffset;
	}

	/**
	 * @return the number of bitplanes that this block has (excluding the sign bitplane)
	 */
	public int getMagnitudeBitPlaneNumber() {
		return this.magnitudeBitPlanes;
	}
	
	/**
	 * @return the real number of bitplanes needed to encode this block 
	 * (e.g: if the max value is 5, then we only need to encode three bitplanes)
	 */
	public int getMaxMagnitudeBitPlaneNumber() {
		int max = 0;
		for (int i = 0; i < this.getHeight(); i++) {
			for (int j = 0; j < this.getWidth(); j++) {
				int magnitude = this.getMagnitudeAt(i, j);
				max = Math.max(max, BitTwiddling.bitsOf(magnitude));
			}
		}
		return max;
	}



	/**
	 * Gets the ith bitPlane from within this codeBlock
	 * @param i the index of the bitplane to get
	 * @return the ith bit plane within this block
	 */
	public CodingPlane getBitPlane(int i) {
		return this.getBitPlane(i, false);
	}
	
	/**
	 * Gets the ith bitPlane from within this codeBlock
	 * @param i the index of the bitplane to get
	 * @param lightWeight if the codingPlane is lightweight (does not have a status matrix)
	 * @return the ith bit plane within this block
	 */
	public CodingPlane getBitPlane(int i, boolean lightWeight) {
		if (i < 0 || i >= this.magnitudeBitPlanes) {
			throw new IllegalArgumentException("Requested plane (" + i + ") does not exist. Available: [0," + (this.magnitudeBitPlanes - 1) + "]");
		}
		return new CodingPlane(this, i, lightWeight);
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
		return this.data.getDataAt(row + rowOffset, column + columnOffset);
	}
	
	/**
	 * @param i
	 * @param j
	 * @return the magnitude of the value at the specified position
	 */
	private int getMagnitudeAt(int row, int column) {
		return this.data.getDataAt(row + rowOffset, column + columnOffset) & ~this.getSignMask();
	}
	
	
	
	/**
	 * Sets the value given at the given position, overwriting existing data!
	 * @param value
	 * @param row
	 * @param column
	 */
	public void setDataAt(int value, int row, int column) {
		this.data.setDataAt(value, row + rowOffset, column + columnOffset);
	}

	/**
	 * @return the subBand this block belongs to
	 */
	public SubBand getSubBand() {
		return this.band;
	}

	/**
	 * @param depth the new depth of this bit plane
	 */
	public void setDepth(int depth) {
		this.magnitudeBitPlanes = depth - 1;
	}

	/**
	 * Clear the contents of this block. Setting to ZERO
	 */
	public void clear() {
		for (int i = 0; i < this.getHeight(); i++) {
			for (int j = 0; j < this.getWidth(); j++) {
				this.setDataAt(0, i, j);
			}
		}
	}
	
	
	/**
	 * @param row
	 * @param column
	 * @return true if this block is responsible for coding the specified position.
	 * i.e: if it falls within this block's boundary
	 */
	public boolean isEncoding(int row, int column) {
		return row >= this.rowOffset && row < this.rowOffset + this.rows &&
				column >= this.columnOffset && column < this.columnOffset + this.columns;
	}

	
}
