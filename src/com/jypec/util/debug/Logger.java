package com.jypec.util.debug;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Logger to aid with tracing execution.
 * @author Daniel
 *
 */
public class Logger {
	//inner classes
	/** Severity scale for the different types of message of the logger */
	public enum SeverityScale {
		/** Informative message, can be safely ignored */
		INFO, 
		/** Warning message. Not fatal but should not be ignored either */
		WARNING, 
		/** An error ocurred. Maybe the program recovers from it, but must be looked into */
		ERROR
	}
	/** The different parameters this logger has. All can be set up later via calls to the Logger object */
	public enum LoggerParameter {
		/** Flag indicating if the INFO messages are to be shown */
		SHOW_INFO, 
		/** Flag indicating if the WARNING messages are to be shown */
		SHOW_WARNING,
		/** Flag indicating if the ERROR messages are to be shown */
		SHOW_ERROR, 
		/** Flag indicating if the Logger should exit when encountering an error*/
		TERMINATE_ON_ERROR, 
		/** Flag indicating if the messages are to be logged for further processing */
		LOG_MESSAGES, 
		/** Value of the default severity of the messages given to the Logger. Can be anything of SeverityScale */
		DEFAULT_SEVERITY
	}
	
	//singleton setup
	private static Logger instance;
	
	static {
		Logger.instance = new Logger(System.out, System.err);
	}
	
	//inner variables
	private boolean show_info;
	private boolean show_warning;
	private boolean show_error;
	private boolean terminate_on_error;
	private boolean log_messages;
	private SeverityScale default_severity = SeverityScale.INFO;
	
	private LinkedList<String> messageLogs;
	private PrintStream out, err;
	
	private Set<Class<? extends Object>> allowedClasses;

	
	/**
	 * Private constructor. Do not allow access from the outside
	 * Just in case we need multiple loggers in the future.
	 * Could've been done all static
	 */
	private Logger(PrintStream out, PrintStream err) {
		this.messageLogs = new LinkedList<String>();
		this.allowedClasses = new HashSet<Class<? extends Object>>();
		this.out = out;
		this.err = err;
	}
	
	/**
	 * @return the only instance of the debugger
	 */
	public static Logger logger() {
		return Logger.instance;
	}
	
	/**
	 * Log with the default SeverityScale
	 * @param sender the Class of the object that logs this message
	 * @param message
	 */
	public void log (Class<? extends Object> sender, String message) {
		this.log(sender, message, this.default_severity);
	}
	/**
	 * Log with the default SeverityScale
	 * @param sender the object that logs this message
	 * @param message
	 */
	public void log (Object sender, String message) {
		this.log(sender.getClass(), message, this.default_severity);
	}
	
	/**
	 * Add a message to the logger with the given severity.
	 * Act according to the loggers configuration
	 * @param sender Class of the object sending the message
	 * @param message
	 * @param sev
	 */
	public void log (Class<? extends Object> sender, String message, SeverityScale sev) {
		if (!this.allowedClasses.contains(sender)) {
			return; //do not log if class is not allowed to log
		}
		
		if (this.log_messages) {
			this.messageLogs.add(message);
		}
		switch (sev) {
		case ERROR:
			if (this.show_error) {
				this.err.println(message);
			}
			if (this.terminate_on_error) {
				System.exit(1);
			}
			break;
		case INFO:
			if (this.show_info) {
				this.out.println(message);
			}
			break;
		case WARNING:
			if (this.show_warning) {
				this.out.println(message);
			}
			break;
		}
	}
	/**
	 * Add a message to the logger with the given severity.
	 * Act according to the loggers configuration
	 * @param sender the Object logging the message
	 * @param message
	 * @param sev
	 */
	public void log(Object sender, String message, SeverityScale sev) {
		this.log(sender.getClass(), message, sev);
	}
	
	/**
	 * Configre the logger parameter p with the given value.
	 * Value is a integer that can be interpreted as:
	 * 		-an integer (Duh)
	 * 		-a boolean (0-> false | other -> true)
	 * 		-an enum (given by .ordinal())
	 * @param p
	 * @param value
	 */
	public void set (LoggerParameter p, int value) {
		switch(p) {
		case LOG_MESSAGES:
			this.log_messages = value != 0;
			break;
		case SHOW_ERROR:
			this.show_error = value != 0;
			break;
		case SHOW_INFO:
			this.show_info = value != 0;
			break;
		case SHOW_WARNING:
			this.show_warning = value != 0;
			break;
		case TERMINATE_ON_ERROR:
			this.terminate_on_error = value != 0;
			break;
		case DEFAULT_SEVERITY:
			this.default_severity = SeverityScale.values()[value];
			break;
		}
	}
	
	/**
	 * Allow objects of a certainc class to produce logs.
	 * @param cls
	 */
	public void allowLogging(Class<? extends Object> cls) {
		this.allowedClasses.add(cls);
	}
	/**
	 * Allow all objects of the class of 'o' to produce logs
	 * @param o
	 */
	public void allowLogging(Object o) {
		this.allowLogging(o.getClass());
	}
	
	
}
