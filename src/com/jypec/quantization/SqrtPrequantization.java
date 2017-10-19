package com.jypec.quantization;

/**
 * Does a square root based transformation of the input 
 * @author Daniel
 */
public class SqrtPrequantization implements PrequantizationTransformer {
	
	private float avg;
	
	/**
	 * @param distributionAverage the average of the <b>original</b> distribution being transformed
	 */
	public SqrtPrequantization(float distributionAverage) {
		this.avg = distributionAverage;
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

}
