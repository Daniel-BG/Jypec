package com.jypec.wavelet.compositeTransforms;

import org.ejml.data.FMatrixRMaj;

import com.jypec.util.Stepper;
import com.jypec.util.debug.Profiler;
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
	public void forwardTransform(FMatrixRMaj s, int height, int width) {
		Profiler.getProfiler().profileStart();
		int[] widths = Stepper.getStepSizes(width, this.steps);
		int[] heights = Stepper.getStepSizes(height, this.steps);
		for (int i = 0; i < steps; i++) {
			this.bdw.forwardTransform(s, heights[i], widths[i]);
		}
		Profiler.getProfiler().profileEnd();
	}

	@Override
	public void reverseTransform(FMatrixRMaj s, int height, int width) {
		int[] widths = Stepper.getStepSizes(width, this.steps);
		int[] heights = Stepper.getStepSizes(height, this.steps);
		for (int i = steps - 1; i >= 0; i--) {
			this.bdw.reverseTransform(s, heights[i], widths[i]);
		}
	}
}
