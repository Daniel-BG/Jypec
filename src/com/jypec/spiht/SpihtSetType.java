package com.jypec.spiht;

/**
 * Represents the types of sets that can be found on the Spiht 
 * coder and decoder
 * @author Daniel
 */
public enum SpihtSetType {
	/** A set which includes all descendants of the current node */
	TYPE_A, 
	/** A set which includes descendants only beyond grandchildren (these included) */
	TYPE_B;
}
