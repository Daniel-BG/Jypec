package com.jypec.util.debug;

import com.jypec.util.debug.InfoFormatter.Precision;

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
	
	private long sttime;
	private int secondFractionPrecision;
	private Precision precision;
	private int baseLine = 0;
	
	/**
	 * private constructor
	 */
	private Logger() {
		this.sttime = System.nanoTime();
		this.setSecondFractionPrecision(3);
		this.setPrecision(Precision.MINUTE);
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
		//timing stuff
		long currTime = System.nanoTime();
		
		//get stack trace
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; //0 is Thread, 1 is this log, 2 is the caller
		
		//print time of logging if necessary
		if (this.precision != Precision.FRACTION || this.secondFractionPrecision != 0) {
			System.out.print("[" + InfoFormatter.timeToString(currTime - sttime, this.secondFractionPrecision, this.precision) + "]");
		}
		
		//get stack size and print dots to represent it
		int stackSize = Thread.currentThread().getStackTrace().length;
		for (int i = stackSize; i > baseLine; i--) {
			System.out.print(".");
		}
		
		//add info to the profilers just in case
		String funcTrace = InfoFormatter.reducedQualifiedName(ste.getClassName(), ste.getMethodName());
		String trace = funcTrace + 
				" (" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
		
		//print logger information
		System.out.println(trace + ": " + message);
	}

	/**
	 * @param isLogging true if you want the logger to print to sysout
	 */
	public void setLogging(boolean isLogging) {
		this.isLogging = isLogging;
	}
	
	/**
	 * @param precision the number of digits to represent second fractions (from 0 up to 9)
	 */
	public void setSecondFractionPrecision(int precision) {
		if (precision < 0 || precision > 9) {
			throw new IllegalArgumentException("Precision must be between 0 and 9 both inclusive");
		}
		this.secondFractionPrecision = precision;
	}
	
	/**
	 * @param p the precision to show on the debugger output
	 */
	public void setPrecision(Precision p) {
		this.precision = p;
	}

	/**
	 * Set the current stack depth as the baseline depth, so its indentation
	 * is zero in the loggin messages, and higher depths get a dot <code>.</code> 
	 * in front of the message for each function in the stack
	 */
	public void setBaseLine() {
		this.baseLine  = Thread.currentThread().getStackTrace().length;
	}

}

