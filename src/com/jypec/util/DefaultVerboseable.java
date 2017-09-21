package com.jypec.util;

/**
 * Default verbose implementation so that inheriting classes don't
 * need to do all the same
 * @author Daniel
 *
 */
public abstract class DefaultVerboseable implements Verboseable {

	private boolean verbose;
	private Verboseable parent;


	@Override
	public void setParentVerboseable(Verboseable parent) {
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
			this.parent.say(Verboseable.INDENT + s);	
		} else if (verbose()) {
			System.out.print(s);
		}
	}
	
	@Override
	public void sayLn(String s) {
		if (parent != null) {
			this.parent.sayLn(Verboseable.INDENT + s);	
		} else if (verbose()) {
			System.out.println(s);
		}
	}

}
