package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.KernelBasedWaveletTransform;

/**
 * @author Daniel
 * Wavelet transform based on CDF 97 synthesis and analysis filters
 */
public class KernelCdf97WaveletTransform extends KernelBasedWaveletTransform {
	
	/**
	 * Create the kernel
	 */
	public KernelCdf97WaveletTransform() {
		super(new KernelCdf97AnalysisLowpass(), new KernelCdf97AnalysisHighpass(), new KernelCdf97SynthesisLowpass(), new KernelCdf97SynthesisHighpass());
	}

}
