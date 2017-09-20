package com.jypec.util;

/**
 * Generic exception for project specific errors that don't fit
 * in predefined exceptions
 * @author Daniel
 */
public class JypecException extends Exception {

	/**
	 * Create a jypec exception with the given message
	 * @param string
	 */
	public JypecException(String string) {
		super(string);
	}

	/**  */
	private static final long serialVersionUID = 3583473650123093125L;

}
