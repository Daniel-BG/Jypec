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
import com.jypec.img.HyperspectralImageData;
import com.jypec.util.MathOperations;
import com.jypec.util.Pair;

/**
 * <p>
 * The following is a simple example of how to perform basic principal component analysis in EJML.
 * </p>
 *
 * <p>
 * Principal Component Analysis (PCA) is typically used to develop a linear model for a set of data
 * (e.g. face images) which can then be used to test for membership.  PCA works by converting the
 * set of data to a new basis that is a subspace of the original set.  The subspace is selected
 * to maximize information.
 * </p>
 * <p>
 * PCA is typically derived as an eigenvalue problem.  However in this implementation {@link org.ejml.interfaces.decomposition.SingularValueDecomposition SVD}
 * is used instead because it will produce a more numerically stable solution.  Computation using EVD requires explicitly
 * computing the variance of each sample set. The variance is computed by squaring the residual, which can
 * cause loss of precision.
 * </p>
 *
 * <p>
 * Usage:<br>
 * 1) call setup()<br>
 * 2) For each sample (e.g. an image ) call addSample()<br>
 * 3) After all the samples have been added call computeBasis()<br>
 * 4) Call  sampleToEigenSpace() , eigenToSampleSpace()
 * </p>
 *
 * @author Peter Abeles
 * @author Daniel Báscones - Heavy modifications
 */


public class PrincipalComponentAnalysis extends ProjectingDimensionalityReduction {    
    // where the data is stored
    private DMatrixRMaj A = new DMatrixRMaj(1,1);
    private int sampleIndex;


    /**
     * Create a PCA object
     */
    public PrincipalComponentAnalysis() {
    	super(DimensionalityReductionAlgorithm.DRA_PCA);
    }

    /**
     * Must be called before any other functions. Declares and sets up internal data structures.
     *
     * @param numSamples Number of samples that will be processed.
     * @param sampleSize Number of elements in each sample.
     */
    public void setup( int numSamples , int sampleSize ) {
        adjustment = new double[ sampleSize ];
        A.reshape(sampleSize, numSamples,false);
        this.dimOrig = sampleSize;
        sampleIndex = 0;
        dimProj = -1;
    }

	/**
	 * Adds a new sample of the raw data to internal data structure for later processing.  All the samples
	 * must be added before computeBasis is called.
	 *
	 * @param sampleData Sample from original raw data.
	 */
    public void addSample( double[] sampleData ) {
        if( this.dimOrig != sampleData.length )
            throw new IllegalArgumentException("Unexpected sample size");
        if( sampleIndex >= A.getNumCols() )
            throw new IllegalArgumentException("Too many samples");

        for( int i = 0; i < sampleData.length; i++ ) {
            A.set(i, sampleIndex,sampleData[i]);
        }
        sampleIndex++;
    }


    
    /**
     * Computes a basis (the principal components) from the most dominant eigenvectors.
     *
     * @param numComponents Number of vectors it will use to describe the data.  Typically much
     * smaller than the number of elements in the input vector.
     */
    public void computeBasis( int numComponents ) {
        if( numComponents > this.dimOrig )
            throw new IllegalArgumentException("More components requested that the data's length.");
        if( sampleIndex != A.getNumCols() )
            throw new IllegalArgumentException("Not all the data has been added");
        if( numComponents > sampleIndex )
            throw new IllegalArgumentException("More data needed to compute the desired number of components");
        
        this.dimProj = numComponents;
        
        DMatrixRMaj ones = new DMatrixRMaj(A.getNumCols(), 1);
        for (int i = 0; i < ones.getNumElements(); i++) {
        	ones.set(i, 1);
        }
        DMatrixRMaj summ = new DMatrixRMaj(this.dimOrig, 1);

        //compute the summation of all the samples
        CommonOps_DDRM.mult(A, ones, summ);
        
        //compute the mean of all samples
        DMatrixRMaj meann = new DMatrixRMaj(this.dimOrig, 1);
        for( int j = 0; j < this.dimOrig; j++ ) {
        	adjustment[j] = summ.get(j) / (double) A.getNumCols();
        	meann.set(j, adjustment[j]);
        }
        
        //create covariance matrix
        DMatrixRMaj s = new DMatrixRMaj(this.dimOrig, this.dimOrig);
        CommonOps_DDRM.multTransB(A, A, s);
        DMatrixRMaj s2 = new DMatrixRMaj(this.dimOrig, this.dimOrig);
        CommonOps_DDRM.multTransB(meann, summ, s2);
        CommonOps_DDRM.subtract(s, s2, s);
        
        //extract eigenvalues and eigenvectors
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
        
        projectionMatrix = new DMatrixRMaj(numComponents, adjustment.length);
        
        for (int i = 0; i < numComponents; i++) {
        	DMatrixRMaj vec = list.get(i).second();
        	for (int j = 0; j < adjustment.length; j++) {
        		projectionMatrix.set(i, j, vec.get(j));
        	}
        }
        
        unprojectionMatrix = new DMatrixRMaj(projectionMatrix);
        CommonOps_DDRM.transpose(unprojectionMatrix);
    }

    /**
     * Returns a vector from the PCA's basis.
     *
     * @param which Which component's vector is to be returned.
     * @return Vector from the PCA basis.
     */
    public double[] getBasisVector( int which ) {
        if( which < 0 || which >= dimProj )
            throw new IllegalArgumentException("Invalid component");

        DMatrixRMaj v = new DMatrixRMaj(1,this.dimOrig);
        CommonOps_DDRM.extract(projectionMatrix,which,which+1,0,this.dimOrig,v,0,0);

        return v.data;
    }

	@Override
	public void train(HyperspectralImageData source) {
		if (this.dimProj <= 0) {
			throw new IllegalStateException("Please first set the number of components for this dimensionality reduction algorithm");
		}
		int nc = this.dimProj;
		this.setup(source.getNumberOfLines() * source.getNumberOfSamples(), source.getNumberOfBands());
		
		this.sayLn("Adding samples to train...");
		for (int i = 0; i < source.getNumberOfLines(); i++) {
			for (int j = 0; j < source.getNumberOfSamples(); j++) {
				this.addSample(source.getPixel(i, j));
			}
		}
		
		this.sayLn("Computing basis");
		this.computeBasis(nc);
	}


	@Override
	public double getMaxValue(HyperspectralImageData img) {
		return MathOperations.getMaximumDistance(img.getDataType().getDynamicRange(), img.getNumberOfBands());
	}

	@Override
	public double getMinValue(HyperspectralImageData img) {
		return -MathOperations.getMaximumDistance(img.getDataType().getDynamicRange(), img.getNumberOfBands());
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
        DMatrixRMaj mean = DMatrixRMaj.wrap(this.dimOrig,1,this.adjustment);

        DMatrixRMaj s = new DMatrixRMaj(this.dimOrig,1,true,sampleData);
        DMatrixRMaj r = new DMatrixRMaj(dimProj,1);

        CommonOps_DDRM.subtract(s, mean, s);

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

        DMatrixRMaj mean = DMatrixRMaj.wrap(this.dimOrig,1,this.adjustment);
        CommonOps_DDRM.add(s,mean,s);

        return s.data;
    }
}
