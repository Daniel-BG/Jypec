package com.jypec.wavelet.liftingTransforms;

import com.jypec.util.arrays.ArrayTransforms;
import com.jypec.wavelet.Wavelet;
import com.jypec.wavelet.kernelTransforms.cdf97.KernelCdf97WaveletTransform;

/**
 * CDF 9 7 adaptation from:
 * https://github.com/VadimKirilchuk/jawelet/wiki/CDF-9-7-Discrete-Wavelet-Transform
 * 
 * @author Daniel
 *
 */
public class LiftingCdf97WaveletTransform implements Wavelet {
	
	private static final float COEFF_PREDICT_1 = -1.586134342f;
	private static final float COEFF_PREDICT_2 = 0.8829110762f; 
	private static final float COEFF_UPDATE_1= -0.05298011854f;
	private static final float COEFF_UPDATE_2 = 0.4435068522f;

	private static final float COEFF_K = 1.230174105f;
	private static final float COEFF_K0 = 1.0f/COEFF_K;
	private static final float COEFF_K1 = COEFF_K/2.0f;
	
	/**
	 * Adds to each odd indexed sample its neighbors multiplied by the given coefficient
	 * Wraps around if needed, mirroring the array <br>
	 * E.g: {1, 0, 1} with COEFF = 1 -> {1, 2, 1} <br>
	 * 		{1, 0, 2, 0} with COEFF = 1 -> {1, 3, 1, 4}
	 * @param s the signal to be treated
	 * @param n the length of s
	 * @param COEFF the prediction coefficient
	 */
	private void predict(float[] s, int n, float COEFF) {
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
	private void update(float[]s, int n, float COEFF) {
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
	private void scale(float[] s, int n, float kEven, float kOdd) {
		for (int i = 0; i < n; i++) {
			if (i%2 == 1) { 
				s[i] *= kOdd;
			} else {
				s[i] *= kEven;
			}
		}
	}
	
	
	@Override
	public void forwardTransform(float[] s, int n) {
		//predict and update
		predict(s, n, LiftingCdf97WaveletTransform.COEFF_PREDICT_1);		
		update(s, n, LiftingCdf97WaveletTransform.COEFF_UPDATE_1);
		predict(s, n, LiftingCdf97WaveletTransform.COEFF_PREDICT_2);	
		update(s, n, LiftingCdf97WaveletTransform.COEFF_UPDATE_2);
		//scale values
		scale(s, n, LiftingCdf97WaveletTransform.COEFF_K0, LiftingCdf97WaveletTransform.COEFF_K1);
		//pack values (low freq first, high freq last)
		ArrayTransforms.pack(s, n);
	}
	
	@Override
	public void reverseTransform(float[] s, int n) {
		//unpack values
		ArrayTransforms.unpack(s, n);
		//unscale values
		scale(s, n, 1/LiftingCdf97WaveletTransform.COEFF_K0, 1/LiftingCdf97WaveletTransform.COEFF_K1);
		//unpredict and unupdate
		update(s, n, -LiftingCdf97WaveletTransform.COEFF_UPDATE_2);
		predict(s, n, -LiftingCdf97WaveletTransform.COEFF_PREDICT_2);
		update(s, n, -LiftingCdf97WaveletTransform.COEFF_UPDATE_1);
		predict(s, n, -LiftingCdf97WaveletTransform.COEFF_PREDICT_1);
	}

	@Override
	public float maxResult(float min, float max) {
		return new KernelCdf97WaveletTransform().maxResult(min, max);
	}

	@Override
	public float minResult(float min, float max) {
		return new KernelCdf97WaveletTransform().minResult(min, max);
	}
	
}
