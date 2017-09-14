package com.jypec;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import com.jypec.cli.InputArguments;
import com.jypec.cli.JypecCLI;

/**
 * Entry point
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
	    //create the parser
	    CommandLineParser parser = new DefaultParser();
	    try {
	        //parse the command line arguments
	        CommandLine line = parser.parse( JypecCLI.getOptions(), args );
	        InputArguments iArgs = InputArguments.parseFrom(line);
	        //go through options
	        if (iArgs.compress) {
	        	Jypec.compress(iArgs);
	        } else if (iArgs.decompress) {
	        	Jypec.decompress(iArgs);
	        } else if (iArgs.help){
	        	printHelp();
	        } 
	        throw new ParseException("Missing options, i don't know what to do");
	    }
	    catch( ParseException exp ) {
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	        printHelp();
	    }
	}
	
	
	/**
	 * Prints help for the command line interface
	 */
	private static void printHelp() {
		JypecCLI.printHelp();
	}

}
