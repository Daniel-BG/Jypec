package com.jypec.dimreduction.alg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.dimreduction.JSATWrapper;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;
import com.jypec.util.bits.BitTwiddling;

import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.clustering.ClustererBase;
import jsat.clustering.kmeans.ElkanKMeans;
import jsat.clustering.kmeans.KMeans;

/**
 * Implements the VQPCA algorithm from 
 * <a href="http://www.ohsu.edu/xd/education/schools/school-of-medicine/departments/basic-science-departments/biomedical-engineering/people/upload/kambhatlaLeen93-icnn.pdf">here</a>
 * @author Daniel
 */
public class VectorQuantizationPrincipalComponentAnalysis extends DimensionalityReduction {

	private int dimOrig;	//number of components in the original space
	private int dimProj;	//number of components to retain on each cluster
	private int numClusters;	//number of clusters to split the original space into
	private int[] classification; //classes of the training points
	private DMatrixRMaj trainedWith; //check that we reduce the same matrix we trained with, Otherwise the algorithm wont work
	private ArrayList<PrincipalComponentAnalysis> pcas;
	
	/**
	 * Default constructor
	 */
	public VectorQuantizationPrincipalComponentAnalysis() {
		super(DimensionalityReductionAlgorithm.DRA_VQPCA);
	}

	@Override
	public void train(DMatrixRMaj source) {
		/** Initialization */
		this.sayLn("Initializing VQPCA...");
		KMeans kmeans = new ElkanKMeans();
		SimpleDataSet dataSet = JSATWrapper.toDataSet(source);
		this.trainedWith = source;
		this.pcas = new ArrayList<PrincipalComponentAnalysis>(this.numClusters);
		this.dimOrig = source.getNumRows();
		
		/** Cluster the data */
		this.sayLn("Clustering data...");
		this.classification = new int[source.getNumCols()];
		kmeans.cluster(dataSet, this.numClusters, null, this.classification);
		List<List<DataPoint>> list = ClustererBase.createClusterListFromAssignmentArray(this.classification, dataSet);
		
		/** Perform PCA for each cluster */
		this.sayLn("Performing " + list.size() + " PCAs");
		for (List<DataPoint> l: list) {
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			pca.setParentVerboseable(this);
			pca.setNumComponents(dimProj);
			pca.train(JSATWrapper.toDMatrixRMaj(new SimpleDataSet(l)));
			pcas.add(pca);
		}
	}

	@Override
	public DMatrixRMaj reduce(DMatrixRMaj source) {
		if (source != this.trainedWith) {
			throw new IllegalArgumentException("The matrix to be reduced must be the same one this was trained with. Won't work otherwise. "
					+ "Note that this is only a shallow chech for object equality, and contents (which should be the same) are not checked");
		}
		/** initialize stuff */
		DMatrixRMaj res = new DMatrixRMaj(this.dimProj, source.getNumCols());
		
		/** Reduce each sample with its cluster's PCA */
		for (int i = 0; i < source.getNumCols(); i++) {
			PrincipalComponentAnalysis pca = this.pcas.get(this.classification[i]);
			DMatrixRMaj col = CommonOps_DDRM.extractColumn(source, i, null);
			col = pca.reduce(col);
			CommonOps_DDRM.insert(col, res, 0, i);
		}
		
		return res;
	}

	@Override
	public DMatrixRMaj boost(DMatrixRMaj source) {
		/** Initialize stuff */
		DMatrixRMaj res = new DMatrixRMaj(this.dimOrig, source.getNumCols());

		/** Boost each sample with its cluster's pca */
		for (int i = 0; i < source.getNumCols(); i++) {
			PrincipalComponentAnalysis pca = this.pcas.get(this.classification[i]);
			DMatrixRMaj col = CommonOps_DDRM.extractColumn(source, i, null);
			col = pca.boost(col);
			CommonOps_DDRM.insert(col, res, 0, i);
		}
		
		return res;
	}

	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
		//write the number of components
		bw.writeInt(this.dimProj);
		bw.writeInt(this.dimOrig);
    	//write the number of clusters
    	bw.writeInt(this.numClusters);
    	//write the cluster indices
    	int len = this.classification.length, bits = BitTwiddling.bitsOf(numClusters - 1);
    	bw.writeInt(len);
    	bw.writeNBitNumberArray(this.classification, bits, len);
    	//padding
    	bw.writeNBitNumber(0, 8 - (len*bits % 8));
    	//write each pca
    	for (PrincipalComponentAnalysis pca: pcas) {
    		pca.doSaveTo(bw);
    	}
	}

	@Override
	public void doLoadFrom(BitInputStream bw) throws IOException {
		//load metadata
		this.dimProj = bw.readInt();
		this.dimOrig = bw.readInt();
		this.numClusters = bw.readInt();
		int len = bw.readInt();
		int bits = BitTwiddling.bitsOf(numClusters - 1);
		this.classification = bw.readNBitNumberArray(bits, len);
		bw.readNBitNumber(8 - (len*bits % 8));
		this.pcas = new ArrayList<PrincipalComponentAnalysis>(numClusters);
		// Load pcas
		for (int i = 0; i < numClusters; i++) {
			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			pca.setParentVerboseable(this);
			pca.doLoadFrom(bw);
			this.pcas.add(pca);
		}
	}

	@Override
	public int getNumComponents() {
		return this.dimProj;
	}

	@Override
	public void setNumComponents(int numComponents) {
		this.dimProj = numComponents;
	}
	
	/**
	 * Set the number of clusters the original space
	 * will be split into
	 * @param numClusters
	 */
	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}

}
