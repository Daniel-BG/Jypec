package com.jypec.mq;

import com.jypec.util.Bit;
import com.jypec.util.Pair;

/**
 * Stores the significance values of a grid of pixels
 * @author Daniel
 *
 */
public class SignificanceTable {
	/**
	 * Possible values for the inner table of this class,
	 * along with methods to treat htem
	 * @author Daniel
	 *
	 */
	public enum SignificanceValue {
		INSIGNIFICANT, SIGNIFICANT_POSITIVE, SIGNIFICANT_NEGATIVE;
		
		public int getValue() {
			return this == INSIGNIFICANT ? 0 : 1;
		}
		
		public boolean isSignificant() {
			return this != SignificanceValue.INSIGNIFICANT;
		}
		
		public int getContribution() {
			switch (this) {
			case INSIGNIFICANT: return 0;
			case SIGNIFICANT_NEGATIVE: return -1;
			case SIGNIFICANT_POSITIVE: return 1;
			default: return 0xff;
			}
		}
	}
	
	//inner storage
	private SignificanceValue[][] table;
	private boolean[][] firstRefinement;
	private int height, width;
	
	/**
	 * Build a table of the specified dimensions. All values are defaulted to insignificant
	 * with the first refinement marked as "true"
	 * @param width
	 * @param height
	 */
	public SignificanceTable(int width, int height) {
		this.width = width;
		this.height = height;
		this.table = new SignificanceValue[height][width];
		this.firstRefinement = new boolean[height][width];
		//initialize just in case
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				this.table[i][j] = SignificanceValue.INSIGNIFICANT;
				this.firstRefinement[i][j] = true;
			}
		}
	}
	
	/**
	 * Set the value at the given position as significant
	 * @param column
	 * @param row
	 */
	public void setSignificant(int row, int column, boolean isNegative) {
		if (!isNegative) {
			this.table[row][column] = SignificanceValue.SIGNIFICANT_POSITIVE;
		} else {
			this.table[row][column] = SignificanceValue.SIGNIFICANT_NEGATIVE;
		}
	}
	
	/**
	 * @param column
	 * @param row
	 * @return true if the value at the given position is significant
	 * (positive or negative significant both)
	 */
	public boolean isSignificant(int row, int column) {
		return this.table[row][column].isSignificant();
	}
	
	/**
	 * Set the value at the given position as insignificant
	 * @param column
	 * @param row
	 */
	public void setInsignificant(int row, int column) {
		this.table[row][column] = SignificanceValue.INSIGNIFICANT;
	}
	
	/**
	 * Returns an array with 3 elements:
	 * 	sumH, sumV, sumD
	 * @param column
	 * @param row
	 * @return
	 */
	private int[] getLocalSums(int row, int column) {
		int sumH = 0, sumD = 0, sumV = 0;
		
		if (column > 0) {
			sumH += this.table[row][column-1].getValue();
			if (row > 0) {
				sumD += this.table[row-1][column-1].getValue();
			}
			if (row < height - 1) {
				sumD += this.table[row+1][column-1].getValue();
			}
		}
		if (column < width - 1) {
			sumH += this.table[row][column+1].getValue();
			if (row > 0) {
				sumD += this.table[row-1][column+1].getValue();
			}
			if (row < height - 1) {
				sumD += this.table[row+1][column+1].getValue();
			}
		}
		if (row > 0) {
			sumV += this.table[row-1][column].getValue();
		}
		if (row < height - 1) {
			sumV += this.table[row+1][column].getValue();
		}
		
		return new int[]{sumH, sumV, sumD};
	}
	
	/**
	 * Get the context at the given position, assuming it refers to the given
	 * subband, according to Table D.1 of ISO/IEC 15444-1:2002 (E)
	 * (D.3.1 JPEG2000 standard)
	 * @param column
	 * @param row
	 * @param subBand
	 * @return
	 */
	public ContextLabel getSignificancePropagationContextAt(int row, int column, SubBand subBand) {
		//d0 v0 d1
		//h0 x  h1
		//d2 v1 d3
		//basically add together the significance of the vertical,
		//horitonztal, and diagonal neighbors
		int sumH, sumV, sumD;
		int[] sums = getLocalSums(row, column);
		sumH = sums[0]; sumV = sums[1]; sumD = sums[2];
		
		//Depending on which subband we are coding, the context
		//will be different
		switch(subBand) {
		//hl is just like LL and LH but with V and H exchanged
		case HL:
			int temp = sumV;
			sumV = sumH;
			sumH = temp;
		case LL:
		case LH:
			if (sumH == 0) {
				if (sumV == 0) {
					if (sumD == 0) {
						return ContextLabel.ZERO;
					} else if (sumD == 1) {
						return ContextLabel.ONE;
					} else { //sumD >= 2
						return ContextLabel.TWO;
					}
				} else if (sumV == 1) {
					return ContextLabel.THREE;
				} else { //sumV == 2 {
					return ContextLabel.FOUR;
				}
			} else if (sumH == 1) {
				if (sumV == 0) {
					if (sumD == 0) {
						return ContextLabel.FIVE;
					} else { //sumD >= 1
						return ContextLabel.SIX;
					}
				} else { //sumV >= 1
					return ContextLabel.SEVEN;
				}
			} else { //sumH == 2
				return ContextLabel.EIGHT;
			}
		default: //HH
			int sumHV = sumH + sumV;
			if (sumD == 0) {
				if (sumHV == 0) {
					return ContextLabel.ZERO;
				} else if (sumHV == 1) {
					return ContextLabel.ONE;
				} else { //sumHV >= 2 
					return ContextLabel.TWO;
				}
			} else if (sumD == 1) {
				if (sumHV == 0) {
					return ContextLabel.THREE;
				} else if (sumHV == 1) {
					return ContextLabel.FOUR;
				} else { //sumHV >= 2 
					return ContextLabel.FIVE;
				}
			} else if (sumD == 2) {
				if (sumHV == 0) {
					return ContextLabel.SIX;
				} else { //sumHV >= 1
					return ContextLabel.SEVEN;
				}
			} else { //sumD >= 3
				return ContextLabel.EIGHT;
			}
		}
	}
	
	
	/**
	 * Gets the sign bit context associated with the given position
	 * (D.3.2 JPEG2000 standard), and the xor bit needed for compression
	 * @param column
	 * @param row
	 * @return
	 */
	public Pair<ContextLabel, Bit> getSignBitDecodingContextAt(int row, int column) {
		SignificanceValue v0, v1, h0, h1;
		v0 = v1 = h0 = h1 = SignificanceValue.INSIGNIFICANT;
		//get neighboring values
		if (column > 0) {
			h0 = this.table[row][column-1];
		}
		if (column < width - 1) {
			h1 = this.table[row][column+1];
		}
		if (row > 0) {
			v0 = this.table[row-1][column];
		}
		if (row < height - 1) {
			v1 = this.table[row+1][column];
		}
		//get contributions
		int horizontalContribution = h0.getContribution() + h1.getContribution();
		int verticalContribution = v0.getContribution() + v1.getContribution();
		
		ContextLabel resContext = null;
		Bit resBit = Bit.BIT_ZERO;
		
		if (horizontalContribution > 0) {
			if (verticalContribution > 0) {
				resContext = ContextLabel.THIRTEEN;
			} else if (verticalContribution == 0) {
				resContext =  ContextLabel.TWELVE;
			} else { //verCont < 0
				resContext =  ContextLabel.ELEVEN;
			}
		} else if (horizontalContribution == 0) {
			if (verticalContribution > 0) {
				resContext =  ContextLabel.TEN;
			} else if (verticalContribution == 0) {
				resContext =  ContextLabel.NINE;
			} else { //verCont < 0
		////
		//// Below this line all states need to have the xor bit enabled
		////
				resBit = Bit.BIT_ONE;
				resContext =  ContextLabel.TEN;
			}
		} else { //horCont < 0
			resBit = Bit.BIT_ONE;
			if (verticalContribution > 0) {
				resContext =  ContextLabel.ELEVEN;
			} else if (verticalContribution == 0) {
				resContext =  ContextLabel.TWELVE;
			} else { //verCont < 0
				resContext =  ContextLabel.THIRTEEN;
			}
		}
		
		//create and return the pair
		return new Pair<ContextLabel, Bit>(resContext, resBit);
	}
	
	
	/**
	 * Gets the magnitude refinement context at the given position
	 * (D.3.3 JPEG2000 standard). Modifies internal variables so that
	 * the first refinement context is separated from subsequent ones
	 * @param column
	 * @param row
	 * @return
	 */
	public ContextLabel getMagnitudeRefinementContextAt(int row, int column) {
		int[] sums = getLocalSums(row, column);
		int totalSum = sums[0] + sums[1] + sums[2];
		if (this.firstRefinement[row][column]) {
			//refine it
			this.firstRefinement[row][column] = false;
			if (totalSum == 0) {
				return ContextLabel.FOURTEEN;
			} else {
				return ContextLabel.FIFTEEN;
			}
		} else {
			return ContextLabel.SIXTEEN;
		}
	}
}
