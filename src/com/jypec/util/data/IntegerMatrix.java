package com.jypec.util.data;

/**
 * Wrapper for integer arrays of arrays
 * @author Daniel
 *
 */
public interface IntegerMatrix {

	public int getDataAt(int row, int column);
	
	public void setDataAt(int data, int row, int column);
	
	public int getRows();
	
	public int getColumns();
	
	public int[][] extractInnerMatrix();
}
