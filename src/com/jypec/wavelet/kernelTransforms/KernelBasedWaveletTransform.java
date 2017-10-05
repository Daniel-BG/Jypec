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
	public void forwardTransform(float[] s, int n) {
		ArrayTransforms.copy(this.forward.transform(s, n), s, n);
		ArrayTransforms.pack(s, n);
	}

	@Override
	public void reverseTransform(float[] s, int n) {
		ArrayTransforms.unpack(s, n);
		float[] lowpass = new float[n];
		float[] highpass = new float[n];
		float[] res = new float[n];
		ArrayTransforms.split(s, lowpass, highpass, n);
		for (int i = 0; i < n; i++) {
			res[i] = this.reverseLowpass.apply(lowpass, n, i) + this.reverseHighpass.apply(highpass, n, i);
		}
		ArrayTransforms.copy(res, s, n);
	}


	private float getLimit(float max, float min, float fp, float fn) {
		float limit = 0;
		limit += max > 0 ? fp * max : 0;
		limit += min < 0 ? fn * min : 0;
		return limit;
	}

	@Override
	public float maxResult(float min, float max) {
		float hpp = this.reverseHighpass.positiveSum();
		float hpn = this.reverseHighpass.negativeSum();
		float lpp = this.reverseLowpass.positiveSum();
		float lpn = this.reverseLowpass.negativeSum();
		
		return Math.max(getLimit(max, min, hpp, hpn), getLimit(max, min, lpp, lpn));
	}



	@Override
	public float minResult(float min, float max) {
		float hpp = this.reverseHighpass.positiveSum();
		float hpn = this.reverseHighpass.negativeSum();
		float lpp = this.reverseLowpass.positiveSum();
		float lpn = this.reverseLowpass.negativeSum();

		//reverse the pn and pp since this goes the other way aroudn
		return Math.min(getLimit(max, min, hpn, hpp), getLimit(max, min, lpn, lpp));
	}

}
