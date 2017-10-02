package com.jypec.arithco.predict.functions;

import com.jypec.arithco.predict.PredictiveFunction;

/**
 * Implements prediction based on the 2D neighbourhood of the samples. This assumes
 * the input data represents a matrix in row major order
 * @author Daniel
 *
 */
public class Basic2DNeighborhoodPredictiveFunction implements PredictiveFunction {

	private int numRows;
	private int numCols;
	
	private static final int FAILED_PREDICTION = -1;
	private int prediction = FAILED_PREDICTION;
	
	
	/**
	 * Build a Basic2DNeighborhood predictive function which will predict for
	 * a numRows * numCols matrix. the function predicts based on the
	 * north, west and northwest samples to the one being predicted.
	 * @param numRows
	 * @param numCols
	 */
	public Basic2DNeighborhoodPredictiveFunction(int numRows, int numCols) {
		this.numCols = numCols;
		this.numRows = numRows;
	}
	
	
	@Override
	public boolean predict(int[] data, int position) {
		if (data.length < numRows * numCols) { //allow for bigger arrays to be used
			throw new IllegalArgumentException("Data dimensions do not match with initialization values!");
		}
		if (position < 0 || position >= numRows * numCols) {
			throw new IllegalArgumentException("The requested position to be predicted is out of bounds");
		}
		
		/** gather neighbors */
		int north = FAILED_PREDICTION, west = FAILED_PREDICTION, northwest = FAILED_PREDICTION;
		if (position >= numCols) {
			north = data[position - numCols];
		}
		if (position % numCols > 0) {
			west = data[position - 1];
			if (position >= numCols) {
				northwest = data[position - 1 - numCols];
			}
		}
		
		/** estimate result */
		this.prediction = FAILED_PREDICTION;
		if (north == -1) {
			this.prediction = west;
		} else if (west == -1) {
			this.prediction = north;
		} else if (north == northwest) {
			this.prediction = north;
		} else {
			this.prediction = west;
		}
		
		/** return true if we made the right prediction */
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
