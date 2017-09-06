package com.jypec.ebc.mq;

import java.util.EnumMap;

import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitStream;
import com.jypec.util.bits.BitTwiddling;

/**
 * Implementation of the MQ arithmetic coder
 * @author Daniel
 *
 */
public class MQArithmeticCoder {

	
	
	//books = JPEG2000 image compression fundamentals. David S. Michael W.
	/** A in the books */
	private int normalizedIntervalLength;
	/** C in the books */
	private int normalizedLowerBound;
	/** T bar */
	private int tempByteBuffer;			
	/** t bar */
	private int countdownTimer;			
	/** L */
	private int codeBytesGenerated;		
	
	
	private EnumMap<ContextLabel, MQProbabilityTable> contextStates;
	
	
	/**
	 * Create and initialize the MQArithmeticCoder with the default values used for encoding
	 */
	public MQArithmeticCoder () {
		this.intialize();
	}
	
	/**
	 * Initializes default values for this coder
	 */
	public void intialize() {
		this.normalizedIntervalLength = MQConstants.DEFAULT_INTERVAL;
		this.normalizedLowerBound = 0;
		this.tempByteBuffer = 0;
		this.countdownTimer = MQConstants.COUNTDOWN_INIT;
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
		int nbits = MQConstants.COUNTDOWN_INIT - this.countdownTimer;
		this.normalizedLowerBound <<= this.countdownTimer;
		while (nbits > 0) {
			this.transferByte(output);
			nbits -= this.countdownTimer;
			this.normalizedLowerBound <<= this.countdownTimer;
		}
		this.transferByte(output);
	}
	
	/**
	 * adds bits marking the end of stream to the given bitstream
	 * @param output 
	 */
	public void markEndOfStream(BitStream output) {
		output.putBits(MQConstants.BYTE_MARKER, 8, BitStream.BitStreamConstants.ORDERING_LEFTMOST_FIRST);
		output.putBits(MQConstants.BYTE_END_OF_MQ_CODER, 8, BitStream.BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * Code an integer with the given context
	 * @param number
	 * @param bitsToCode number of bits to be coded (taken from the LSBs and coded from MSB to LSB)
	 * @param context
	 * @param output
	 */
	public void codeNumberWithContext(int number, int bitsToCode, ContextLabel context, BitStream output) {
		for (int i = 0x1 << (bitsToCode - 1); i > 0; i>>=1) {
			this.codeSymbol(Bit.fromInteger(number & i), context, output);
		}
	}
	
	
	/**
	 * Codes the given symbol with the given context outputting in the given output
	 * @param symbol
	 * @param context
	 * @param output 
	 */
	public void codeSymbol(Bit symbol, ContextLabel context, BitStream output) {
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
		if (this.normalizedIntervalLength < MQConstants.DEFAULT_INTERVAL) {
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
		while (this.normalizedIntervalLength < MQConstants.DEFAULT_INTERVAL) {
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
			this.updateAfterByte(MQConstants.C_MSBS_MASK, MQConstants.C_MSBS_SHIFT, 7);
		} else {
			this.tempByteBuffer += BitTwiddling.maskAndShift(this.normalizedLowerBound, MQConstants.C_CARRY_MASK, MQConstants.C_CARRY_SHIFT);
			this.normalizedLowerBound &= (~MQConstants.C_CARRY_MASK);
			this.putByte(output);
			if (this.tempByteBuffer == 0xff) {
				this.updateAfterByte(MQConstants.C_MSBS_MASK, MQConstants.C_MSBS_SHIFT, 7);
			} else {
				this.updateAfterByte(MQConstants.C_PART_MASK, MQConstants.C_PART_SHIFT, 8);
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
