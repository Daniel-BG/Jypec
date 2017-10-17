package com.jypec.util.debug;

import com.jypec.util.MathOperations;

/**
 * Some common functions for the logger and profiler for formatting times and traces
 * @author Daniel
 */
public class InfoFormatter {
	
	/**
	 * Precision of timer
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
	
	
	/**
	 * @param nanotime the time to convert, in nanoseconds
	 * @param secondFractionPrecision the number of decimal places to represent for second fractions
	 * @param precision the precision up to which represent. e.g if the precision is of minutes, hours and days wont be shown
	 * @return a string representing the time in DDDTHHMMSS.FFFF format
	 */
	public static String timeToString(long nanotime, int secondFractionPrecision, Precision precision) {
		if (secondFractionPrecision != 9) {
			nanotime /= MathOperations.pow(10, 9 - secondFractionPrecision);
		}
		long fraction = nanotime % MathOperations.pow(10, secondFractionPrecision);
		nanotime /= MathOperations.pow(10, secondFractionPrecision);
		long seconds = nanotime % 60;
		nanotime /= 60;
		long minutes = nanotime % 60;
		nanotime /= 60;
		long hours = nanotime % 24;
		nanotime /= 24;
		long days = nanotime;
		
		String res = "";
		switch(precision) {
		case DAY:
			res += Long.toString(days) + "T";
		case HOUR:
			res += String.format("%02d", hours) + ":";
		case MINUTE:
			res += String.format("%02d", minutes) + ":";
		case SECOND:
			res += String.format("%02d", seconds);
		case FRACTION:
			if (secondFractionPrecision > 0) {
				if (precision != Precision.FRACTION) {
					res += ".";
				}
				res += String.format("%0" + secondFractionPrecision + "d", fraction);
			}
		default:
			break;
		}
		return res;
	}
	
	
	/**
	 * Converts the fully qual name and methodname given by traces to a
	 * less cluttered view of just the first letter of each package and class 
	 * that is in the trace, plus the method name. <br><br>
	 * e.g: com.util.Clazz.method -> c.u.C.method
	 * @param fullyQualifiedName
	 * @param methodName
	 * @return the reduced string
	 */
	public static String reducedQualifiedName(String fullyQualifiedName, String methodName) {
		String[] parts = fullyQualifiedName.split("\\.");
		String res = "";
		for (String s: parts) {
			res += s.substring(0, 1) + ".";
		}
		return res + methodName;
	}
	
}
