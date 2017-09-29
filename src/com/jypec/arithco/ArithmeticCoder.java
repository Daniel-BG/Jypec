package com.jypec.arithco;

import java.io.IOException;
import com.jypec.util.bits.Bit;
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
	/** Settings */
	
	/** Splitting points for the coding interval */
	private final long topValue;
	private final long firstQtr;
	private final long half;
	private final long thirdQtr;

	/** special symbol end of file. Not counted in {@link #numberOfChars} */
	private final int endOfFileSymbol;
	
	/** model the probabilities and such */
	private ArithmeticCoderModel acm;
	

	
	/**
	 * Create an arithmetic coder with default values
	 * @param codeValueBits number of bits the code value has
	 * @param numberOfChars number of different chars that can be input. A char must be in the range [0, numberOfChars) 
	 * @param maxFrequency maximum frequency allowed for any char
	 */
	public ArithmeticCoder(int codeValueBits, int numberOfChars, int maxFrequency) {
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
	 * Code the array of symbols given
	 * @param symbols
	 * @param bos the output stream where to output bits
	 */
	public void code(int[] symbols, BitOutputStream bos) {
		acm.startModel();
		//TODO initialize bos? (not necessary)
		startEncoding();
		
		for (int i = 0; i < symbols.length; i++) {
			int ch = symbols[i];
			int symbol = acm.charToIndex[ch];
			encodeSymbol(symbol, bos);
			acm.updateModel(symbol);
		}
		
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void doneOutputingBits(BitOutputStream bos) {
		try {
			bos.paddingFlush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
