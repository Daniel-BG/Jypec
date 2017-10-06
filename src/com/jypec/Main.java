package com.jypec;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import com.jypec.cli.InputArguments;
import com.jypec.cli.JypecCLI;
import com.jypec.util.JypecException;
import com.jypec.util.debug.Logger;

/**
 * Entry point
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//static initializations
		Logger.getLogger().setBaseLine();
	    //create the parser
	    CommandLineParser parser = new DefaultParser();
	    try {
	        //parse the command line arguments
	        CommandLine line = parser.parse( JypecCLI.getOptions(), args );
	        InputArguments iArgs = InputArguments.parseFrom(line);
	        Logger.getLogger().setLogging(iArgs.verbose);
	        Logger.getLogger().log("Executing args: " + String.join(" ", args));
	        //go through options
	        if (iArgs.compress) {
	        	Jypec.compress(iArgs);
	        } else if (iArgs.decompress) {
	        	Jypec.decompress(iArgs);
	        } else if (iArgs.compare) {
	        	Jypec.compare(iArgs);
	        } else if (iArgs.help){
	        	printHelp();
	        } else {
	        	throw new ParseException("Missing options -c -d, i don't know what to do");
	        }
	    }
	    catch( ParseException exp ) {
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	        printHelp();
	    }
	    catch( IOException ioe) {
	    	System.err.println( "Failed when reading/writing.  Reason: " + ioe.getMessage() );
	    } catch (JypecException je) {
	    	System.err.println( "Jypec raised an exception.  Reason: " + je.getMessage() );
		}
	}
	
	
	/**
	 * Prints help for the command line interface
	 */
	private static void printHelp() {
		JypecCLI.printHelp();
	}

}
