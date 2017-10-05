package com.jypec.util.bits;

import java.io.FileOutputStream;
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
 * be dumped via {@link #dumpInBitOutputStream(BitOutputStream)} <br><br>
 * If you want this to behave like {@link BitOutputStream}, construct using
 * {@link #BitOutputStreamTree(OutputStream)} and ignore tree methods
 * @author Daniel
 *
 */
public class BitOutputStreamTree extends BitOutputStream {
	
	/** This allows this BOST to behave as a BitOutputStream to allow,
	 * amongst others, direct output to {@link FileOutputStream} */
	private OutputStream destination;
	/** Parent BOST for tree traversal purposes */
	private BitOutputStreamTree parent;
	/** List of children for tree traversal purposes */
	private List<BitOutputStreamTree> children;
	/** True if this BOST's {@link #addChild(String)} should spawn
	 * a new BOST, or false for returning <code>this</code> instead,
	 * which ignores the tree structure in favor of a more optimized code */
	private boolean spawnChildren;
	/** Name of the node for tagging the tree structure */
	private String name;
	
	/** Internal BitPipe, used if <code>destination == null</code> to store
	 * the bits output. Its contents can be emptied via {@link #bis} or a 
	 * call to {@link #dumpInBitOutputStream(BitOutputStream)} */
	private BitPipe bitPipe;
	/** Internal {@link BitInputStream} for outputting the bits sent here */
	private BitInputStream bis;
	
	
	/********************************/
	/**	CONSTRUCTORS/INITIALIZERS	*/
	/********************************/
	private BitOutputStreamTree(OutputStream destination, String name, boolean spawnChildren) {
		super(destination);
		this.name = name;
		if (destination == null) { //do not bother creating if we are not using it
			this.bitPipe = new BitPipe();
			this.createBis();
		}
		this.spawnChildren = spawnChildren;
	}

	/**
	 * Build a bstn
	 * @param name the name of this tree
	 * @param spawnChildren if this tree should spawn children (less efficient)
	 */
	public BitOutputStreamTree(String name, boolean spawnChildren) {
		this(null, name, spawnChildren);
	}
	
	/**
	 * same as calling {@link #BitOutputStreamTree(null, false)}
	 */
	public BitOutputStreamTree() {
		this(null, null, false);
	}
	
	/**
	 * Use this constructor when you want this {@link BitOutputStreamTree} to 
	 * behave exactly as a {@link BitOutputStream}
	 * @param destination
	 */
	public BitOutputStreamTree(OutputStream destination) {
		this(destination, null, false);
	}
	
	private void createBis() {
		this.bis = new BitInputStream(null) {
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
	    		if (bitPipe.getNumberOfBits() > (long) Integer.MAX_VALUE) {
	    			return Integer.MAX_VALUE;
	    		}
	    		return (int) bitPipe.getNumberOfBits();
	    	}
	    	
	    	@Override
	    	public void close() {
	    		//nothing
	    	}
		};
	}
	/********************************/
	
	
	/******************************/
	/** BIT OUTPUT STREAM METHODS */
	/********************************/
	@Override
	public void writeBit(int bit) throws IOException {
		if (this.destination != null) {
			super.writeBit(bit);
			return;
		}
		
		if (hasChildren()) {
			throw new IllegalStateException("You shouldn't add bits to a tree node if it has children, since it is traversed in preorder and this would break it");
		}
		bitPipe.putBit(bit);
	}

	@Override
	public void writeByte(byte i) throws IOException {
		if (this.destination != null) {
			super.writeByte(i);
			return;
		}
		
		this.writeNBitNumber(i & 0xff, 8);
	}
	
	@Override
	public void paddingFlush() throws IOException {
		if (this.destination != null) {
			super.paddingFlush();
			return;
		}
		
		int bufferSize = (int) this.getBitsOutput() % 8;
		//flush remaining bits padding with zeroes
		if (bufferSize > 0) {
			this.writeNBitNumber(0, 8 - bufferSize);
		}
	}
	
	@Override
	public void close() throws IOException {
		if (this.destination != null) {
			super.close();
		}
	}
	/********************************/

	
	/********************************/
	/**		TREE METHODS			*/
	/********************************/
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
	public long getNodeBits() {
		if (this.destination == null) {
			return this.bitPipe.getNumberOfBits();
		} else {
			return this.getBitsOutput();
		}
	}
	
	/**
	 * @return the number of bits stored in this tree (recursively looking in subtrees)
	 */
	public long getTreeBits() {
		if (this.destination != null) {
			return this.getBitsOutput();
		}
		
		long count = this.bitPipe.getNumberOfBits();
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
	
	/**
	 * Read bits from here (you read what has been written with the parent {@link BitOutputStreamTree})
	 * Native {@link InputStream} methods will not work since there is no
	 * underlying {@link InputStream}. Use only those provided
	 * by {@link BitInputStream}<br><br>
	 * This object will only work if this {@link BitOutputStreamTree} was not
	 * constructed with {@link #BitOutputStreamTree(OutputStream)}
	 * @return the internal {@link BitInputStream}, or <code>null</code> if this was constructed
	 * using {@link #BitOutputStreamTree(OutputStream)}
	 */
	public BitInputStream getBis() {
		return this.bis;
	}
	/********************************/

}
