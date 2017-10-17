package com.jypec.util.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.jypec.util.debug.InfoFormatter.Precision;

/**
 * Class that aids with the profiling of the program
 * @author Daniel
 */
public class Profiler {
	
	//singleton
	private static Profiler instance;
	
	static {
		instance = new Profiler();
	}

	private boolean isProfiling = false;
	private ProfileTree profileTree;
	private int secondFractionPrecision;
	private Precision precision;
	
	private Profiler() {
		this.profileTree = new ProfileTree("Program", "", null);
		this.profileTree.startTiming();
		this.setSecondFractionPrecision(3);
		this.setPrecision(Precision.MINUTE);
	}
	
	/**
	 * @return the singleton profiler
	 */
	public static Profiler getProfiler() {
		return instance;
	}
	
	/**
	 * @param isProfiling true if you want the profiler to profile
	 */
	public void setProfiling(boolean isProfiling) {
		this.isProfiling = isProfiling;
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
	 * Dumps the profiling information gathered with the logger
	 */
	public void profile() {
		if (!this.profileTree.isRoot()) {
			throw new IllegalStateException("The profiling has not ended. Some profilers are not closed!");
		}
		
		if (!this.isProfiling) {
			System.out.println("Profiling information not available. Make sure to enable the profiler");
			return;
		}
		
		this.profileTree.endTiming();
		System.out.println("----Profiling information below: <percent of parent> [<time>] <trace>----");
		this.profileTree.dumpProfilingInformation(this.profileTree.getTime(), 0);
	}
	
	/**
	 * Start profiling. Call this at the beginning of a function
	 */
	public void profileStart() {
		if (!this.isProfiling) {
			return;
		}
		
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; //0 is Thread, 1 is this log, 2 is the caller
		String funcTrace = InfoFormatter.reducedQualifiedName(ste.getClassName(), ste.getMethodName());
		String lineInfo = "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
		
		if (this.profileTree.getChildren(funcTrace) != null) {
			this.profileTree = this.profileTree.getChildren(funcTrace);
		} else {
			this.profileTree = this.profileTree.addChildren(funcTrace, lineInfo);
		}
		this.profileTree.startTiming();
	}
	
	/**
	 * End profiling. Call this at the end of a function
	 */
	public void profileEnd() {
		if (!this.isProfiling) {
			return;
		}
		
		//check that the profile is ended from within the same function
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; //0 is Thread, 1 is this log, 2 is the caller
		String funcTrace = InfoFormatter.reducedQualifiedName(ste.getClassName(), ste.getMethodName());
		if (!funcTrace.equals(this.profileTree.getName())) {
			throw new IllegalStateException("Trying to end a profile from a different function is not allowed!");
		}
		
		this.profileTree.endTiming();
		this.profileTree = this.profileTree.goToParent();
	}
	
	private class ProfileTree {

		private String name;
		private String lineInfo;
		private long accTime;
		private long stTime = -1;
		private HashMap<String, ProfileTree> children; 
		private ProfileTree parent;
		
		public ProfileTree(String name, String lineInfo, ProfileTree parent) {
			this.name = name;
			this.lineInfo = lineInfo;
			this.children = new HashMap<String, ProfileTree>();
			this.parent = parent;
			this.accTime = 0;
		}
		
		public boolean isTiming() {
			return this.stTime != -1;
		}
		
		public boolean isRoot() {
			return this.parent == null;
		}
		
		public ProfileTree startTiming() {
			if (this.isTiming()) {
				throw new IllegalStateException("Make sure to end a profiling interval before starting a new one.");
			}
			this.stTime = System.nanoTime();
			return this;
		}
		
		public ProfileTree endTiming() {
			if (!this.isTiming()) {
				throw new IllegalStateException("Oops, you are trying to end a profile that has not even started. Make sure to call startTiming before (in the same function)");
			}
			this.accTime += System.nanoTime() - this.stTime;
			this.stTime = -1;
			return this;
		}
		
		public long getTime() {
			return this.accTime;
		}
		
		public ProfileTree addChildren(String name, String lineInfo) {
			ProfileTree tree = new ProfileTree(name, lineInfo, this);
			this.children.put(name, tree);
			return tree;
		}
		
		public ProfileTree getChildren(String name) {
			return this.children.get(name);
		}
		
		public ProfileTree goToParent() {
			if (this.parent == null) {
				throw new IllegalStateException("You are trying to end the root profile. Not allowed");
			}
			return this.parent;
		}
		
		public String getName() {
			return this.name;
		}
		
		public void dumpProfilingInformation(long totalTime, int level) {
			double percent = 100.0 * (double) this.accTime / (double) totalTime;
			if (this.isRoot()) {
				System.out.println("[" + InfoFormatter.timeToString(this.accTime, secondFractionPrecision, precision) + "]" + this.name);
			} else {
				System.out.print(" ");
				for (int i = 0; i < level - 1; i++) {
					System.out.print("           ");
				}
				System.out.println(String.format("%08.5f", percent) + "% [" + InfoFormatter.timeToString(this.accTime, secondFractionPrecision, precision) + "]" + this.name + " " + this.lineInfo);	
			}
			
			//get profiling entries
			List<Entry<String, ProfileTree>> profileList = new ArrayList<Entry<String, ProfileTree>>();
			profileList.addAll(this.children.entrySet());
			
			//sort them descending
			Collections.sort(profileList, new Comparator<Entry<String, ProfileTree>>() {
				@Override
				public int compare(Entry<String, ProfileTree> o1, Entry<String, ProfileTree> o2) {
					return Long.compare(o2.getValue().getTime(), o1.getValue().getTime());
				}
			});
			//print them
			for (Entry<String, ProfileTree> e: profileList) {
				e.getValue().dumpProfilingInformation(this.accTime, level + 1);
			}
		}
	}
}
