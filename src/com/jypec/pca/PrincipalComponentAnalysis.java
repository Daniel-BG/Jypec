package com.jypec.pca;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.SingularOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;

import com.jypec.img.HyperspectralImage;
import com.jypec.util.BitStream;
import com.jypec.util.io.BitStreamDataReaderWriter;

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
 */


public class PrincipalComponentAnalysis {

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
                DecompositionFactory_DDRM.svd(A.numRows, A.numCols, false, true, false);
        if( !svd.decompose(A) )
            throw new RuntimeException("SVD failed");

        V_t = svd.getV(null,true);
        DMatrixRMaj W = svd.getW(null);

        // Singular values are in an arbitrary order initially
        SingularOps_DDRM.descendingOrder(null,false,W,V_t,true);

        // strip off unneeded components and find the basis
        V_t.reshape(numComponents,mean.length,true);
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

        DMatrixRMaj v = new DMatrixRMaj(1,A.numCols);
        CommonOps_DDRM.extract(V_t,which,which+1,0,A.numCols,v,0,0);

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
     * Projects a whole hyperspectral image "src" onto the destination "dst" image
     * @param src source of information in the sample space
     * @param dst where the result is saved in the eigen space
     */
    public void imageToEigenSpace(HyperspectralImage src, HyperspectralImage dst) {
    	if (src.getNumberOfLines() != dst.getNumberOfLines() ||
    			src.getNumberOfSamples() != dst.getNumberOfSamples() ||
    			src.getNumberOfBands() != this.sampleSize ||
    			dst.getNumberOfBands() != this.numComponents) {
    		throw new IllegalArgumentException("Image dimensions do not match with the expected PCA matrix transform size");
    	}
    	
		for (int i = 0; i < src.getNumberOfLines(); i++) {
			for (int j = 0; j < src.getNumberOfSamples(); j++) {
				dst.setPixel(this.sampleToEigenSpace(src.getPixel(i, j)), i, j);
			}
		}
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
     * Undoes the projection of a whole hyperspectral image 
     * "src" onto the destination "dst" image.
     * @param src the source of information, in the eigen space
     * @param dst where the result is saved, in the sample space
     */
    public void imageToSampleSpace(HyperspectralImage src, HyperspectralImage dst) {
    	if (src.getNumberOfLines() != dst.getNumberOfLines() ||
    			src.getNumberOfSamples() != dst.getNumberOfSamples() ||
    			src.getNumberOfBands() != this.numComponents ||
    			dst.getNumberOfBands() != this.sampleSize) {
    		throw new IllegalArgumentException("Image dimensions do not match with the expected PCA matrix transform size");
    	}
    	
		for (int i = 0; i < src.getNumberOfLines(); i++) {
			for (int j = 0; j < src.getNumberOfSamples(); j++) {
				dst.setPixel(this.eigenToSampleSpace(src.getPixel(i, j)), i, j);
			}
		}
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
        if( sample.length != A.numCols )
            throw new IllegalArgumentException("Expected input vector to be in sample space");

        DMatrixRMaj dots = new DMatrixRMaj(numComponents,1);
        DMatrixRMaj s = DMatrixRMaj.wrap(A.numCols,1,sample);

        CommonOps_DDRM.mult(V_t,s,dots);

        return NormOps_DDRM.normF(dots);
    }
    
    
    /**
     * Stores the necessary information in the output stream so that afterwards, a call
     * can be made to {@link #restoreFromBitStream(BitStream)} to set the PCA up so that
     * eigen-space samples can be restored onto the original space
     * @param bw writer to the output stream
     */
    public void saveToBitStream(BitStreamDataReaderWriter bw) {
    	//write the number of dimensions in the original space
    	bw.writeInt(this.sampleSize);
    	//write the number of dimensions in the reduced space
    	bw.writeInt(numComponents);
    	//write the mean
    	bw.writeDoubleArray(mean, this.sampleSize);
    	//write the matrix
    	bw.writeDoubleArray(V_t.getData(), this.sampleSize * numComponents);
    }
    
    /**
     * Restore the PCA object from the given bitstream so that it can perform projections
     * (direct and inverse) without training again
     * @param bw reader to the output stream
     */
    public void restoreFromBitStream(BitStreamDataReaderWriter bw) {
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

	/**
	 * Computes the PCA algorithm for the given source image, with the target space
	 * being of dimension pcaDim
	 * @param srcImg
	 * @param pcaDim
	 */
	public void computeBasisFrom(HyperspectralImage srcImg, int pcaDim) {
		this.setup(srcImg.getNumberOfLines() * srcImg.getNumberOfSamples(), srcImg.getNumberOfBands());
		
		for (int i = 0; i < srcImg.getNumberOfLines(); i++) {
			for (int j = 0; j < srcImg.getNumberOfSamples(); j++) {
				this.addSample(srcImg.getPixel(i, j));
			}
		}
		
		this.computeBasis(pcaDim);
	}
	
	/**
	 * @return the number of components that this PCA reduces to
	 */
	public int getNumComponents() {
		return this.numComponents;
	}
    
}