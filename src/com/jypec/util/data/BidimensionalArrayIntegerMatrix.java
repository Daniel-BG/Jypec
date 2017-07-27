package com.jypec.util.data;

public class BidimensionalArrayIntegerMatrix implements IntegerMatrix {
	
	private int[][] data;
	private int rows;
	private int columns;
	
	
	public BidimensionalArrayIntegerMatrix(int[][] data, int rows, int columns) {
		this.data = data;
		this.rows = rows;
		this.columns = columns;
	}
	
	
	public static IntegerMatrix newMatrix(int rows, int columns) {
		return new BidimensionalArrayIntegerMatrix(new int[rows][columns], rows, columns);
	}
	
	@Override
	public int getDataAt(int row, int column) {
		return this.data[row][column];
	}
	
	@Override
	public void setDataAt(int data, int row, int column) {
		this.data[row][column] = data; 
	}
	
	@Override
	public int getRows() {
		return this.rows;
	}
	
	@Override
	public int getColumns() {
		return this.columns;
	}


	@Override
	public int[][] extractInnerMatrix() {
		return this.data;
	}

}
