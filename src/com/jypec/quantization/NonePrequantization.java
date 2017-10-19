package com.jypec.quantization;

import java.io.IOException;

import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Prequantization class that does no prequantization
 * @author Daniel
 */
public class NonePrequantization extends PrequantizationTransformer {

	@Override
	public void forwardTransform(float[][] s, int width, int height) {
		return;
	}

	@Override
	public void reverseTransform(float[][] s, int width, int height) {
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
	public void train(float[][] s) {
		//nothing to do
	}

}
