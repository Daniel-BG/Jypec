package com.jypec.util;

import java.util.LinkedList;

/**
 * Implements a BitStream in a FIFO way.
 * Bits are shifted from the right to a bit queue, and taken from the left.
 * This is encapsulated in an integer queue.
 * @author Daniel
 *
 */
public class FIFOBitStream implements BitStream {
	private static final int LEFTMOST_BIT_MASK = 0x1 << (Integer.SIZE - 1);
	private static final int RIGHTMOST_BIT_MASK = 0x1;
	
	
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
	//stores the last read bits
	private int lastReadBits;
	
	/**
	 * Create a FIFO bit stream ready for use
	 */
	public FIFOBitStream() {
		this.storage = new LinkedList<Integer>();
		this.size = 0;
		this.outputLeft = 0;
		this.inputLeft = Integer.SIZE;
		this.lastReadBits = 0;
	}
	
	
	@Override
	public Bit getBit() {
		return Bit.fromInteger(this.getBitAsInt());
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
	
	@Override
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
		//update the latest read bits
		this.lastReadBits <<= 1;
		this.lastReadBits |= res;
		//return the bit
		return res;
	}

	
	@Override
	public void putBit(int bit) {
		if (bit != 0 && bit != 1) {
			throw new IllegalStateException("Whoops @FIFOBitStream.putBit");
		}
		bit = Bit.normalize(bit);
		
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

	@Override
	public void putBit(Bit bit) {
		this.putBit(bit.toInteger());
	}

	@Override
	public void putBits(int bits, int quantity, BitStreamConstants ordering) {
		switch (ordering) {
		case ORDERING_LEFTMOST_FIRST:
			//adjust the bits so that the first one is in the leftmost position
			bits <<= Integer.SIZE - quantity;
			for (int i = 0; i < quantity; i++) {
				putBit(Bit.fromInteger(bits & LEFTMOST_BIT_MASK));
				bits <<= 1;
			}
			break;
		case ORDERING_RIGHTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				putBit(Bit.fromInteger(bits & RIGHTMOST_BIT_MASK));
				bits >>= 1;
			}
			break;
		}
	}
	
	@Override
	public int getBits(int quantity, BitStreamConstants ordering) {
		int result = 0;
		switch (ordering) {
		case ORDERING_LEFTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				result >>= 1;
				result &= LEFTMOST_BIT_MASK;
				result |= getBitAsInt() << (Integer.SIZE - 1);
			}
			break;
		case ORDERING_RIGHTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				result <<= 1;
				result |= getBitAsInt();
			}
			break;
		}
		
		return result;
	}

	@Override
	public int getNumberOfBits() {
		return this.size;
	}


	@Override
	public int[] getStreamDimensions() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String dumpHex() {
		char[] symbols = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
		String output = "";
		int acc = 0;
		int cnt = 0;
		while (this.getNumberOfBits() > 0) {
			acc <<= 1;
			acc += this.getBitAsInt();
			cnt++;
			if (cnt == 4) {
				output += symbols[acc];
				acc = 0;
				cnt = 0;
			}
		}
		if (cnt > 0) {
			acc <<= 4 - cnt;
			output += symbols[acc];
		}
		
		return output;
	}


	@Override
	public int getLastReadBits() {
		return this.lastReadBits;
	}






}
