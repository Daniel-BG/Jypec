package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.ReversedKernel;

public class KernelCdf97SynthesisHighpass extends ReversedKernel {

	public KernelCdf97SynthesisHighpass() {
		super(new KernelCdf97AnalysisLowpass());
	}

}
