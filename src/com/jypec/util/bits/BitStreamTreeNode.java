package com.jypec.util.bits;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Tree-like structure that stores bit streams in its nodes, to give
 * some sense to otherwise difficult to understand bit streams. <br>
 * Note that, as usual with trees, the same structure that represents 
 * the trees also represents the nodes, since there is no functional
 * distinction
 * @author Daniel
 *
 */
public class BitStreamTreeNode {
	
	private BitStreamTreeNode parent;
	private List<BitStreamTreeNode> children;
	private BitPipe bitPipe;
	private String name;
	
	/**
	 * Read bits from here (you read what has been written by {@link #bos})
	 * Native {@link InputStream} methods will not work since there is no
	 * underlying {@link InputStream}. Use only those provided
	 * by {@link BitInputStream}
	 */
	public BitInputStream bis = new BitInputStream(null) {

		@Override
		public int readBitAsInt() throws IOException {
			return bitPipe.getBitAsInt();
		}

		@Override
		public byte readByte() throws IOException {
			return (byte) this.readNBitNumber(8); 
		}
		
	};
	
	/**
	 * Write bits here (which you can later read with {@link #bis}.
	 * Native {@link OutputStream} methods will not work since there is no
	 * underlying {@link OutputStream}. Use only those
	 * provided by {@link BitOutputStream}
	 */
	public BitOutputStream bos = new BitOutputStream(null) {

		@Override
		public void writeBit(int bit) throws IOException {
			bitPipe.putBit(bit);
		}

		@Override
		public void writeByte(byte i) throws IOException {
			this.writeNBitNumber(i & 0xff, 8);
		}
		
	};
	
	
	/**
	 * Build a bstn
	 * @param name the name of this tree
	 */
	public BitStreamTreeNode(String name) {
		this.name = name;
		this.bitPipe = new BitPipe();
	}
	
	
	public String getName() {
		return this.name;
	}
	
	
	
	/**
	 * @return true if this is the root of the tree
	 */
	public boolean isRoot() {
		return parent != null;
	}
	
	/**
	 * @return the parent of this tree, or null if not present
	 */
	public BitStreamTreeNode getParent() {
		return parent;
	}
	
	/**
	 * @return the root of this tree, which is itself if it has no parent
	 */
	public BitStreamTreeNode getRoot() {
		if (this.isRoot()) {
			return this;
		}
		return this.parent.getRoot();
	}
	
	/**
	 * @return true if this tree has children
	 */
	public boolean hasChildren() {
		return (this.children != null && !this.children.isEmpty());
	}
	
	
	/**
	 * @return the number of children
	 */
	public int getNumberOfChildren() {
		if (this.children == null) {
			return 0;
		} else {
			return this.children.size();
		}
	}
	
	/**
	 * @param index the number of the child to be returned
	 * @return the requested child
	 */
	public BitStreamTreeNode getChild(int index) {
		return this.children.get(index);
	}
	
	
	/**
	 * @return the number of bits stored in this node
	 */
	public int getNodeBits() {
		return this.bitPipe.getNumberOfBits();
	}
	
	/**
	 * @return the number of bits stored in this tree (recursively looking in subtrees)
	 */
	public int getTreeBits() {
		int count = this.bitPipe.getNumberOfBits();
		if (this.hasChildren()) {
			for (BitStreamTreeNode bstn: this.children) {
				count += bstn.getTreeBits();
			}
		}
		return count;
	}
	

}
