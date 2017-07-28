package com.jypec.wavelet.liftingTransforms;

import com.jypec.util.arrays.ArrayTransforms;
import com.jypec.wavelet.Wavelet;

/**
 * CDF 9 7 adaptation from:
 * https://github.com/VadimKirilchuk/jawelet/wiki/CDF-9-7-Discrete-Wavelet-Transform
 * 
 * @author Daniel
 *
 */
public class LiftingCdf97WaveletTransform implements Wavelet {
	
	private static final double COEFF_PREDICT_1 = -1.586134342;
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
	private void predict(double[] s, int n, double COEFF) {
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
	private void update(double[]s, int n, double COEFF) {
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
	private void scale(double[] s, int n, double COEFF) {
		for (int i = 0; i < n; i++) {
			if (i%2 == 1) { 
				s[i] *= COEFF;
			} else {
				s[i] /= COEFF;
			}
		}
	}
	
	
	@Override
	public void forwardTransform(double[] s, int n) {
		//predict and update
		predict(s, n, LiftingCdf97WaveletTransform.COEFF_PREDICT_1);		
		update(s, n, LiftingCdf97WaveletTransform.COEFF_UPDATE_1);
		predict(s, n, LiftingCdf97WaveletTransform.COEFF_PREDICT_2);	
		update(s, n, LiftingCdf97WaveletTransform.COEFF_UPDATE_2);
		//scale values
		scale(s, n, LiftingCdf97WaveletTransform.COEFF_SCALE);
		//pack values (low freq first, high freq last)
		ArrayTransforms.pack(s, n);
	}
	
	@Override
	public void reverseTransform(double[] s, int n) {
		//unpack values
		ArrayTransforms.unpack(s, n);
		//unscale values
		scale(s, n, 1/LiftingCdf97WaveletTransform.COEFF_SCALE);
		//unpredict and unupdate
		update(s, n, -LiftingCdf97WaveletTransform.COEFF_UPDATE_2);
		predict(s, n, -LiftingCdf97WaveletTransform.COEFF_PREDICT_2);
		update(s, n, -LiftingCdf97WaveletTransform.COEFF_UPDATE_1);
		predict(s, n, -LiftingCdf97WaveletTransform.COEFF_PREDICT_1);
	}
	
}
