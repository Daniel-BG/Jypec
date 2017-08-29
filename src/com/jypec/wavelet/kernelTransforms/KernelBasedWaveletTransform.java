package com.jypec.wavelet.kernelTransforms;

import com.jypec.util.arrays.ArrayTransforms;
import com.jypec.wavelet.Wavelet;

/**
 * @author Daniel
 * A wavelet transform based on kernels applied to elements of the matrix that is filtered
 */
public class KernelBasedWaveletTransform implements Wavelet {

	private KernelApplier forward;
	private Kernel reverseLowpass;
	private Kernel reverseHighpass;
	
	/**
	 * Create the wavelet transform based on a quartet of filters. Both highpass and lowpass for analysis and synthesis
	 * @param analysisLowpass
	 * @param analysisHighpass
	 * @param synthesisLowpass
	 * @param synthesisHighpass
	 */
	public KernelBasedWaveletTransform(Kernel analysisLowpass, Kernel analysisHighpass, Kernel synthesisLowpass, Kernel synthesisHighpass) {
		this.forward = new KernelApplier(analysisLowpass, analysisHighpass);
		this.reverseHighpass = synthesisHighpass;
		this.reverseLowpass = synthesisLowpass;
	}
	
	
	
	@Override
	public void forwardTransform(double[] s, int n) {
		ArrayTransforms.copy(this.forward.transform(s, n), s, n);
		ArrayTransforms.pack(s, n);
	}

	@Override
	public void reverseTransform(double[] s, int n) {
		ArrayTransforms.unpack(s, n);
		double[] lowpass = new double[n];
		double[] highpass = new double[n];
		double[] res = new double[n];
		ArrayTransforms.split(s, lowpass, highpass, n);
		for (int i = 0; i < n; i++) {
			res[i] = this.reverseLowpass.apply(lowpass, n, i) + this.reverseHighpass.apply(highpass, n, i);
		}
		ArrayTransforms.copy(res, s, n);
	}

}
