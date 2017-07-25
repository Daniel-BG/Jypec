package com.jypec;

import java.util.Random;

import com.jypec.wavelet.Wavelet;
import com.jypec.wavelet.kernelTransforms.cdf97.KernelCdf97WaveletTransform;

/**
 * Tests go here
 * @author Daniel
 *
 */
public class Main {

	public static void main(String[] args) {
		Wavelet testWavelet = new KernelCdf97WaveletTransform();
		
		Random r = new Random(1);
		for (int i = 1; i < 200; i++) {
			double[] s = new double[i];
			double[] res = new double[i];
			for (int j = 0; j < s.length; j++) {
				s[j] = r.nextGaussian() * 1000;
				res[j] = s[j];
			}
			
			testWavelet.forwardTransform(s, i);
			testWavelet.reverseTransform(s, i);
			
			System.out.println(i + " done");
		}
		
	}

}
