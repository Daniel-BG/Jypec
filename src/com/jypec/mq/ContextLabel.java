package com.jypec.mq;

import com.jypec.util.Bit;

/**
 * Enum storing all the Context labels used by the MQ-Coder
 * @author Daniel
 *
 */
public enum ContextLabel {
	//special context label zero. Used only in cleanup mode
	ZERO,	 
	//labels for significance propagation
	ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT,
	//labels for sign encoding
	NINE, TEN, ELEVEN, TWELVE, THIRTEEN,
	//labels for refinement
	FOURTEEN, FIFTEEN, SIXTEEN,
	//special label for run_length coding
	RUN_LENGTH, 
	//special label for encoding pointers in run_length mode when failing to make a chain
	UNIFORM; //note: should always be in state 46.
	
	
	/**
	 * Get the default state of this context's table
	 * @see MQProbabilityTable
	 * @return an integer defining the state
	 */
	public int getDefaultState() {
		switch(this) {
		case UNIFORM:
			return 46;
		case RUN_LENGTH:
			return 3;
		case ZERO:
			return 4;
		default:
			return 0;	
		}
	}
	
	/**
	 * @return the default Most Probably Symbol (MPS) for this context
	 */
	public Bit getDefaultMPS() {
		return Bit.BIT_ZERO;
	}
}
