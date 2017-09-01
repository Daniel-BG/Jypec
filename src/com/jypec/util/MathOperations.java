package com.jypec.util;

/**
 * @author Daniel
 * various math operations to help us along
 */
public class MathOperations {
	
	
	/**
	 * @param absoluteMaxValue
	 * @param d
	 * @return the maximum distance between two samples existing in a "d"-dimensional space,
	 * bounded from (0,...,0) to (absoluteMaxValue,...,absoluteMaxValue)
	 */
	public static double getMaximumDistance(double absoluteMaxValue, double d) {
		double valueIncrement = Math.sqrt(d);
		return absoluteMaxValue * valueIncrement;
	}

	
	
	/**
	 * @param val
	 * @param base
	 * @return the logarithm of "val" in base "base"
	 */
	public static double logBase(double val, double base) {
		return Math.log10(val) / Math.log10(base);
	}
}
