package com.jypec.util.debug;

/**
 * @author Daniel
 *
 */
public class Logger {
	//singleton
	private static Logger instance;
	
	static {
		instance = new Logger();
	}
	
	private boolean isLogging = false;
	
	
	/**
	 * private constructor
	 */
	private Logger() {
		//set up stuff if needed
	}
	
	
	/**
	 * @return the singleton logger
	 */
	public static Logger getLogger() {
		return instance;
	}
	
	
	/**
	 * @param message log this message
	 */
	public void log(String message) {
		if (!isLogging) {
			return;
		}
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; //0 is Thread, 1 is this log, 2 is the caller
		int stackSize = Thread.currentThread().getStackTrace().length - 2;
		for (int i = stackSize; i >= 0; i--) {
			System.out.print(".");
		}
		
		System.out.println(Logger.reducedQualifiedName(ste.getClassName(), ste.getMethodName()) + 
				" (" + ste.getFileName() + ":" + ste.getLineNumber() + "): " + message);
	}
	
	/**
	 * @param isLogging true if you want the logger to print to sysout
	 */
	public void setLogging(boolean isLogging) {
		this.isLogging = isLogging;
	}
	
	
	private static String reducedQualifiedName(String fullyQualifiedName, String methodName) {
		String[] parts = fullyQualifiedName.split("\\.");
		String res = "";
		for (String s: parts) {
			res += s.substring(0, 1) + ".";
		}
		return res + methodName;
	}
	
	
}
