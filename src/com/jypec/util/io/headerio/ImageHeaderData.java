package com.jypec.util.io.headerio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jypec.util.Utilities;
import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * Stores image header data
 * @author Daniel
 */
public class ImageHeaderData {
	
	private Map<HeaderConstants, Object> data;
	private static final String DATA_PATTERN = "^([^=\\n\\r]+?)\\s+=\\s+([^\\{].*?|\\{.*?\\})$";
	
	/**
	 * Create a new header data
	 */
	public ImageHeaderData() {
		 this.reset();	
	}
	
	private void reset() {
		this.data = new EnumMap<HeaderConstants, Object>(HeaderConstants.class);	
	}
	
	/**
	 * Sets the given value for the given field
	 * @param field
	 * @param value
	 */
	public void setData(HeaderConstants field, Object value) {
		this.data.put(field, value);
	}
	
	/** 
	 * @param field
	 * @return the data for the given field
	 */
	public Object getData(HeaderConstants field) {
		return this.data.get(field);
	}
	
	/**
	 * @param stream Stream where to load from
	 * @throws IOException if something fails when reading
	 * @return if the data is embbeded in the input stream, the number of BYTES at which it is offset
	 * counting from where the pointer was when this function was called. Note that the pointer for
	 * stream might have gone past the data starting point!. If the data is not embedded, then return 0
	 */
	public int loadFromUncompressedStream(InputStream stream) throws IOException {
		this.reset();
		Scanner scn = new Scanner(stream);
		int horizon = 0; //for now no limit. If we find the "header offset" keyword, set the horizon to it
		while (true) {
			//find the next within horizon
			String s = scn.findWithinHorizon(Pattern.compile(DATA_PATTERN, Pattern.MULTILINE | Pattern.DOTALL), horizon);
			if (s == null) {
				break;
			}
			//split into individual parts and create the parameter
			Matcher m = Pattern.compile(DATA_PATTERN, Pattern.MULTILINE | Pattern.DOTALL).matcher(s);
			m.find(); //should always work since s is this pattern, we only use this to split it
			ParameterReaderWriter prw = new ParameterReaderWriter(m.group(1));
			prw.parseData(m.group(2));
			//if it is the offset, set the new horizon
			if (prw.getHeaderConstant() == HeaderConstants.HEADER_OFFSET) {
				/** 
				 * Note that setting the horizon this way will probably make the scanner go past
				 * the header limit, maybe with some other data structures this can be fixed so 
				 * that this function returns the input stream in the exact position it needs to be
				 * for the data to start to be read
				 */
				horizon = (int) prw.getData();
			} else { //otherwise save it. Do not save the offset since it'll probably change
				this.data.put(prw.getHeaderConstant(), prw.getData());
			}
		}
		scn.close();
		return horizon;
	}

	/**
	 * Load the header from the given compressed stream
	 * @param brw
	 * @return the number of BYTES read
	 */
	public int loadFromCompressedStream(BitStreamDataReaderWriter brw) {
		int bits = brw.availableBits();
		this.reset();
		while (brw.availableBytes() > 0) {
			ParameterReaderWriter prw = ParameterReaderWriter.readNextCompressedParameter(brw);
			if (prw.getHeaderConstant() == HeaderConstants.HEADER_TERMINATION) {
				break;
			}
			this.setData(prw.getHeaderConstant(), prw.getData());
		}
		bits -= brw.availableBits();
		if (bits % 8 == 0) {
			return bits / 8;
		} else {
			throw new IllegalStateException("The number of bits read is not a multiple of 8. Padding bits must not have been read somewhere");
		}
	}
	
	
	/**
	 * save inner information into a compressed stream
	 * @param brw
	 * @return the number of BYTES written to the stream
	 */
	public int saveToCompressedStream(BitStreamDataReaderWriter brw) {
		int bits = brw.availableBits();
		for (Entry<HeaderConstants, Object> e: this.data.entrySet()) {
			ParameterReaderWriter prw = new ParameterReaderWriter(e.getKey());
			prw.setData(e.getValue());
			if (prw.getHeaderConstant() != HeaderConstants.HEADER_OFFSET) { //do not save the header offset as it is different
				prw.compress(brw);
			}
		}
		brw.writeByte((byte)HeaderConstants.HEADER_TERMINATION.ordinal());
		
		bits = brw.availableBits() - bits;
		if (bits % 8 == 0) {
			return bits / 8;
		} else {
			throw new IllegalStateException("The number of bits saved is not a multiple of 8. Padding bits must be missing somewhere");
		}
	}
	
	/**
	 * Save inner information into an uncompressed stream
	 * @param brw where to save it
	 * @return the number of BYTES written
	 * @throws IOException 
	 */
	public int saveToUncompressedStream(OutputStream brw) throws IOException {
		ArrayList<Byte> list = new ArrayList<Byte>();
		byte[] lineSeparator = "\n".getBytes(StandardCharsets.UTF_8);

		for (Entry<HeaderConstants, Object> e: this.data.entrySet()) {
			ParameterReaderWriter prw = new ParameterReaderWriter(e.getKey());
			prw.setData(e.getValue());
			//add the parameter
			Utilities.addAllBytes(list, prw.toString().getBytes(StandardCharsets.UTF_8));
			//add a newline
			Utilities.addAllBytes(list, lineSeparator);
		}
		Utilities.addAllBytes(list, "header offset = ".getBytes(StandardCharsets.UTF_8));
		
		Integer listSize = list.size() + lineSeparator.length;
		Integer totalSize = listSize + listSize.toString().length();
		if (totalSize.toString().length() != listSize.toString().length()) {
			totalSize++;
		}
		Utilities.addAllBytes(list, totalSize.toString().getBytes());
		Utilities.addAllBytes(list, lineSeparator);
		//now the data could be compressed into the outputstream
		
		byte[] result = new byte[list.size()];
		int index = 0;
		for (Byte b: list) {
			result[index] = b;
			index++;
		}
		brw.write(result);
		
		return result.length;
	}
	
	
}
