package com.jypec.mq;

import java.util.EnumMap;
import com.jypec.util.Bit;
import com.jypec.util.BitStream;
import com.jypec.util.BitTwiddling;
import com.jypec.util.CodingBlock;
import com.jypec.util.CodingPlane;
import com.jypec.util.Pair;
import com.jypec.util.debug.Logger;

/**
 * MQCoder that given an input stream codes it into the
 * desired output bitstream with successive calls to code()
 * @author Daniel
 *
 */
public class MQCoder {
	private static final int DEFAULT_INTERVAL = 0x8000;
	private static final int C_MSBS_MASK = 0xff00000;
	private static final int C_PART_MASK = 0x7f80000;
	private static final int C_CARRY_MASK = 0x8000000;
	private static final int C_MSBS_SHIFT = 20;
	private static final int C_PART_SHIFT = 19;
	private static final int C_CARRY_SHIFT = 27;
	
	EnumMap<ContextLabel, MQProbabilityTable> contextStates;
	SignificanceTable sigTable;

	
	int normalizedIntervalLength;	//A in the books
	int normalizedLowerBound;		//C in the books
	int tempByteBuffer;				//T bar in the books
	int countdownTimer;				//t bar in the books
	int codeBytesGenerated;			//L in the books
	
	
	private void initialize(int width, int height) {
		this.normalizedIntervalLength = DEFAULT_INTERVAL;
		this.normalizedLowerBound = 0;
		this.tempByteBuffer = 0;
		this.countdownTimer = 12;
		this.codeBytesGenerated = -1;

		this.contextStates = new EnumMap<ContextLabel, MQProbabilityTable>(ContextLabel.class);
		
		for (ContextLabel contextLabel: ContextLabel.values()) {
			this.contextStates.put(
					contextLabel, 
					new MQProbabilityTable(
							contextLabel.getDefaultState(), 
							contextLabel.getDefaultMPS()));
		}
		
		sigTable = new SignificanceTable(width, height);
	}
	
	
	/**
	 * Codes the given block into the given output, resetting
	 * statistics every time this function is called
	 */ 
	public void code(CodingBlock block, BitStream output) {
		this.initialize(block.getWidth(), block.getHeight());
		Logger.logger().log("Coder initialized!");
		
		int numberOfBitPlanes = block.getBitPlaneNumber();
		
		CodingPlane plane = block.getBitPlane(0);
		
		//fist bitplane is coded within a cleanup scheme
		Logger.logger().log("Performing initial Cleanup...");
		this.codeCleanup(plane, output);
		Logger.logger().log("Initial Cleanup done!");
		//rest of bitplanes are coded with a 3-step scheme
		for (int i = 1; i < numberOfBitPlanes; i++) {
			Logger.logger().log("Coding plane: " + i);
			plane = block.getBitPlane(i);
			this.codeSignificance(plane, output);
			Logger.logger().log("\tSignificance propagation done!");
			this.codeRefinement(plane, output);
			Logger.logger().log("\tCode refinement done!");
			this.codeCleanup(plane, output);
			Logger.logger().log("\tCleanup done!");
		}
		
		this.dumpRemainingBits(output);
	}
	
	/**
	 * Dump the extra bits that did not overflow the accumulators
	 * @param output
	 */
	private void dumpRemainingBits(BitStream output) {
		int nbits = 12 - this.countdownTimer;
		this.normalizedLowerBound <<= this.countdownTimer;
		while (nbits > 0) {
			this.transferByte(output);
			nbits -= this.countdownTimer;
			this.normalizedLowerBound <<= this.countdownTimer;
		}
		this.transferByte(output);
	}


	/**
	 * @param plane
	 * @param row
	 * @param column
	 * @return true if the 4-sample strip starting at the given position is all uncoded
	 */
	private boolean areAllUncodedAt(CodingPlane plane, int row, int column) {
		for (int j = 0; j < 4; j++) {
			if (plane.isCoded(j + row, column)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param plane
	 * @param row
	 * @param column
	 * @return true if the 4-sample strip starting at the given position has zero context
	 * for all of its samples
	 */
	private boolean areAllZeroContextAt(CodingPlane plane, int row, int column) {
		for (int j = 0; j < 4; j++) {
			if (this.sigTable.getSignificancePropagationContextAt(j + row, column, plane.getSubBand()) != ContextLabel.ZERO) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param plane
	 * @param row
	 * @param column
	 * @return the index of the first non zero bit in the 4-sample strip starting
	 * at the given position for the given plane, or -1 if all are zero.
	 */
	private int firstNonZeroBitAt(CodingPlane plane, int row, int column) {
		for (int j = 0; j < 4; j++) {
			if (plane.getSymbolAt(j + row, column) != Bit.BIT_ZERO) {
				return j;
			}
		}
		return -1;
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
				if (areAllUncodedAt(plane, s*4, i) && areAllZeroContextAt(plane, s*4, i)) {
					//make j point to the first non_zero bit
					j = firstNonZeroBitAt(plane, s*4, i);
					if (j == -1) {	//if j==-1 that means all are zero and we can run-length code it 
						this.codeSymbol(Bit.BIT_ZERO, ContextLabel.RUN_LENGTH, output);
						continue;
					} else {		//otherwise we failed.
						//encode a 1 to signal failure
						this.codeSymbol(Bit.BIT_ONE, ContextLabel.RUN_LENGTH, output);
						//encode the position of the first 1 and its sign (two bit quantity + sign bit)
						this.codeNumberWithContext(j, 2, ContextLabel.UNIFORM, output);
						this.codeSignificanceBit(plane, output, s*4 + j, i, true);
						//point j to the next one and then encode the rest as per usual
						j++;
					}	
				}
				//code whatever is left as usual
				for (; j < 4; j++) {
					this.codeSignificanceBit(plane, output, j + s*4, i, false);
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
			this.codeSymbol(symbol, this.sigTable.getSignificancePropagationContextAt(row, column, plane.getSubBand()), output);
		}
		//if it is one, it has become significant
		if (symbol == Bit.BIT_ONE) {
			//mark as significant (positive or negative)
			boolean isNegative = plane.isNegativeAt(row, column);
			this.sigTable.setSignificant(row, column, isNegative);
			//code sign (inverted if the context bit says so some cases)
			Pair<ContextLabel, Bit> signCtxBit = this.sigTable.getSignBitDecodingContextAt(row, column);
			Bit bit = signCtxBit.second();
			Bit sign = Bit.fromBoolean(isNegative);
			if (bit == Bit.BIT_ONE) { //xor the sign bit
				sign = sign.getInverse();
			}
			this.codeSymbol(sign, signCtxBit.first(), output);
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
		this.codeSymbol(plane.getSymbolAt(row, column), this.sigTable.getMagnitudeRefinementContextAt(row, column), output);
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
	
	
	/**
	 * Code an integer with the given context
	 * @param number
	 * @param bitsToCode: number of bits to be coded (taken from the LSBs and coded from MSB to LSB)
	 * @param context
	 * @param output
	 */
	private void codeNumberWithContext(int number, int bitsToCode, ContextLabel context, BitStream output) {
		for (int i = 0x1 << (bitsToCode - 1); i > 0; i>>=1) {
			this.codeSymbol(Bit.fromInteger(number & i), context, output);
		}
	}

	
	/**
	 * Codes the given symbol with the given context
	 * @param symbol
	 * @param context
	 */
	private void codeSymbol(Bit symbol, ContextLabel context, BitStream output) {
		//get the table associated to this context
		MQProbabilityTable table = this.contextStates.get(context);
		if (table == null) {
			throw new IllegalStateException("Did not find the table associated to context: " + context + " @MQCoder.codeSymbol");
		}
		//get the elements needed from said table
		Bit predictedSymbol = table.getPrediction();
		Bit adjustedSymbol = table.getPrediction();
		int normalizedProbability = table.getPEstimate();
		
		//adjust prediction
		this.normalizedIntervalLength -= normalizedProbability;
		if (this.normalizedIntervalLength < normalizedProbability) {
			adjustedSymbol = adjustedSymbol.getInverse(); //exchange MPS and LPS 
		}
		
		//adjust interval
		if (symbol == predictedSymbol) {
			//MPS gets the upper sub interval
			this.normalizedLowerBound += normalizedProbability;
		} else {
			//LPS gets the lower sub interval
			this.normalizedIntervalLength = normalizedProbability;
		}
		
		//change state
		if (this.normalizedIntervalLength < DEFAULT_INTERVAL) {
			//we got what we expected, change the state following the
			//most probable symbol state change
			if (symbol == predictedSymbol) {
				table.changeStateMPS();
			} 
			//we got screwed, check if the prediction needs to be changed
			//and either way change the state following the LPS change
			else { 
				if (table.needToSwap()) {
					table.changePrediction();
				}
				table.changeStateLPS();
			}
		}
		
		//renormalization shift
		while (this.normalizedIntervalLength < DEFAULT_INTERVAL) {
			this.normalizedIntervalLength <<= 1;
			this.normalizedLowerBound <<= 1;
			this.countdownTimer -= 1;
			if (this.countdownTimer == 0) {
				this.transferByte(output);
			}
		}
	}
	
	/**
	 * The buffer is full and a byte needs to be moved out to make room
	 * for the next
	 */
	private void transferByte(BitStream output) {
		//this is because jpeg uses the 0xff as a marker.
		//probably not necessary since we will use custom compression most likely
		if (this.tempByteBuffer == 0xff) {
			this.putByte(output);
			this.updateAfterByte(C_MSBS_MASK, C_MSBS_SHIFT, 7);
		} else {
			this.tempByteBuffer += BitTwiddling.maskAndShift(this.normalizedLowerBound, C_CARRY_MASK, C_CARRY_SHIFT);
			this.normalizedLowerBound &= (~C_CARRY_MASK);
			this.putByte(output);
			if (this.tempByteBuffer == 0xff) {
				this.updateAfterByte(C_MSBS_MASK, C_MSBS_SHIFT, 7);
			} else {
				this.updateAfterByte(C_PART_MASK, C_PART_SHIFT, 8);
			}
		}
	}
	
	/**
	 * Update inner elements after outputting a byte
	 * @param mask
	 * @param shift
	 * @param countdownReset
	 */
	private void updateAfterByte(int mask, int shift, int countdownReset) {
		this.tempByteBuffer = BitTwiddling.maskAndShift(this.normalizedLowerBound, mask, shift);
		this.normalizedLowerBound &= (~mask);
		this.countdownTimer = countdownReset;
	}
	
	/**
	 * Output and log the current byte buffer
	 */
	private void putByte(BitStream output) {
		if (this.codeBytesGenerated >= 0) {
			output.putBits(this.tempByteBuffer, 8, BitStream.BitStreamConstants.ORDERING_LEFTMOST_FIRST);
		}
		this.codeBytesGenerated++;
	}	
}
