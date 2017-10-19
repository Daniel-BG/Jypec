package com.jypec.wavelet.compositeTransforms;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;

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
	public void forwardTransform(FMatrixRMaj s, int height, int width) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			FMatrixRMaj subMat = CommonOps_FDRM.extractRow(s, i, null);
			float[] row = subMat.getData();
			this.baseWavelet.forwardTransform(row, width);
			CommonOps_FDRM.insert(subMat, s, i, 0);
		}
		//transpose and transform along the other axis
		CommonOps_FDRM.transpose(s);
		for (int j = 0; j < width; j++) {
			FMatrixRMaj subMat = CommonOps_FDRM.extractRow(s, j, null);
			float[] row = subMat.getData();
			this.baseWavelet.forwardTransform(row, height);
			CommonOps_FDRM.insert(subMat, s, j, 0);
		}
		//retranspose and return
		CommonOps_FDRM.transpose(s);
	}

	@Override
	public void reverseTransform(FMatrixRMaj s, int height, int width) {
		//transform along one axis
		for (int i = 0; i < height; i++) {
			FMatrixRMaj subMat = CommonOps_FDRM.extractRow(s, i, null);
			float[] row = subMat.getData();
			this.baseWavelet.reverseTransform(row, width);
			CommonOps_FDRM.insert(subMat, s, i, 0);
		}
		//transpose and transform along the other axis
		CommonOps_FDRM.transpose(s);
		for (int j = 0; j < width; j++) {
			FMatrixRMaj subMat = CommonOps_FDRM.extractRow(s, j, null);
			float[] row = subMat.getData();
			this.baseWavelet.reverseTransform(row, height);
			CommonOps_FDRM.insert(subMat, s, j, 0);
		}
		//retranspose and return
		CommonOps_FDRM.transpose(s);
	}
	
	
	
	
}
