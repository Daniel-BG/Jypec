package com.jypec.wavelet.compositeTransforms;

import com.jypec.util.arrays.MatrixTransforms;
import com.jypec.wavelet.BidimensionalWavelet;
import com.jypec.wavelet.Wavelet;

/**
 * Extend a 1-D wavelet to 2-D
 * @author Daniel
 *
 */
public class OneDimensionalWaveletExtender implements BidimensionalWavelet {

	private Wavelet baseWavelet;
	
	/**
	 * Create a Bidimensional wavelet
	 * @param baseWavelet 1-D wavelet that is to be applied in both dimensions
	 */
	public OneDimensionalWaveletExtender(Wavelet baseWavelet) {
		this.baseWavelet = baseWavelet;
	}
	
	
	@Override
	public void forwardTransform(float[][] s, int width, int height) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			this.baseWavelet.forwardTransform(s[i], width);
		}
		//transpose and transform along the other axis
		float[][] tmp = new float[width][height];
		MatrixTransforms.transpose(s, tmp, width, height);
		for (int j = 0; j < width; j++) {
			this.baseWavelet.forwardTransform(tmp[j], height);
		}
		//retranspose and return
		MatrixTransforms.transpose(tmp, s, height, width);
	}

	@Override
	public void reverseTransform(float[][] s, int width, int height) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			this.baseWavelet.reverseTransform(s[i], width);
		}
		//transpose and transform along the other axis
		float[][] tmp = new float[width][height];
		MatrixTransforms.transpose(s, tmp, width, height);
		for (int j = 0; j < width; j++) {
			this.baseWavelet.reverseTransform(tmp[j], height);
		}
		//retranspose and return
		MatrixTransforms.transpose(tmp, s, height, width);
	}
	
	
}
