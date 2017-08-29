package com.jypec.util.data;

/**
 * Wrapper for integer arrays of arrays
 * @author Daniel
 *
 */
public interface IntegerMatrix {

	/**
	 * @param row
	 * @param column
	 * @return the data at the given row and column
	 */
	public int getDataAt(int row, int column);
	
	/**
	 * Set the given data at the given position
	 * @param data
	 * @param row
	 * @param column
	 */
	public void setDataAt(int data, int row, int column);
	
	/**
	 * @return the number of rows of this matrix
	 */
	public int getRows();
	
	/**
	 * @return the number of columns of this matrix
	 */
	public int getColumns();
	
	/**
	 * @return the raw array that stores the data used in this object
	 */
	public int[][] extractInnerMatrix();
}
