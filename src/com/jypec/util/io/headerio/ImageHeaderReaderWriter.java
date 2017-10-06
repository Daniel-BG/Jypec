package com.jypec.util.io.headerio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jypec.img.HeaderConstants;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.Utilities;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Read/Write image headers
 * @author Daniel
 *
 */
public class ImageHeaderReaderWriter {
	
	private static final byte CODE_ENVI_HEADER = (byte) 'E';
	private static final byte CODE_JYPEC_HEADER = (byte) 0xff;
	

	private static final String DATA_PATTERN = "^([^=\\n\\r]+?)\\s+=\\s+([^\\{].*?|\\{.*?\\})\\s*$";

	/**
	 * Checks whether the data is compressed or uncompressed then 
	 * loads it into this object
	 * @param bis
	 * @param ihd 
	 * @return the number of bytes which the data occupied in the bit input stream
	 * @throws IOException 
	 */
	public static int loadFromStream(BitInputStream bis, ImageHeaderData ihd) throws IOException {
		byte fileCode = bis.readByte();
		switch(fileCode) {
		case CODE_ENVI_HEADER:
			return ImageHeaderReaderWriter.loadFromUncompressedStream(bis, ihd);
		case CODE_JYPEC_HEADER:
			int res = ImageHeaderReaderWriter.loadFromCompressedStream(bis, ihd);
			ihd.setWasCompressed(true);
			return res;
		default:
			throw new IOException("Header code was not recognized");
		}
	}
	
	/**
	 * @param stream Stream where to load from
	 * @param ihd 
	 * @throws IOException if something fails when reading
	 * @return if the data is embbeded in the input stream, the number of BYTES at which it is offset
	 * counting from where the pointer was when this function was called. Note that the pointer for
	 * stream might have gone past the data starting point!. If the data is not embedded, then return 0
	 */
	private static int loadFromUncompressedStream(InputStream stream, ImageHeaderData ihd) throws IOException {
		ihd.clear();
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
			ParameterReaderWriter prw = new ParameterReaderWriter(m.group(1).trim());
			prw.parseData(m.group(2).trim());
			//if it is the offset, set the new horizon
			if (prw.getHeaderConstant() == HeaderConstants.HEADER_OFFSET) {
				/** 
				 * Note that setting the horizon this way will probably make the scanner go past
				 * the header limit, maybe with some other data structures this can be fixed so 
				 * that this function returns the input stream in the exact position it needs to be
				 * for the data to start to be read
				 */
				horizon = (int) prw.getData();
			} else if (prw.getHeaderConstant() == HeaderConstants.HEADER_UNKNOWN) { //otherwise save it. Do not save the offset since it'll probably change
				//ignore
			} else {
				ihd.put(prw.getHeaderConstant(), prw.getData());
			}
		}
		scn.close();
		return horizon;
	}

	/**
	 * Load the header from the given compressed stream
	 * @param brw
	 * @param ihd 
	 * @return the number of BYTES read
	 * @throws IOException 
	 */
	private static int loadFromCompressedStream(BitInputStream brw, ImageHeaderData ihd) throws IOException {
		int bits = brw.getBitsInput();
		ihd.clear();
		while (brw.available() > 0) {
			ParameterReaderWriter prw = ParameterReaderWriter.readNextCompressedParameter(brw);
			if (prw.getHeaderConstant() == HeaderConstants.HEADER_TERMINATION) {
				break;
			}
			ihd.put(prw.getHeaderConstant(), prw.getData());
		}
		bits = brw.getBitsInput() - bits;
		if (bits % 8 == 0) {
			return bits / 8;
		} else {
			throw new IllegalStateException("The number of bits read is not a multiple of 8. Padding bits must not have been read somewhere");
		}
	}
	
	
	/**
	 * save inner information into a compressed stream
	 * @param ihd 
	 * @param bstn
	 * @param essential if true only the essential information to read the image is output, everything else discarded
	 * @return the number of BYTES written to the stream
	 * @throws IOException 
	 */
	public static int saveToCompressedStream(ImageHeaderData ihd, BitOutputStreamTree bstn, boolean essential) throws IOException {
		long bits = bstn.getTreeBits();
		bstn.writeByte(CODE_JYPEC_HEADER);
		for (Entry<HeaderConstants, Object> e: ihd.entrySet()) {
			ParameterReaderWriter prw = new ParameterReaderWriter(e.getKey());
			prw.setData(e.getValue());
			if (prw.getHeaderConstant() != HeaderConstants.HEADER_OFFSET && (!essential || prw.getHeaderConstant().isEssential())) { //do not save the header offset as it is different
				prw.compress(bstn.addChild(e.getKey().name()));
			}
		}
		bstn.addChild("header termination").writeByte((byte)HeaderConstants.HEADER_TERMINATION.ordinal());
		
		bits = bstn.getTreeBits() - bits;
		if (bits % 8 == 0) {
			return (int) bits / 8;
		} else {
			throw new IllegalStateException("The number of bits saved is not a multiple of 8. Padding bits must be missing somewhere");
		}
	}
	
	/**
	 * Save inner information into an uncompressed stream
	 * @param ihd 
	 * @param brw where to save it
	 * @param essential if true only the essential information to read the image is output, everything else discarded
	 * @param embedded set to true if the data goes after in the same output stream (this enables calculation of header offset)
	 * @return the number of BYTES written
	 * @throws IOException 
	 */
	public static int saveToUncompressedStream(ImageHeaderData ihd, OutputStream brw, boolean essential, boolean embedded) throws IOException {
		ArrayList<Byte> list = new ArrayList<Byte>();
		byte[] lineSeparator = "\n".getBytes(StandardCharsets.UTF_8);
		
		/** Add header and all entries */
		Utilities.addAllBytes(list, "ENVI\n".getBytes(StandardCharsets.UTF_8));
		for (Entry<HeaderConstants, Object> e: ihd.entrySet()) {
			ParameterReaderWriter prw = new ParameterReaderWriter(e.getKey());
			if (essential && !prw.getHeaderConstant().isEssential()) {
				continue;
			}
			
			prw.setData(e.getValue());
			//add the parameter
			Utilities.addAllBytes(list, prw.toString().getBytes(StandardCharsets.UTF_8));
			//add a newline
			Utilities.addAllBytes(list, lineSeparator);
		}
		
		/** If data comes afterwards, indicate header offset, otherwise ommit */
		if (embedded) {
			Utilities.addAllBytes(list, "header offset = ".getBytes(StandardCharsets.UTF_8));
			Integer listSize = list.size() + lineSeparator.length;
			Integer totalSize = listSize + listSize.toString().length();
			if (totalSize.toString().length() != listSize.toString().length()) {
				totalSize++;
			}
			Utilities.addAllBytes(list, totalSize.toString().getBytes());
			Utilities.addAllBytes(list, lineSeparator);
		}

		/** Output result */
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
