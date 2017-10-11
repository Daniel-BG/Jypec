package com.jypec.spiht;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.ebc.data.CodingPlane;
import com.jypec.util.Pair;
import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitOutputStream;

/**
 * Spiht coding algorithm
 * @author Daniel
 */
public class SpihtCoder extends SpihtCoDecBase {

	/**
	 * Code the given block
	 * @param block block to code
	 * @param bos BitOutputStream where to write output data
	 * @param partitionsX partitions along the horizontal direction of the block
	 * @param partitionsY partitions along the vertical direction of the block
	 * @throws IOException 
	 */
	public void code(CodingBlock block, BitOutputStream bos, int[] partitionsX, int[] partitionsY) throws IOException {
		this.initialize(block, partitionsX, partitionsY);
		CodingPlane cp;
		int k = 0;
		while (k < block.getMagnitudeBitPlaneNumber()) {
			cp = block.getBitPlane(block.getMagnitudeBitPlaneNumber() - 1 - k); 
			this.codeSignificancePass(cp, bos);
			this.codeRefinementPass(cp, bos);
			k++;
		}
	}
		
	/**
	 * Perform a significance pass over the given plane, any bits output go to bos
	 * @param cp
	 * @param bos
	 * @throws IOException
	 */
	private void codeSignificancePass(CodingPlane cp, BitOutputStream bos) throws IOException {
		//for each entry in lic
		Iterator<TreeNode> itlic = lic.iterator();
		while (itlic.hasNext()) {
			TreeNode tn = itlic.next();
			//output its magnitude bit
			Bit bit = tn.getBitOf(cp);
			this.output(bit, bos);
			//if it is one
			if (bit == Bit.BIT_ONE) {
				//output its sign bit
				this.output(tn.getSignBitOf(cp), bos);
				//and move element to the list of significant coefficients
				this.lsc.add(new Pair<TreeNode, Boolean>(tn, false));
				itlic.remove();
			} 
		}
		
		Queue<Pair<TreeNode, SpihtSetType>> currentLis = lis;
		lis = new LinkedList<Pair<TreeNode, SpihtSetType>>();
		//keep exploring the lis, and the newly created lists, until no more are created
		do {
			Queue<Pair<TreeNode, SpihtSetType>> nextLis = exploreLis(currentLis, cp, bos);
			lis.addAll(currentLis);
			currentLis = nextLis;
		} while (!currentLis.isEmpty());
	}
	
	/**
	 * Perform the significance pass over a portion of the lis.
	 * Obsolete elements are deleted from the input list, and
	 * new elements returned as a new one
	 * @param currentLis
	 * @param cp
	 * @param bos
	 * @return
	 * @throws IOException
	 */
	private Queue<Pair<TreeNode, SpihtSetType>> exploreLis(Queue<Pair<TreeNode, SpihtSetType>> currentLis, CodingPlane cp, BitOutputStream bos) throws IOException {
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
					//output the descendant status (1 if at least one descendant is 1, 0 otherwise)
					boolean oneInDescendants = pct.first().oneInDescendants(cp);
					this.output(Bit.fromBoolean(oneInDescendants), bos);
					//if 1 was output
					if (oneInDescendants) {
						//for each child
						for (TreeNode c: pct.first().getChildren()) {
							//output its magnitude bit
							Bit bit = c.getBitOf(cp);
							this.output(bit, bos);
							//if zero, add to lic
							if (bit == Bit.BIT_ZERO) {
								lic.add(c);
							//if one, output sign and add to lsc
							} else {
								this.output(c.getSignBitOf(cp), bos);
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
					//output if it has a 1 in its grand children (skip 1 gen)
					boolean oneInGrandChildren = pct.first().oneInGrandChildren(cp);
					this.output(Bit.fromBoolean(oneInGrandChildren), bos);
					//if 1 was output
					if (oneInGrandChildren) {
						//delete the element from lis
						itlis.remove();
						//add all children as type a
						for (TreeNode c: pct.first().getChildren()) {
							lisAdditions.add(new Pair<TreeNode, SpihtSetType>(c, SpihtSetType.TYPE_A));
						}
					}
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}	
		
		return lisAdditions;
	}

	/**
	 * Perform a refinement pass over the given plane. Any bits output go to bos
	 * @param cp
	 * @param bos
	 * @throws IOException
	 */
	private void codeRefinementPass(CodingPlane cp, BitOutputStream bos) throws IOException {
		//for each element in the previous lsc
		for (Pair<TreeNode, Boolean> ptb: lsc) {
			//if already refined
			if (ptb.second()) {
				//output bit
				this.output(ptb.first().getBitOf(cp), bos);
			} else { //refine
				ptb.setSecond(true);
			}
		}
	}
	
	/**
	 * Output the given bit in the given bos. Omissible function
	 * but wrapper just in case
	 * @param b
	 * @param bos
	 * @throws IOException
	 */
	private void output(Bit b, BitOutputStream bos) throws IOException {
		bos.writeBit(b);
	}

}
