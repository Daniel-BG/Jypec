package com.jypec.quantization;

/**
 * Does a log based transformation of the input. 
 * @author Daniel
 */
public class LogPrequantization implements PrequantizationTransformer {

	
	private float avg; //average of the distribution being transformed
	
	/**
	 * Builds a logarithmic pre quantization transformation
	 * @param distributionAverage the average of the <b>original</b> samples being transformed
	 */
	public LogPrequantization(float distributionAverage) {
		this.avg = distributionAverage;
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

}
