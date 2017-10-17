package com.jypec.util.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

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
	private HashMap<String, Long> profilerStartingPoints;
	private HashMap<String, Long> profilerAccumulatedTime;
	
	
	/**
	 * private constructor
	 */
	private Logger() {
		this.sttime = System.nanoTime();
		this.setSecondFractionPrecision(3);
		this.setPrecision(Precision.MINUTE);
		this.profilerStartingPoints = new HashMap<String, Long>();
		this.profilerAccumulatedTime = new HashMap<String, Long>();
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
			System.out.print("[" + timeToString(currTime - sttime) + "]");
		}
		
		//get stack size and print dots to represent it
		int stackSize = Thread.currentThread().getStackTrace().length;
		for (int i = stackSize; i > baseLine; i--) {
			System.out.print(".");
		}
		
		//add info to the profilers just in case
		String funcTrace = Logger.reducedQualifiedName(ste.getClassName(), ste.getMethodName());
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
	
	
	/**
	 * Dumps the profiling information gathered with the logger
	 */
	public void profile() {
		long totalTime = System.nanoTime() - this.sttime;
		System.out.println("----Profiling information below: [<time>] <trace>----");
		System.out.println("Total:   [" + timeToString(totalTime) + "]");
		this.dumpProfilingInformation(profilerAccumulatedTime, totalTime);
	}
	
	
	/**
	 * Start profiling. Call this at the beginning of a function
	 */
	public void profileStart() {
		long currTime = System.nanoTime();
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; //0 is Thread, 1 is this log, 2 is the caller
		String funcTrace = Logger.reducedQualifiedName(ste.getClassName(), ste.getMethodName());
		if (this.profilerStartingPoints.containsKey(funcTrace)) {
			throw new IllegalStateException("Make sure to end a profiling interval before starting a new one. If you are profiling a recursive function, call here before the first call to it");
		}
		this.profilerStartingPoints.put(funcTrace, currTime);
	}
	
	/**
	 * End profiling. Call this at the end of a function
	 */
	public void profileEnd() {
		long currTime = System.nanoTime();
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; //0 is Thread, 1 is this log, 2 is the caller
		String funcTrace = Logger.reducedQualifiedName(ste.getClassName(), ste.getMethodName());
		if (!this.profilerStartingPoints.containsKey(funcTrace)) {
			throw new IllegalStateException("Oops, you are trying to end a profile that has not even started. Make sure to call profileStart before (in the same function)");
		}
		long elapsedTime = currTime - this.profilerStartingPoints.remove(funcTrace);
		if (this.profilerAccumulatedTime.containsKey(funcTrace)) {
			this.profilerAccumulatedTime.put(funcTrace, this.profilerAccumulatedTime.get(funcTrace) + elapsedTime);
		} else {
			this.profilerAccumulatedTime.put(funcTrace, elapsedTime);
		}
	}
	
	
	
	private void dumpProfilingInformation(HashMap<String, Long> profiler, long totalTime) {
		//get profiling entries
		List<Entry<String, Long>> profileList = new ArrayList<Entry<String, Long>>();
		profileList.addAll(profiler.entrySet());
		
		//sort them descending
		Collections.sort(profileList, new Comparator<Entry<String, Long>>() {
			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				return Long.compare(o2.getValue(), o1.getValue());
			}
		});
		//print them
		for (Entry<String, Long> e: profileList) {
			double percent = 100.0 * (double) e.getValue() / (double) totalTime;
			System.out.println(String.format("%07.4f", percent) + "% [" + timeToString(e.getValue()) + "]" + e.getKey());
		}
	}
	
	
	private class ProfileTree {

		private String name;
		private long nanoTime;
		private HashSet<ProfileTree> children; 
		
		public ProfileTree(String name) {
			this.name = name;
			this.children = new HashSet<ProfileTree>();
		}
		
		public ProfileTree startTiming() {
			this.nanoTime = System.nanoTime();
			return this;
		}
		
		public ProfileTree endTiming() {
			this.nanoTime = System.nanoTime() - this.nanoTime;
			return this;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
	
}

