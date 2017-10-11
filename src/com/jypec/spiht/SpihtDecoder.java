package com.jypec.spiht;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.jypec.ebc.data.CodingBlock;
import com.jypec.ebc.data.CodingPlane;
import com.jypec.util.Pair;
import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitInputStream;

/**
 * Spiht Decoding Algorithm
 * @author Daniel
 *
 */
public class SpihtDecoder extends SpihtCoDecBase {
	
	private boolean progressive;
	
	/**
	 * Create a Spiht Decoder
	 * @param progressive if true, decoding is done in a progressive manner estimating 
	 * at all times the expected result. If false, the decoding is assumed to be lossless
	 * and thus the value is not estimated until the full decoding procedure has ended
	 * and the coded value is then recovered exactly as coded
	 */
	public SpihtDecoder(boolean progressive) {
		this.progressive = progressive;
	}

	/**
	 * Decode the given inputstream into the given block
	 * @param block
	 * @param bis
	 * @param partitionsX
	 * @param partitionsY
	 * @throws IOException
	 */
	public void deCode(BitInputStream bis, CodingBlock block, int[] partitionsX, int[] partitionsY) throws IOException {
		this.initialize(block, partitionsX, partitionsY);
		CodingPlane cp;
		int k = 0;
		while (k < block.getMagnitudeBitPlaneNumber()) {
			cp = block.getBitPlane(block.getMagnitudeBitPlaneNumber() - 1 - k); 
			this.decodeSignificancePass(cp, bis);
			this.decodeRefinementPass(cp, bis);
			k++;
			
		}
	}
	
	/**
	 * Perform a significance pass
	 * @param cp
	 * @param bis
	 * @throws IOException
	 */
	private void decodeSignificancePass(CodingPlane cp, BitInputStream bis) throws IOException {
		//for each entry in lic
		Iterator<TreeNode> itlic = lic.iterator();
		while (itlic.hasNext()) {
			TreeNode tn = itlic.next();
			//input its magnitude bit
			Bit bit = this.input(bis);
			//if it is one
			if (bit == Bit.BIT_ONE) {
				//set its sign bit and the magnitude
				tn.setBitOf(cp, bit, this.progressive);
				tn.setSignBitOf(cp, this.input(bis));
				//and move element to the list of significant coefficients
				this.lsc.add(new Pair<TreeNode, Boolean>(tn, false));
				itlic.remove();
			} 
		}
		
		Queue<Pair<TreeNode, SpihtSetType>> currentLis = lis;
		lis = new LinkedList<Pair<TreeNode, SpihtSetType>>();
		//keep exploring the lis, and the newly created lists, until no more are created
		do {
			Queue<Pair<TreeNode, SpihtSetType>> nextLis = exploreLis(currentLis, cp, bis);
			lis.addAll(currentLis);
			currentLis = nextLis;
		} while (!currentLis.isEmpty());
	}
	
	/**
	 * Perform a partial lis exploration. The list is modified if nodes are deleted,
	 * and new ones returned in a new list for further exploration in a subsequent call
	 * @param currentLis
	 * @param cp
	 * @param bis
	 * @return
	 * @throws IOException
	 */
	private Queue<Pair<TreeNode, SpihtSetType>> exploreLis(Queue<Pair<TreeNode, SpihtSetType>> currentLis, CodingPlane cp, BitInputStream bis) throws IOException {
		//new elements being added to lis
		Queue<Pair<TreeNode, SpihtSetType>> lisAdditions = new LinkedList<Pair<TreeNode, SpihtSetType>>();
		//for each element in the list of insignificant sets
		Iterator<Pair<TreeNode, SpihtSetType>> itlis = currentLis.iterator();
		while (itlis.hasNext()) {
			//get current element
			Pair<TreeNode, SpihtSetType> pct = itlis.next();
			switch(pct.second()) {
				//if the set is of type a
				case TYPE_A:
					//get the descendant status (1 if at least one descendant is 1, 0 otherwise)
					boolean oneInDescendants = this.input(bis).toBoolean(); 
					//if 1 was input
					if (oneInDescendants) {
						//for each child
						for (TreeNode c: pct.first().getChildren()) {
							//input its magnitude bit
							Bit bit = this.input(bis);
							//if zero, add to lic
							if (bit == Bit.BIT_ZERO) {
								lic.add(c);
							//if one, input sign and add to lsc
							} else {
								//set sign and magnitude
								c.setBitOf(cp, bit, this.progressive);
								c.setSignBitOf(cp, this.input(bis));
								lsc.add(new Pair<TreeNode, Boolean>(c, false));
							}
						}
						//remove the element
						itlis.remove();
						//if it has grand children, comes back as a type b
						if (pct.first().hasGrandChildren()) {
							lisAdditions.add(new Pair<TreeNode, SpihtSetType>(pct.first(), SpihtSetType.TYPE_B));
						} 
					}
					break;
				case TYPE_B:
					//input if it has a one in its grandchildren
					boolean oneInGrandChildren = this.input(bis).toBoolean();
					//if 1 was input
					if (oneInGrandChildren) {
						//add all children as type a
						for (TreeNode c: pct.first().getChildren()) {
							lisAdditions.add(new Pair<TreeNode, SpihtSetType>(c, SpihtSetType.TYPE_A));
						}
						//delete the element from lis
						itlis.remove();
					}
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}	
		return lisAdditions;
	}
	
	private void decodeRefinementPass(CodingPlane cp, BitInputStream bis) throws IOException {
		//for each element in the previous lsc
		for (Pair<TreeNode, Boolean> ptb: lsc) {
			//if already refined
			if (ptb.second()) {
				//input bit
				ptb.first().setBitOf(cp, this.input(bis), this.progressive);
			} else { //refine
				ptb.setSecond(true);
			}
		}
	}
	
	/**
	 * Input a bit from the inputStream
	 * @param bis
	 * @return
	 * @throws IOException 
	 */
	private Bit input(BitInputStream bis) throws IOException {
		return bis.readBit();
	}
	
}
