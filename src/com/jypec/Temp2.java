package com.jypec;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.mq.ContextLabel;

public class Temp2 {
	
	
	
	public static void main(String[] args) {
		
		for(int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				
				
			}
			System.out.println("\\\\\n");
		}
		// TODO Auto-generated method stub

	}
	
	
	public String gco(int i) {
		switch(i) {
		case -1:
			return "N";
		case 0:
			return "I";
		case 1:
			return "P";
			default:
				throw new IllegalArgumentException();
		}
	}
	
	
	
	public ContextLabel getSignificancePropagationContextAt(int t, int tr, int r, int br, int b, int bl, int l, int tl, SubBand subBand) {
		//d0 v0 d1
		//h0 x  h1
		//d2 v1 d3
		//basically add together the significance of the vertical,
		//horitonztal, and diagonal neighbors
		int sumH, sumV, sumD;
		sumH = Math.abs(l) + Math.abs(r);
		sumV = Math.abs(t) + Math.abs(b);
		sumD = Math.abs(tl) + Math.abs(tr) + Math.abs(bl) + Math.abs(br);
		
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




}
