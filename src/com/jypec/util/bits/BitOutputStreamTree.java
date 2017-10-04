package com.jypec.util.bits;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree-like structure that stores bit streams in its nodes, to give
 * some sense to otherwise difficult to understand bit streams. <br>
 * Note that, as usual with trees, the same structure that represents 
 * the trees also represents the nodes, since there is no functional
 * distinction<br><br>
 * Extends the {@link BitOutputStream} functionality, restricting parent
 * {@link OutputStream} usage, but allowing the creation of a tree-like structure
 * which organizes the bits in bins. The internal bit-buffers can then 
 * be dumped via {@link #dumpInBitOutputStream(BitOutputStream)}
 * @author Daniel
 *
 */
public class BitOutputStreamTree extends BitOutputStream {
	
	private BitOutputStreamTree parent;
	private List<BitOutputStreamTree> children;
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
		private int lastBitsRead = 0;
		
		@Override
        public int readBitAsInt() throws IOException {
			int bit = bitPipe.getBitAsInt();
			this.lastBitsRead <<= 1;
			this.lastBitsRead += bit;
			return bit;
		}

        @Override
        public byte readByte() throws IOException {
        	return (byte) this.readNBitNumber(8);
        }
        
    	@Override
    	public int getLastReadBits() {
    		return this.lastBitsRead;
    	}
    	
    	@Override
    	public int available() {
    		return bitPipe.getNumberOfBits();
    	}
    	
    	@Override
    	public void close() {
    		//nothing
    	}
	};
	
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
	
	@Override
	public void close() {
		//pass
	}
	
	
	/**
	 * Build a bstn
	 * @param name the name of this tree
	 * @param spawnChildren if this tree should spawn children (less efficient)
	 */
	public BitOutputStreamTree(String name, boolean spawnChildren) {
		super(null);
		this.name = name;
		this.bitPipe = new BitPipe();
		this.spawnChildren = spawnChildren;
	}
	
	/**
	 * same as calling {@link #BitOutputStreamTree(null, false)}
	 */
	public BitOutputStreamTree() {
		this(null, false);
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
		return parent == null;
	}
	
	/**
	 * @return the parent of this tree, or null if not present
	 */
	public BitOutputStreamTree getParent() {
		return parent;
	}
	
	/**
	 * @return the root of this tree, which is itself if it has no parent
	 */
	public BitOutputStreamTree getRoot() {
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
	public BitOutputStreamTree getChild(int index) {
		return this.children.get(index);
	}
	
	/**
	 * Adds a child to this bstn, and returns it
	 * @param name the name of the child bstn
	 * @return the newly created child
	 */
	public BitOutputStreamTree addChild(String name) {
		if (!this.spawnChildren) {
			return this;
		}
		
		if (this.children == null) {
			this.children = new ArrayList<BitOutputStreamTree>();
		}
		BitOutputStreamTree bstn = new BitOutputStreamTree(name, this.spawnChildren);
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
			for (BitOutputStreamTree bstn: this.children) {
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
			for (BitOutputStreamTree bstn: this.children) {
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
			for (BitOutputStreamTree bstn: this.children) {
				res += bstn.layoutTreeStructure(indent + "\t");
			}
		}
		return res;
	}

}
