package com.jypec.util.io.headerio.enums;

import java.lang.reflect.Field;

/**
 * Ordering of the bytes in the image file. 
 * Only applies to byte-width data types
 * @author Daniel
 *
 */
public enum ByteOrdering {
	/** Big endian. Start with the most significant byte in the lowest memory address */
	BIG_ENDIAN("1"),
	/** Little endian. Start with the least significant byte in the lowest memory address */
	LITTLE_ENDIAN("0");

	/**
	 * Hackity hack stuff to modify the internal fields of the enum, such that
	 * {@link #name()} produces the name sent to the constructor, and {@link #valueOf(String)} therefore
	 * constructs an enum the name sent to the constructor, instead of the field String. <br>
	 * If not done this way, an enum {@link #name()} returns the same string as the enum's name, 
	 * and {@link #valueOf(String)} requires that same default name to work.
	 * @param name
	 */
	private ByteOrdering(String name) {
		try {
			Field fieldName;
			fieldName = getClass().getSuperclass().getDeclaredField("name");
			fieldName.setAccessible(true);
			fieldName.set(this, name);
			fieldName.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} 
    }

};