package com.jypec.quantization;

import java.io.IOException;

import org.ejml.data.FMatrixRMaj;

import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Prequantization class that does no prequantization
 * @author Daniel
 */
public class NonePrequantization extends PrequantizationTransformer {

	@Override
	public void forwardTransform(FMatrixRMaj s) {
		return;
	}

	@Override
	public void reverseTransform(FMatrixRMaj s) {
		return;
	}

	/**
	 * Constructor
	 */
	public NonePrequantization() {
		super(PrequantizationTypes.PREQUANT_NONE);
	}

	@Override
	public float forward(float input) {
		return input;
	}

	@Override
	public float reverse(float input) {
		return input;
	}

	@Override
	public void doSaveTo(BitOutputStreamTree bost) throws IOException {
		//nothing to do
	}

	@Override
	public void train(FMatrixRMaj s) {
		//nothing to do
	}

}
