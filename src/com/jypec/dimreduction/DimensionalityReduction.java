package com.jypec.dimreduction;

import com.jypec.comdec.ComParameters;
import com.jypec.img.HyperspectralImage;
import com.jypec.util.bits.BitStreamDataReaderWriter;

/**
 * @author Daniel
 * Base interface for implementing various dimensionality reduction algorithms
 */
public interface DimensionalityReduction {
	
	/**
	 * Train this dimensionality reduction with the given image, to analize and then
	 * be able to {@link #reduce(HyperspectralImage, HyperspectralImage)} it (or others)
	 * to a lower dimension space
	 * @param source the source image. Pixels will be analyzed (in the spectral dimension) and
	 * based on similarities, will later be reduced without the loss of significant information,
	 * with calls to {@link #reduce(HyperspectralImage, HyperspectralImage)}
	 * @param targetDimension target dimension, which can be met for some algorithms, and for others
	 * will act as a guide
	 */
	public void train(HyperspectralImage source, int targetDimension);
	
	
	/**
	 * Reduces the spectral dimension of the given image, into a new space. 
	 * The spatial dimensions of the image remain unchanged. 
	 * @param source the source image
	 * @param dst will hold the result: the source image projected into the smaller dimension space
	 */
	public void reduce(HyperspectralImage source, HyperspectralImage dst);
	
	
	
	/**
	 * Boosts an image's spectral dimension from the reduced space into the original one.
	 * Spatial dimensions remain unchanged
	 * @param source the source image (in the reduced dimension space)
	 * @param dst will hold the result: the original image in the original space
	 */
	public void boost(HyperspectralImage source, HyperspectralImage dst);
	
	
	
	/**
	 * Saves the necessary information into the given bistream so as to later
	 * reconstruct this Object from a call to {@link #loadFrom(BitStreamDataReaderWriter)}
	 * @param bw The BitStream handler that encapsulates the BitStream
	 */
	public void saveTo(BitStreamDataReaderWriter bw);
	
	
	/**
	 * Loads the necessary data from the BitStream so as to be able to {@link #boost(HyperspectralImage)}
	 * an image into its original space. The given BitStream must've been filled with 
	 * {@link #saveTo(BitStreamDataReaderWriter)}
	 * @param bw The BitStream handler that encapsulates the BitStream
	 * @param cp Compressor Parameters in case it needs global info to restore
	 */
	public void loadFrom(BitStreamDataReaderWriter bw, ComParameters cp);
	
	
	/**
	 * @return the target dimension the algorithm is reducing to / restoring from
	 */
	public int getNumComponents();

	/**
	 * @param img where to get the max value from
	 * @return the maximum value that the reduced image can have on its samples
	 */
	public double getMaxValue(HyperspectralImage img);
	
	/**
	 * @param img where to get the min value from
	 * @return the minimum value that the reduced image can have on its samples
	 */
	public double getMinValue(HyperspectralImage img);
	

}
