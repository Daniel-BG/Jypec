package com.jypec.quantization;

import java.io.IOException;

import com.jypec.util.arrays.MatrixOperations;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Does a log based transformation of the input. 
 * @author Daniel
 */
public class LogPrequantization extends PrequantizationTransformer {

	
	private float avg; //average of the distribution being transformed
	
	/**
	 * Builds a logarithmic pre quantization transformation
	 * @param distributionAverage the average of the <b>original</b> samples being transformed
	 */
	public LogPrequantization(float distributionAverage) {
		super(PrequantizationTypes.PREQUANT_LOG);
		this.avg = distributionAverage;
	}
	
	/**
	 * Default constructor with average 0
	 */
	public LogPrequantization() {
		this(0);
	}

	@Override
	public float forward(float input) {
		if (input > avg) {
			return (float) Math.log(input - avg + 1);
		} else if (input < avg) {
			return -(float) Math.log(avg - input + 1);
		} else {
			return 0;
		}
	}

	@Override
	public float reverse(float input) {
		if (input > 0) {
			return (float) Math.exp(input) - 1 + avg;
		} else if (input < 0) {
			return (float) -Math.exp(-input) + avg + 1;
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
