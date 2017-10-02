package com.jypec.arithco.predict;

/**
 * Interface for implementing various predictive functions
 * that aim to predict the next value in a given array using
 * the information given up to that point
 * @author Daniel
 *
 */
public interface PredictiveFunction {
	/**
	 * constant indicating a failed prediction
	 */
	public static final int FAILED_PREDICTION = -1;
	
	/**
	 * @param data the int array we are making predictions for. only values >= 0 allowed
	 * @param position the position to be predicted. all previous values from 
	 * data might be used for the prediction, but usage of data 
	 * from further positions is not allowed
	 * @return true if the prediction was successful, false otherwise
	 */
	public boolean predict(int[] data, int position);
	
	/**
	 * @return the predicted value when {@link #predict(int[], int)}
	 * was last called
	 */
	public int getPrediction();
	
	
	/**
	 * Reset internal statistics for the predictive function. This is
	 * intented for reusability of objects
	 */
	public void resetStatistics();

}
