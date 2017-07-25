package com.jypec.wavelet.kernelTransforms;

/**
 * Class used to join two kernels (lowpass and highpass) to apply them together to a signal
 * @author Daniel
 *
 */
public class KernelApplier {

	private Kernel lowpass, highpass;
	
	/**
	 * Build a kernel applier with the given low and highpass filters
	 * @param lowpass
	 * @param highpass
	 */
	public KernelApplier(Kernel lowpass, Kernel highpass) {
		this.lowpass = lowpass;
		this.highpass = highpass;
	}
	
	/**
	 * Transform the given signal according to this KernelApplier's kernels
	 * @param s
	 * @param n
	 * @return the new transformed signal. The original one remains unchanged
	 */
	public double[] transform(double[] s, int n) {
		double[] transformed = new double[n];
		for (int i = 0; i < n; i++) {
			if (i % 2 == 0) {
				transformed[i] = this.lowpass.apply(s, n, i);
			} else {
				transformed[i] = this.highpass.apply(s, n, i);
			}
		}
		return transformed;
	}
	
}
