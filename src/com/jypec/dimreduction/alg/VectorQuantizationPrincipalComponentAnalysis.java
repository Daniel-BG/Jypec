package com.jypec.dimreduction.alg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;

import com.jypec.arithco.predict.PredictiveArithmeticCodec;
import com.jypec.arithco.predict.functions.Basic1DPredictiveFunction;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.dimreduction.JSATWrapper;
import com.jypec.dimreduction.SMILEWrapper;
import com.jypec.dimreduction.SMILEWrapper.CentroidWrapper;
import com.jypec.util.arrays.EJMLExtensions;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;
import com.jypec.util.debug.Logger;
import com.jypec.util.debug.Profiler;

import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.clustering.ClustererBase;
import jsat.clustering.KClusterer;
import jsat.clustering.kmeans.ElkanKMeans;
import smile.clustering.KMeans;

/**
 * Implements the VQPCA algorithm from 
 * <a href="http://www.ohsu.edu/xd/education/schools/school-of-medicine/departments/basic-science-departments/biomedical-engineering/people/upload/kambhatlaLeen93-icnn.pdf">here</a>
 * @author Daniel
 */
public class VectorQuantizationPrincipalComponentAnalysis extends DimensionalityReduction {
	
	/**
	 * Library to do vector quantizaiton with
	 * @author Daniel
	 */
	public enum Library {
		/** JSAT library */
		JSAT, 
		/** SMILE library */
		SMILE
	};
	private Library library;

	private int dimOrig;			//number of components in the original space
	private int numClusters;		//number of clusters to split the original space into
	private int[] classification;	//classes of the training points
	private FMatrixRMaj trainedWith;//check that we reduce the same matrix we trained with, Otherwise the algorithm wont work
	private ArrayList<PrincipalComponentAnalysis> pcas;
	private CentroidWrapper centroids; 	//kmeans algorithm result from SMILE library
	
	/**
	 * Default constructor
	 */
	public VectorQuantizationPrincipalComponentAnalysis() {
		super(DimensionalityReductionAlgorithm.DRA_VQPCA);
	}
	
	@Override
	public FMatrixRMaj preprocess(FMatrixRMaj source) {
		return source; //we cannot reduce the source in VQPCA
	}

	@Override
	public boolean doTrain(FMatrixRMaj source) {
		if (library == Library.SMILE) {
			return this.doTrainSMILE(source);
		} else {
			return this.doTrainJSAT(source);
		}
	}
	
	
	private boolean doTrainSMILE(FMatrixRMaj source) {
		this.trainedWith = source;
		if (this.reductionInTrainingRequested()) {
			source = EJMLExtensions.getSubSet(source, percentTraining);
		}
		
		/** Initialization */
		Logger.getLogger().log("Initializing VQPCA...");
		double[][] dataSet = SMILEWrapper.toDoubleMatrix(source);
		this.pcas = new ArrayList<PrincipalComponentAnalysis>(this.numClusters);
		this.dimOrig = source.getNumRows();
		
		/** Cluster the data */
		Logger.getLogger().log("Clustering data...");
		KMeans kmeans = new KMeans(dataSet, this.numClusters); //self trains when built
		this.centroids = new SMILEWrapper.CentroidWrapper(kmeans.centroids());
		int[] partialClassification = kmeans.getClusterLabel();
		
		/** Perform PCA for each cluster */
		Logger.getLogger().log("Performing " + this.numClusters + " PCAs");
		int fails = 0;
		for (int i = 0; i < this.numClusters; i++) {
			FMatrixRMaj cluster = SMILEWrapper.getDatapointsFromCluster(i + fails, partialClassification, source);
			Logger.getLogger().log("Cluster #" + i + " has size: " + cluster.getNumCols());
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			pca.setNumComponents(dimProj);
			pca.setPercentTraining(percentTraining);
			if (!pca.train(cluster)) {
				Logger.getLogger().log("Could not train PCA since cluster was not big enough. Reducing in 1 number of clusters...");
				this.centroids.deleteCentroid(i);
				i--; fails++;
				this.numClusters--;
			} else {
				pcas.add(pca);
			}
		}
		
		return this.numClusters == 0 ? false : true; 
	}
	
	private boolean doTrainJSAT(FMatrixRMaj source) {
		/** Initialization */
		Logger.getLogger().log("Initializing VQPCA...");
		SimpleDataSet dataSet = JSATWrapper.toDataSet(source);
		this.trainedWith = source;
		this.pcas = new ArrayList<PrincipalComponentAnalysis>(this.numClusters);
		this.dimOrig = source.getNumRows();
		
		/** Cluster the data */
		Logger.getLogger().log("Clustering data...");
		this.classification = new int[source.getNumCols()];
		KClusterer clusterer = new ElkanKMeans();
		clusterer.cluster(dataSet, this.numClusters, null, this.classification);
		
		/** Perform PCA for each cluster */
		Logger.getLogger().log("Performing " + this.numClusters + " PCAs");
		for (int i = 0; i < this.numClusters; i++) {
			List<DataPoint> l = ClustererBase.getDatapointsFromCluster(i, classification, dataSet, null);
			Logger.getLogger().log("Cluster #" + i + " has size: " + l.size());
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			pca.setNumComponents(dimProj);
			pca.setPercentTraining(percentTraining);
			pca.train(JSATWrapper.toFMatrixRMaj(new SimpleDataSet(l)));
			pcas.add(pca);
		}
		
		return true;
	}

	@Override
	public FMatrixRMaj reduce(FMatrixRMaj source) {
		if (source != this.trainedWith) {
			System.err.println("The matrix to be reduced must be the same one this was trained with. Won't work otherwise. "
					+ "Note that this is only a shallow chech for object equality, same contents with different objects will print this");
		}
		Logger.getLogger().log("Reducing dimensionality VQPCA...");
		/** initialize stuff */
		FMatrixRMaj res = new FMatrixRMaj(this.dimProj, source.getNumCols());
		this.classification = new int[source.getNumCols()];
		
		/** Reduce each sample with its cluster's PCA */
		for (int i = 0; i < source.getNumCols(); i++) {
			PrincipalComponentAnalysis pca;
			if (library == Library.JSAT) {
				pca = this.pcas.get(this.classification[i]);
			} else {
				double[] sample = SMILEWrapper.extractSample(source, i);
				int cluster = this.centroids.predict(sample);
				this.classification[i] = cluster;
				pca = this.pcas.get(cluster);
			}
			FMatrixRMaj col = CommonOps_FDRM.extractColumn(source, i, null);
			col = pca.reduce(col);
			CommonOps_FDRM.insert(col, res, 0, i);
		}
		
		return res;
	}

	@Override
	public FMatrixRMaj boost(FMatrixRMaj source) {
		/** Initialize stuff */
		FMatrixRMaj res = new FMatrixRMaj(this.dimOrig, source.getNumCols());

		/** Boost each sample with its cluster's pca */
		for (int i = 0; i < source.getNumCols(); i++) {
			PrincipalComponentAnalysis pca = this.pcas.get(this.classification[i]);
			FMatrixRMaj col = CommonOps_FDRM.extractColumn(source, i, null);
			col = pca.boost(col);
			CommonOps_FDRM.insert(col, res, 0, i);
		}
		
		return res;
	}

	@Override
	public void doSaveTo(BitOutputStreamTree bw) throws IOException {
		Profiler.getProfiler().profileStart();
		/** write metadata */
		bw.addChild("dim proj").writeInt(this.dimProj);
		bw.addChild("dim orig").writeInt(this.dimOrig);
    	bw.addChild("num clusters").writeInt(this.numClusters);
    	
    	/** arith code the cluster indices */
    	long cbits = bw.getTreeBits();
    	PredictiveArithmeticCodec pac = new PredictiveArithmeticCodec(new Basic1DPredictiveFunction());
    	pac.code(this.classification, numClusters, bw.addChild("class classification"));
    	cbits = bw.getTreeBits() - cbits;
    	if (cbits % 8 != 0) {
    		bw.addChild("padding").writeNBitNumber(0, 8 - (int) (cbits % 8)); //padding
    	}
    	
    	/** write each pca */
    	for (PrincipalComponentAnalysis pca: pcas) {
    		pca.doSaveTo(bw.addChild("dr#"));
    	}
    	Profiler.getProfiler().profileEnd();
	}

	@Override
	public void doLoadFrom(BitInputStream bw) throws IOException {
		/** load metadata */
		this.dimProj = bw.readInt();
		this.dimOrig = bw.readInt();
		this.numClusters = bw.readInt();
		
		/** arith decode the cluster indices */
		int cbits = bw.getBitsInput();
		PredictiveArithmeticCodec pac = new PredictiveArithmeticCodec(new Basic1DPredictiveFunction());
    	this.classification = pac.decode(numClusters, bw);
		cbits = bw.getBitsInput() - cbits;
		if (cbits % 8 != 0) {
		bw.readNBitNumber(8 - (cbits % 8));
		}
		
		/** load each pca */
		this.pcas = new ArrayList<PrincipalComponentAnalysis>(numClusters);
		for (int i = 0; i < numClusters; i++) {
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			pca.doLoadFrom(bw);
			this.pcas.add(pca);
		}
	}
	
	/**
	 * Set the number of clusters the original space
	 * will be split into
	 * @param numClusters
	 */
	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}
	
	/**
	 * @param library the library to use 
	 */
	public void setLibrary(Library library) {
		this.library = library;
	}

	@Override
	protected void doLoadFrom(String[] args) {
		int dimensions = Integer.parseInt(args[0]);
		int clusters = Integer.parseInt(args[1]);
		if (args.length < 3) {
			this.setLibrary(Library.SMILE);
		} else {
			Library library = Library.valueOf(args[2].toUpperCase());
			this.setLibrary(library);
		}
		this.setNumComponents(dimensions);
		this.setNumClusters(clusters);
		
	}



}
