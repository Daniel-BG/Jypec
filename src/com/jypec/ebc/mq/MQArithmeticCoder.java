package com.jypec.ebc.mq;

import java.util.EnumMap;

import com.jypec.util.Bit;
import com.jypec.util.BitStream;
import com.jypec.util.BitTwiddling;
import com.jypec.util.debug.Logger;

/**
 * Implementation of the MQ arithmetic coder
 * @author Daniel
 *
 */
public class MQArithmeticCoder {
	private static final int DEFAULT_INTERVAL = 0x8000;
	private static final int C_MSBS_MASK = 0xff00000;
	private static final int C_PART_MASK = 0x7f80000;
	private static final int C_CARRY_MASK = 0x8000000;
	private static final int C_MSBS_SHIFT = 20;
	private static final int C_PART_SHIFT = 19;
	private static final int C_CARRY_SHIFT = 27;
	
	
	//books = JPEG2000 image compression fundamentals. David S. Michael W.
	private int normalizedIntervalLength;	//A in the books
	private int normalizedLowerBound;		//C in the books
	private int tempByteBuffer;				//T bar in the books
	private int countdownTimer;				//t bar in the books
	private int codeBytesGenerated;			//L in the books
	
	
	private EnumMap<ContextLabel, MQProbabilityTable> contextStates;
	
	
	public MQArithmeticCoder () {
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
	}
	
	/**
	 * Dump the extra bits that did not overflow the accumulators
	 * @param output
	 */
	public void dumpRemainingBits(BitStream output) {
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
	 * Code an integer with the given context
	 * @param number
	 * @param bitsToCode: number of bits to be coded (taken from the LSBs and coded from MSB to LSB)
	 * @param context
	 * @param output
	 */
	public void codeNumberWithContext(int number, int bitsToCode, ContextLabel context, BitStream output) {
		for (int i = 0x1 << (bitsToCode - 1); i > 0; i>>=1) {
			this.codeSymbol(Bit.fromInteger(number & i), context, output);
		}
	}
	
	
	/**
	 * Codes the given symbol with the given context
	 * @param symbol
	 * @param context
	 */
	public void codeSymbol(Bit symbol, ContextLabel context, BitStream output) {
		Logger.logger().log("Coding symbol: " + symbol + " w/ context: " + context);
		//get the table associated to this context
		MQProbabilityTable table = this.contextStates.get(context);
		//get the elements needed from said table
		Bit prediction = table.getPrediction();
		int normalizedProbability = table.getPEstimate();
		
		//adjust prediction
		this.normalizedIntervalLength -= normalizedProbability;
		if (this.normalizedIntervalLength < normalizedProbability) {
			prediction = prediction.getInverse(); //exchange MPS and LPS 
		}
		
		//adjust interval
		if (symbol == prediction) {
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
			if (symbol == table.getPrediction()) {
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
