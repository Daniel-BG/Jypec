package com.jypec.quantization;

import java.io.IOException;

import org.ejml.data.FMatrixRMaj;

import com.jypec.util.arrays.EJMLExtensions;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Does a linear transformation between the average and std, and a square root transformation for values over that range
 * @author Daniel
 *
 */
public class SplitLinearSqrtPrequantization extends PrequantizationTransformer {

	private float avg, std;
	
	/**
	 * @param distributionAverage average of input distribution
	 * @param distributionStd standard deviation of input distribution
	 */
	public SplitLinearSqrtPrequantization(float distributionAverage, float distributionStd) {
		super(PrequantizationTypes.PREQUANT_SPLIT_LINEAR_SQRT);
		this.avg = distributionAverage;
		this.std = distributionStd;
	}
	
	/**
	 * Default constructor with avg=0, std=1
	 */
	public SplitLinearSqrtPrequantization() {
		this(0, 1);
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

	@Override
	public void doSaveTo(BitOutputStreamTree bost) throws IOException {
		bost.writeFloat(avg);
		bost.writeFloat(std);
	}

	@Override
	public void train(FMatrixRMaj s) {
		this.avg = EJMLExtensions.avg(s);
		this.std = EJMLExtensions.std(s, this.avg);
	}

}
