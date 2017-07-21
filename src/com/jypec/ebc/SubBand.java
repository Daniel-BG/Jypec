package com.jypec.ebc;

/**
 * Enum for all the types of subbands possible. Subbands are made when a DWT is applied
 * to a 2D data matrix in both directions. The high and low pass filters separate the data 
 * in two parts along both dimensions (for a total of four). These are denoted with the letters
 * 'L' and 'H' for low and high passed subBands. Two letters are used, the first indicates
 * which filter was applied in the horizontal direction, and the second in the vertical direction.
 * 
 * 			LL  |  HL
 * Image -> ----+----
 * 			LH  |  HH
 * 
 * @author Daniel
 *
 */
public enum SubBand {
	LL, LH, HL, HH
}
