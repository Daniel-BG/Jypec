package com.jypec.spiht;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.ebc.data.CodingPlane;
import com.jypec.util.Pair;
import com.jypec.util.bits.Bit;
import com.jypec.util.bits.BitOutputStream;

/**
 * Spiht algorithm
 * @author Daniel
 */
public class Spiht {
	
	private enum SpihtType {TYPE_A, TYPE_B}
	
	/** List of Significant coefficients */
	private Stack<TreeNode> lsc;
	/** Previous list of significant coefficients */
	private Stack<TreeNode> plsc;
	/** List of Insignificant sets */
	private Stack<Pair<TreeNode, SpihtType>> lis;
	/** List of Insignificant coefficients */
	private Stack<TreeNode> lic;
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
			this.significancePass(cp, bos);
			this.refinementPass(cp, bos);
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
		this.lsc = new Stack<TreeNode>();
		this.lic = new Stack<TreeNode>();
		this.initializeLic();
		this.lis = new Stack<Pair<TreeNode, SpihtType>>();
		this.initializeLis();
	}

	/**
	 * Initialize LIC with all nodes in the LL subBand
	 */
	private void initializeLic() {
		for (int i = 0; i < partitionsX[0]; i++) {
			for (int j = 0; j < partitionsY[0]; j++) {
				this.lic.add(new TreeNode(i, j));
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
	private void significancePass(CodingPlane cp, BitOutputStream bos) throws IOException {
		this.plsc = this.lsc;
		this.lsc = new Stack<TreeNode>();
		
		while (!lic.isEmpty()) {
			TreeNode c = lic.pop();
			Bit bit = c.getBitOf(cp);
			this.output(bit, bos);
			if (bit == Bit.BIT_ONE) {
				this.output(c.getSignBitOf(cp), bos);
				this.lsc.push(c);
			}
		}
		while (!lis.isEmpty()) {
			Pair<TreeNode, SpihtType> pct = lis.pop();
			switch(pct.second()) {
			case TYPE_A:
				boolean oneInDescendants = pct.first().oneInDescendants(cp);
				this.output(Bit.fromBoolean(oneInDescendants), bos);
				if (oneInDescendants) {
					for (TreeNode c: pct.first().children) {
						Bit bit = c.getBitOf(cp);
						this.output(bit, bos);
						if (bit == Bit.BIT_ZERO) {
							lic.push(c);
						} else {
							this.output(c.getSignBitOf(cp), bos);
							lsc.push(c);
						}
					}
					if (pct.first().hasGrandChildren()) {
						//comes back as a type b
						lis.push(new Pair<TreeNode, SpihtType>(pct.first(), SpihtType.TYPE_B));
					}
				}
				break;
			case TYPE_B:
				boolean oneInGrandChildren = pct.first().oneInGrandChildren(cp);
				this.output(Bit.fromBoolean(oneInGrandChildren), bos);
				if (oneInGrandChildren) {
					for (TreeNode c: pct.first().children) {
						lis.push(new Pair<TreeNode, SpihtType>(c, SpihtType.TYPE_A));
					}
				}
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}		
	}
	
	/**
	 * Perform a refinement pass over the given plane. Any bits output go to bos
	 * @param cp
	 * @param bos
	 * @throws IOException
	 */
	private void refinementPass(CodingPlane cp, BitOutputStream bos) throws IOException {
		while (!this.plsc.isEmpty()) {
			TreeNode c = this.plsc.pop();
			this.output(c.getBitOf(cp), bos);
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
	
	
	private class TreeNode {
		int x;
		int y;
		private List<TreeNode> children;
		
		public TreeNode(int absx, int absy) {
			this(absx, absy, SubBand.LL, 0);
		}

		private TreeNode(int absx, int absy, SubBand subBand, int step) {
			this.children = new ArrayList<TreeNode>();
			this.x = absx;
			this.y = absy;
			
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
			int cwidth = partitionsX[step] - partitionsX[step - 1];
			int nwidth = partitionsX[step + 1] - partitionsX[step];
			boolean shrinkLeft = nwidth % cwidth != 0;
			int cheight = partitionsY[step] - partitionsY[step - 1];
			int nheight = partitionsY[step + 1] - partitionsY[step];
			boolean shrinkUp = nheight % cheight != 0;
			
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
			this.addChildren(targetx, targety, subBand, step, shrinkLeft, shrinkUp, false, false);
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
			
			
			this.children.add(new TreeNode(targetx  , targety  , band, step + 1));
			if (!shrinkLeft) {
				this.children.add(new TreeNode(targetx+1, targety  , band, step + 1));
			}
			if (!shrinkUp) {
				this.children.add(new TreeNode(targetx  , targety+1, band, step + 1));
			}
			if (!shrinkLeft && ! shrinkUp) {
				this.children.add(new TreeNode(targetx+1, targety+1, band, step + 1));
			}
			if (growRight) {
				this.children.add(new TreeNode(targetx+2, targety  , band, step + 1));
				this.children.add(new TreeNode(targetx+2, targety+1, band, step + 1));
			}
			if (growDown) {
				this.children.add(new TreeNode(targetx  , targety+2, band, step + 1));
				this.children.add(new TreeNode(targetx+1, targety+2, band, step + 1));
			}
			if (growDown && growRight) {
				this.children.add(new TreeNode(targetx+2, targety+2, band, step + 1));
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
		 * @param bp
		 * @return The sign of the sample at this Node's coordinates in the given plane
		 */
		public Bit getSignBitOf(CodingPlane bp) {
			return Bit.fromBoolean(bp.isNegativeAt(this.y, this.x));
		}

		/**
		 * @param plane
		 * @return the bit of the plane at this Node's cordinates
		 */
		public Bit getBitOf(CodingPlane plane) {
			return plane.getSymbolAt(this.y, this.x);
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
