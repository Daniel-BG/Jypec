package com.jypec.wavelet;

/**
 * @author Daniel
 * Wavelet interface to apply wavelet transforms to one dimensional arrays
 */
public interface Wavelet {
	
	/**
	 * Transforms the signal in place, applying the wavelet transform.
	 * This means the first half of the array will get the low frequency
	 * coefficients, while the second half will get the high frequency ones.
	 * The signal is mirrored in the ends to make the transform, as values
	 * are needed to complete computation.
	 * 
	 * Transformation is made with the "lifting" scheme instead of kerning 
	 * the signal. This saves around half of the operations needed.
	 * @param s the signal that is to be transformed
	 * @param n the length of the wavelet transform
	 */
	public void forwardTransform(double[] s, int n);
	
	/**
	 * Reverses the transform applied by {@link #forwardTransform(double[])} and recovers
	 * the original signal.
	 * @param s
	 * @param n the length of the wavelet transform
	 */
	public void reverseTransform(double[] s, int n);
	
	/**
	 * @param min
	 * @param max
	 * @return the maximum value that this wavelet will output when fed
	 * values in the given range
	 */
	public double maxResult(double min, double max);
	
	/**
	 * @param min
	 * @param max
	 * @return the minimum value that this wavelet will output when fed
	 * values in the given range
	 */
	public double minResult(double min, double max);
	
}
