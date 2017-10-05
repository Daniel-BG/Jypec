package com.jypec.wavelet.kernelTransforms;

/**
 * Class to store a kernel to be applied on a vector. The kernel is centered around the 
 * convolved element and symmetric. This means that the 0th coefficient is the middle one,
 * and then there are more to both sides. <br>
 * E.g: you can do {@link #getCoefficient(int)} with negative numbers.
 * @author Daniel
 *
 */
public abstract class Kernel {
	
	protected float[] coefficients;
	private int lenght;
	
	/**
	 * Build a Kernel with the given coefficients. It is assumed that the coefficients
	 * sent here are only the positive-indexed ones, since the other side is simmetric. <br>
	 * E.g: for building the kernel {2, 1, 0, 1, 2} send the vector {0, 1, 2}.
	 * @param coefficients
	 */
	public Kernel(float[] coefficients) {
		this.setUp(coefficients);
	}
	
	/**
	 * Build a kernel with the given coefficients, multiplying them by "scaling"
	 * @param coefficients
	 * @param scaling
	 */
	public Kernel(float[] coefficients, float scaling) {
		this.setUp(coefficients);
		for (int i = 0; i < this.getSymmetricLength(); i++) {
			this.coefficients[i] *= scaling;
		}
	}
	
	/**
	 * Set up internal variables
	 * @param coefficients
	 */
	private void setUp(float[] coefficients) {
		this.coefficients = coefficients;
		this.lenght = this.coefficients.length*2 - 1;
	}
	
	
	/**
	 * Apply this kernel
	 * @param s array where the kernel is to be applied. s is NOT modified by this function
	 * @param n length of the array
	 * @param where position where to apply it
	 * @return the convolved value 
	 */
	public float apply(float[] s, int n, int where) {
		float res = 0;
		int limit = this.getSymmetricLength();
		for (int i = -limit + 1; i < limit; i++) {
			//get the current coefficient
			float coeff = this.getCoefficient(i);
			//get the signal index where to apply it. Wrap around if index is not within limits
			int index = where + i;
			while (index < 0 || index >= n) {
				if (n == 1) {
					index = 0;
					break;
				}
				if (index < 0) {
					index = -index;
				}
				if (index >= n) {
					index = (n - 1) - (index - (n - 1));
				}
			}
			//add to accumulator
			res += coeff * s[index];
		}

		if (n == 1) {
			res *= this.cornerCaseFactor();
		}
		
		return res;
	}
	
	
	/**
	 * Faster than {@link #getCoefficient(int)} but only works for
	 * positive values of i
	 * @param i
	 * @return the coefficient at the specified position
	 */
	public float getSymmetricCoefficient(int i) {
		return this.coefficients[i];
	}
	
	/**
	 * @param i
	 * @return the coefficient at the specified position
	 */
	public float getCoefficient(int i) {
		if (i < 0) {
			i = -i;
		}
		return this.coefficients[i];
	}
	
	/**
	 * @return the total length of this kernel, counting both positive and negative
	 * indexed coefficients
	 */
	public int getLength() {
		return this.lenght;
	}
	
	/**
	 * @return the length of this kernel counting only positive-indexed coefficients
	 */
	public int getSymmetricLength() {
		return this.coefficients.length;
	}
	
	/**
	 * @return a copy of the positive-indexed coefficients in this kernel
	 */
	public float[] getSymmetricCoefficients() {
		return this.coefficients.clone();
	}
	
	/**
	 * @return factor which must be used to normalize the result in case of transforming arrays of only 1 value
	 */
	public float cornerCaseFactor() {
		return 1.0f;
	}
	
	/**
	 * @return the sum of all positive coefficients within the kernel
	 * this result is always >= 0
	 */
	public float positiveSum() {
		float acc = 0;
		for (int i = 0; i < this.lenght; i++) {
			if (this.coefficients[i] > 0) {
				acc += this.coefficients[i] * i == 0 ? 1 : 2;
			}
		}
		return acc;
	}
	
	/**
	 * @return the sum of all negative coefficients within the kernel
	 * this result is always <= 0
	 */
	public float negativeSum() {
		float acc = 0;
		for (int i = 0; i < this.lenght; i++) {
			if (this.coefficients[i] < 0) {
				acc += this.coefficients[i] * i == 0 ? 1 : 2;
			}
		}
		return acc;
	}
}
