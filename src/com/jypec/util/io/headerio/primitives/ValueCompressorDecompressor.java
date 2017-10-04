package com.jypec.util.io.headerio.primitives;

import java.io.IOException;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Class to transform strings to values (uncompressed to compressed) <br>
 * or values to strings (compressed to uncompressed)
 * @author Daniel
 */
public abstract class ValueCompressorDecompressor {
	
	/**
	 * Interpret the given object (usually a string) and save it internally,
	 * parsed according to this compressorDecompressor's parsing.
	 * It can be retrieved by {@link #getObject()}
	 * @param obj
	 */
	public abstract void parse(Object obj);
	
	/**
	 * @return the object that has been parsed by {@link #parse(Object)} 
	 * or uncompressed by {@link #uncompress(BitStreamDataReaderWriter)}
	 */
	public abstract Object getObject();
	
	/**
	 * Sets the inner object
	 * @param obj
	 */
	public abstract void setObject(Object obj);
	
	/**
	 * Compress the inner object (see {@link #getObject()}) into the given stream
	 * @param brw
	 * @throws IOException 
	 */
	public abstract void compress(BitOutputStreamTree brw) throws IOException;
	
	/**
	 * Does {@link #parse(Object)} followed by 
	 * {@link #compress(BitStreamDataReaderWriter)} followed by
	 * {@link #getObject()}
	 * @param obj the object to compressed
	 * @param brw where to compress the object
	 * @return the compressed object
	 * @throws IOException 
	 */
	public Object parseCompressAndReturn(Object obj, BitOutputStreamTree brw) throws IOException{
		this.parse(obj);
		this.compress(brw);
		return this.getObject();
	}
	
	/**
	 * Read the value from the given stream and return the representing string
	 * @param brw
	 * @throws IOException 
	 */
	public abstract void uncompress(BitInputStream brw) throws IOException;
	
	/**
	 * Does {@link #uncompress(BitStreamDataReaderWriter)} followed by
	 * {@link #getObject()}
	 * @param brw
	 * @return the uncompressed object
	 * @throws IOException 
	 */
	public Object uncompressAndReturn(BitInputStream brw) throws IOException {
		this.uncompress(brw);
		return this.getObject();
	}
	
	
	/**
	 * @return the inner object representation as a String for saving in an uncompressed String
	 * (not always equal to {@link #getObject()}.toString()
	 */
	public abstract String unParse();

}
