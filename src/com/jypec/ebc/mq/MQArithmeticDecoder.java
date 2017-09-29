package com.jypec.ebc.mq;

import java.io.IOException;
import java.util.EnumMap;

import com.jypec.ebc.EBCoder;
import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitTwiddling;
import com.jypec.util.bits.BitStreamConstants;

/**
 * Implementation of the MQ arithmetic decoder
 * @author Daniel
 *
 */
public class MQArithmeticDecoder {


	//books = JPEG2000 image compression fundamentals. David S. Michael W.
	private int normalizedIntervalLength;	//A in the books
	private int normalizedLowerBound;		//C in the books
	private int tempByteBuffer;				//T bar in the books
	private int countdownTimer;				//t bar in the books
	private int codeBytesRead;				//L in the books
	//specific to the decoder
	private int maxCodeBytesToRead;
	private int lastByteRead;
	//flag indicating if the decoding has finished
	private boolean isFinished = false;
	
	
	private EnumMap<ContextLabel, MQProbabilityTable> contextStates;
	
	/**
	 * Initialize the decoder, make it ready to rumble
	 * @param input where to read from
	 * @throws IOException 
	 */
	public MQArithmeticDecoder(BitInputStream input) throws IOException {
		this.initialize(input);
	}
	
	/**
	 * Initializes this decoder starting inmmediately with the bits in the input BitStream
	 * @param input
	 * @throws IOException 
	 */
	public void initialize(BitInputStream input) throws IOException {
		this.contextStates = new EnumMap<ContextLabel, MQProbabilityTable>(ContextLabel.class);
		
		for (ContextLabel contextLabel: ContextLabel.values()) {
			this.contextStates.put(
					contextLabel, 
					new MQProbabilityTable(
							contextLabel.getDefaultState(), 
							contextLabel.getDefaultMPS()));
		}
		
		this.isFinished = false;
		this.lastByteRead = 0; 
		this.tempByteBuffer = 0;
		this.codeBytesRead = 0;
		this.maxCodeBytesToRead = -1;
		this.normalizedLowerBound = 0;
		this.fillLSBs(input);
		this.normalizedLowerBound <<= this.countdownTimer;
		this.fillLSBs(input);
		this.normalizedLowerBound <<= 7;
		this.countdownTimer -= 7;
		this.normalizedIntervalLength = MQConstants.DEFAULT_INTERVAL;
	}
	
	
	/**
	 * Procedure that loads the next byte into C for decoding
	 * @throws IOException 
	 */
	private void fillLSBs(BitInputStream input) throws IOException {
		this.countdownTimer = 8;
		if (this.codeBytesRead == this.maxCodeBytesToRead) {
			this.normalizedLowerBound += 0xff;
		} else {			
			this.lastByteRead = input.readBits(8, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
			if (this.tempByteBuffer == 0xff && this.lastByteRead >= MQConstants.SPECIAL_CODE_START_INTERVAL) {
				this.maxCodeBytesToRead = this.codeBytesRead; //max has been reached, automatically stop reading further
				this.normalizedLowerBound += 0xff;
				this.isFinished = true;
			} else {
				if (this.tempByteBuffer == 0xff) {
					this.countdownTimer = 7;
				}
				this.tempByteBuffer = this.lastByteRead;
				this.codeBytesRead++;
				this.normalizedLowerBound += this.tempByteBuffer << (8 - this.countdownTimer);
			}
		}
	}
	
	/**
	 * @param input
	 * @param bitsToDecode
	 * @param context
	 * @return the next unsigned integer in the input stream decoded using the given context 
	 * @throws IOException 
	 */
	public int decodeNumberWithContext(BitInputStream input, int bitsToDecode, ContextLabel context) throws IOException {
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
	 * @return the next bit decoded from input using context
	 * @throws IOException 
	 */
	public Bit decodeSymbol(BitInputStream input, ContextLabel context) throws IOException {
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
		int lowerBoundActive = BitTwiddling.maskAndShift(this.normalizedLowerBound, MQConstants.C_ACTIVE_MASK, MQConstants.C_ACTIVE_SHIFT);
		if (lowerBoundActive < normalizedProbability) {
			recoveredSymbol = prediction.getInverse();
			this.normalizedIntervalLength = normalizedProbability;
		} else {
			recoveredSymbol = prediction;
			lowerBoundActive -= normalizedProbability;
			this.normalizedLowerBound &= ~MQConstants.C_ACTIVE_MASK;
			this.normalizedLowerBound |= lowerBoundActive << MQConstants.C_ACTIVE_SHIFT;
		}
		
		//change state
		if (this.normalizedIntervalLength < MQConstants.DEFAULT_INTERVAL) {
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
		while (this.normalizedIntervalLength < MQConstants.DEFAULT_INTERVAL) {
			this.renormalizeOnce(input);
		}
		
		return recoveredSymbol;
	}

	/**
	 * Renormalize-one procedure that adjusts the intervals
	 * and reads from the input if necessary
	 * @param input
	 * @throws IOException 
	 */
	private void renormalizeOnce(BitInputStream input) throws IOException {
		if (this.countdownTimer == 0) {
			this.fillLSBs(input);
		} 
		this.normalizedIntervalLength <<= 1;
		this.normalizedLowerBound <<= 1;
		this.countdownTimer--;
	}

	/**
	 * @return true if the decoding has reached the bit flag
	 */
	public boolean isFinished() {
		return this.isFinished;
	}
	
}
