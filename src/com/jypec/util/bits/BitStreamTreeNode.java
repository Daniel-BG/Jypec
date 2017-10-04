package com.jypec.util.bits;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
	private boolean spawnChildren;
	
	/**
	 * Read bits from here (you read what has been written by {@link #bos})
	 * Native {@link InputStream} methods will not work since there is no
	 * underlying {@link InputStream}. Use only those provided
	 * by {@link BitInputStream}
	 */
	public final BitInputStream bis = new BitInputStream(null) {

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
	 * Write bits here (which you can later read with {@link #bis}).
	 * Native {@link OutputStream} methods will not work since there is no
	 * underlying {@link OutputStream}. Use only those
	 * provided by {@link BitOutputStream}
	 */
	public final BitOutputStream bos = new BitOutputStream(null) {

		@Override
		public void writeBit(int bit) throws IOException {
			if (hasChildren()) {
				throw new IllegalStateException("You shouldn't add bits to a tree node if it has children, since it is traversed in preorder and this would break it");
			}
			bitPipe.putBit(bit);
		}

		@Override
		public void writeByte(byte i) throws IOException {
			this.writeNBitNumber(i & 0xff, 8);
		}
		
		@Override
		public void paddingFlush() throws IOException {
			int bufferSize = this.getBitsOutput() % 8;
			//flush remaining bits padding with zeroes
			if (bufferSize > 0) {
				this.writeNBitNumber(0, 8 - bufferSize);
			}
		}
		
	};
	
	
	/**
	 * Build a bstn
	 * @param name the name of this tree
	 * @param spawnChildren if this tree should spawn children (less efficient)
	 */
	public BitStreamTreeNode(String name, boolean spawnChildren) {
		this.name = name;
		this.bitPipe = new BitPipe();
		this.spawnChildren = spawnChildren;
	}
	
	
	/**
	 * @return the name of this node
	 */
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
	 * Adds a child to this bstn, and returns it
	 * @param name the name of the child bstn
	 * @return the newly created child
	 */
	public BitStreamTreeNode addChild(String name) {
		if (!this.spawnChildren) {
			return this;
		}
		
		if (this.children == null) {
			this.children = new ArrayList<BitStreamTreeNode>();
		}
		BitStreamTreeNode bstn = new BitStreamTreeNode(name, this.spawnChildren);
		this.children.add(bstn);
		return bstn;
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
	
	
	/**
	 * @param target where to dump this tree's data (in PREORDER!)
	 * @throws IOException 
	 */
	public void dumpInBitOutputStream(BitOutputStream target) throws IOException {
		while (!this.bitPipe.isEmpty()) {
			target.writeBit(this.bitPipe.getBitAsInt());
		}
		if (this.hasChildren()) {
			for (BitStreamTreeNode bstn: this.children) {
				bstn.dumpInBitOutputStream(target);
			}
		}
	}
	
	
	/**
	 * @param indent
	 * @return the inner tree structure as a String for visual representation
	 */
	public String layoutTreeStructure(String indent) {
		if (indent == null) {
			indent = "";
		}
		String res = indent + this.name + " (" + this.getTreeBits() + ") (+" + this.getNodeBits() +")\n";
		if (this.hasChildren()) {
			for (BitStreamTreeNode bstn: this.children) {
				res += bstn.layoutTreeStructure(indent + "\t");
			}
		}
		return res;
	}

}
