package com.jypec.util.arrays;

/**
 * Class for generic array manipulation functions
 * @author Daniel
 *
 */
public class ArrayTransforms {

	/**
	 * Packs the even-indexed samples into the first half of the array, 
	 * and the odd-indexed samples into the second half.
	 * @note A consequence of this is that the first half is always equal or
	 * 		exactly one less than the second half
	 * @param s the signal to be packed
	 * @param n the length of s
	 */
	public static void pack(double[] s, int n) {
		// Pack
		double[] tempBank = new double[n];
		
		for (int i = 0; i < n; i++) {
			if (i%2 == 0) {
				tempBank[i/2] = s[i];
			} else {
				tempBank[n/2+i/2 + (n%2)] = s[i];
			}
		}
		for (int i = 0; i < n; i++) {
			s[i] = tempBank[i];
		}
	}
	
	/**
	 * Reverts the process done by {@link #pack(double[], int)}, arranging the samples
	 * in their corresponding positions.
	 * @param s the signal to be treated
	 * @param n lenght of the signal
	 */
	public static void unpack(double[] s, int n) {
		double[] tempBank = new double[n];
		
		for (int i = 0; i < n; i++) {
			if (i%2 == 0) {
				tempBank[i] = s[i/2];
			} else {
				tempBank[i] = s[n/2+i/2 + (n%2)];
			}
		}
		for (int i = 0; i < n; i++) {
			s[i] = tempBank[i];
		}
	}

	/**
	 * Copy the first array into the second up to the nth element.
	 * Pointers do not change, but the inner values of dst do.
	 * @param src
	 * @param dst
	 * @param n
	 */
	public static void copy(double[] src, double[] dst, int n) {
		for (int i = 0; i < n; i++) {
			dst[i] = src[i];
		}
	}

	/**
	 * Splits src into its even and odd samples, leaving zeroes in between in each of the outputs <br>
	 * E.g: {1,2,3,4,5} -> {1,0,3,0,4} + {0,2,0,4,0}
	 * @param src
	 * @param even
	 * @param odd
	 * @param n
	 */
	public static void split(double[] src, double[] even, double[] odd, int n) {
		for (int i = 0; i < n; i++) {
			if (i % 2 == 0) {
				even[i] = src[i];
			} else {
				odd[i] = src[i];
			}
		}
	}
	
	
	/**
	 * @param d a double array
	 * @return the same array casted to float
	 */
	public static float[] changeType(double[] d) {
		float[] f = new float[d.length];
		for (int i = 0; i < d.length; i++) {
			f[i] = (float) d[i];
		}
		return f;
	}
	
	/**
	 * @param f a float array
	 * @return the same array casted to double
	 */
	public static double[] changeType(float[] f) {
		double[] d = new double[f.length];
		for (int i = 0; i < f.length; i++) {
			d[i] = (double) f[i];
		}
		return d;
	}
}
