package com.jypec.wavelet.kernelTransforms;

/**
 * Creates a "reverse" kernel to the one sent to the constructor.
 * Reverse here means that all odd-indexed samples sign is changed
 * @author Daniel
 *
 */
public abstract class ReversedKernel extends Kernel {

	/**
	 * Create the reverse kernel of the one sent
	 * @param k
	 */
	public ReversedKernel(Kernel k) {
		super(k.getSymmetricCoefficients());
		
		for (int i = 1; i < this.getSymmetricLength(); i+=2) {
			this.coefficients[i] = -this.coefficients[i];
		}
	}

}
