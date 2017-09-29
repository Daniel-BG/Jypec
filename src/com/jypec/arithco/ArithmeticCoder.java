package com.jypec.arithco;

import java.io.IOException;
import java.util.ArrayList;

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
	/** Settings */
	
	/** Size of code values */
	private final int codeValueBits;
	
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
		//set generator values
		this.codeValueBits = codeValueBits;
		
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
	
	
	
	/**
	 * Decode the internal BitOutputStream into the list of integers that formed it
	 * @param bis the input stream from where to read bits
	 * @return the decoded list of integers
	 */
	public Integer[] decode(BitInputStream bis) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		acm.startModel();
		startInputingBits();
		startDecoding(bis);
		while(true) {
			int symbol = decodeSymbol(acm.cumFreq, bis);
			if (symbol == endOfFileSymbol) {
				System.out.println("EOF reached");
				break;
			}
			int ch = acm.indexToChar[symbol];
			System.out.println("Char decoded: " + ch);
			list.add(ch);
			acm.updateModel(symbol);
		}
		return list.toArray(new Integer[list.size()]);
	}
	
	
	private long value;
	
	private void startDecoding(BitInputStream bis) {
		value = 0;
		for (int i = 0; i < codeValueBits; i++) {
			value = value*2 + inputBit(bis);
		}
		low = 0;
		high = topValue;
	}
	
	
	private int decodeSymbol(int[] cumFreq, BitInputStream bis) {
		long range = high - low + 1L;
		long cum = (((value - low) + 1L)*cumFreq[0] - 1L)/range;
		
		int symbol;
		for (symbol = 1; cumFreq[symbol] > cum;) {
			symbol++;
		} 
		
		high = low + (range*cumFreq[symbol-1])/cumFreq[0] - 1;
		low = low + (range*cumFreq[symbol])/cumFreq[0];
		
		while(true) {
			if (high < half) {
				//nothing
			} else if (low >= half) {
				value -= half;
				low -= half;
				high -= half;
			} else if (low >= firstQtr && high < thirdQtr) {
				value -= firstQtr;
				low -= firstQtr;
				high -= firstQtr;
			} else {
				break;
			}
			low = 2*low;
			high = 2*high + 1;
			value = 2*value + inputBit(bis);
		}
		
		return symbol;
	}
	
	private int garbageBits;
	
	private void startInputingBits() {
		garbageBits = 0;
	}
	
	
	private int inputBit(BitInputStream bis) {
		int t = 0;
		boolean endOfFile = false;
		try {
			t = bis.readBitAsInt();
		} catch (IOException e) {
			endOfFile = true;
		}
		if (endOfFile) {
			//TODO mark somehow the end of the stream
			System.out.println("Garbage bit read!");
			garbageBits++;
			if (garbageBits > codeValueBits - 2) {
				throw new IllegalStateException("Bad input file");
			}
		}
		return t;
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
	
	/** ADAPTIVE MODEL */
	
	/** symbol frequencies */

	

	
	
}
