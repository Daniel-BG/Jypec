package com.jypec.dimreduction.alg;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.DMatrixRMaj;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.MatrixOperations;

import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.datatransform.FastICA;
import jsat.linear.DenseVector;
import jsat.linear.Matrix;
import jsat.linear.Vec;

/**
 * Wrapper for the ICA algorithm from
 * <a href="https://github.com/EdwardRaff/JSAT/blob/master/JSAT/src/jsat/datatransform/FastICA.java"> here </a> 
 * @author Daniel
 *
 */
public class IndependentComponentAnalysis extends ProjectingDimensionalityReduction {

	/**
	 * Default constructor
	 */
	public IndependentComponentAnalysis() {
		super(DimensionalityReductionAlgorithm.DRA_ICA);
	}

	@Override
	public void train(DMatrixRMaj source) {		
		dimOrig = source.getNumRows();
		/** Transform source to JSAT notation */
		List<DataPoint> points = new ArrayList<DataPoint>();
		for (int i = 0; i < source.getNumCols(); i++) {
			double[] array = new double[source.getNumRows()];
			for (int j = 0; j < source.getNumRows(); j++) {
				array[j] = source.get(j, i);
			}
			Vec vec = new DenseVector(array);
			DataPoint dp = new DataPoint(vec);
			points.add(dp);
		}
		SimpleDataSet dataSet = new SimpleDataSet(points);
		
		/** Perform ICA */
		FastICA fica = new FastICA(dimProj);
		fica.fit(dataSet);
		
		Matrix mixing, unmixing;
		/** Reflection hackity hack to get private fields */
		try {
			Field f = fica.getClass().getDeclaredField("mixing"); //NoSuchFieldException
			f.setAccessible(true);
			mixing = (Matrix) f.get(fica);
			
			f = fica.getClass().getDeclaredField("unmixing"); //NoSuchFieldException
			f.setAccessible(true);
			unmixing = (Matrix) f.get(fica);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Failed when reflecting for ICA");
		}
		
		/** Get data in a readable way */
		this.projectionMatrix = new DMatrixRMaj(this.dimProj, this.dimOrig);
		this.unprojectionMatrix = new DMatrixRMaj(this.dimOrig, this.dimProj);
		for (int i = 0; i < this.dimProj; i++) {
			for (int j = 0; j < this.dimOrig; j++) {
				this.projectionMatrix.set(i, j, unmixing.get(j, i));
				this.unprojectionMatrix.set(j, i, mixing.get(i, j));
			}
		}
		
		adjustment = new DMatrixRMaj(this.dimOrig, 1);
		MatrixOperations.generateCovarianceMatrix(source, null, null, adjustment); //calculate mean instead of doing more reflection hacks
		System.out.println("Test");
	}
	

}
