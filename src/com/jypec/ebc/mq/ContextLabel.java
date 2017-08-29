package com.jypec.ebc.mq;

import com.jypec.util.Bit;

/**
 * Enum storing all the Context labels used by the MQ-Coder
 * @author Daniel
 *
 */
public enum ContextLabel {
	/** special context label zero. Used only in cleanup mode*/
	ZERO,	 
	/**label for significance propagation (1/8)*/
	ONE, 
	/**label for significance propagation (2/8)*/
	TWO, 
	/**label for significance propagation (3/8)*/
	THREE, 
	/**label for significance propagation (4/8)*/
	FOUR, 
	/**label for significance propagation (5/8)*/
	FIVE, 
	/**label for significance propagation (6/8)*/
	SIX, 
	/**label for significance propagation (7/8)*/
	SEVEN, 
	/**label for significance propagation (8/8)*/
	EIGHT,
	/**label for sign encoding (1/5)*/
	NINE, 
	/**label for sign encoding (2/5)*/
	TEN, 
	/**label for sign encoding (3/5)*/
	ELEVEN, 
	/**label for sign encoding (4/5)*/
	TWELVE, 
	/**label for sign encoding (5/5)*/
	THIRTEEN,
	/**label for refinement (1/3)*/
	FOURTEEN, 
	/**label for refinement (2/3)*/
	FIFTEEN, 
	/**label for refinement (3/3)*/
	SIXTEEN,
	/**special label for run_length coding*/
	RUN_LENGTH, 
	/**special label for encoding pointers in run_length mode when failing to make a chain 
	 * note: should always be in state 46.*/
	UNIFORM;
	
	
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
