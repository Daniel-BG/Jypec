package com.jypec.arithco;

/**
 * Store information about frequencies and such
 * @author Daniel
 */
public class ArithmeticCoderModel {
	
	/** maximum frequency allowed */
	protected final int maxFrequency;
	/** number of distinct elements being coded */
	private final int numberOfChars;
	/** number of symbols being coded */
	private final int numberOfSymbols;
	
	/** translation table from char to index. This translates from the input 
	 * raw element to an index within the frequency table. That is where the 
	 * element's frequency lies */
	protected  final int[] charToIndex;
	
	/** Translation table from index to char. Extra place used to accomodate
	 * the end of file index */
	protected  final int[] indexToChar;

	/** cummulative frequency table. Accomodates the cummulative frequencies of all
	 * symbols. Has one extra place to accomodate the zero value on the left, and 
	 * the total value on the right */
	protected final int[] cumFreq;
	
	/** symbol frequencies */
	protected final int[] freq;
	
	
	/**
	 * Create the arithmetic coder model for the given parameters
	 * @param maxFrequency
	 * @param numberOfChars
	 * @param numberOfSymbols
	 */
	public ArithmeticCoderModel(int maxFrequency, int numberOfChars) {
		this.maxFrequency = maxFrequency;
		this.numberOfChars = numberOfChars;
		this.numberOfSymbols = numberOfChars + 1;
		
		//set tables
		this.charToIndex = new int[numberOfChars];
		this.indexToChar = new int[numberOfSymbols + 1];
		this.cumFreq = new int[numberOfSymbols + 1];
		
		//model stuff
		this.freq = new int[numberOfSymbols + 1];
	}
	
	
	/**
	 * Reset statistics for the model
	 */
	public void startModel() {
		for (int i = 0; i < numberOfChars; i++) {
			charToIndex[i] = i+1;
			indexToChar[i+1] = i;
		}
		for (int i = 0; i <= numberOfSymbols; i++) {
			freq[i] = 1;
			cumFreq[i] = numberOfSymbols - i;
		}
		freq[0] = 0;
		
		if (cumFreq[0] > maxFrequency) {
			throw new IllegalStateException("Cummulative frequency exceeds maximum allowed!");
		}
	}
	
	
	/**
	 * Update the model assuming we found the given symbol
	 * @param symbol
	 */
	public void updateModel(int symbol) {
		//if max frequency is reached, normalize by dividing by two
		if (cumFreq[0] == maxFrequency) {
			int cum = 0;
			for (int i = numberOfSymbols; i >= 0; i--) {
				freq[i] = (freq[i] + 1)  / 2;
				cumFreq[i] = cum;
				cum += freq[i];
			}
		} else if (cumFreq[0] > maxFrequency) {
			System.out.println("ca");
		}
		
		int i;
		for (i = symbol; freq[i] == freq[i-1];) {
			i--;
		}
		if (i < symbol) {
			int chI = indexToChar[i];
			int chSymbol = indexToChar[symbol];
			indexToChar[i] = chSymbol;
			indexToChar[symbol] = chI;
			charToIndex[chI] = symbol;
			charToIndex[chSymbol] = i;
		}
		freq[i] += 1;
		while (i > 0) {
			i--;
			cumFreq[i] += 1;
		}
		
	}
}
