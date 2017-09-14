package com.jypec.util.io.headerio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		//TODO still does not uncompress streams with embedded data!
		while (brw.availableBytes() > 0) {
			ParameterReaderWriter prw = ParameterReaderWriter.readNextCompressedParameter(brw);
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
			prw.compress(brw);
		}
	}
	
	/**
	 * Save inner information into an uncompressed stream
	 * @param brw where to save it
	 * @throws IOException 
	 */
	public void saveToUncompressedStream(OutputStream brw) throws IOException {
		for (Entry<HeaderConstants, Object> e: this.data.entrySet()) {
			ParameterReaderWriter prw = new ParameterReaderWriter(e.getKey());
			prw.setData(e.getValue());
			brw.write(prw.toString().getBytes(StandardCharsets.UTF_8));
			brw.write("\n".getBytes(StandardCharsets.UTF_8));
		}
	}
	
	
}
