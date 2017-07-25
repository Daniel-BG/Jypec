package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.ReversedKernel;

public class KernelCdf97SynthesisLowpass extends ReversedKernel {

	@Override
	public double cornerCaseFactor() {
		return 0.5;
	}

	public KernelCdf97SynthesisLowpass() {
		super(new KernelCdf97AnalysisHighpass());
	}

	
}

