package com.jypec.ebc.mq;

import com.jypec.util.bits.Bit;

/**
 * Probability Table used for encoding. 
 * Stores the current prediction and state, and
 * can be changed via calls to its methods
 * @author Daniel
 *
 */
public class MQProbabilityTable {
	private static final Bit INITIAL_BIT = Bit.BIT_ONE;
	private static final int INITIAL_STATE = 0x0;
	
	//from state 0 we would go to 1, from 1 to 2, 
	//from 5 to 38...
	private static final int[] SIGMA_MPS = 
		{1, 2, 3, 4, 5, 38, 7, 8, 9, 10, 
		11, 12, 13, 29, 15, 16, 17, 18, 19, 20, 
		21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 
		31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 
		41, 42, 43, 44, 45, 45, 46};
	private static final int[] SIGMA_LPS = 
		{1, 6, 9, 12, 29, 33, 6, 14, 14, 14, 
		17, 18, 20, 21, 14, 14, 15, 16, 17, 18, 
		19, 19, 20, 21, 22, 23, 24, 25, 26, 27, 
		28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
		38, 39, 40, 41, 42, 43, 46};
	//we have to swap MPS and LPS in states 0, 6 and 14
	private static final boolean[] X_S = 
		{true, false, false, false, false, false, true, false, false, false, 
		false, false, false, false, true, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false};
	//normalized probability estimates for all 47 states
	private static final int[] P_ESTIMATE = 
		{0X5601, 0X3401, 0X1801, 0X0AC1, 0X0521, 0X0221, 0X5601, 0X5401, 0X4801, 0X3801,
		0X3001, 0X2401, 0X1C01, 0X1601, 0X5601, 0X5401, 0X5101, 0X4801, 0X3801, 0X3401,
		0X3001, 0X2801, 0X2401, 0X2201, 0X1C01, 0X1801, 0X1601, 0X1401, 0X1201, 0X1101,
		0X0AC1, 0X09C1, 0X08A1, 0X0521, 0X0441, 0X02A1, 0X0221, 0X0141, 0X0111, 0X0085,
		0X0049, 0X0025, 0X0015, 0X0009, 0X0005, 0X0001, 0X5601};
	
	
	private Bit prediction;
	private int state;
	
	/**
	 * Create the table with the initial value
	 */
	public MQProbabilityTable() {
		this(INITIAL_STATE, INITIAL_BIT);
	}
	
	/**
	 * Create a table with the given initial values
	 * @param state
	 * @param MPS
	 */
	public MQProbabilityTable(int state, Bit MPS) {
		this.prediction = MPS;
		if (state < 0 || state > 46) {
			throw new IllegalArgumentException("State out of bounds @MQProbabilityTable()");
		}
		this.state = state;
	}
	
	/**
	 * @return the current prediction of this table
	 */
	public Bit getPrediction() {
		return this.prediction;
	}
	
	/**
	 * Changes the current prediction to the opposite one
	 * (e.g: if current is 0 it changes to 1 and vice versa)
	 */
	public void changePrediction() {
		this.prediction = this.prediction.getInverse();
	}
	
	/**
	 * Changes the current state to the one given by following
	 * the MPS pointer of the table
	 */
	public void changeStateMPS() {
		this.state = SIGMA_MPS[this.state];
	}

	/**
	 * Changes the current state to the one given by following
	 * the LPS pointer of the table
	 */
	public void changeStateLPS() {
		this.state = SIGMA_LPS[this.state];
	}

	/**
	 * @return the probability estimate in the current state
	 */
	public int getPEstimate() {
		return P_ESTIMATE[this.state];
	}

	/**
	 * @return true if the MPS and LPS roles need to be swapped
	 */
	public boolean needToSwap() {
		return X_S[this.state];
	}

}
