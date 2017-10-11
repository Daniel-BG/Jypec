package com.jypec.spiht;

import java.util.LinkedList;
import java.util.Queue;

import com.jypec.ebc.data.CodingBlock;
import com.jypec.util.Pair;

/**
 * Base with the shared stuff between the SPIHT coder and decoder
 * @author Daniel
 * @see SpihtCoder
 * @see SpihtDecoder
 */
public abstract class SpihtCoDecBase {
	
	/** List of Significant coefficients */
	protected Queue<Pair<TreeNode, Boolean>> lsc;
	/** List of Insignificant sets */
	protected Queue<Pair<TreeNode, SpihtSetType>> lis;
	/** List of Insignificant coefficients */
	protected Queue<TreeNode> lic;
	/** Shared x partitions across nodes */
	protected int[] partitionsX;
	/** Shared y partitions across nodes */
	protected int[] partitionsY;
	
	
	/**
	 * Initialize the coding lists and partition arrays prior to coding
	 * @param block
	 * @param partitionsX
	 * @param partitionsY
	 */
	public void initialize(CodingBlock block, int[] partitionsX, int[] partitionsY) {
		this.partitionsX = partitionsX;
		this.partitionsY = partitionsY;
		this.lsc = new LinkedList<Pair<TreeNode, Boolean>>();
		this.lic = new LinkedList<TreeNode>();
		this.initializeLic();
		this.lis = new LinkedList<Pair<TreeNode, SpihtSetType>>();
		this.initializeLis();
	}

	/**
	 * Initialize LIC with all nodes in the LL subBand
	 */
	private void initializeLic() {
		for (int i = 0; i < partitionsX[0]; i++) {
			for (int j = 0; j < partitionsY[0]; j++) {
				this.lic.add(new TreeNode(i, j, partitionsX, partitionsY));
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
				this.lis.add(new Pair<TreeNode, SpihtSetType>(tn, SpihtSetType.TYPE_A));
			}
		}		
	}
}
