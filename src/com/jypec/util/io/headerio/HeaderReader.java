package com.jypec.util.io.headerio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read image headers
 * @author Daniel
 *
 */
public class HeaderReader {
	/**
	 * Read header data from the given sequence
	 * @param s
	 * @return the image header data
	 */
	public static ImageHeaderData readHeaderData(CharSequence s) {
		ImageHeaderData data = new ImageHeaderData();
		Matcher m = Pattern.compile("^([^=\\n\\r]+?)\\s+=\\s+([^\\{].*?|\\{.*?\\})$", Pattern.MULTILINE | Pattern.DOTALL).matcher(s);
		
		/** Split the charsequence into the different data groups */
		while (m.find()) {
			//System.out.println(m.group(0));
			System.out.println("G1: " + m.group(1));
			System.out.println("G2: " + m.group(2));
			System.out.println("");
		}
		
		
		return data;
	}

}
