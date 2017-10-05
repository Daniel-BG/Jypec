package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.ReversedKernel;

/**
 * @author Daniel
 * Synthesis lowpass filter for the CDF 97 filter
 */
public class KernelCdf97SynthesisLowpass extends ReversedKernel {

	@Override
	public float cornerCaseFactor() {
		return 0.5f;
	}

	/**
	 * Create the kernel
	 */
	public KernelCdf97SynthesisLowpass() {
		super(new KernelCdf97AnalysisHighpass());
	}

	
}

