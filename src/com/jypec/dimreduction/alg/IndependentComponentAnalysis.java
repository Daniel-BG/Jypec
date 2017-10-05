package com.jypec.dimreduction.alg;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;

import com.jypec.dimreduction.JSATWrapper;
import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.MatrixOperations;

import jsat.SimpleDataSet;
import jsat.datatransform.FastICA;
import jsat.linear.Matrix;

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
	public void train(FMatrixRMaj source) {		
		dimOrig = source.getNumRows();
		/** Get mean first, since we will be centering the data around zero */
		adjustment = new FMatrixRMaj(this.dimOrig, 1);
		MatrixOperations.generateCovarianceMatrix(source, null, null, adjustment); //calculate mean instead of doing more reflection hacks
		
		/** Transform source to JSAT notation */
		FMatrixRMaj adjustmentSubstract = new FMatrixRMaj(source.getNumRows(), source.getNumCols());
		CommonOps_FDRM.mult(adjustment, MatrixOperations.ones(1, source.getNumCols()), adjustmentSubstract);
		CommonOps_FDRM.subtract(source, adjustmentSubstract, adjustmentSubstract);
		SimpleDataSet dataSet = JSATWrapper.toDataSet(adjustmentSubstract);
		
		/** Perform ICA */
		FastICA fica = new FastICA(dimProj);
		fica.setPreWhitened(true);
		fica.fit(dataSet);
		
		/** Reflection hackity hack to get private fields */
		Matrix mixing = (Matrix) JSATWrapper.getField(fica, "mixing");
		Matrix unmixing = (Matrix) JSATWrapper.getField(fica, "unmixing");
		
		/** Get data in a readable way */
		this.projectionMatrix = JSATWrapper.toFMatrixRMaj(unmixing, true);
		this.unprojectionMatrix = JSATWrapper.toFMatrixRMaj(mixing, true);
	}
	

}
