package com.jypec.arithco.predict.functions;

import com.jypec.arithco.predict.PredictiveFunction;

/**
 * Tries to predict using only the previous sample
 * @author Daniel
 *
 */
public class Basic1DPredictiveFunction implements PredictiveFunction {
	
	private int prediction = FAILED_PREDICTION;

	@Override
	public boolean predict(int[] data, int position) {
		if (position > 0) {
			this.prediction = data[position - 1];
		}
		
		return this.prediction == data[position];
	}

	@Override
	public int getPrediction() {
		return this.prediction;
	}

	@Override
	public void resetStatistics() {
		this.prediction = FAILED_PREDICTION;
	}

}
