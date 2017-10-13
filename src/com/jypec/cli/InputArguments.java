package com.jypec.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.jypec.util.datastructures.LowKeyHashMap;

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
	/** True if the user whats a tree structure of the file */
	public boolean showTree = false;
	
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
	public boolean requestBits = false;
	/** Number of bits to use when compressing */
	public int bits = -1;
	/** True if wavelet transform was requested */
	public boolean requestWavelet = false;
	/** Number of passes of the wavelet transform, or -1 if not set */
	public int passes = -1;
	/** If we want the inner classes to be verbose */
	public boolean verbose;
	/** Band shaves */
	public LowKeyHashMap<Integer, Integer> shaves;
	/** Percent of samples used for training */
	public double percentTraining;
	/** True if the training is done with less samples than the reduction */
	public boolean requestTrainingReduction;
	/** True if we are supposed to analyze the input image */
	public boolean analyze;
	/** True if outlier detection is to be used */
	public boolean requestOutliers;
	/** Percent of outliers to be hardcoded */
	public double percentOutliers;
	
	
	/**
	 * @param line line where to parse the commands from
	 * @return an InputArguments object filled from the command line
	 * @throws ParseException 
	 */
	public static InputArguments parseFrom(CommandLine line) throws ParseException {
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
		args.showTree = line.hasOption(JypecCLI.OPTION_TREE);
		args.analyze = line.hasOption(JypecCLI.OPTION_ANALYZE);
		
		if (args.requestReduction = line.hasOption(JypecCLI.OPTION_REDUCTION)) {
			args.reductionArgs = line.getOptionValues(JypecCLI.OPTION_REDUCTION);
		}
		if (args.requestBits = line.hasOption(JypecCLI.OPTION_BITS)) {
			args.bits = Integer.parseInt(line.getOptionValue(JypecCLI.OPTION_BITS));
		}
		if (args.requestWavelet = line.hasOption(JypecCLI.OPTION_WAVELET)) {
			args.passes = Integer.parseInt(line.getOptionValue(JypecCLI.OPTION_WAVELET));
		}
		if (args.requestTrainingReduction = line.hasOption(JypecCLI.OPTION_TRAINING_REDUCTION)) {
			args.percentTraining = Double.parseDouble(line.getOptionValue(JypecCLI.OPTION_TRAINING_REDUCTION));
		}
		if (args.requestOutliers = line.hasOption(JypecCLI.OPTION_HARDCODE_OUTLIERS)) {
			args.percentOutliers = Double.parseDouble(line.getOptionValue(JypecCLI.OPTION_HARDCODE_OUTLIERS));
		}
		
		args.shaves = new LowKeyHashMap<Integer, Integer>();
		if (line.hasOption(JypecCLI.OPTION_SHAVE)) {
			String[] values = line.getOptionValues(JypecCLI.OPTION_SHAVE);
			if (values.length % 2 != 0) {
				throw new ParseException("Values for shaving come in pairs");
			}
			for (int i = 0; i < values.length; i+=2) {
				int band = Integer.parseInt(values[i]);
				int shave = Integer.parseInt(values[i+1]);
				args.shaves.put(band, shave);
			}
		}
		
		return args;
		
	}
}
