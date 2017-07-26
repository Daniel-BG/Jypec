package com.jypec.ebc.mq;

import java.util.EnumMap;

import com.jypec.ebc.EBCoder;
import com.jypec.util.Bit;
import com.jypec.util.BitStream;
import com.jypec.util.BitTwiddling;
import com.jypec.util.BitStream.BitStreamConstants;

/**
 * Implementation of the MQ arithmetic decoder
 * @author Daniel
 *
 */
public class MQArithmeticDecoder {
	private static final int DEFAULT_INTERVAL = 0x8000;
	private static final int C_ACTIVE_MASK = 0xffff00;
	private static final int C_ACTIVE_SHIFT = 8;

	//books = JPEG2000 image compression fundamentals. David S. Michael W.
	private int normalizedIntervalLength;	//A in the books
	private int normalizedLowerBound;		//C in the books
	private int tempByteBuffer;				//T bar in the books
	private int countdownTimer;				//t bar in the books
	private int codeBytesRead;				//L in the books
	//specific to the decoder
	private int maxCodeBytesToRead = -1;
	private int lastByteRead;
	
	
	private EnumMap<ContextLabel, MQProbabilityTable> contextStates;
	
	/**
	 * Initialize the decoder, make it ready to rumble
	 * @param width
	 * @param height
	 */
	public MQArithmeticDecoder(BitStream input) {
		this.contextStates = new EnumMap<ContextLabel, MQProbabilityTable>(ContextLabel.class);
		
		for (ContextLabel contextLabel: ContextLabel.values()) {
			this.contextStates.put(
					contextLabel, 
					new MQProbabilityTable(
							contextLabel.getDefaultState(), 
							contextLabel.getDefaultMPS()));
		}
		
		this.lastByteRead = 0; 
		this.tempByteBuffer = 0;
		this.codeBytesRead = 0;
		this.normalizedLowerBound = 0;
		this.fillLSBs(input);
		this.normalizedLowerBound <<= this.countdownTimer;
		this.fillLSBs(input);
		this.normalizedLowerBound <<= 7;
		this.countdownTimer -= 7;
		this.normalizedIntervalLength = MQArithmeticDecoder.DEFAULT_INTERVAL;
	}
	
	/**
	 * Procedure that loads the next byte into C for decoding
	 */
	private void fillLSBs(BitStream input) {
		this.lastByteRead = input.getBits(8, BitStreamConstants.ORDERING_RIGHTMOST_FIRST);

		//Logger.logger().log("Removing: " + Integer.toHexString(this.lastByteRead));
		this.countdownTimer = 8;
		if (this.codeBytesRead == this.maxCodeBytesToRead || (this.tempByteBuffer == 0xff && this.lastByteRead > 0x8f)) {
			//this.normalizedLowerBound += 0xff;
		} else {
			if (this.tempByteBuffer == 0xff) {
				this.countdownTimer = 7;
			}
			this.tempByteBuffer = this.lastByteRead;
			this.codeBytesRead++;
			this.normalizedLowerBound += this.tempByteBuffer << (8 - this.countdownTimer);
		}
	}
	
	/**
	 * Decodes an unsigned integer int the input stream using the given context
	 * @param input
	 * @param bitsToDecode
	 * @param context
	 * @return
	 */
	public int decodeNumberWithContext(BitStream input, int bitsToDecode, ContextLabel context) {
		int res = 0;
		for (int i = 0; i < bitsToDecode; i++) {
			res <<= 1;
			res += this.decodeSymbol(input, context).toInteger();
		}
		return res;
	}
	
	/**
	 * Decodes and returns a symbol from the input stream with the given context.
	 * Pretty much the same as the encoding but reversed
	 * @see EBCoder#code(com.jypec.util.CodingBlock, BitStream)
	 * @param input
	 * @param context
	 * @return
	 */
	public Bit decodeSymbol(BitStream input, ContextLabel context) {
		//get the table associated to this context
		MQProbabilityTable table = this.contextStates.get(context);
		//get the elements from the table
		Bit prediction = table.getPrediction();
		int normalizedProbability = table.getPEstimate();
		//adjust prediction
		this.normalizedIntervalLength -= normalizedProbability;
		if (this.normalizedIntervalLength < normalizedProbability) {
			prediction = prediction.getInverse();
		}
		
		//adjust interval and get output
		Bit recoveredSymbol;
		int lowerBoundActive = BitTwiddling.maskAndShift(this.normalizedLowerBound, MQArithmeticDecoder.C_ACTIVE_MASK, MQArithmeticDecoder.C_ACTIVE_SHIFT);
		if (lowerBoundActive < normalizedProbability) {
			recoveredSymbol = prediction.getInverse();
			this.normalizedIntervalLength = normalizedProbability;
		} else {
			recoveredSymbol = prediction;
			lowerBoundActive -= normalizedProbability;
			this.normalizedLowerBound &= ~MQArithmeticDecoder.C_ACTIVE_MASK;
			this.normalizedLowerBound |= lowerBoundActive << MQArithmeticDecoder.C_ACTIVE_SHIFT;
		}
		
		//change state
		if (this.normalizedIntervalLength < MQArithmeticDecoder.DEFAULT_INTERVAL) {
			if (recoveredSymbol == table.getPrediction()) {
				table.changeStateMPS();
			} else {
				if (table.needToSwap()) {
					table.changePrediction();
				}
				table.changeStateLPS();
			}
		}
		
		//renormalize
		while (this.normalizedIntervalLength < MQArithmeticDecoder.DEFAULT_INTERVAL) {
			this.renormalizeOnce(input);
		}
		
		
		return recoveredSymbol;
	}

	/**
	 * Renormalize-one procedure that adjusts the intervals
	 * and reads from the input if necessary
	 * @param input
	 */
	private void renormalizeOnce(BitStream input) {
		if (this.countdownTimer == 0) {
			this.fillLSBs(input);
		} 
		this.normalizedIntervalLength <<= 1;
		this.normalizedLowerBound <<= 1;
		this.countdownTimer--;
	}
	
}
