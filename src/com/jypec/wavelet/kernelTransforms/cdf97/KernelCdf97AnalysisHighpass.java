package com.jypec.wavelet.kernelTransforms.cdf97;

import com.jypec.wavelet.kernelTransforms.Kernel;

/**
 * @author Daniel
 * HighPass analysis filter for the CDF 97 filter
 */
public class KernelCdf97AnalysisHighpass extends Kernel{

	/**
	 * Create the kernel
	 */
	public KernelCdf97AnalysisHighpass() {
		super(new double[]{
				1.11508705,
				-0.591271763114,
				-0.057543526229,
				0.091271763114
		});
	}

}
