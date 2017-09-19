package com.jypec.dimreduction;

import java.io.IOException;

import com.jypec.cli.InputArguments;
import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.alg.DeletingDimensionalityReduction;
import com.jypec.dimreduction.alg.PrincipalComponentAnalysis;
import com.jypec.img.HyperspectralImage;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.io.headerio.ImageHeaderData;

/**
 * @author Daniel
 * Base interface for implementing various dimensionality reduction algorithms
 */
public abstract class DimensionalityReduction {
	
	/**
	 * @author Daniel
	 * enums all subclasses so that save and loads methods can use codes to reload them
	 */
	public enum DimensionalityReductionAlgorithm {
		/** {@link PrincipalComponentAnalysis}*/
		DRA_PCA, 
		/** {@link DeletingDimensionalityReduction} */
		DRA_DELETING_DIMENSIONALITY_REDUCTION
	}
	
	private DimensionalityReductionAlgorithm dra;
	
	/**
	 * @param dra indicates the type of reduction being made
	 */
	public DimensionalityReduction(DimensionalityReductionAlgorithm dra) {
		this.dra = dra;
	}
	
	
	/**
	 * Train this dimensionality reduction with the given image, to analize and then
	 * be able to {@link #reduce(HyperspectralImage, HyperspectralImage)} it (or others)
	 * to a lower dimension space
	 * @param source the source image. Pixels will be analyzed (in the spectral dimension) and
	 * based on similarities, will later be reduced without the loss of significant information,
	 * with calls to {@link #reduce(HyperspectralImage, HyperspectralImage)}
	 */
	public abstract void train(HyperspectralImage source);
	
	
	/**
	 * Reduces the spectral dimension of the given image, into a new space. 
	 * The spatial dimensions of the image remain unchanged. 
	 * @param source the source image
	 * @return the source image projected into the smaller dimension space
	 */
	public abstract double[][][] reduce(HyperspectralImage source);
	
	
	
	/**
	 * Boosts an image's spectral dimension from the reduced space into the original one.
	 * Spatial dimensions remain unchanged
	 * @param source the source image (in the reduced dimension space)
	 * @param dst will hold the result: the original image in the original space
	 */
	public abstract void boost(double[][][] source, HyperspectralImage dst);
	
	
	/**
	 * Saves the necessary information into the given bistream so as to later
	 * reconstruct this Object from a call to {@link #loadFrom(BitStreamDataReaderWriter)}
	 * @param bw The BitStream handler that encapsulates the BitStream
	 * @throws IOException 
	 */
	public final void saveTo(BitOutputStream bw) throws IOException {
		bw.writeByte((byte) this.dra.ordinal());
		this.doSaveTo(bw);
	}
	

	/**
	 * Save the information specific to each algorithm
	 * @param bw
	 * @throws IOException 
	 */
	public abstract void doSaveTo(BitOutputStream bw) throws IOException;
	
	
	/**
	 * Loads the necessary data from the BitStream so as to be able to {@link #boost(HyperspectralImage)}
	 * an image into its original space. The given BitStream must've been filled with 
	 * {@link #saveTo(BitStreamDataReaderWriter)}
	 * @param bw The BitStream handler that encapsulates the BitStream
	 * @param cp Compressor Parameters in case it needs global info to restore
	 * @param ihd Image parameters in case it needs information
	 * @return the proper dimensionality reduction algorithm
	 * @throws IOException 
	 */
	public static final DimensionalityReduction loadFrom(BitInputStream bw, ComParameters cp, ImageHeaderData ihd) throws IOException {
		DimensionalityReduction dr;
		byte type = bw.readByte();
		
		if (type < 0 || type > DimensionalityReductionAlgorithm.values().length) {
			throw new IllegalArgumentException("Cannot load that kind of Dimensionality Reduction algorithm: " + type);
		}
		
		DimensionalityReductionAlgorithm dra = DimensionalityReductionAlgorithm.values()[type];
		
		switch(dra) {
		case DRA_DELETING_DIMENSIONALITY_REDUCTION:
			dr = new DeletingDimensionalityReduction();
			break;
		case DRA_PCA:
			dr = new PrincipalComponentAnalysis();
			break;
		default:
			throw new IllegalArgumentException("Cannot load that kind of Dimensionality Reduction algorithm: " + type);
		}
		
		dr.doLoadFrom(bw, cp, ihd);
		
		return dr;
	}
	
	/**
	 * Load the information specific to this algorithm
	 * @param bw
	 * @param cp
	 * @param ihd
	 * @throws IOException 
	 */
	public abstract void doLoadFrom(BitInputStream bw, ComParameters cp, ImageHeaderData ihd) throws IOException;
	
	
	/**
	 * @return the target dimension the algorithm is reducing to / restoring from
	 */
	public abstract int getNumComponents();
	
	/**
	 * Set the number of components this dimensionality reduction will be reducing to
	 * @param numComponents 
	 */
	public abstract void setNumComponents(int numComponents);

	/**
	 * @param img where to get the max value from
	 * @return the maximum value that the reduced image can have on its samples
	 */
	public abstract double getMaxValue(HyperspectralImage img);
	
	/**
	 * @param img where to get the min value from
	 * @return the minimum value that the reduced image can have on its samples
	 */
	public abstract double getMinValue(HyperspectralImage img);

	/**
	 * Load the proper dimensionality reduction algorithm selected in the input arguments
	 * @param args
	 * @return the selected algorithm
	 */
	public static DimensionalityReduction loadFrom(InputArguments args) {
		if (args.requestReduction) {
			//only PCA for now
			if (args.reductionArgs.length == 2 && args.reductionArgs[0].toLowerCase().equals("pca")) {
				int dimensions = Integer.parseInt(args.reductionArgs[1]);
				PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
				pca.setNumComponents(dimensions);
				return pca;
			}
		}
		//default to no reduction
		return new DeletingDimensionalityReduction();
	}
	

}
