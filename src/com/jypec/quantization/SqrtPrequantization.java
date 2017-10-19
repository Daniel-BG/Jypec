package com.jypec.quantization;

import java.io.IOException;

import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Does a square root based transformation of the input 
 * @author Daniel
 */
public class SqrtPrequantization extends PrequantizationTransformer {
	
	private float avg;
	
	/**
	 * @param distributionAverage the average of the <b>original</b> distribution being transformed
	 */
	public SqrtPrequantization(float distributionAverage) {
		super(PrequantizationTypes.PREQUANT_SQRT);
		this.avg = distributionAverage;
	}

	/**
	 * Default constructor with avg = 0
	 */
	public SqrtPrequantization() {
		this(0);
	}

	@Override
	public float forward(float input) {
		if (input > avg) {
			return (float) Math.sqrt(input - avg);
		} else if (input < avg) {
			return -(float) Math.sqrt(avg - input);
		} else {
			return 0;
		}
	}

	@Override
	public float reverse(float input) {
		if (input > 0) {
			return (float) input * input + avg;
		} else if (input < 0) {
			return (float) -input * input + avg;
		} else {
			return avg;
		}
	}

	@Override
	public void doSaveTo(BitOutputStreamTree bost) throws IOException {
		bost.writeFloat(avg);
	}

	@Override
	public void train(float[][] s) {
		this.avg = MatrixOperations.avg(s);
	}

}
