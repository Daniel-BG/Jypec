package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.ReversedKernel;


/**
 * @author Daniel
 * Synthesis highpass filter for the CDF 97 filter
 */
public class KernelCdf97SynthesisHighpass extends ReversedKernel {

	/**
	 * Create the kernel
	 */
	public KernelCdf97SynthesisHighpass() {
		super(new KernelCdf97AnalysisLowpass());
	}

}
