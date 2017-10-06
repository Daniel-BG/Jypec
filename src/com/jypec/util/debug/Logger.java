package com.jypec.util.debug;

import com.jypec.util.MathOperations;

/**
 * @author Daniel
 *
 */
public class Logger {
	//singleton
	private static Logger instance;
	/**
	 * Precision of logger timer
	 * @author Daniel
	 */
	public enum Precision {
		/** Output only fractions of second */
		FRACTION, 
		/** Output up to seconds*/
		SECOND, 
		/** Output up to minutes */
		MINUTE, 
		/** Output up to hours */
		HOUR, 
		/** Output up to days */
		DAY
	};
	
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
		//get stack trace
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; //0 is Thread, 1 is this log, 2 is the caller
		
		//print time of logging if necessary
		if (this.precision != Precision.FRACTION || this.secondFractionPrecision != 0) {
			System.out.print("[" + timeToString(System.nanoTime() - sttime) + "]");
		}
		
		//get stack size and print dots to represent it
		int stackSize = Thread.currentThread().getStackTrace().length;
		for (int i = stackSize; i > baseLine; i--) {
			System.out.print(".");
		}
		
		//print logger information
		System.out.println(Logger.reducedQualifiedName(ste.getClassName(), ste.getMethodName()) + 
				" (" + ste.getFileName() + ":" + ste.getLineNumber() + "): " + message);
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
	
	private static String reducedQualifiedName(String fullyQualifiedName, String methodName) {
		String[] parts = fullyQualifiedName.split("\\.");
		String res = "";
		for (String s: parts) {
			res += s.substring(0, 1) + ".";
		}
		return res + methodName;
	}
	
	
	private String timeToString(long elapsedtime) {
		if (this.secondFractionPrecision != 9) {
			elapsedtime /= MathOperations.pow(10, 9 - this.secondFractionPrecision);
		}
		long fraction = elapsedtime % MathOperations.pow(10, this.secondFractionPrecision);
		elapsedtime /= MathOperations.pow(10, this.secondFractionPrecision);
		long seconds = elapsedtime % 60;
		elapsedtime /= 60;
		long minutes = elapsedtime % 60;
		elapsedtime /= 60;
		long hours = elapsedtime % 24;
		elapsedtime /= 24;
		long days = elapsedtime;
		
		String res = "";
		switch(this.precision) {
		case DAY:
			res += Long.toString(days) + "T";
		case HOUR:
			res += String.format("%02d", hours) + ":";
		case MINUTE:
			res += String.format("%02d", minutes) + ":";
		case SECOND:
			res += String.format("%02d", seconds);
		case FRACTION:
			if (this.secondFractionPrecision > 0) {
				if (this.precision != Precision.FRACTION) {
					res += ".";
				}
				res += String.format("%0" + this.secondFractionPrecision + "d", fraction);
			}
		default:
			break;
		}
		return res;
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
