package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.KernelBasedWaveletTransform;

public class KernelCdf97WaveletTransform extends KernelBasedWaveletTransform {
	
	public KernelCdf97WaveletTransform() {
		super(new KernelCdf97AnalysisLowpass(), new KernelCdf97AnalysisHighpass(), new KernelCdf97SynthesisLowpass(), new KernelCdf97SynthesisHighpass());
	}

}
