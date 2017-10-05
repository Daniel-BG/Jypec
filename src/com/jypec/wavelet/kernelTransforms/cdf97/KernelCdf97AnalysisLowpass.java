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
		super(new float[]{
				0.602949018236f,
				0.266864118443f,
				-0.078223266529f,
				-0.016864118443f,	
				0.026748757411f
		});
	}
	
}
