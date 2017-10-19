package com.jypec.quantization;

/**
 * Does a linear transformation between the average and std, and a square root transformation for values over that range
 * @author Daniel
 *
 */
public class SplitLinearSqrtPrequantization implements PrequantizationTransformer {

	private float avg, std;
	
	/**
	 * @param distributionAverage average of input distribution
	 * @param distributionStd standard deviation of input distribution
	 */
	public SplitLinearSqrtPrequantization(float distributionAverage, float distributionStd) {
		this.avg = distributionAverage;
		this.std = distributionStd;
	}
	
	
	@Override
	public float forward(float input) {
		if (input > avg) {
			float diff = (input - avg) / std;
			if (diff < 1) {
				return diff;
			} else {
				return (float) Math.sqrt(diff);
			}
		} else if (input < avg) {
			float diff = (avg - input) / std;
			if (diff < 1) {
				return -diff;
			} else {
				return -(float) Math.sqrt(diff);
			}
		} else {
			return 0;
		}
	}

	@Override
	public float reverse(float input) {
		if (input > 1) {
			return input * input * std + avg;
		} else if (input > 0) {
			return input * std + avg;
		} else if (input < -1) {
			return - input * input * std + avg;
		} else if (input < 0) {
			return input * std + avg;
		} else {
			return avg;
		}
	}

}
