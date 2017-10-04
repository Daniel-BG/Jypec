package com.jypec.temp;

/**
 * Default verbose implementation so that inheriting classes don't
 * need to do all the same
 * @author Daniel
 *
 */
public abstract class DefaultVerboseableClass implements VerboseableClass {

	private boolean verbose;
	private VerboseableClass parent;


	@Override
	public void setParentVerboseable(VerboseableClass parent) {
		this.parent = parent;
	}

	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public boolean verbose() {
		return this.verbose;
	}
	
	@Override
	public void say(String s) {
		if (parent != null) {
			this.parent.say(VerboseableClass.INDENT + s);	
		} else if (verbose()) {
			System.out.print(s);
		}
	}
	
	@Override
	public void sayLn(String s) {
		if (parent != null) {
			this.parent.sayLn(VerboseableClass.INDENT + s);	
		} else if (verbose()) {
			System.out.println(s);
		}
	}
	
	@Override
	public void continueSaying(String s) {
		if (parent != null) {
			this.parent.continueSaying(s);
		} else if (verbose()) {
			System.out.print(s);
		}
	}

}
