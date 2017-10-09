package com.jypec.analysis;

import smile.interpolation.CubicSplineInterpolation1D;

/**
 * Analyzes hyperspectral image components
 * @author Daniel
 *
 */
public class Analyzer {

	
	/**
	 * @param pixel
	 * @return true if this pixel is black: i.e: every sample has the same value
	 */
	public static boolean isBlack(float[] pixel) {
		if (pixel.length == 1) {
			return false;
		}
		
		float first = pixel[0];
		for (int i = 1; i < pixel.length; i++) {
			if (pixel[i] != first) {
				return false;
			}
		}
		
		return true;
	}

	
	private static final double ABERRATION_DIFF_THRESHOLD = 50;
	private static final double ABERRATION_QUOT_THRESHOLD = 4;
	
	/**
	 * @param cp
	 * @return the aberration value (Difference between real and interpolated) or zero if not aberration is found
	 */
	public static double hasAberration(float[] cp) {
		if (cp.length < 2) {
			System.err.println("Cannot know if it has aberration, too few samples");
		}
		
		double[] y = new double[4];
		y[0] = cp[0]; y[1] = cp[1]; y[2] = cp[3]; y[3] = cp[4];
		double[] x = new double[4];
		x[0] = 0; x[1] = 1; x[2] = 3; x[3] = 4;
		
		
		for (int i = 3; i < cp.length; i++) {
			CubicSplineInterpolation1D csi = new CubicSplineInterpolation1D(x, y);
			double interp = csi.interpolate(2);
			double magnitude = (Math.abs(y[0]) + Math.abs(y[1]) + Math.abs(y[2]) + Math.abs(y[3])) / 4;
			double real = cp[i-1];
			double diff = Math.abs(interp - real);
			if (diff > magnitude && Math.abs(diff - magnitude) > ABERRATION_DIFF_THRESHOLD && diff / magnitude > ABERRATION_QUOT_THRESHOLD) {
				return Math.abs(real - interp);
			}
			
			if (i < cp.length - 2) {
				y[0] = cp[i-2];
				y[1] = cp[i-1];
				y[2] = cp[i+1];
				y[3] = cp[i+2];
			}
			
		}
		
		return 0;
	}
	
}
