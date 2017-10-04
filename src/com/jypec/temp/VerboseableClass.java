package com.jypec.temp;

/**
 * For classes to implement if they have the option to be verbose.
 * Maybe extend in the future to include more options
 * @author Daniel
 *
 */
public interface VerboseableClass {
	
	/**
	 * Indentation used when a child verbose calls its parent to write
	 */
	public static final String INDENT = "\t";

	/**
	 * @param verbose if we want this object to output information while it works
	 */
	public void setVerbose(boolean verbose);
	
	/**
	 * @return true if this object verbose parameter is true
	 */
	public boolean verbose();
	
	/**
	 * Prints something out inline if {@link #verbose()} returns true
	 * @param s the string to print out
	 */
	public void say(String s);
	
	/**
	 * Prints something out ending the line if {@link #verbose()} returns true
	 * @param s the string to print out
	 */
	public void sayLn(String s);
	
	/**
	 * Inline say for when we do not want to add tabs
	 * @param s
	 */
	public void continueSaying(String s);
	
	/**
	 * @param parent another verboseable object within which this one will work, so that 
	 * its output is offset by tabs to be more user-friendly <br>
	 * If a parent is set, then the verbose status of this object should be ignored,
	 * using that of the parent
	 */
	public void setParentVerboseable(VerboseableClass parent);
}
