package com.jypec.util.io.headerio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jypec.util.Utilities;
import com.jypec.util.bits.BitStreamDataReaderWriter;
import com.jypec.util.io.IOUtilities;

/**
 * Stores image header data
 * @author Daniel
 */
public class ImageHeaderData {
	
	private Map<HeaderConstants, Object> data;
	
	/**
	 * Create a new header data
	 */
	public ImageHeaderData() {
		 this.setUp();	
	}
	
	private void setUp() {
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
	 */
	public void loadFromUncompressedStream(InputStream stream) throws IOException {
		this.setUp();
		//TODO in case the header is embedded, read only up to the data!
		String s = IOUtilities.fullyReadStream(stream);		
		
		Matcher m = Pattern.compile("^([^=\\n\\r]+?)\\s+=\\s+([^\\{].*?|\\{.*?\\})$", Pattern.MULTILINE | Pattern.DOTALL).matcher(s);
		
		/** Split the charsequence into the different data groups and parse all */
		while (m.find()) {
			ParameterReaderWriter prw = new ParameterReaderWriter(m.group(1));
			prw.parseData(m.group(2));
			this.data.put(prw.getHeaderConstant(), prw.getData());
		}
	}

	/**
	 * Load the header from the given compressed stream
	 * @param brw
	 */
	public void loadFromCompressedStream(BitStreamDataReaderWriter brw) {
		this.setUp();
		while (brw.availableBytes() > 0) {
			ParameterReaderWriter prw = ParameterReaderWriter.readNextCompressedParameter(brw);
			if (prw.getHeaderConstant() == HeaderConstants.HEADER_TERMINATION) {
				break;
			}
			this.setData(prw.getHeaderConstant(), prw.getData());
		}
	}
	
	
	/**
	 * save inner information into a compressed stream
	 * @param brw
	 */
	public void saveToCompressedStream(BitStreamDataReaderWriter brw) {
		for (Entry<HeaderConstants, Object> e: this.data.entrySet()) {
			ParameterReaderWriter prw = new ParameterReaderWriter(e.getKey());
			prw.setData(e.getValue());
			if (prw.getHeaderConstant() != HeaderConstants.HEADER_OFFSET) { //do not save the header offset as it is different
				prw.compress(brw);
			}
		}
		brw.writeByte((byte)HeaderConstants.HEADER_TERMINATION.ordinal());
	}
	
	/**
	 * Save inner information into an uncompressed stream
	 * @param brw where to save it
	 * @throws IOException 
	 */
	public void saveToUncompressedStream(OutputStream brw) throws IOException {
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
	}
	
	
}
