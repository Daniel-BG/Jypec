package com.jypec.arithco;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Implement the arithmetic coder as per <br>
 * ARITHMETIC CODING FOR DATA COMPRESSION by
 * IAN H. WITTEN, RADFORD M. NEAL, and JOHN G. CLEARY 
 * (<a href="https://ftp.cs.nyu.edu/~roweis/csc310-2006/extras/p520-witten.pdf">link</a>)
 * <br>
 * Notation:  
 * <ul>
 * 		<li>a <b>char</b> is an element pertaining to the type of elements being coded </li>
 * 		<li>a <b>symbol</b> is any of the elements, plus the end of file symbol </li>
 * </ul>
 * 
 * @author Daniel
 *
 */
public class ArithmeticCoder {
	/** state */
	private enum State {BUILT, INITIALIZED, CODING, FINISHED};
	private State state; 
	
	
	/** Settings */
	private final int codeValueBits;
	private final int numberOfChars;
	
	/** Splitting points for the coding interval */
	private final long topValue;
	private final long firstQtr;
	private final long half;
	private final long thirdQtr;

	/** special symbol end of file. Not counted in {@link #numberOfChars} */
	private final int endOfFileSymbol;
	
	/** model the probabilities and such */
	private ArithmeticCoderModel acm;
	
	private ArithmeticDecoder ad;
	private BitOutputStream adbos;
	private ByteArrayOutputStream adbaos;

	
	/**
	 * Create an arithmetic coder with default values
	 * @param codeValueBits number of bits the code value has
	 * @param numberOfChars number of different chars that can be input. A char must be in the range [0, numberOfChars) 
	 * @param maxFrequency maximum frequency allowed for any char
	 */
	public ArithmeticCoder(int codeValueBits, int numberOfChars, int maxFrequency) {
		this.state = State.BUILT;
		this.codeValueBits = codeValueBits;
		this.numberOfChars = numberOfChars;
		//set interval values
		this.topValue = (1L << codeValueBits) - 1;
		this.firstQtr = topValue/4+1;
		this.half = 2*firstQtr;
		this.thirdQtr = 3*firstQtr;
		
		//set number of symbols and such
		this.endOfFileSymbol = numberOfChars + 1;
		
		//set model
		this.acm = new ArithmeticCoderModel(maxFrequency, numberOfChars);
	}
	
	
	
	/**
	 * Initializes this arithmetic coder and the underlying probability model
	 */
	public void initialize() {
		if (this.state == State.CODING) {
			System.err.println("You are initializing a coder that's still coding");
		}
		this.state = State.INITIALIZED;
		
		/** hack to get the mininum number of extra bits needed for decompression 
		 * delete if you find out how the math works */
		this.ad = new ArithmeticDecoder(this.codeValueBits, this.numberOfChars, acm.maxFrequency);
		this.adbaos = new ByteArrayOutputStream();
		this.adbos = new BitOutputStream(adbaos);
		/** end hack */
		
		acm.startModel();
		startEncoding();
	}
	
	/**
	 * Code the symbol given
	 * @param input the input to be coded
	 * @param bos the output stream where to output bits
	 */
	public void code(int input, BitOutputStream bos) {
		switch(this.state) {
		case INITIALIZED:
			this.cbits = bos.getBitsOutput(); //for padding later
		case CODING:
			break;
		case BUILT:
		case FINISHED:
		default:
			throw new IllegalStateException("Cannot code right now. Please first initialize the coder");
		}
		this.state = State.CODING;

		int symbol = acm.charToIndex[input];
		encodeSymbol(symbol, bos);
		acm.updateModel(symbol);		
	}
	
	/**
	 * @param input the array of symbols to be coded
	 * @param bos where to put the coded data
	 */
	public void code(int[] input, BitOutputStream bos) {
		for (int i = 0; i < input.length; i++) {
			this.code(input[i], bos);
		}
	}
	
	
	/**
	 * Finish the coding in the given output stream
	 * @param bos
	 */
	public void finishCoding(BitOutputStream bos) {
		if (this.state != State.CODING) {
			throw new IllegalStateException("Can only finish coding if we are coding");
		}
		this.state = State.FINISHED;
		
		encodeSymbol(endOfFileSymbol, bos);
		doneEncoding(bos);
		doneOutputingBits(bos);
	}
	
	private long low, high;
	private long bitsToFollow;
	
	private void startEncoding() {
		low = 0;
		high = topValue;
		bitsToFollow = 0;
	}
	
	private void encodeSymbol(int symbol, BitOutputStream bos) {
		long range;
		range = high - low + 1L;
		high = low + (range*acm.cumFreq[symbol-1])/acm.cumFreq[0] - 1L;
		low = low + (range*acm.cumFreq[symbol])/acm.cumFreq[0];
		while(true) {
			if (high < half) {
				bitPlusFollow(0, bos);
			} else if (low >= half) {
				bitPlusFollow(1, bos);
				low -= half;
				high -= half;
			} else if (low >= firstQtr && high < thirdQtr) {
				bitsToFollow += 1;
				low -= firstQtr;
				high -= firstQtr;
			} else {
				break;
			}
			low  = low*2;
			high = high*2 + 1;
		}
	}
	
	private void doneEncoding(BitOutputStream bos) {
		bitsToFollow += 1;
		if (low < firstQtr) {
			bitPlusFollow(0, bos);
		} else {
			bitPlusFollow(1, bos);
		}
	}
	
	private void bitPlusFollow(int bit, BitOutputStream bos) {
		outputBit(bit, bos);
		while(bitsToFollow > 0) {
			outputBit(Bit.fromInteger(bit).getInverse().toInteger(), bos);
			bitsToFollow -= 1;
		}
	}
	
	private void outputBit(int bit, BitOutputStream bos) {
		try {
			bos.writeBit(bit);
			adbos.writeBit(bit);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int cbits;
	
	/** this whole function is basically a hack to get the number of garbage bits 
	 * that will be later decoded, so that they can be put right now, and de decoder
	 * ends perfectly at the end of the codeword. Otherwise we don't know where the decoder ends, 
	 * and if the word is embedded in a stream, the decoder might overstep its ground. 
	 * @param bos
	 */
	private void doneOutputingBits(BitOutputStream bos) {
		try {
			this.adbos.paddingFlush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(adbaos.toByteArray());
		BitInputStream bis = new BitInputStream(bais);
		int bitsWritten = bos.getBitsOutput() - cbits;
		this.ad.decode(bis);
		int bitsRead = bis.getBitsInput();
		int garbage = this.ad.getGarbageBits();
		int totalGarbage = garbage+bitsRead - bitsWritten;
		
		try {
			bos.writeNBitNumber(0, totalGarbage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
