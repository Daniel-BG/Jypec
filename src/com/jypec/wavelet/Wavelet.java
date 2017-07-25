package com.jypec.wavelet;

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
	 */
	public void forwardTransform(double[] s, int n);
	
	/**
	 * Reverses the transform applied by {@link #forwardTransform(double[])} and recovers
	 * the original signal.
	 * @param s
	 */
	public void reverseTransform(double[] s, int n);
	
}
