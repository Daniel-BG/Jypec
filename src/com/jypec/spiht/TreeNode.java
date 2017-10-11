package com.jypec.spiht;

import java.util.ArrayList;
import java.util.List;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingPlane;
import com.jypec.util.bits.Bit;

/**
 * Node class used for the SPIHT algorithm.
 * @author Daniel
 * @see SpihtCoder
 * @see SpihtDecoder
 */
public class TreeNode {
	int x;
	int y;
	private List<TreeNode> children;
	
	/**
	 * Create a new TreeNode, which will also spawn any descendants as its children
	 * @param absx absolute x position
	 * @param absy absolute y position
	 * @param partitionsX partitions of the x coordinate
	 * @param partitionsY partitions of the y coordinate (must be of same length as partitionsX)
	 * @param parent 
	 */
	public TreeNode(int absx, int absy, int[] partitionsX, int[] partitionsY) {
		this(absx, absy, partitionsX, partitionsY, SubBand.LL, 0);
	}

	private TreeNode(int absx, int absy, int[] partitionsX, int[] partitionsY, SubBand subBand, int step) {
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
			this.initializeRootNodeChildren(partitionsX, partitionsY);
		}  else {
			this.initializeBranchNodeChildren(subBand, step, partitionsX, partitionsY);
		}
	}
	
	/**
	 * Initialize a branch node
	 * @param absx
	 * @param absy
	 * @param subBand
	 * @param step
	 */
	private void initializeBranchNodeChildren(SubBand subBand, int step, int[] partitionsX, int[] partitionsY) {
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
		this.addChildren(targetx, targety, partitionsX, partitionsY, subBand, step, shrinkLeft, shrinkUp, growDown, growRight);
	}

	/**
	 * Initialize this Object as a root node (pertaining to the LL subband)
	 * @param absx
	 * @param absy
	 * @param subBand
	 * @param step
	 */
	private void initializeRootNodeChildren(int[] partitionsX, int[] partitionsY) {
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
		this.addChildren(targetx, targety, partitionsX, partitionsY, band, 0, shrinkLeft, shrinkUp, growDown, growRight); //passing lots of params to avoid extra memory used for node fields
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
	private void addChildren(int targetx, int targety, int[] partitionsX, int[] partitionsY, SubBand band, int step, boolean shrinkLeft, boolean shrinkUp, boolean growDown, boolean growRight) {
		if (step >= partitionsX.length - 1) {
			return; //cannot have children
		}
		
		
		this.children.add(new TreeNode(targetx  , targety  , partitionsX, partitionsY, band, step + 1));
		if (!shrinkLeft) {
			this.children.add(new TreeNode(targetx+1, targety  , partitionsX, partitionsY, band, step + 1));
		}
		if (!shrinkUp) {
			this.children.add(new TreeNode(targetx  , targety+1, partitionsX, partitionsY, band, step + 1));
		}
		if (!shrinkLeft && ! shrinkUp) {
			this.children.add(new TreeNode(targetx+1, targety+1, partitionsX, partitionsY, band, step + 1));
		}
		if (growRight) {
			this.children.add(new TreeNode(targetx+2, targety  , partitionsX, partitionsY, band, step + 1));
			if (!shrinkUp) {
				this.children.add(new TreeNode(targetx+2, targety+1, partitionsX, partitionsY, band, step + 1));
			}
		}
		if (growDown) {
			this.children.add(new TreeNode(targetx  , targety+2, partitionsX, partitionsY, band, step + 1));
			if (!shrinkLeft) {
				this.children.add(new TreeNode(targetx+1, targety+2, partitionsX, partitionsY, band, step + 1));
			}
		}
		if (growDown && growRight) {
			this.children.add(new TreeNode(targetx+2, targety+2, partitionsX, partitionsY, band, step + 1));
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
	 * @param progressive if this flag is <code>true</code>, the bit is set and the value of the sample is estimated as
	 * half its possible range, for progressive decoding
	 */
	public void setBitOf(CodingPlane plane, Bit bit, boolean progressive) {
		plane.setSymbolAt(this.y, this.x, bit);
		if (progressive) {
			CodingPlane next = plane.nextPlane();
			if (next != null) {
				next.setSymbolAt(this.y, this.x, Bit.BIT_ONE);
			}
		}
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
	
	/**
	 * @return a list of the children of this node
	 */
	public List<TreeNode> getChildren() {
		return this.children;
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