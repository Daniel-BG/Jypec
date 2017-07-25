package com.jypec.wavelet.kernelTransforms;

import com.jypec.util.arrays.ArrayTransforms;
import com.jypec.wavelet.Wavelet;

public class KernelBasedWaveletTransform implements Wavelet {

	private KernelApplier forward;
	private Kernel reverseLowpass;
	private Kernel reverseHighpass;
	
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
