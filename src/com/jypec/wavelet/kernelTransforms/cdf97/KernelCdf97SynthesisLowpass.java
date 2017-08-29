package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.ReversedKernel;

/**
 * @author Daniel
 * Synthesis lowpass filter for the CDF 97 filter
 */
public class KernelCdf97SynthesisLowpass extends ReversedKernel {

	@Override
	public double cornerCaseFactor() {
		return 0.5;
	}

	/**
	 * Create the kernel
	 */
	public KernelCdf97SynthesisLowpass() {
		super(new KernelCdf97AnalysisHighpass());
	}

	
}

