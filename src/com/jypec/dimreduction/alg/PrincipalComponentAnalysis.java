package com.jypec.dimreduction.alg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.MathOperations;
import com.jypec.util.Pair;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

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
 * 4) Call  sampleToEigenSpace() , eigenToSampleSpace() , errorMembership() , response()
 * </p>
 *
 * @author Peter Abeles
 * @author Daniel Báscones - Heavy modifications
 */


public class PrincipalComponentAnalysis extends DimensionalityReduction {

    // principal component subspace is stored in the rows
    private DMatrixRMaj V_t;

    // how many principal components are used
    private int numComponents;

    // dimension of the original space
    private int sampleSize;
    
    // where the data is stored
    private DMatrixRMaj A = new DMatrixRMaj(1,1);
    private int sampleIndex;

    // mean values of each element across all the samples
    double mean[];

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
        mean = new double[ sampleSize ];
        A.reshape(numSamples,sampleSize,false);
        this.sampleSize = sampleSize;
        sampleIndex = 0;
        numComponents = -1;
    }

	/**
	 * Adds a new sample of the raw data to internal data structure for later processing.  All the samples
	 * must be added before computeBasis is called.
	 *
	 * @param sampleData Sample from original raw data.
	 */
    public void addSample( double[] sampleData ) {
        if( this.sampleSize != sampleData.length )
            throw new IllegalArgumentException("Unexpected sample size");
        if( sampleIndex >= A.getNumRows() )
            throw new IllegalArgumentException("Too many samples");

        for( int i = 0; i < sampleData.length; i++ ) {
            A.set(sampleIndex,i,sampleData[i]);
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
        if( numComponents > this.sampleSize )
            throw new IllegalArgumentException("More components requested that the data's length.");
        if( sampleIndex != A.getNumRows() )
            throw new IllegalArgumentException("Not all the data has been added");
        if( numComponents > sampleIndex )
            throw new IllegalArgumentException("More data needed to compute the desired number of components");
        
        this.numComponents = numComponents;
        
        DMatrixRMaj ones = new DMatrixRMaj(A.getNumRows(), 1);
        for (int i = 0; i < ones.getNumElements(); i++) {
        	ones.set(i, 1);
        }
        DMatrixRMaj summ = new DMatrixRMaj(this.sampleSize, 1);

        //compute the summation of all the samples
        CommonOps_DDRM.multTransA(A, ones, summ);
        
        //compute the mean of all samples
        DMatrixRMaj meann = new DMatrixRMaj(this.sampleSize, 1);
        for( int j = 0; j < this.sampleSize; j++ ) {
        	mean[j] = summ.get(j) / (double) A.getNumRows();
        	meann.set(j, mean[j]);
        }
        
        //create covariance matrix
        DMatrixRMaj s = new DMatrixRMaj(this.sampleSize, this.sampleSize);
        CommonOps_DDRM.multTransA(A, A, s);
        DMatrixRMaj s2 = new DMatrixRMaj(this.sampleSize, this.sampleSize);
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
        
        V_t = new DMatrixRMaj(numComponents, mean.length);
        
        for (int i = 0; i < numComponents; i++) {
        	DMatrixRMaj vec = list.get(i).second();
        	for (int j = 0; j < mean.length; j++) {
        		V_t.set(i, j, vec.get(j));
        	}
        }
    }

    /**
     * Returns a vector from the PCA's basis.
     *
     * @param which Which component's vector is to be returned.
     * @return Vector from the PCA basis.
     */
    public double[] getBasisVector( int which ) {
        if( which < 0 || which >= numComponents )
            throw new IllegalArgumentException("Invalid component");

        DMatrixRMaj v = new DMatrixRMaj(1,this.sampleSize);
        CommonOps_DDRM.extract(V_t,which,which+1,0,this.sampleSize,v,0,0);

        return v.data;
    }

    /**
     * Converts a vector from sample space into eigen space.
     *
     * @param sampleData Sample space data.
     * @return Eigen space projection.
     */
    public double[] sampleToEigenSpace( double[] sampleData ) {
        if( sampleData.length != this.sampleSize )
            throw new IllegalArgumentException("Unexpected sample length");
        DMatrixRMaj mean = DMatrixRMaj.wrap(this.sampleSize,1,this.mean);

        DMatrixRMaj s = new DMatrixRMaj(this.sampleSize,1,true,sampleData);
        DMatrixRMaj r = new DMatrixRMaj(numComponents,1);

        CommonOps_DDRM.subtract(s, mean, s);

        CommonOps_DDRM.mult(V_t,s,r);

        return r.data;
    }

    /**
     * Converts a vector from eigen space into sample space.
     *
     * @param eigenData Eigen space data.
     * @return Sample space projection.
     */
    public double[] eigenToSampleSpace( double[] eigenData ) {
        if( eigenData.length != numComponents )
            throw new IllegalArgumentException("Unexpected sample length");

        DMatrixRMaj s = new DMatrixRMaj(this.sampleSize,1);
        DMatrixRMaj r = DMatrixRMaj.wrap(numComponents,1,eigenData);
        
        CommonOps_DDRM.multTransA(V_t,r,s);

        DMatrixRMaj mean = DMatrixRMaj.wrap(this.sampleSize,1,this.mean);
        CommonOps_DDRM.add(s,mean,s);

        return s.data;
    }

    /**
     * <p>
     * The membership error for a sample.  If the error is less than a threshold then
     * it can be considered a member.  The threshold's value depends on the data set.
     * </p>
     * <p>
     * The error is computed by projecting the sample into eigenspace then projecting
     * it back into sample space and
     * </p>
     * 
     * @param sampleA The sample whose membership status is being considered.
     * @return Its membership error.
     */
    public double errorMembership( double[] sampleA ) {
        double[] eig = sampleToEigenSpace(sampleA);
        double[] reproj = eigenToSampleSpace(eig);


        double total = 0;
        for( int i = 0; i < reproj.length; i++ ) {
            double d = sampleA[i] - reproj[i];
            total += d*d;
        }

        return Math.sqrt(total);
    }

    /**
     * Computes the dot product of each basis vector against the sample.  Can be used as a measure
     * for membership in the training sample set.  High values correspond to a better fit.
     *
     * @param sample Sample of original data.
     * @return Higher value indicates it is more likely to be a member of input dataset.
     */
    public double response( double[] sample ) {
        if( sample.length != this.sampleSize )
            throw new IllegalArgumentException("Expected input vector to be in sample space");

        DMatrixRMaj dots = new DMatrixRMaj(numComponents,1);
        DMatrixRMaj s = DMatrixRMaj.wrap(this.sampleSize,1,sample);

        CommonOps_DDRM.mult(V_t,s,dots);

        return NormOps_DDRM.normF(dots);
    }
	
	/**
	 * @return the number of components that this PCA reduces to
	 */
	public int getNumComponents() {
		return this.numComponents;
	}


	
    @Override
    public void doSaveTo(BitOutputStream bw) throws IOException {
    	//write the number of dimensions in the original space
    	bw.writeInt(this.sampleSize);
    	//write the number of dimensions in the reduced space
    	bw.writeInt(numComponents);
    	//write the mean
    	bw.writeDoubleArray(mean, this.sampleSize);
    	//write the matrix
    	bw.writeDoubleArray(V_t.getData(), this.sampleSize * numComponents);
    }
    
    @Override
    public void doLoadFrom(BitInputStream bw, ComParameters cp, ImageHeaderData ihd) throws IOException {
    	//read the number of dimensions in the original space
    	this.sampleSize = bw.readInt();
    	//read the number of dimensions in the reduced space
    	this.numComponents = bw.readInt();
    	//read the mean
    	this.mean = bw.readDoubleArray(sampleSize);
    	//read the projection matrix
    	V_t = new DMatrixRMaj();
    	V_t.setData(bw.readDoubleArray(this.sampleSize * this.numComponents));
    	V_t.reshape(numComponents,mean.length,true);
    }

	@Override
	public double[][][] reduce(HyperspectralImageData src) {
		double[][][] res = new double[this.numComponents][src.getNumberOfLines()][src.getNumberOfSamples()];
		for (int i = 0; i < src.getNumberOfLines(); i++) {
			for (int j = 0; j < src.getNumberOfSamples(); j++) {
				double[] proj = this.sampleToEigenSpace(src.getPixel(i, j));
				for (int k = 0; k < this.numComponents; k++) {
					res[k][i][j] = proj[k];
				}
			}
		}
		return res;
	}

	@Override
	public void boost(double[][][] src, HyperspectralImageData dst) {
		double[] pixel = new double[this.numComponents];
		for (int i = 0; i < dst.getNumberOfLines(); i++) {
			for (int j = 0; j < dst.getNumberOfSamples(); j++) {
				for (int k = 0; k < this.numComponents; k++) {
					pixel[k] = src[k][i][j];
				}
				dst.setPixel(this.eigenToSampleSpace(pixel), i, j);
			}
		}
	}

	@Override
	public void train(HyperspectralImageData source) {
		if (this.numComponents <= 0) {
			throw new IllegalStateException("Please first set the number of components for this dimensionality reduction algorithm");
		}
		int nc = this.numComponents;
		this.setup(source.getNumberOfLines() * source.getNumberOfSamples(), source.getNumberOfBands());
		
		for (int i = 0; i < source.getNumberOfLines(); i++) {
			for (int j = 0; j < source.getNumberOfSamples(); j++) {
				this.addSample(source.getPixel(i, j));
			}
		}
		
		this.computeBasis(nc);
	}
	

	@Override
	public void setNumComponents(int numComponents) {
		this.numComponents = numComponents;
	}


	@Override
	public double getMaxValue(HyperspectralImageData img) {
		return MathOperations.getMaximumDistance(img.getDataType().getMagnitudeAbsoluteRange(), img.getNumberOfBands());
	}

	@Override
	public double getMinValue(HyperspectralImageData img) {
		return -MathOperations.getMaximumDistance(img.getDataType().getMagnitudeAbsoluteRange(), img.getNumberOfBands());
	}



    
}

/* Older PCA version.
public void computeBasis2( int numComponents ) {
    if( numComponents > this.sampleSize )
        throw new IllegalArgumentException("More components requested that the data's length.");
    if( sampleIndex != A.getNumRows() )
        throw new IllegalArgumentException("Not all the data has been added");
    if( numComponents > sampleIndex )
        throw new IllegalArgumentException("More data needed to compute the desired number of components");

    this.numComponents = numComponents;

    // compute the mean of all the samples
    for( int i = 0; i < A.getNumRows(); i++ ) {
        for( int j = 0; j < mean.length; j++ ) {
            mean[j] += A.get(i,j);
        }
    }
    for( int j = 0; j < mean.length; j++ ) {
        mean[j] /= A.getNumRows();
    }

    // subtract the mean from the original data
    for( int i = 0; i < A.getNumRows(); i++ ) {
        for( int j = 0; j < mean.length; j++ ) {
            A.set(i,j,A.get(i,j)-mean[j]);
        }
    }

    // Compute SVD and save time by not computing U
    SingularValueDecomposition<DMatrixRMaj> svd =
            DecompositionFactory_DDRM.svd(A.numRows, this.sampleSize, false, true, false);
    if( !svd.decompose(A) )
        throw new RuntimeException("SVD failed");

    V_t = svd.getV(null,true);
    DMatrixRMaj W = svd.getW(null);

    // Singular values are in an arbitrary order initially
    SingularOps_DDRM.descendingOrder(null,false,W,V_t,true);

    // strip off unneeded components and find the basis
    V_t.reshape(numComponents,mean.length,true);
}*/