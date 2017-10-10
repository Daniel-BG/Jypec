package com.jypec.spiht;

/**
 * Simple two dimensional integer coordinate
 * with final fields so it cannot be edited
 * @author Daniel
 *
 */
public class Coordinate {
	/** X coordinate */
	public final int x;
	/** Y coordinate */
	public final int y;
	
	/**
	 * Build a coordinate
	 * @param x
	 * @param y
	 */
	public Coordinate(int x, int y) {
		this.x = x; 
		this.y = y;
	}
}
