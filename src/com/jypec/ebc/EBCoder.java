package com.jypec.ebc;

import com.jypec.ebc.mq.ContextLabel;
import com.jypec.ebc.mq.MQArithmeticCoder;
import com.jypec.util.Bit;
import com.jypec.util.BitStream;
import com.jypec.util.CodingBlock;
import com.jypec.util.CodingPlane;
import com.jypec.util.Pair;

/**
 * MQCoder that given an input block codes it into the
 * desired output bitstream
 * @author Daniel
 *
 */
public class EBCoder {
	private SignificanceTable sigTable;
	private MQArithmeticCoder coder;

	
	private void initialize(int width, int height) {
		coder = new MQArithmeticCoder();
		sigTable = new SignificanceTable(width, height);
	}
	
	
	/**
	 * Codes the given block into the given output, resetting
	 * statistics every time this function is called
	 */ 
	public void code(CodingBlock block, BitStream output) {
		this.initialize(block.getWidth(), block.getHeight());
		
		int numberOfBitPlanes = block.getBitPlaneNumber();
		
		CodingPlane plane = block.getBitPlane(numberOfBitPlanes - 1);
		
		//fist bitplane is coded within a cleanup scheme
		this.codeCleanup(plane, output);
		//rest of bitplanes are coded with a 3-step scheme
		for (int i = numberOfBitPlanes - 2; i >= 0; i--) {
			plane = block.getBitPlane(i);
			this.codeSignificance(plane, output);
			this.codeRefinement(plane, output);
			this.codeCleanup(plane, output);
		}
		//end coding by dumping the remaining bits in the buffer
		//and marking the end of the stream
		this.coder.dumpRemainingBits(output);
		this.coder.markEndOfStream(output);
	}
	
	/**
	 * Code the given plane in the cleanup pass. This function may look innocent
	 * but it is hard to understand because it uses weird flow control to allow
	 * for code reutilization
	 * @param plane
	 * @param output
	 */
	private void codeCleanup(CodingPlane plane, BitStream output) {
		//code full strips within the block
		for (int s = 0; s < plane.getFullStripsNumber(); s++) {
			for (int i = 0; i < plane.getWidth(); i++) {
				//okay so here we are once per strip. We assume we are gonna code the first value
				//that'll change if needed
				int j = 0;
				
				//now if none are coded, and all have a zero context, we'll try to run-length code it
				//otherwise we jump to the end with j=0 and code them as usual
				if (plane.isStripUncoded(s*4, i) && this.sigTable.isStripZeroContext(plane.getSubBand(), s*4, i)) {
					//make j point to the first non_zero bit
					j = plane.stripFirstNonZeroBitAt(s*4, i);
					if (j == -1) {	//if j==-1 that means all are zero and we can run-length code it 
						this.coder.codeSymbol(Bit.BIT_ZERO, ContextLabel.RUN_LENGTH, output);
						continue;
					} else {		//otherwise we failed.
						//encode a 1 to signal failure
						this.coder.codeSymbol(Bit.BIT_ONE, ContextLabel.RUN_LENGTH, output);
						//encode the position of the first 1 and its sign (two bit quantity + sign bit)
						this.coder.codeNumberWithContext(j, 2, ContextLabel.UNIFORM, output);
						this.codeSignificanceBit(plane, output, s*4 + j, i, true);
						//point j to the next one and then encode the rest as per usual
						j++;
					}	
				}
				//code whatever is left as usual
				for (; j < 4; j++) {
					if (!plane.isCoded(j + s*4, i)) {
						this.codeSignificanceBit(plane, output, j + s*4, i, false);
					}
				}
			}
		}
		
		//code remaining values at the end (not in 4-high strips)
		for (int i = 0; i < plane.getWidth(); i++) {
			for (int j = plane.getLastStripOffset(); j < plane.getLastStripHeight() + plane.getLastStripOffset(); j++) {
				if (!plane.isCoded(j, i)) {
					this.codeSignificanceBit(plane, output, j, i, false);
				}
			}
		}
	}
	


	/**
	 * Code the bit in the given position following the rules of significance propagation
	 * coding. 
	 * @param plane
	 * @param output
	 * @param column
	 * @param row
	 * @param onlySign if only the sign is to be coded, ignoring the magnitude
	 */
	private void codeSignificanceBit(CodingPlane plane, BitStream output, int row, int column, boolean onlySign) {
		//code the bit (zero or one we code it anyways)
		Bit symbol = plane.getSymbolAt(row, column);
		if (!onlySign) {
			this.coder.codeSymbol(symbol, this.sigTable.getSignificancePropagationContextAt(row, column, plane.getSubBand()), output);
		}
		//if it is one, it has become significant
		if (symbol == Bit.BIT_ONE) {
			//mark as significant (positive or negative)
			boolean isNegative = plane.isNegativeAt(row, column);
			this.sigTable.setSignificant(row, column, isNegative);
			//code sign (inverted if the context bit says so some cases)
			Pair<ContextLabel, Bit> signCtxBit = this.sigTable.getSignBitDecodingContextAt(row, column);
			Bit xorBit = signCtxBit.second();
			Bit sign = Bit.fromBoolean(isNegative);
			if (xorBit == Bit.BIT_ONE) { //xor the sign bit
				sign = sign.getInverse();
			}
			this.coder.codeSymbol(sign, signCtxBit.first(), output);
		}
		//mark this bit as coded so future passes do not code it again
		plane.setCoded(row, column);
	}
	
	/**
	 * Code the significance pass over the given plane
	 * @param plane
	 * @param output
	 */
	private void codeSignificance(CodingPlane plane, BitStream output) {
		//code full strips within the block
		for (int s = 0; s < plane.getFullStripsNumber(); s++) {
			for (int i = 0; i < plane.getWidth(); i++) {
				for (int j = 0; j < 4; j++) {
					if (this.sigTable.getSignificancePropagationContextAt(j + 4*s, i, plane.getSubBand()) != ContextLabel.ZERO) {
						this.codeSignificanceBit(plane, output, j + 4*s, i, false);
					}
				}
			}
		}
		
		//code remaining values at the end (not in 4-high strips)
		for (int i = 0; i < plane.getWidth(); i++) {
			for (int j = 0; j < plane.getLastStripHeight(); j++) {
				if (this.sigTable.getSignificancePropagationContextAt(j + 4*plane.getFullStripsNumber(), i, plane.getSubBand()) != ContextLabel.ZERO) {
					this.codeSignificanceBit(plane, output, j + 4*plane.getFullStripsNumber(), i, false);
				}
			}
		}
	}
	
	/**
	 * Code the given position as a refinement bit
	 * @param plane
	 * @param output
	 * @param column
	 * @param row
	 */
	private void codeRefinementBit(CodingPlane plane, BitStream output, int row, int column) {
		this.coder.codeSymbol(plane.getSymbolAt(row, column), this.sigTable.getMagnitudeRefinementContextAt(row, column), output);
		plane.setCoded(row, column);
	}
	
	/**
	 * Code the refinement pass over the given plane
	 * @param plane
	 * @param output
	 */
	private void codeRefinement(CodingPlane plane, BitStream output) {
		//code full strips within the block
		for (int s = 0; s < plane.getFullStripsNumber(); s++) {
			for (int i = 0; i < plane.getWidth(); i++) {
				for (int j = 0; j < 4; j++) {
					if (!plane.isCoded(j + s*4, i) && this.sigTable.isSignificant(j + s*4, i)) {
						this.codeRefinementBit(plane, output, j + s*4, i);
					}
				}
			}
		}
		
		//code remaining values at the end (not in 4-high strips)
		for (int i = 0; i < plane.getWidth(); i++) {
			for (int j = 0; j < plane.getLastStripHeight(); j++) {
				if (!plane.isCoded(j + 4*plane.getFullStripsNumber(), i) && this.sigTable.isSignificant(j + 4*plane.getFullStripsNumber(), i)) {
					this.codeRefinementBit(plane, output, j + 4*plane.getFullStripsNumber(), i);
				}
			}
		}
	}

}
