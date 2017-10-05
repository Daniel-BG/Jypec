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
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;
import com.jypec.util.debug.Logger;

import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.clustering.ClustererBase;
import jsat.clustering.KClusterer;
import jsat.clustering.kmeans.ElkanKMeans;

/**
 * Implements the VQPCA algorithm from 
 * <a href="http://www.ohsu.edu/xd/education/schools/school-of-medicine/departments/basic-science-departments/biomedical-engineering/people/upload/kambhatlaLeen93-icnn.pdf">here</a>
 * @author Daniel
 */
public class VectorQuantizationPrincipalComponentAnalysis extends DimensionalityReduction {

	private int dimOrig;			//number of components in the original space
	private int numClusters;		//number of clusters to split the original space into
	private int[] classification;	//classes of the training points
	private FMatrixRMaj trainedWith;//check that we reduce the same matrix we trained with, Otherwise the algorithm wont work
	private ArrayList<PrincipalComponentAnalysis> pcas;
	
	/**
	 * Default constructor
	 */
	public VectorQuantizationPrincipalComponentAnalysis() {
		super(DimensionalityReductionAlgorithm.DRA_VQPCA);
	}

	@Override
	public void train(FMatrixRMaj source) {
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
			pca.train(JSATWrapper.toFMatrixRMaj(new SimpleDataSet(l)));
			pcas.add(pca);
		}
	}

	@Override
	public FMatrixRMaj reduce(FMatrixRMaj source) {
		if (source != this.trainedWith) {
			System.err.println("The matrix to be reduced must be the same one this was trained with. Won't work otherwise. "
					+ "Note that this is only a shallow chech for object equality, same contents with different objects will print this");
		}
		/** initialize stuff */
		FMatrixRMaj res = new FMatrixRMaj(this.dimProj, source.getNumCols());
		
		/** Reduce each sample with its cluster's PCA */
		for (int i = 0; i < source.getNumCols(); i++) {
			PrincipalComponentAnalysis pca = this.pcas.get(this.classification[i]);
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

	@Override
	protected void doLoadFrom(String[] args) {
		int dimensions = Integer.parseInt(args[0]);
		int clusters = Integer.parseInt(args[1]);
		this.setNumComponents(dimensions);
		this.setNumClusters(clusters);
	}

}
