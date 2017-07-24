package com.jypec.wavelet;

/**
 * CDF 9 7 adaptation from:
 * https://github.com/VadimKirilchuk/jawelet/wiki/CDF-9-7-Discrete-Wavelet-Transform
 * 
 * @author Daniel
 *
 */
public class WaveletTransform {
	
	private static final double COEFF_PREDICT_1 = 1.586134342;
	private static final double COEFF_PREDICT_2 = 0.8829110762; 
	private static final double COEFF_UPDATE_1= -0.05298011854;
	private static final double COEFF_UPDATE_2 = 0.4435068522;
	private static final double COEFF_SCALE = 1/1.149604398;
	
	/**
	 * Adds to each odd indexed sample its neighbors multiplied by the given coefficient
	 * Wraps around if needed, mirroring the array <br>
	 * E.g: {1, 0, 1} with COEFF = 1 -> {1, 2, 1} <br>
	 * 		{1, 0, 2, 0} with COEFF = 1 -> {1, 3, 1, 4}
	 * @param s the signal to be treated
	 * @param n the length of s
	 * @param COEFF the prediction coefficient
	 */
	private static void predict(double[] s, int n, double COEFF) {
		// Predict inner values
		for (int i = 1; i < n - 1; i+=2) {
			s[i] += COEFF * (s[i-1] + s[i+1]);
		}
		// Wrap around
		if (n % 2 == 0 && n > 1) {
			s[n-1] += 2*COEFF*s[n-2];
		}
	}
	
	/**
	 * Adds to each EVEN indexed sample its neighbors multiplied by the given coefficient
	 * Wraps around if needed, mirroring the array
 	 * E.g: {1, 1, 1} with COEFF = 1 -> {3, 1, 3}
	 * 		{1, 1, 2, 1} with COEFF = 1 -> {3, 1, 4, 1}
	 * @param s the signal to be treated
	 * @param n the length of s
	 * @param COEFF the updating coefficient
	 */
	private static void update(double[]s, int n, double COEFF) {
		// Update first coeff
		if (n > 1) {
			s[0] += 2*COEFF*s[1];
		}
		// Update inner values
		for (int i = 2; i < n - 1; i+=2) {
			s[i] += COEFF * (s[i-1] + s[i+1]);
		}
		// Wrap around
		if (n % 2 != 0 && n > 1) {
			s[n-1] += 2*COEFF*s[n-2];
		}
	}
	
	/**
	 * Scales the ODD samples by COEFF, and the EVEN samples by 1/COEFF 
	 * @param s the signal to be scaled
	 * @param n the length of s
	 * @param COEFF the coefficient applied
	 */
	private static void scale(double[] s, int n, double COEFF) {
		for (int i = 0; i < n; i++) {
			if (i%2 == 1) { 
				s[i] *= COEFF;
			} else {
				s[i] /= COEFF;
			}
		}
	}
	
	/**
	 * Packs the even-indexed samples into the first half of the array, 
	 * and the odd-indexed samples into the second half.
	 * @note A consequence of this is that the first half is always equal or
	 * 		exactly one less than the second half
	 * @param s the signal to be packed
	 * @param n the length of s
	 */
	private static void pack(double[] s, int n) {
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
	private static void unpack(double[] s, int n) {
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
	public static void forwardTransform(double[] s, int n) {
		//predict and update
		predict(s, n, WaveletTransform.COEFF_PREDICT_1);		
		update(s, n, WaveletTransform.COEFF_UPDATE_1);
		predict(s, n, WaveletTransform.COEFF_PREDICT_2);	
		update(s, n, WaveletTransform.COEFF_UPDATE_2);
		//scale values
		scale(s, n, WaveletTransform.COEFF_SCALE);
		//pack values (low freq first, high freq last)
		pack(s, n);
	}
	
	/**
	 * Reverses the transform applied by {@link #forwardTransform(double[])} and recovers
	 * the original signal.
	 * @param s
	 */
	public static void reverseTransform(double[] s, int n) {
		//unpack values
		unpack(s, n);
		//unscale values
		scale(s, n, 1/WaveletTransform.COEFF_SCALE);
		//unpredict and unupdate
		update(s, n, -WaveletTransform.COEFF_UPDATE_2);
		predict(s, n, -WaveletTransform.COEFF_PREDICT_2);
		update(s, n, -WaveletTransform.COEFF_UPDATE_1);
		predict(s, n, -WaveletTransform.COEFF_PREDICT_1);
	}
	
}
