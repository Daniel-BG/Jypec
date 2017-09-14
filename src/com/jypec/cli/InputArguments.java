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
	/** True if the result shall be embedded */
	public boolean embed = false;
	/** True if help was requested */
	public boolean help = false;
	/** True if stats are to be shown after compression */
	public boolean showCompressionStats;
	
	//files
	/** Input file path. Null if not set */
	public String input = null;
	/** Metadata file path. Null if not set */
	public String metadata = null;
	/** Output file path. Null if not set */
	public String output = null;
	
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
	
	
	/**
	 * @param line line where to parse the commands from
	 * @return an InputArguments object filled from the command line
	 */
	public static InputArguments parseFrom(CommandLine line) {
		InputArguments args = new InputArguments();

		args.compress = line.hasOption(JypecCLI.OPTION_COMPRESS);
		args.decompress = line.hasOption(JypecCLI.OPTION_DECOMPRESS);
		args.embed = line.hasOption(JypecCLI.OPTION_EMBED);
		args.showCompressionStats = line.hasOption(JypecCLI.OPTION_SHOW_COMPRESSION_STATS);
		args.help = line.hasOption(JypecCLI.OPTION_HELP);
		args.input = line.getOptionValue(JypecCLI.OPTION_INPUT);
		args.output = line.getOptionValue(JypecCLI.OPTION_OUTPUT);
		args.metadata = line.getOptionValue(JypecCLI.OPTION_METADATA);
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
