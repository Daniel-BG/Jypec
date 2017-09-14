package com.jypec.util;

import java.util.Collection;

/**
 * Little things that do not fit anywhere else
 * @author Daniel
 *
 */
public class Utilities {
	
	/**
	 * Add all bytes from list to c
	 * @param c
	 * @param list
	 */
	public static void addAllBytes(Collection<Byte> c, byte[] list) {
		for (int i = 0; i < list.length; i++) {
			c.add(list[i]);
		}
	}

}
