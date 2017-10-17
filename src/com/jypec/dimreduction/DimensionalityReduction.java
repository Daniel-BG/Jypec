package com.jypec.dimreduction;

import java.io.IOException;
import java.util.Arrays;

import org.ejml.data.FMatrixRMaj;

import com.jypec.cli.InputArguments;
import com.jypec.dimreduction.alg.DeletingDimensionalityReduction;
import com.jypec.dimreduction.alg.IndependentComponentAnalysis;
import com.jypec.dimreduction.alg.MinimumNoiseFraction;
import com.jypec.dimreduction.alg.PrincipalComponentAnalysis;
import com.jypec.dimreduction.alg.SingularValueDecomposition;
import com.jypec.dimreduction.alg.VectorQuantizationPrincipalComponentAnalysis;
import com.jypec.dimreduction.alg.VertexComponentAnalysis;
import com.jypec.util.arrays.EJMLExtensions;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;
import com.jypec.util.debug.Profiler;

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
		DRA_DELETING_DIMENSIONALITY_REDUCTION, 
		/** {@link MinimumNoiseFraction} */
		DRA_MNF,
		/** {@link IndependentComponentAnalysis} */
		DRA_ICA,
		/** {@link VectorQuantizationPrincipalComponentAnalysis} */
		DRA_VQPCA,
		/** {@link PrincipalComponentAnalysisSVD} */
		DRA_PCASVD,
		/** {@link VertexComponentAnalysis} */
		DRA_VCA
	}
	
	private DimensionalityReductionAlgorithm dra;
	protected int dimProj = -1;
	protected double percentTraining = PERCENT_FULL;
	private static final double PERCENT_FULL = 1;
	
	/**
	 * @param dra indicates the type of reduction being made
	 */
	public DimensionalityReduction(DimensionalityReductionAlgorithm dra) {
		this.dra = dra;
	}
	
	
	/**
	 * Wrapper to call {@link #train(FMatrixRMaj)} then {@link #reduce(FMatrixRMaj)}
	 * @param source
	 * @return the reduced matrix after training with source
	 */
	public FMatrixRMaj trainReduce(FMatrixRMaj source) {
		Profiler.getProfiler().profileStart();
		this.train(source);
		FMatrixRMaj res = this.reduce(source);
		Profiler.getProfiler().profileEnd();
		return res;
	}
	
	/**
	 * Train this dimensionality reduction with the given matrix, to analize and then
	 * be able to {@link #reduce(FMatrixRMaj, FMatrixRMaj)} it (or others)
	 * to a lower dimension space
	 * @param source the source matrix. Samples will be analyzed and
	 * based on similarities, will later be reduced without the loss of significant information,
	 * with calls to {@link #reduce(FMatrixRMaj, FMatrixRMaj)}.
	 * <br>
	 * samples are assumed to be the <b>columns</b> of source
	 * @return true if training was sucessful
	 */
	public final boolean train(FMatrixRMaj source) {
		FMatrixRMaj res = this.preprocess(source);
		return this.doTrain(res);
	}
	
	
	/**
	 * Do the actual training after potentially reducing
	 * the number of input data with {@link #preprocess(FMatrixRMaj)}
	 * @param source
	 * @return true if training was sucessful
	 */
	public abstract boolean doTrain(FMatrixRMaj source);
	
	
	/**
	 * @param source
	 * @return a potentially reduced version of the source, so that
	 * {@link #doTrain(FMatrixRMaj)} has a easier time training
	 */
	public FMatrixRMaj preprocess(FMatrixRMaj source) {
		Profiler.getProfiler().profileStart();
		if (this.reductionInTrainingRequested()) {
			source = EJMLExtensions.getSubSet(source, percentTraining);
		}
		Profiler.getProfiler().profileEnd();
		return source;
	}
	
	
	/**
	 * Reduces the dimension of the given matrix, into a new space. The given matrix
	 * might be changed.
	 * @param source the source matrix
	 * @return the source matrix projected into the smaller dimension space
	 */
	public abstract FMatrixRMaj reduce(FMatrixRMaj source);
	
	
	
	/**
	 * Boosts a matrix's dimension from the reduced space into the original one.
	 * Spatial dimensions remain unchanged. The input matrices might change.
	 * @param source the source matrix (in the reduced dimension space)
	 * @return the original matrix in the original space
	 */
	public abstract FMatrixRMaj boost(FMatrixRMaj source);
	
	
	/**
	 * Saves the necessary information into the given bistream so as to later
	 * reconstruct this Object from a call to {@link #loadFrom(BitStreamDataReaderWriter)}
	 * @param bw The BitStream handler that encapsulates the BitStream
	 * @throws IOException 
	 */
	public final void saveTo(BitOutputStreamTree bw) throws IOException {
		bw.addChild("code").writeByte((byte) this.dra.ordinal());
		this.doSaveTo(bw.addChild("data"));
	}
	

	/**
	 * Save the information specific to each algorithm
	 * @param bw
	 * @throws IOException 
	 */
	public abstract void doSaveTo(BitOutputStreamTree bw) throws IOException;
	
	
	/**
	 * Loads the necessary data from the BitStream so as to be able to {@link #boost(FMatrixRMaj)}
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
		case DRA_PCASVD:
			dr = new SingularValueDecomposition();
			break;
		case DRA_VCA:
			dr = new VertexComponentAnalysis();
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
	public final int getNumComponents() {
		return this.dimProj;
	}
	
	/**
	 * Set the number of components this dimensionality reduction will be reducing to
	 * @param numComponents 
	 */
	public final void setNumComponents(int numComponents) {
		this.dimProj = numComponents;
	}
	
	
	/**
	 * @param percentTraining the percent of samples used for training
	 */
	public final void setPercentTraining(double percentTraining) {
		if (percentTraining < 0 || percentTraining > PERCENT_FULL) {
			throw new IllegalArgumentException("Percent of training must be between 0 and 1");
		}
		this.percentTraining = percentTraining;
	}
	
	/**
	 * @return true if this dim reduction is supposed to reduce the input dataset size
	 * before training
	 */
	public final boolean reductionInTrainingRequested() {
		return this.percentTraining != PERCENT_FULL;
	}

	/**
	 * @param img where to get the max value from
	 * @return the maximum value that the reduced matrix can have on its samples
	 */
	public float getMaxValue(FMatrixRMaj img) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param img where to get the min value from
	 * @return the minimum value that the reduced matrix can have on its samples
	 */
	public float getMinValue(FMatrixRMaj img){
		throw new UnsupportedOperationException();
	}

	/**
	 * Load the proper dimensionality reduction algorithm selected in the input arguments
	 * @param args
	 * @return the selected algorithm
	 */
	public static DimensionalityReduction loadFrom(InputArguments args) {
		if (args.requestReduction) {
			if (args.reductionArgs == null || args.reductionArgs.length < 1) {
				throw new IllegalArgumentException("Need at least the name of the reduction algorithm");
			}
			DimensionalityReduction dr;
			
			switch(args.reductionArgs[0].toLowerCase()) {
				case "pca":
					dr = new PrincipalComponentAnalysis();
					break;
				case "mnf":
					dr = new MinimumNoiseFraction();
					break;
				case "ica":
					dr = new IndependentComponentAnalysis();
					break;
				case "vqpca":
					dr = new VectorQuantizationPrincipalComponentAnalysis();
					break;
				case "svd":
					dr = new SingularValueDecomposition();
					break;
				case "vca":
					dr = new VertexComponentAnalysis();
					break;
				default:
					throw new UnsupportedOperationException("The algorithm: " + args.reductionArgs[0] + " requested is not available");
			}
			dr.doLoadFrom(Arrays.copyOfRange(args.reductionArgs, 1, args.reductionArgs.length));
			return dr;
			
		}
		//default to no reduction
		return new DeletingDimensionalityReduction();
	}
	
	/**
	 * Used to load specific parameters of algorithms, once known its type
	 * @param args
	 */
	protected abstract void doLoadFrom(String[] args);

}
