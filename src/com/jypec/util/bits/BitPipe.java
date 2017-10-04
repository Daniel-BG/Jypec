package com.jypec.util.bits;

import java.util.LinkedList;

/**
 * Queue for bits
 * @author Daniel
 *
 */
public class BitPipe {

	private static final int LEFTMOST_BIT_MASK = 0x1 << (Integer.SIZE - 1);
	
	//where to store bits if it gets too big
	LinkedList<Integer> storage;
	//output is where we are taking bits from, input where we are putting them in
	//bits might be taken from the input if they are asked for before the whole input fills up
	int output, input;
	//bits left in the output (to be output)
	//and bits left in the input (to be filled)
	int outputLeft, inputLeft;
	//current size (number of bits stored)
	int size;
		
	
	/**
	 * Create a BitPipe
	 */
	public BitPipe() {
		this.storage = new LinkedList<Integer>();
		this.size = 0;
		this.outputLeft = 0;
		this.inputLeft = Integer.SIZE;
	}
	
	/**
	 * @param bit the bit to put
	 */
	public void putBit(Bit bit) {
		this.unsafePutBit(bit.toInteger());
	}
	
	/**
	 * @param bit the bit (in integer form) to put
	 */
	public void putBit(int bit) {
		if (bit != 0 && bit != 1) {
			throw new IllegalStateException("Whoops @FIFOBitStream.putBit");
		}
		bit = Bit.normalize(bit);
		this.unsafePutBit(bit);
	}
	
	/**
	 * @param bit puts the given int as a bit assuming it is either
	 * a 0x0 or a 0x1, without checks
	 */
	public void unsafePutBit(int bit) {
		//if there is space in the input
		if (inputLeft > 0) {
			input <<= 1;
			input += bit;
			inputLeft--;
		//else push a value to the queue and restart the input
		} else {
			storage.addLast(input);
			input = bit;
			inputLeft = Integer.SIZE - 1;
		}
		this.size++;
	}
	
	/**
	 * @return the next bit in this pipe
	 */
	public Bit getBit() {
		return Bit.fromInteger(getBitAsInt());
	}
	
	/**
	 * @return the next bit in this pipe, as a 0x0 or 0x1
	 */
	public int getBitAsInt() {
		int res;
		//check if there are bits in the output
		if (outputLeft > 0) {
			res = removeBitFromOutput();
		}
		//check if there are bits in the intermediate storage
		else if (!storage.isEmpty()) {
			output = storage.removeFirst();
			outputLeft = Integer.SIZE;
			res = removeBitFromOutput();
		}
		//check if there are bits in the input
		else if (inputLeft < Integer.SIZE) {
			res = removeBitFromInput();
		}
		//nothing to return, throw exception
		else {
			throw new IndexOutOfBoundsException();
		}
		//return the bit
		return res;
	}
	
	/**
	 * Removes and returns the leftmost bit from the output
	 * @return
	 */
	private int removeBitFromOutput() {
		outputLeft--;
		int temp = output & LEFTMOST_BIT_MASK;
		output <<= 1;
		this.size--;
		return Bit.normalize(temp);
	}
	
	/**
	 * Removes and returns the leftmost (first to come in) element from the input
	 * @return
	 */
	private int removeBitFromInput() {
		int temp = input & (0x1 << (Integer.SIZE - 1 - inputLeft));
		inputLeft++;
		this.size--;
		return Bit.normalize(temp);
	}
	
	/**
	 * @return the number of bits stored in this pipe
	 */
	public int getNumberOfBits() {
		throw new UnsupportedOperationException();
	}
}
