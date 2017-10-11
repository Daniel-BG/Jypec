package com.jypec.spiht;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.ebc.data.CodingPlane;
import com.jypec.util.Pair;
import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.debug.Logger;

/**
 * Spiht algorithm
 * @author Daniel
 */
public class Spiht {
	
	private enum SpihtType {TYPE_A, TYPE_B}
	
	/** List of Significant coefficients */
	private Queue<Pair<TreeNode, Boolean>> lsc;
	/** List of Insignificant sets */
	private Queue<Pair<TreeNode, SpihtType>> lis;
	/** List of Insignificant coefficients */
	private Queue<TreeNode> lic;
	/** Shared x partitions across nodes */
	private int[] partitionsX;
	/** Shared y partitions across nodes */
	private int[] partitionsY;
	
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
	 * Initialize the coding lists and partition arrays prior to coding
	 * @param block
	 * @param partitionsX
	 * @param partitionsY
	 */
	private void initialize(CodingBlock block, int[] partitionsX, int[] partitionsY) {
		this.partitionsX = partitionsX;
		this.partitionsY = partitionsY;
		this.lsc = new LinkedList<Pair<TreeNode, Boolean>>();
		this.lic = new LinkedList<TreeNode>();
		this.initializeLic();
		this.checkLic();
		this.lis = new LinkedList<Pair<TreeNode, SpihtType>>();
		this.initializeLis();
	}

	//TODO delete after testing
	private void checkLic() {
		boolean[][] hasIt = new boolean[350][350];
		Queue<TreeNode> q = new LinkedList<TreeNode>();
		q.addAll(lic);
		while (!q.isEmpty()) {
			TreeNode tn = q.poll();
			if (hasIt[tn.y][tn.x]) {
				Logger.getLogger().log("AlreadY! @ (" + tn.y + "," + tn.x + ")");
			}
			hasIt[tn.y][tn.x] = true;
			q.addAll(tn.children);
		}
		
		for (int i = 0; i < 350; i++) {
			for (int j = 0; j < 350; j++) {
				if (!hasIt[i][j])
					Logger.getLogger().log("Fails @ (" + i + "," + j + ")");
			}
		}
		
	}

	/**
	 * Initialize LIC with all nodes in the LL subBand
	 */
	private void initializeLic() {
		for (int i = 0; i < partitionsX[0]; i++) {
			for (int j = 0; j < partitionsY[0]; j++) {
				this.lic.add(new TreeNode(i, j, null));
			}
		}
	}

	/**
	 * Initialize LIS with all nodes in the LL subBand 
	 * that have children
	 */
	private void initializeLis() {
		for (TreeNode tn: this.lic) {
			if (tn.hasChildren()) {
				this.lis.add(new Pair<TreeNode, SpihtType>(tn, SpihtType.TYPE_A));
			}
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
		
		Queue<Pair<TreeNode, SpihtType>> currentLis = lis;
		lis = new LinkedList<Pair<TreeNode, SpihtType>>();
		//keep exploring the lis, and the newly created lists, until no more are created
		do {
			Queue<Pair<TreeNode, SpihtType>> nextLis = exploreLis(currentLis, cp, bos);
			lis.addAll(currentLis);
			currentLis = nextLis;
		} while (!currentLis.isEmpty());
	}
	
	private Queue<Pair<TreeNode, SpihtType>> exploreLis(Queue<Pair<TreeNode, SpihtType>> currentLis, CodingPlane cp, BitOutputStream bos) throws IOException {
		//new elements being added to lis
		Queue<Pair<TreeNode, SpihtType>> lisAdditions = new LinkedList<Pair<TreeNode, SpihtType>>();
		//for each element in the list of insignificant sets
		Iterator<Pair<TreeNode, SpihtType>> itlis = currentLis.iterator();
		while (itlis.hasNext()) {
			//get current element
			Pair<TreeNode, SpihtType> pct = itlis.next();
			switch(pct.second()) {
				//if the set is of type a
				case TYPE_A:
					//output the descendant status (1 if at least one descendant is 1, 0 otherwise)
					boolean oneInDescendants = pct.first().oneInDescendants(cp);
					this.output(Bit.fromBoolean(oneInDescendants), bos);
					//if 1 was output
					if (oneInDescendants) {
						//for each child
						for (TreeNode c: pct.first().children) {
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
							lisAdditions.add(new Pair<TreeNode, SpihtType>(pct.first(), SpihtType.TYPE_B));
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
						for (TreeNode c: pct.first().children) {
							lisAdditions.add(new Pair<TreeNode, SpihtType>(c, SpihtType.TYPE_A));
						}
					}
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}	
		
		return lisAdditions;
	}

	private void decodeSignificancePass(CodingPlane cp, BitInputStream bis) throws IOException {
		//for each entry in lic
		Iterator<TreeNode> itlic = lic.iterator();
		while (itlic.hasNext()) {
			TreeNode tn = itlic.next();
			//input its magnitude bit
			Bit bit = this.input(bis);
			tn.setBitOf(cp, bit);
			//if it is one
			if (bit == Bit.BIT_ONE) {
				//set its sign bit
				tn.setSignBitOf(cp, this.input(bis));
				//and move element to the list of significant coefficients
				this.lsc.add(new Pair<TreeNode, Boolean>(tn, false));
				itlic.remove();
			} 
		}
		
		Queue<Pair<TreeNode, SpihtType>> currentLis = lis;
		lis = new LinkedList<Pair<TreeNode, SpihtType>>();
		//keep exploring the lis, and the newly created lists, until no more are created
		do {
			Queue<Pair<TreeNode, SpihtType>> nextLis = exploreLis(currentLis, cp, bis);
			lis.addAll(currentLis);
			currentLis = nextLis;
		} while (!currentLis.isEmpty());
	}
	
	private Queue<Pair<TreeNode, SpihtType>> exploreLis(Queue<Pair<TreeNode, SpihtType>> currentLis, CodingPlane cp, BitInputStream bis) throws IOException {
		//new elements being added to lis
		Queue<Pair<TreeNode, SpihtType>> lisAdditions = new LinkedList<Pair<TreeNode, SpihtType>>();
		//for each element in the list of insignificant sets
		Iterator<Pair<TreeNode, SpihtType>> itlis = currentLis.iterator();
		while (itlis.hasNext()) {
			//get current element
			Pair<TreeNode, SpihtType> pct = itlis.next();
			switch(pct.second()) {
				//if the set is of type a
				case TYPE_A:
					//get the descendant status (1 if at least one descendant is 1, 0 otherwise)
					boolean oneInDescendants = this.input(bis).toBoolean(); 
					//if 1 was input
					if (oneInDescendants) {
						//for each child
						for (TreeNode c: pct.first().children) {
							//input its magnitude bit
							Bit bit = this.input(bis);
							c.setBitOf(cp, bit);
							//if zero, add to lic
							if (bit == Bit.BIT_ZERO) {
								lic.add(c);
							//if one, input sign and add to lsc
							} else {
								c.setSignBitOf(cp, this.input(bis));
								lsc.add(new Pair<TreeNode, Boolean>(c, false));
							}
						}
						//remove the element
						itlis.remove();
						//if it has grand children, comes back as a type b
						if (pct.first().hasGrandChildren()) {
							lisAdditions.add(new Pair<TreeNode, SpihtType>(pct.first(), SpihtType.TYPE_B));
						} 
					}
					break;
				case TYPE_B:
					//input if it has a one in its grandchildren
					boolean oneInGrandChildren = this.input(bis).toBoolean();
					//if 1 was input
					if (oneInGrandChildren) {
						//add all children as type a
						for (TreeNode c: pct.first().children) {
							lisAdditions.add(new Pair<TreeNode, SpihtType>(c, SpihtType.TYPE_A));
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
	
	
	private void decodeRefinementPass(CodingPlane cp, BitInputStream bis) throws IOException {
		//for each element in the previous lsc
		for (Pair<TreeNode, Boolean> ptb: lsc) {
			//if already refined
			if (ptb.second()) {
				//input bit
				ptb.first().setBitOf(cp, this.input(bis));
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
	
	/**
	 * Input a bit from the inputStream
	 * @param bis
	 * @return
	 * @throws IOException 
	 */
	private Bit input(BitInputStream bis) throws IOException {
		return bis.readBit();
	}

	
	
	private class TreeNode {
		int x;
		int y;
		private List<TreeNode> children;
		@SuppressWarnings("unused")
		private TreeNode parent; //TODO delete after testing, unncessary data
		
		public TreeNode(int absx, int absy, TreeNode parent) {
			this(absx, absy, SubBand.LL, 0, parent);
		}

		private TreeNode(int absx, int absy, SubBand subBand, int step, TreeNode parent) {
			this.children = new ArrayList<TreeNode>();
			this.x = absx;
			this.y = absy;
			this.parent = parent;
			
			//check that it is possible to do this
			if (partitionsX[0] == 1 || partitionsY[0] == 1) {
				throw new IllegalArgumentException("Cannot build a tree with a first partition size of 1 in either direction");
			}
			if (partitionsX.length != partitionsY.length) {
				//can probably do something about this and allow different number of partitions, for now ignore
				throw new IllegalArgumentException("The number of partitions must be the same in both directions");
			}
			if (step == 0) {
				this.initializeRootNodeChildren();
			}  else {
				this.initializeBranchNodeChildren(subBand, step);
			}
		}
		
		/**
		 * Initialize a branch node
		 * @param absx
		 * @param absy
		 * @param subBand
		 * @param step
		 */
		private void initializeBranchNodeChildren(SubBand subBand, int step) {
			if (step >= partitionsX.length - 1) {
				return; //basically do not add children
			}
			
			//find out partition offset
			int localx, localy;
			switch(subBand) {
				case HH:
					localx = this.x - partitionsX[step - 1];
					localy = this.y - partitionsY[step - 1];
					break;
				case HL:
					localx = this.x - partitionsX[step - 1];
					localy = this.y;
					break;
				case LH:
					localx = this.x;
					localy = this.y - partitionsY[step - 1];
					break;
				case LL:
				default:
					throw new UnsupportedOperationException();
			}
			//precalc some flags
			int cwidth, nwidth, cheight, nheight;
			switch(subBand) {
				case HH:
					cwidth = partitionsX[step] - partitionsX[step - 1];
					nwidth = partitionsX[step + 1] - partitionsX[step];
					cheight = partitionsY[step] - partitionsY[step - 1];
					nheight = partitionsY[step + 1] - partitionsY[step];
					break;
				case HL:
					cwidth = partitionsX[step] - partitionsX[step - 1];
					nwidth = partitionsX[step + 1] - partitionsX[step];
					cheight = partitionsY[step - 1];
					nheight = partitionsY[step];
					break;
				case LH:
					cwidth = partitionsX[step - 1];
					nwidth = partitionsX[step];
					cheight = partitionsY[step] - partitionsY[step - 1];
					nheight = partitionsY[step + 1] - partitionsY[step];
					break;
				case LL:
				default:
					throw new UnsupportedOperationException();
			}

			boolean shrinkLeft = false, shrinkUp = false, growRight = false, growDown = false;
			if (nwidth > 2*cwidth && localx == cwidth - 1) {
				growRight = true;
			}
			if (nwidth < 2*cwidth && localx == cwidth - 1) {
				shrinkLeft = true;
			}
			if (nheight > 2*cheight && localy == cheight - 1) {
				growDown = true;
			}
			if (nheight < 2*cheight && localy == cheight - 1) {
				shrinkUp = true;
			}
			
			/** Adjust top left corner of children cluster */
			int targetx, targety;
			switch(subBand) {
				case HL:
					targetx = partitionsX[step] + localx * 2;
					targety = localy * 2;
					break;
				case LH:
					targetx = localx * 2;
					targety = partitionsY[step] + localy * 2;
					break;
				case HH:
					targetx = partitionsX[step] + localx * 2;
					targety = partitionsY[step] + localy * 2;
					break;
				default:
					throw new UnsupportedOperationException();
			}
			
			/** Add children */
			this.addChildren(targetx, targety, subBand, step, shrinkLeft, shrinkUp, growDown, growRight);
		}

		/**
		 * Initialize this Object as a root node (pertaining to the LL subband)
		 * @param absx
		 * @param absy
		 * @param subBand
		 * @param step
		 */
		private void initializeRootNodeChildren() {
			//find out partition offset
			boolean localoddx = this.x % 2 != 0;
			boolean localoddy = this.y % 2 != 0;
			if (!localoddx && !localoddy) { //if even, no children, thats how life works
				return;
			}
			
			//precalc some flags
			boolean oddx = partitionsX[0] % 2 != 0;
			boolean oddy = partitionsY[0] % 2 != 0;
			
			/** Check if the children set has to grow or shrink in both directions */
			boolean growRight = false, growDown = false;
			boolean shrinkUp = false, shrinkLeft = false;
			if (oddx) {
				if (x == partitionsX[0] - 1) {
					shrinkLeft = true;
				} else if (x == partitionsX[0] - 2) {
					growRight = true;
				}
			}
			if (oddy) {
				if (y == partitionsY[0] - 1) {
					shrinkUp = true;
				} else if (y == partitionsY[0] - 2) {
					growDown = true;
				}
			}
			
			/** Calculate target position of children */
			int targetx, targety;

			if (localoddx) {
				targetx = partitionsX[0] + this.x - (this.x % 2);
			} else {
				targetx = this.x;
			}
			if (localoddy) {
				targety = partitionsY[0] + this.y - (this.y % 2);
			} else {
				targety = this.y;
			}
			
			/** Calculate which subband we point to, and add children */
			SubBand band = getBand(localoddx, localoddy);
			this.addChildren(targetx, targety, band, 0, shrinkLeft, shrinkUp, growDown, growRight); //passing lots of params to avoid extra memory used for node fields
		}
		
		
		/**
		 * Adds the children. One is always added, and up to 9 depending on boundary conditions.
		 * Usually, images with side a multiple of 2^(number of wavelet passes), the number of
		 * children is four.
		 * @param targetx
		 * @param targety
		 * @param band
		 * @param step
		 * @param shrinkLeft
		 * @param shrinkUp
		 * @param growDown
		 * @param growRight
		 */
		private void addChildren(int targetx, int targety, SubBand band, int step, boolean shrinkLeft, boolean shrinkUp, boolean growDown, boolean growRight) {
			if (step >= partitionsX.length - 1) {
				return; //cannot have children
			}
			
			
			this.children.add(new TreeNode(targetx  , targety  , band, step + 1, this));
			if (!shrinkLeft) {
				this.children.add(new TreeNode(targetx+1, targety  , band, step + 1, this));
			}
			if (!shrinkUp) {
				this.children.add(new TreeNode(targetx  , targety+1, band, step + 1, this));
			}
			if (!shrinkLeft && ! shrinkUp) {
				this.children.add(new TreeNode(targetx+1, targety+1, band, step + 1, this));
			}
			if (growRight) {
				this.children.add(new TreeNode(targetx+2, targety  , band, step + 1, this));
				if (!shrinkUp) {
					this.children.add(new TreeNode(targetx+2, targety+1, band, step + 1, this));
				}
			}
			if (growDown) {
				this.children.add(new TreeNode(targetx  , targety+2, band, step + 1, this));
				if (!shrinkLeft) {
					this.children.add(new TreeNode(targetx+1, targety+2, band, step + 1, this));
				}
			}
			if (growDown && growRight) {
				this.children.add(new TreeNode(targetx+2, targety+2, band, step + 1, this));
			}
		}

		/**
		 * For the nodes in the LL band, this returns which subBand their children belong
		 * to. For nodes in other bands their children always are on the same subBand
		 * @param oddx
		 * @param oddy
		 * @return
		 */
		private SubBand getBand(boolean oddx, boolean oddy) {
			if (oddx && oddy) {
				return SubBand.HH;
			} else if (oddx && !oddy) {
				return SubBand.HL;
			} else if (!oddx & oddy) {
				return SubBand.LH;
			} else {
				return SubBand.LL;
			}
		}
		
		/** BOOLEAN functions to guide the algorithm */
		
		/**
		 * @return <code>true</code> if this node has >= 1 child
		 */
		public boolean hasChildren() {
			return !children.isEmpty();
		}
		
		/**
		 * @return <code>true</code> if this node has >= 1 child with >= 1 child
		 */
		public boolean hasGrandChildren() {
			for (TreeNode tn: this.children) {
				if (tn.hasChildren()) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * @param plane
		 * @return The sign of the sample at this Node's coordinates in the given plane
		 */
		public Bit getSignBitOf(CodingPlane plane) {
			return Bit.fromBoolean(plane.isNegativeAt(this.y, this.x));
		}

		/**
		 * @param plane
		 * @return the bit of the plane at this Node's cordinates
		 */
		public Bit getBitOf(CodingPlane plane) {
			return plane.getSymbolAt(this.y, this.x);
		}
		
		/**
		 * Set the given bit in this {@link TreeNode}'s position in the plane
		 * @param plane
		 * @param bit
		 */
		public void setBitOf(CodingPlane plane, Bit bit) {
			plane.setSymbolAt(this.y, this.x, bit);
		}

		/**
		 * Set the given sign in this {@link TreeNode}'s position in the plane
		 * @param plane
		 * @param sign
		 */
		public void setSignBitOf(CodingPlane plane, Bit sign) {
			plane.setSignAt(this.y, this.x, sign);			
		}

		/**
		 * @param plane
		 * @return true if there is at least a ONE bit in one of this nodes descendants in the given plane
		 */
		public boolean oneInDescendants(CodingPlane plane) {
			for (TreeNode tn: this.children) {
				if (tn.getBitOf(plane) == Bit.BIT_ONE) {
					return true;
				}
				if (tn.oneInDescendants(plane)) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * @param plane
		 * @return true if there is at least a ONE bit in one of this nodes descendants of in the given plane.
		 * Direct descendants are not looked into, only grandchildren and beyond
		 */
		public boolean oneInGrandChildren(CodingPlane plane) {
			for (TreeNode tn: this.children) {
				if (tn.oneInDescendants(plane)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			String res = "(" + this.y + "," + this.x + "): {";
			for (TreeNode tn: this.children) {
				res += tn.toString() + ";";
			}
			res += "}";
			return res;
		}
	}

}
