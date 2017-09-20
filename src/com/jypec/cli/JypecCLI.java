package com.jypec.cli;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Anything related to command line interface stuff goes here
 * @author Daniel
 */
public class JypecCLI {
	/** Help option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_HELP = "help";
	/** Compress option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_COMPRESS = "compress";
	/** Decompress option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_DECOMPRESS = "decompress";
	/** Input option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_INPUT = "input";
	/** Input header option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_INPUT_HEADER = "input_header";
	/** Output option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_OUTPUT = "output";
	/** Output header option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_OUTPUT_HEADER = "output_header";
	/** Reduction algorithm option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_REDUCTION = "reduction";
	/** Wavelet algorithm option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_WAVELET = "wavelet";
	/** JPEG2000 bitshave option constant. Use for retrieving arguments and/or flags */
	public static final String OPTION_SHAVE = "shave";
	/** Show compressions stats. Use for retrieving arguments and/or flags  */
	public static final String OPTION_SHOW_COMPRESSION_STATS = "stats";
	/** Flag to not output the header. Use for retrieving arguments and/or flags  */
	public static final String OPTION_NO_HEADER_OUTPUT = "no_header_output";
	/** Flag to not output the header. Use for retrieving arguments and/or flags  */
	public static final String OPTION_COMPARE = "compare";
	
	/* Options for jypec */
	private static Options jypecOptions;
	/* Only one instance */
	static {
		/* flags */
		Option help				= new Option("h", OPTION_HELP, false, "print this message");
		Option compress			= new Option("c", OPTION_COMPRESS, false, "compress the input image");
		Option decompress		= new Option("d", OPTION_DECOMPRESS, false, "decompress the input image");
		Option compare			= new Option("k", OPTION_COMPARE, false, "compare the input and output images"); //k for kompare
		Option compressionStats = new Option(null, OPTION_SHOW_COMPRESSION_STATS, false, "show compression stats");
		Option noHeaderOutput 	= new Option(null, OPTION_NO_HEADER_OUTPUT, false, "do not output the header");
		
		
		/* input output files */
		Option input = Option
				.builder("i")
				.argName("file")
				.desc("input file")
				.hasArg()
				.longOpt(OPTION_INPUT)
				.required()
				.build();
		
		Option inputHeader = Option
				.builder()
				.argName("file")
				.desc("input file header location")
				.hasArg()
				.longOpt(OPTION_INPUT_HEADER)
				.build();
		
		Option output = 	Option
				.builder("o")
				.argName("file")
				.desc("output file")
				.hasArg()
				.longOpt(OPTION_OUTPUT)
				.required()
				.build();
		
		Option outputHeader = Option
				.builder()
				.argName("file")
				.desc("output file header location")
				.hasArg()
				.longOpt(OPTION_OUTPUT_HEADER)
				.build();
		
		/* algorithm options (only if compressing) */
		
		Option reduction = Option
				.builder("r")
				.desc("dimensionality reduction algorithm and options")
				.hasArgs()
				.argName("args[]")
				.longOpt(OPTION_REDUCTION)
				.build();
		
		Option wavelet = Option
				.builder("w")
				.desc("number of wavelet passes")
				.hasArg()
				.argName("passes")
				.longOpt(OPTION_WAVELET)
				.build();
		
		Option bitshave = Option
				.builder("s")
				.desc("Bits to shave from the encoded bitplanes")
				.hasArg()
				.argName("quantity")
				.longOpt(OPTION_SHAVE)
				.build();
		
		jypecOptions = new Options();
		
		jypecOptions.addOption(bitshave);
		jypecOptions.addOption(wavelet);
		jypecOptions.addOption(reduction);
		jypecOptions.addOption(output);
		jypecOptions.addOption(input);
		jypecOptions.addOption(compress);
		jypecOptions.addOption(decompress);
		jypecOptions.addOption(help);
		jypecOptions.addOption(compressionStats);
		jypecOptions.addOption(inputHeader);
		jypecOptions.addOption(outputHeader);
		jypecOptions.addOption(noHeaderOutput);
		jypecOptions.addOption(compare);
	}
	
	
	/**
	 * testing
	 * @return the options for the jypec cli
	 */
	public static Options getOptions() {
		return jypecOptions;
	}

	/**
	 * Prints the help for the command line interface of jypec
	 */
	public static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "jypec", JypecCLI.getOptions());
	}

}
