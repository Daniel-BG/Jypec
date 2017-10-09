package com.jypec.dimreduction.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F32;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.Pair;
import com.jypec.util.arrays.EJMLExtensions;
import com.jypec.util.debug.Logger;

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
	public boolean doTrain(FMatrixRMaj data) {
		if (this.reductionInTrainingRequested()) {
			data = EJMLExtensions.getSubSet(data, percentTraining);
		}
		
		Logger.getLogger().log("Taking samples...");
		dimOrig = data.getNumRows();
		
		Logger.getLogger().log("Computing covariance matrix...");

		adjustment = new FMatrixRMaj(dimOrig, 1);
		FMatrixRMaj s = new FMatrixRMaj(dimOrig, dimOrig);
		EJMLExtensions.generateCovarianceMatrix(data, s, null, adjustment);
        
		/** Extract eigenvalues, order and keep the most significant */
		Logger.getLogger().log("Extracting eigenvalues...");
        EigenDecomposition_F32<FMatrixRMaj> dec = DecompositionFactory_FDRM.eig(s.getNumElements(), true, true);
        boolean res = dec.decompose(s);
        if (!res) {
        	Logger.getLogger().log("Decomposition failed");
        	return false;
        }
        

        List<Pair<Float, FMatrixRMaj>> list = new ArrayList<Pair<Float, FMatrixRMaj>>();
        for (int i = 0; i < dec.getNumberOfEigenvalues(); i++) {
        	Complex_F32 val = dec.getEigenvalue(i);
        	FMatrixRMaj vec = dec.getEigenVector(i);
        	list.add(new Pair<Float, FMatrixRMaj>(val.real, vec));
        }
        Collections.sort(list, new Comparator<Pair<Float, FMatrixRMaj>>() {
			@Override
			public int compare(Pair<Float, FMatrixRMaj> o1, Pair<Float, FMatrixRMaj> o2) {
				return Float.compare(o2.first(), o1.first());
			}
        });
        
        /** Create projection and unprojection matrices */
        projectionMatrix = new FMatrixRMaj(dimProj, dimOrig);
        
        for (int i = 0; i < dimProj; i++) {
        	FMatrixRMaj vec = list.get(i).second();
        	for (int j = 0; j < dimOrig; j++) {
        		projectionMatrix.set(i, j, vec.get(j));
        	}
        }
        
        unprojectionMatrix = new FMatrixRMaj(projectionMatrix);
        CommonOps_FDRM.transpose(unprojectionMatrix);
        
        return true;
	}
	
	
    /**
     * Converts a vector from sample space into eigen space.
     *
     * @param sampleData Sample space data.
     * @return Eigen space projection.
     */
    public float[] sampleToEigenSpace( float[] sampleData ) {
        if( sampleData.length != this.dimOrig )
            throw new IllegalArgumentException("Unexpected sample length");

        FMatrixRMaj s = new FMatrixRMaj(this.dimOrig,1,true,sampleData);
        FMatrixRMaj r = new FMatrixRMaj(dimProj,1);

        CommonOps_FDRM.subtract(s, adjustment, s);

        CommonOps_FDRM.mult(projectionMatrix,s,r);

        return r.data;
    }

    /**
     * Converts a vector from eigen space into sample space.
     *
     * @param eigenData Eigen space data.
     * @return Sample space projection.
     */
    public float[] eigenToSampleSpace( float[] eigenData ) {
        if( eigenData.length != dimProj )
            throw new IllegalArgumentException("Unexpected sample length");

        FMatrixRMaj s = new FMatrixRMaj(this.dimOrig,1);
        FMatrixRMaj r = FMatrixRMaj.wrap(dimProj,1,eigenData);
        
        CommonOps_FDRM.mult(unprojectionMatrix,r,s);

        CommonOps_FDRM.add(s,adjustment,s);

        return s.data;
    }
}
