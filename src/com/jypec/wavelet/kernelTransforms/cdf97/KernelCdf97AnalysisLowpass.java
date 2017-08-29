package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.Kernel;

/**
 * @author Daniel
 * Lowpass analysis filter for the CDF 97 filter
 */
public class KernelCdf97AnalysisLowpass extends Kernel {

	/**
	 * Create the kernel
	 */
	public KernelCdf97AnalysisLowpass() {
		super(new double[]{
				0.602949018236,
				0.266864118443,
				-0.078223266529,
				-0.016864118443,	
				0.026748757411
		});
	}
	
}
