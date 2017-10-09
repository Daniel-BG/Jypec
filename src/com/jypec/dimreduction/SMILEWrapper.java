package com.jypec.dimreduction;

import org.ejml.data.FMatrixRMaj;

import smile.math.Math;

/**
 * Some functions to wrap around the SMILE library methods<br>
 * <a href="https://github.com/haifengl/smile">SMILE library</a>
 * @author Daniel
 *
 */
public class SMILEWrapper {

	/**
	 * @param source
	 * @return source as a double matrix
	 */
	public static double[][] toDoubleMatrix(FMatrixRMaj source) {
		double[][] res = new double[source.getNumCols()][source.getNumRows()];
		for (int i = 0; i < source.getNumRows(); i++) {
			for (int j = 0; j < source.getNumCols(); j++) {
				res[j][i] = source.get(i, j);
			}
		}
		return res;
	}

	/**
	 * Returns a subset of the columns of <code>source</code>. The colums to keep are the entries 
	 * of <code>partialClassification</code> that match <code>index</code> 
	 * @param index
	 * @param partialClassification
	 * @param source
	 * @return the subset
	 */
	public static FMatrixRMaj getDatapointsFromCluster(int index, int[] partialClassification, FMatrixRMaj source) {
		if (partialClassification.length != source.getNumCols()) {
			throw new IllegalArgumentException("Dimensions do not match");
		}
		
		int count = 0;
		for (int i = 0; i < partialClassification.length; i++) {
			if (partialClassification[i] == index) {
				count++;
			}
		}
		
		FMatrixRMaj res = new FMatrixRMaj(source.getNumRows(), count);
		
		int partialIndex = 0;
		for (int j = 0; j < source.getNumCols(); j++) {
			if (partialClassification[j] == index) {
				for (int i = 0; i < source.getNumRows(); i++) {
					res.set(i, partialIndex, source.get(i, j));
				}
				partialIndex++;
			}
		}
		
		return res;
	}

	/**
	 * @param source
	 * @param index
	 * @return the index'th column of <code>source</code>
	 */
	public static double[] extractSample(FMatrixRMaj source, int index) {
		double[] sample = new double[source.getNumRows()];
		
		for (int i = 0; i < source.getNumRows(); i++) {
			sample[i] = source.get(i, index);
		}
		
		return sample;
	}
	
	
	/**
	 * Wraps around the KMEANS result to add some functionality
	 * @author Daniel
	 *
	 */
	public static class CentroidWrapper {
		
		private double[][] centroids;
		private int numberOfCentroids;
		private int dimension;
		
		/**
		 * Create a centroid wrapper around the given centroids
		 * @param centroids
		 */
		public CentroidWrapper(double[][] centroids) {
			this.centroids = centroids;
			this.numberOfCentroids = centroids.length;
			this.dimension = centroids[0].length;
		}
		
		/**
		 * @param index the index of the centroid to be deleted (in case it is not needed)
		 */
		public void deleteCentroid(int index) {
			double[][] tmp = new double[numberOfCentroids - 1][dimension];
			int offset = 0;
			for (int i = 0; i < numberOfCentroids - 1; i++) {
				if (i == index) {
					offset++;
				}
				for (int j = 0; j < dimension; j++) {
					tmp[i][j] = centroids[i+offset][j];
				}
			}
			this.centroids = tmp;
			numberOfCentroids--;
		}
		
		
	    /**
	     * @param x
	     * @return the centroid which x belongs to
	     */
	    public int predict(double[] x) {
	        double minDist = Double.MAX_VALUE;
	        int bestCluster = 0;

	        for (int i = 0; i < numberOfCentroids; i++) {
	            double dist = Math.squaredDistance(x, centroids[i]);
	            if (dist < minDist) {
	                minDist = dist;
	                bestCluster = i;
	            }
	        }

	        return bestCluster;
	    }
		
	}

}


