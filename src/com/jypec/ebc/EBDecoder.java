package com.jypec.ebc;

import java.io.IOException;

import com.jypec.ebc.data.CodingBlock;
import com.jypec.ebc.data.CodingPlane;
import com.jypec.ebc.mq.ContextLabel;
import com.jypec.ebc.mq.MQArithmeticDecoder;
import com.jypec.ebc.mq.MQConstants;
import com.jypec.util.Pair;
import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitStreamConstants;
import com.jypec.util.debug.Profiler;

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
	 * @throws IOException 
	 */
	private void initialize(BitInputStream input, CodingBlock output) throws IOException {
		//decoder = new MQArithmeticDecoder(input);
		if (decoder == null) {
			decoder = new MQArithmeticDecoder(input);
		} else {
			decoder.initialize(input);
		}
		sigTable = new SignificanceTable(output.getWidth(), output.getHeight());
	}
	
	/**
	 * Decode the bitstream and put the result into the given CodeBlock.
	 * Parameters of the decoding process, such as block width, height or depth
	 * are expected to be set in the given codeblock, and the input BitStream is
	 * assumed to be pointing directly at the beggining of the coded data.
	 * @param input
	 * @param output
	 * @throws IOException 
	 */
	public void decode(BitInputStream input, CodingBlock output) throws IOException {
		this.initialize(input, output);
		int numberOfBitPlanes = output.getMagnitudeBitPlaneNumber();
		
		//decode over all planes
		for (int i = numberOfBitPlanes - 1; i >= 0; i--) {
			CodingPlane plane = output.getBitPlane(i);
			if (i < numberOfBitPlanes - 1) { //first plane has only cleanup
				this.decodeSignificance(input, plane);
				this.decodeRefinement(input, plane);
			}
			this.decodeCleanup(input, plane);
		}
		
		this.removeMarkEndOfStream(input);
	}
	

	private void decodeCleanup(BitInputStream input, CodingPlane plane) throws IOException {
		Profiler.getProfiler().profileStart();
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
		Profiler.getProfiler().profileEnd();
	}
	
	/**
	 * Decode a refinement bit at the given position
	 * @param input
	 * @param plane
	 * @param row
	 * @param column
	 * @throws IOException 
	 */
	private void decodeRefinementBit(BitInputStream input, CodingPlane plane, int row, int column) throws IOException {
		ContextLabel ctx = this.sigTable.getMagnitudeRefinementContextAt(row, column);
		Bit symbol = this.decoder.decodeSymbol(input, ctx);
		
		plane.setSymbolAt(row, column, symbol);
		plane.setCoded(row, column);
	}
	
	/**
	 * Decode a refinement pass from the input into the given plane
	 * @param input
	 * @param plane
	 * @throws IOException 
	 */
	private void decodeRefinement(BitInputStream input, CodingPlane plane) throws IOException {
		Profiler.getProfiler().profileStart();
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
		Profiler.getProfiler().profileEnd();
	}
	
	/**
	 * Decodes a bit from the input into the plane, using the significance propagation pass
	 * @param input
	 * @param plane
	 * @param row
	 * @param column
	 * @param onlySign if true, assume the magnitude was already decoded, and decode only the sign
	 * @throws IOException 
	 */
	private void decodeSignificanceBit(BitInputStream input, CodingPlane plane, int row, int column, boolean onlySign) throws IOException {
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
	 * @throws IOException 
	 */
	private void decodeSignificance(BitInputStream input, CodingPlane plane) throws IOException {
		Profiler.getProfiler().profileStart();
		//decode full strips within the block
		for (int s = 0; s < plane.getFullStripsNumber(); s++) {
			for (int i = 0; i < plane.getWidth(); i++) {
				for (int j = 0; j < 4; j++) {
					if (!this.sigTable.isSignificant(j + 4*s, i) && this.sigTable.getSignificancePropagationContextAt(j + 4*s, i, plane.getSubBand()) != ContextLabel.ZERO) {
						this.decodeSignificanceBit(input, plane, j + 4*s, i, false);
					}
				}
			}
		}
		
		//code remaining values at the end (not in 4-high strips)
		for (int i = 0; i < plane.getWidth(); i++) {
			for (int j = 0; j < plane.getLastStripHeight(); j++) {
				if (!this.sigTable.isSignificant(j, i) && this.sigTable.getSignificancePropagationContextAt(j + 4*plane.getFullStripsNumber(), i, plane.getSubBand()) != ContextLabel.ZERO) {
					this.decodeSignificanceBit(input, plane, j + 4*plane.getFullStripsNumber(), i, false);
				}
			}
		}
		Profiler.getProfiler().profileEnd();
	}
	
	/**
	 * Remove the last bits of the stream which mark the end of the block, if present
	 * @throws IOException 
	 */
	private void removeMarkEndOfStream(BitInputStream input) throws IOException {
		//the stream might end with extra bytes. Remove those if present
		while ((input.getLastReadBits() & MQConstants.CODE_MASK) != MQConstants.CODE_END_OF_BLOCK) {
			input.readBits(8, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
		}
	}

}
