package com.jypec.dimreduction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.FMatrixRMaj;

import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;
import jsat.linear.DenseVector;
import jsat.linear.Matrix;
import jsat.linear.Vec;

/**
 * Provides wrapping funcitonality over the JSAT library 
 * @author Daniel
 *
 */
public class JSATWrapper {
	
	/**
	 * Transforms data in a matrix to a JSAT dataset. Each column of
	 * the matrix is one data vector for the returned data set
	 * @param source
	 * @return the data set consisting of the matrix's vectors
	 */
	public static SimpleDataSet toDataSet(FMatrixRMaj source) {
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
		return new SimpleDataSet(points);
	}
	
	
	/**
	 * @param source
	 * @return a FMatrixRMaj whose columns are the samples of <code>source</code>
	 */
	public static FMatrixRMaj toFMatrixRMaj(SimpleDataSet source) {
		return toFMatrixRMaj(source.getDataMatrixView(), true);
	}
	
	/**
	 * Transform the given JSAT matrix to a EJML matrix.
	 * @param m
	 * @param transpose if the matrix returned should be a transpose of the original.
	 * @return the EJML matrix
	 */
	public static FMatrixRMaj toFMatrixRMaj(Matrix m, boolean transpose) {
		FMatrixRMaj result;
		if (transpose) {
			result = new FMatrixRMaj(m.cols(), m.rows()); //invert dimensions since the order is reversed
		} else {
			result = new FMatrixRMaj(m.rows(), m.cols()); //invert dimensions since the order is reversed
		}
		for (int i = 0; i < result.getNumRows(); i++) {
			for (int j = 0; j < result.getNumCols(); j++) {
				if (transpose) {
					result.set(i, j, (float) m.get(j, i));
				} else {
					result.set(i, j, (float) m.get(i, j));
				}
			}
		}
		
		return result;
	}
	
	
	/**
	 * @param obj
	 * @param field
	 * @return the object stored in the given <code>field</code> of <code>obj</code>
	 * @throws RuntimeException if the field doesn't exist
	 */
	public static Object getField(Object obj, String field) {
		Object extracted;
		try {
			Field f = obj.getClass().getDeclaredField(field);
			f.setAccessible(true);
			extracted = f.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Failed when reflecting");
		}
		return extracted;
	}

}
