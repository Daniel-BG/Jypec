package com.jypec.wavelet.compositeTransforms;

import com.jypec.util.Stepper;
import com.jypec.util.debug.Logger;
import com.jypec.wavelet.BidimensionalWavelet;

/**
 * @author Daniel
 * Applies a two dimensional wavelet to a wave matrix, doing so recursively for the vertical and
 * horizontal lowpass region
 */
public class RecursiveBidimensionalWavelet implements BidimensionalWavelet {
	
	private BidimensionalWavelet bdw;
	private int steps;
	
	/**
	 * @param bdw
	 * @param steps
	 */
	public RecursiveBidimensionalWavelet(BidimensionalWavelet bdw, int steps) {
		this.bdw = bdw;
		this.steps = steps;
	}
	


	@Override
	public void forwardTransform(float[][] s, int width, int height) {
		Logger.getLogger().profileStart();
		int[] widths = Stepper.getStepSizes(width, this.steps);
		int[] heights = Stepper.getStepSizes(height, this.steps);
		for (int i = 0; i < steps; i++) {
			this.bdw.forwardTransform(s, widths[i], heights[i]);
		}
		Logger.getLogger().profileEnd();
	}

	@Override
	public void reverseTransform(float[][] s, int width, int height) {
		int[] widths = Stepper.getStepSizes(width, this.steps);
		int[] heights = Stepper.getStepSizes(height, this.steps);
		for (int i = steps - 1; i >= 0; i--) {
			this.bdw.reverseTransform(s, widths[i], heights[i]);
		}
	}
}
