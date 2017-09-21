package com.jypec.cli;

import org.apache.commons.cli.CommandLine;

/**
 * Store input arguments in their parsed form for easier processing
 * @author Daniel
 */
public class InputArguments {

	//flags
	/** True if compression was requested*/
	public boolean compress = false;
	/** True if decompression was requested */
	public boolean decompress = false;
	/** True if help was requested */
	public boolean help = false;
	/** True if stats are to be shown after compression */
	public boolean showCompressionStats = false;
	/** True if header is not wanted to be outputted */
	public boolean dontOutputHeader = false;
	/** True if comparing two images */
	public boolean compare = false;
	/** True if we only want to output essential header info */
	public boolean essentialHeader = false;
	
	//files
	/** Input file path. Null if not set */
	public String input = null;
	/** Input file header. Null if not set */
	public String inputHeader = null;
	/** Output file path. Null if not set */
	public String output = null;
	/** Output file header. Null if not set */
	public String outputHeader = null;
	
	//compression config
	/** True if dimensionality reduction was requested */
	public boolean requestReduction = false;
	/** Arguments to create the dimensionality reduction */
	public String[] reductionArgs = null;
	/** True if bit shaving was requested */
	public boolean requestShave = false;
	/** Number of bits to be shaved, or -1 if not set */
	public int shave = -1;
	/** True if wavelet transform was requested */
	public boolean requestWavelet = false;
	/** Number of passes of the wavelet transform, or -1 if not set */
	public int passes = -1;
	/** If we want the inner classes to be verbose */
	public boolean verbose;
	
	
	/**
	 * @param line line where to parse the commands from
	 * @return an InputArguments object filled from the command line
	 */
	public static InputArguments parseFrom(CommandLine line) {
		InputArguments args = new InputArguments();

		args.compress = line.hasOption(JypecCLI.OPTION_COMPRESS);
		args.decompress = line.hasOption(JypecCLI.OPTION_DECOMPRESS);
		args.showCompressionStats = line.hasOption(JypecCLI.OPTION_SHOW_COMPRESSION_STATS);
		args.help = line.hasOption(JypecCLI.OPTION_HELP);
		args.input = line.getOptionValue(JypecCLI.OPTION_INPUT);
		args.output = line.getOptionValue(JypecCLI.OPTION_OUTPUT);
		args.inputHeader = line.getOptionValue(JypecCLI.OPTION_INPUT_HEADER);
		args.outputHeader = line.getOptionValue(JypecCLI.OPTION_OUTPUT_HEADER);
		args.dontOutputHeader = line.hasOption(JypecCLI.OPTION_NO_HEADER_OUTPUT);
		args.compare = line.hasOption(JypecCLI.OPTION_COMPARE);
		args.essentialHeader = line.hasOption(JypecCLI.OPTION_OUTPUT_ESSENTIAL_HEADER);
		args.verbose = line.hasOption(JypecCLI.OPTION_VERBOSE);
		
		if (args.requestReduction = line.hasOption(JypecCLI.OPTION_REDUCTION)) {
			args.reductionArgs = line.getOptionValues(JypecCLI.OPTION_REDUCTION);
		}
		if (args.requestShave = line.hasOption(JypecCLI.OPTION_SHAVE)) {
			args.shave = Integer.parseInt(line.getOptionValue(JypecCLI.OPTION_SHAVE));
		}
		if (args.requestWavelet = line.hasOption(JypecCLI.OPTION_WAVELET)) {
			args.passes = Integer.parseInt(line.getOptionValue(JypecCLI.OPTION_WAVELET));
		}
		
		return args;
		
	}
}
