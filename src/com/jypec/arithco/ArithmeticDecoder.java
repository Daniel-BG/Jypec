package com.jypec.arithco;

import java.io.IOException;
import java.util.ArrayList;

import com.jypec.util.bits.BitInputStream;

/**
 * Dual class of {@link ArithmeticCoder}. Undoes what the coder did
 * @author Daniel
 */
public class ArithmeticDecoder {
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
	public ArithmeticDecoder(int codeValueBits, int numberOfChars, int maxFrequency) {
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
	
	
	
	private long low, high;
	
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
}
