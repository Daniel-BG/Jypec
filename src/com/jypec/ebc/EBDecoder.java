package com.jypec.ebc;

import com.jypec.ebc.data.CodingBlock;
import com.jypec.ebc.data.CodingPlane;
import com.jypec.ebc.mq.ContextLabel;
import com.jypec.ebc.mq.MQArithmeticDecoder;
import com.jypec.util.Bit;
import com.jypec.util.BitStream;
import com.jypec.util.Pair;

/**
 * MQDecoder dual to the MQcoder
 * @see EBCoder
 * @author Daniel
 *
 */
public class EBDecoder {

	
	private SignificanceTable sigTable;
	private MQArithmeticDecoder decoder;

	
	/**
	 * Initialize the decoder, make it ready to rumble
	 * @param width
	 * @param height
	 */
	private void initialize(BitStream input, CodingBlock output) {
		decoder = new MQArithmeticDecoder(input);
		sigTable = new SignificanceTable(output.getWidth(), output.getHeight());
	}
	
	/**
	 * Decode the bitstream and put the result into the given CodeBlock.
	 * Parameters of the decoding process, such as block width, height or depth
	 * are expected to be set in the given codeblock, and the input BitStream is
	 * assumed to be pointing directly at the beggining of the coded data.
	 * @param input
	 * @param output
	 */
	public void decode(BitStream input, CodingBlock output) {
		this.initialize(input, output);
		int numberOfBitPlanes = output.getBitPlaneNumber();
		
		//decode first cleanup pass
		CodingPlane plane = output.getBitPlane(numberOfBitPlanes - 1);
		this.decodeCleanup(input, plane);
		
		//decode rest of passes over the remaining planes
		for (int i = numberOfBitPlanes - 2; i >= 0; i--) {
			plane = output.getBitPlane(i);
			this.decodeSignificance(input, plane);
			this.decodeRefinement(input, plane);
			this.decodeCleanup(input, plane);
		}
	}
	
	
	private void decodeCleanup(BitStream input, CodingPlane plane) {
		//code full strips within the block
		for (int s = 0; s < plane.getFullStripsNumber(); s++) {
			for (int i = 0; i < plane.getWidth(); i++) {
				//start decoding here
				int j = 0;
				//check if we entered this strip in run_length mode
				if (plane.isStripUncoded(s*4, i) && this.sigTable.isStripZeroContext(plane.getSubBand(), s*4, i)) {
					//decode run_length bit
					Bit runLengthBit = this.decoder.decodeSymbol(input, ContextLabel.RUN_LENGTH);
					//this means we were on run-length mode and can decode all as zeroes
					if (runLengthBit == Bit.BIT_ZERO) {
						//TODO decode full column as zeroes
						for (j = 0; j < 4; j++) {
							plane.setSymbolAt(s*4 + j, i, Bit.BIT_ZERO);
						}
						continue;
					} else {
						//decode the index where the 1 was found
						j = this.decoder.decodeNumberWithContext(input, 2, ContextLabel.UNIFORM);
						plane.setSymbolAt(s*4 + j, i, Bit.BIT_ONE);
						this.decodeSignificanceBit(input, plane, s*4 + j, i, true);
						j++;
					}
				}
				//decode the rest
				for (; j < 4; j++) {
					if (!plane.isCoded(s*4 + j, i)) { 
						this.decodeSignificanceBit(input, plane, s*4 + j, i, false);
					}
				}
			}
		}
		
		//code remaining values at the end (not in 4-high strips)
		for (int i = 0; i < plane.getWidth(); i++) {
			for (int j = plane.getLastStripOffset(); j < plane.getLastStripHeight() + plane.getLastStripOffset(); j++) {
				if (!plane.isCoded(j, i)) {
					this.decodeSignificanceBit(input, plane, j, i, false);
				}
			}
		}
	}
	
	/**
	 * Decode a refinement bit at the given position
	 * @param input
	 * @param plane
	 * @param row
	 * @param column
	 */
	private void decodeRefinementBit(BitStream input, CodingPlane plane, int row, int column) {
		ContextLabel ctx = this.sigTable.getMagnitudeRefinementContextAt(row, column);
		Bit symbol = this.decoder.decodeSymbol(input, ctx);
		
		plane.setSymbolAt(row, column, symbol);
		plane.setCoded(row, column);
	}
	
	/**
	 * Decode a refinement pass from the input into the given plane
	 * @param input
	 * @param plane
	 */
	private void decodeRefinement(BitStream input, CodingPlane plane) {
		//decode full strips within the block
		for (int s = 0; s < plane.getFullStripsNumber(); s++) {
			for (int i = 0; i < plane.getWidth(); i++) {
				for (int j = 0; j < 4; j++) {
					if (!plane.isCoded(j + s*4, i) && this.sigTable.isSignificant(j + s*4, i)) {
						this.decodeRefinementBit(input, plane, j + s*4, i);
					}
				}
			}
		}
		
		//decode remaining values at the end (not in 4-high strips)
		for (int i = 0; i < plane.getWidth(); i++) {
			for (int j = 0; j < plane.getLastStripHeight(); j++) {
				if (!plane.isCoded(j + 4*plane.getFullStripsNumber(), i) && this.sigTable.isSignificant(j + 4*plane.getFullStripsNumber(), i)) {
					this.decodeRefinementBit(input, plane, j + 4*plane.getFullStripsNumber(), i);
				}
			}
		}
	}
	
	/**
	 * Decodes a bit from the input into the plane, using the significance propagation pass
	 * @param input
	 * @param plane
	 * @param row
	 * @param column
	 * @param onlySign: if true, assume the magnitude was already decoded, and decode only the sign
	 */
	private void decodeSignificanceBit(BitStream input, CodingPlane plane, int row, int column, boolean onlySign) {
		//decode the magnitude
		Bit magnitude = null;
		if (!onlySign) {
			magnitude = this.decoder.decodeSymbol(input, this.sigTable.getSignificancePropagationContextAt(row, column, plane.getSubBand()));
			plane.setSymbolAt(row, column, magnitude);
		}
		//decode the sign
		if (onlySign || magnitude == Bit.BIT_ONE) {
			Pair<ContextLabel, Bit> signCtxBit = this.sigTable.getSignBitDecodingContextAt(row, column);
			Bit xorBit = signCtxBit.second();
			Bit sign = this.decoder.decodeSymbol(input, signCtxBit.first());
			if (xorBit == Bit.BIT_ONE) {
				sign = sign.getInverse();
			}
			plane.setSignAt(row, column, sign);
			//if coding the sign, this coefficient just became significant. Set it
			this.sigTable.setSignificant(row, column, sign == Bit.BIT_ONE);
		}
		//mark as coded (well decoded in this case) so we can jump over next time
		plane.setCoded(row, column);
	}
	
	/**
	 * Decode a full significance pass from the input into the given plane
	 * @param input
	 * @param plane
	 */
	private void decodeSignificance(BitStream input, CodingPlane plane) {
		//decode full strips within the block
		for (int s = 0; s < plane.getFullStripsNumber(); s++) {
			for (int i = 0; i < plane.getWidth(); i++) {
				for (int j = 0; j < 4; j++) {
					if (this.sigTable.getSignificancePropagationContextAt(j + 4*s, i, plane.getSubBand()) != ContextLabel.ZERO) {
						this.decodeSignificanceBit(input, plane, j + 4*s, i, false);
					}
				}
			}
		}
		
		//code remaining values at the end (not in 4-high strips)
		for (int i = 0; i < plane.getWidth(); i++) {
			for (int j = 0; j < plane.getLastStripHeight(); j++) {
				if (this.sigTable.getSignificancePropagationContextAt(j + 4*plane.getFullStripsNumber(), i, plane.getSubBand()) != ContextLabel.ZERO) {
					this.decodeSignificanceBit(input, plane, j + 4*plane.getFullStripsNumber(), i, false);
				}
			}
		}
	}

}
