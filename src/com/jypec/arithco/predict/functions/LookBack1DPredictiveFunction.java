package com.jypec.arithco.predict.functions;

import com.jypec.arithco.predict.PredictiveFunction;

/**
 * Implements prediction based on the 2D neighbourhood of the samples. This assumes
 * the input data represents a matrix in row major order
 * @author Daniel
 *
 */
public class LookBack1DPredictiveFunction implements PredictiveFunction {
	private int prediction = FAILED_PREDICTION;
	private int lookBack;
	
	private int[] findings;
	
	/**
	 * Build a predictive function which will look back <code>lookBack</code> samples behind and 
	 * use that data to predict
	 * @param lookBack number of values behind the current position to take into account for prediction
	 * @param possibleValues possible values that the input data might have (which must be in the [0,possibleValues-1] range
	 */
	public LookBack1DPredictiveFunction(int lookBack, int possibleValues) {
		if (lookBack <= 0) {
			throw new IllegalArgumentException("lookback must be positive");
		}
		
		this.lookBack = lookBack;
		findings = new int[possibleValues];
	}
	
	@Override
	public boolean predict(int[] data, int position) {
		if (position <= this.lookBack) {
			return false;
		}
		
		/** fill findings */
		this.cleanFindings();
		for (int i = 0; i < lookBack; i++) {
			this.findings[data[position-1-i]]++;
		}
		
		/** get max (or maxes) */
		int index = 0, max = -1;
		boolean onlyMax = true;
		for (int i = 0; i < findings.length; i++) {
			if (findings[i] > max) {
				index = i;
				max = findings[i];
				onlyMax = true;
			} else if (findings[i] >= max) {
				onlyMax = false;
			}
		}
		
		/** if only max return it, otherwise return closest max to index */
		if (onlyMax) {
			this.prediction = index;
		} else {
			for (int i = 0; i < lookBack; i++) {
				if (findings[data[position-i-1]] == max) {
					this.prediction = i;
				}
			}
		}
		
		/** return true if we made the right prediction */
		return this.prediction == data[position];
	}

	private void cleanFindings() {
		for (int i = 0; i < findings.length; i++) {
			findings[i] = 0;
		}
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
