package com.jypec.dimreduction;

import java.io.IOException;

import org.ejml.data.DMatrixRMaj;

import com.jypec.cli.InputArguments;
import com.jypec.dimreduction.alg.DeletingDimensionalityReduction;
import com.jypec.dimreduction.alg.IndependentComponentAnalysis;
import com.jypec.dimreduction.alg.MinimumNoiseFraction;
import com.jypec.dimreduction.alg.PrincipalComponentAnalysis;
import com.jypec.dimreduction.alg.VectorQuantizationPrincipalComponentAnalysis;
import com.jypec.util.DefaultVerboseable;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * @author Daniel
 * Base interface for implementing various dimensionality reduction algorithms
 */
public abstract class DimensionalityReduction extends DefaultVerboseable {

	/**
	 * @author Daniel
	 * enums all subclasses so that save and loads methods can use codes to reload them
	 */
	public enum DimensionalityReductionAlgorithm {
		/** {@link PrincipalComponentAnalysis}*/
		DRA_PCA, 
		/** {@link DeletingDimensionalityReduction} */
		DRA_DELETING_DIMENSIONALITY_REDUCTION, 
		/** {@link MinimumNoiseFraction} */
		DRA_MNF,
		/** {@link IndependentComponentAnalysis} */
		DRA_ICA,
		/** {@link VectorQuantizationPrincipalComponentAnalysis} */
		DRA_VQPCA
	}
	
	private DimensionalityReductionAlgorithm dra;
	
	/**
	 * @param dra indicates the type of reduction being made
	 */
	public DimensionalityReduction(DimensionalityReductionAlgorithm dra) {
		this.dra = dra;
	}
	
	
	/**
	 * Wrapper to call {@link #train(DMatrixRMaj)} then {@link #reduce(DMatrixRMaj)}
	 * @param source
	 * @return the reduced matrix after training with source
	 */
	public DMatrixRMaj trainReduce(DMatrixRMaj source) {
		this.train(source);
		return this.reduce(source);
	}
	
	/**
	 * Train this dimensionality reduction with the given matrix, to analize and then
	 * be able to {@link #reduce(DMatrixRMaj, DMatrixRMaj)} it (or others)
	 * to a lower dimension space
	 * @param source the source matrix. Samples will be analyzed and
	 * based on similarities, will later be reduced without the loss of significant information,
	 * with calls to {@link #reduce(DMatrixRMaj, DMatrixRMaj)}.
	 * <br>
	 * samples are assumed to be the <b>columns</b> of source
	 */
	public abstract void train(DMatrixRMaj source);
	
	
	/**
	 * Reduces the dimension of the given matrix, into a new space. The given matrix
	 * might be changed.
	 * @param source the source matrix
	 * @return the source matrix projected into the smaller dimension space
	 */
	public abstract DMatrixRMaj reduce(DMatrixRMaj source);
	
	
	
	/**
	 * Boosts a matrix's dimension from the reduced space into the original one.
	 * Spatial dimensions remain unchanged. The input matrices might change.
	 * @param source the source matrix (in the reduced dimension space)
	 * @return the original matrix in the original space
	 */
	public abstract DMatrixRMaj boost(DMatrixRMaj source);
	
	
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
	 * Loads the necessary data from the BitStream so as to be able to {@link #boost(DMatrixRMaj)}
	 * an matrix into its original space. The given BitStream must've been filled with 
	 * {@link #saveTo(BitStreamDataReaderWriter)}
	 * @param bw The BitStream handler that encapsulates the BitStream
	 * @return the proper dimensionality reduction algorithm
	 * @throws IOException 
	 */
	public static final DimensionalityReduction loadFrom(BitInputStream bw) throws IOException {
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
		case DRA_MNF:
			dr = new MinimumNoiseFraction();
			break;
		case DRA_ICA:
			dr = new IndependentComponentAnalysis();
			break;
		case DRA_VQPCA:
			dr = new VectorQuantizationPrincipalComponentAnalysis();
			break;
		default:
			throw new IllegalArgumentException("Cannot load that kind of Dimensionality Reduction algorithm: " + type);
		}
		
		dr.doLoadFrom(bw);
		
		return dr;
	}
	
	/**
	 * Load the information specific to this algorithm
	 * @param bw where to load from
	 * @throws IOException 
	 */
	public abstract void doLoadFrom(BitInputStream bw) throws IOException;
	
	
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
	 * @return the maximum value that the reduced matrix can have on its samples
	 */
	public double getMaxValue(DMatrixRMaj img) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param img where to get the min value from
	 * @return the minimum value that the reduced matrix can have on its samples
	 */
	public double getMinValue(DMatrixRMaj img){
		throw new UnsupportedOperationException();
	}

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
			} else if (args.reductionArgs.length == 2 && args.reductionArgs[0].toLowerCase().equals("mnf")) {
				int dimensions = Integer.parseInt(args.reductionArgs[1]);
				MinimumNoiseFraction mnf = new MinimumNoiseFraction();
				mnf.setNumComponents(dimensions);
				return mnf;
			} else if (args.reductionArgs.length == 2 && args.reductionArgs[0].toLowerCase().equals("ica")) {
				int dimensions = Integer.parseInt(args.reductionArgs[1]);
				IndependentComponentAnalysis ica = new IndependentComponentAnalysis();
				ica.setNumComponents(dimensions);
				return ica;
			} else if (args.reductionArgs.length == 3 && args.reductionArgs[0].toLowerCase().equals("vqpca")) {
				int dimensions = Integer.parseInt(args.reductionArgs[1]);
				int clusters = Integer.parseInt(args.reductionArgs[2]);
				VectorQuantizationPrincipalComponentAnalysis vqpca = new VectorQuantizationPrincipalComponentAnalysis();
				vqpca.setNumComponents(dimensions);
				vqpca.setNumClusters(clusters);
				return vqpca;
			}
		}
		//default to no reduction
		return new DeletingDimensionalityReduction();
	}
	

}
