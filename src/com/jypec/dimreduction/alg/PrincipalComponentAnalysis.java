package com.jypec.dimreduction.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.Pair;
import com.jypec.util.arrays.MatrixOperations;

/**
 * PCA implementation for dimensionality reduction
 *
 * @author Daniel Báscones - Heavy modifications
 * @author Peter Abeles - <a href="https://ejml.org/wiki/index.php?title=Example_Principal_Component_Analysis">Original code</a>
 */


public class PrincipalComponentAnalysis extends ProjectingDimensionalityReduction {    

    /**
     * Create a PCA object
     */
    public PrincipalComponentAnalysis() {
    	super(DimensionalityReductionAlgorithm.DRA_PCA);
    }

	@Override
	public void train(DMatrixRMaj data) {
		this.sayLn("Taking samples...");
		dimOrig = data.getNumRows();
		
		this.sayLn("Computing covariance matrix...");

		adjustment = new DMatrixRMaj(dimOrig, 1);
		DMatrixRMaj s = new DMatrixRMaj(dimOrig, dimOrig);
		MatrixOperations.generateCovarianceMatrix(data, s, null, adjustment);
        
		/** Extract eigenvalues, order and keep the most significant */
        this.sayLn("Extracting eigenvalues...");
        EigenDecomposition_F64<DMatrixRMaj> dec = DecompositionFactory_DDRM.eig(s.getNumElements(), true, true);
        dec.decompose(s);

        List<Pair<Double, DMatrixRMaj>> list = new ArrayList<Pair<Double, DMatrixRMaj>>();
        for (int i = 0; i < s.getNumCols(); i++) {
        	Complex_F64 val = dec.getEigenvalue(i);
        	DMatrixRMaj vec = dec.getEigenVector(i);
        	list.add(new Pair<Double, DMatrixRMaj>(val.real, vec));
        }
        Collections.sort(list, new Comparator<Pair<Double, DMatrixRMaj>>() {
			@Override
			public int compare(Pair<Double, DMatrixRMaj> o1, Pair<Double, DMatrixRMaj> o2) {
				return Double.compare(o2.first(), o1.first());
			}
        });
        
        /** Create projection and unprojection matrices */
        projectionMatrix = new DMatrixRMaj(dimProj, dimOrig);
        
        for (int i = 0; i < dimProj; i++) {
        	DMatrixRMaj vec = list.get(i).second();
        	for (int j = 0; j < dimOrig; j++) {
        		projectionMatrix.set(i, j, vec.get(j));
        	}
        }
        
        unprojectionMatrix = new DMatrixRMaj(projectionMatrix);
        CommonOps_DDRM.transpose(unprojectionMatrix);
	}
	
	
    /**
     * Converts a vector from sample space into eigen space.
     *
     * @param sampleData Sample space data.
     * @return Eigen space projection.
     */
    public double[] sampleToEigenSpace( double[] sampleData ) {
        if( sampleData.length != this.dimOrig )
            throw new IllegalArgumentException("Unexpected sample length");

        DMatrixRMaj s = new DMatrixRMaj(this.dimOrig,1,true,sampleData);
        DMatrixRMaj r = new DMatrixRMaj(dimProj,1);

        CommonOps_DDRM.subtract(s, adjustment, s);

        CommonOps_DDRM.mult(projectionMatrix,s,r);

        return r.data;
    }

    /**
     * Converts a vector from eigen space into sample space.
     *
     * @param eigenData Eigen space data.
     * @return Sample space projection.
     */
    public double[] eigenToSampleSpace( double[] eigenData ) {
        if( eigenData.length != dimProj )
            throw new IllegalArgumentException("Unexpected sample length");

        DMatrixRMaj s = new DMatrixRMaj(this.dimOrig,1);
        DMatrixRMaj r = DMatrixRMaj.wrap(dimProj,1,eigenData);
        
        CommonOps_DDRM.mult(unprojectionMatrix,r,s);

        CommonOps_DDRM.add(s,adjustment,s);

        return s.data;
    }
}
