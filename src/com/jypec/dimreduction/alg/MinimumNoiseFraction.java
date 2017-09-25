package com.jypec.dimreduction.alg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageHeaderData;
import com.jypec.util.Pair;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Implements the Minimum noise fraction algorithm for dimensionality reduction
 * <br>
 * Taken from "Real-Time Noise Removal for Line-Scanning Hyperspectral
 * Devices Using a Minimum Noise Fraction-Based Approach"
 * by Asgeir Bjorgan and Lise Lyngsnes Randeberg
 * <br>
 * (<a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4367363/pdf/sensors-15-03362.pdf">Visited 2017-09-25</a>)
 * @author Daniel
 */
public class MinimumNoiseFraction extends DimensionalityReduction {

	private int numComponents;
	private int sampleSize;
	private DMatrixRMaj projectionMatrix;
    private double mean[];
	
	
	/**
	 * Build a MNF algorithm
	 */
	public MinimumNoiseFraction() {
		super(DimensionalityReductionAlgorithm.DRA_MNF);
	}
	
	/**
	 * Extract the noise from the data. the formula used is: <br>
	 * noise(i,j) = (data(i,j) - data(i,j+1))/2 <br>
	 * except for the last value where: <br>
	 * noise(i,j) = (data(i,j) - data(i,j-1))/2 <br>
	 * @param data
	 * @return
	 */
	private static DMatrixRMaj extractNoise(DMatrixRMaj data) {
		//assume pushbroom sensor and only extract horizontal noise
		DMatrixRMaj res = new DMatrixRMaj(data.getNumRows(), data.getNumCols());
		for (int i = 0; i < data.getNumCols(); i++) {
			for (int j = 0; j < data.getNumRows(); j++) {
				double val = data.get(j, i);
				if (j < data.getNumRows() - 1) {
					val -= data.get(j+1, i);
				} else {
					val -= data.get(j-1, i);
				}
				res.set(j, i, val / 2.0);
			}
		}
		return res;
	}

	@Override
	public void train(HyperspectralImageData source) {
		//initialize values
		sampleSize = source.getNumberOfBands();
		int samples = source.getNumberOfLines() * source.getNumberOfSamples();
		this.mean = new double[sampleSize];
		//find out data and noise. The data is NOT zero-meaned,
		//while the noise is assumed to be
		DMatrixRMaj data = source.toDoubleMatrix();
		DMatrixRMaj noise = extractNoise(data);
		
		/**Create data covariance matrix */
        //compute the summation of all the samples
        DMatrixRMaj ones = new DMatrixRMaj(samples, 1);
        for (int i = 0; i < ones.getNumElements(); i++) {
        	ones.set(i, 1);
        }
        DMatrixRMaj summ = new DMatrixRMaj(sampleSize, 1);
        CommonOps_DDRM.multTransA(data, ones, summ);
		
		//compute the mean of all samples
        DMatrixRMaj meann = new DMatrixRMaj(sampleSize, 1);
        for( int j = 0; j < sampleSize; j++ ) {
        	mean[j] = summ.get(j) / (double) data.getNumRows();
        	meann.set(j, mean[j]);
        }
        
        //create covariance matrix
        DMatrixRMaj sData = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransA(data, data, sData);
        DMatrixRMaj sData2 = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransB(meann, summ, sData2);
        CommonOps_DDRM.subtract(sData, sData2, sData);
		/*********************************/
        
        /**Create noise covariance matrix */
        DMatrixRMaj sNoise = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.multTransA(noise, noise, sNoise);
        /**********************************/
        
        /**Solve the eigenvalue problem*/
        CommonOps_DDRM.invert(sNoise);
        DMatrixRMaj eigenMat = new DMatrixRMaj(sampleSize, sampleSize);
        CommonOps_DDRM.mult(sNoise, sData, eigenMat);
        EigenDecomposition_F64<DMatrixRMaj> dec = DecompositionFactory_DDRM.eig(sData.getNumElements(), true, true);
        dec.decompose(eigenMat);
        /*******************************/
        
        
        /**Extract the interesting eigenvectors*/
        List<Pair<Double, DMatrixRMaj>> list = new ArrayList<Pair<Double, DMatrixRMaj>>();
        for (int i = 0; i < sampleSize; i++) {
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
        /***************************************/

        /**Create projection matrix*/
        projectionMatrix = new DMatrixRMaj(numComponents, sampleSize);
        
        for (int i = 0; i < numComponents; i++) {
        	DMatrixRMaj vec = list.get(i).second();
        	for (int j = 0; j < sampleSize; j++) {
        		projectionMatrix.set(i, j, vec.get(j));
        	}
        }
        /************************^**/
	}

	@Override
	public DMatrixRMaj reduce(HyperspectralImageData source) {
		DMatrixRMaj img = source.toDoubleMatrix();
		DMatrixRMaj res = new DMatrixRMaj(this.numComponents, img.getNumCols());
		CommonOps_DDRM.multTransAB(img, this.projectionMatrix, res);
		return res;
	}

	@Override
	public void boost(DMatrixRMaj source, HyperspectralImageData dst) {
		this.sayLn("Boosting samples from reduced space to the original...");
		DMatrixRMaj res = new DMatrixRMaj(this.sampleSize, source.getNumCols());
		CommonOps_DDRM.multTransA(source, this.projectionMatrix, res);
		dst.copyDataFrom(res);
	}

	@Override
	public void doSaveTo(BitOutputStream bw) throws IOException {
		//save transform matrix
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doLoadFrom(BitInputStream bw, ComParameters cp, ImageHeaderData ihd) throws IOException {
		//load transform matrix
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumComponents() {
		return this.numComponents;
	}

	@Override
	public void setNumComponents(int numComponents) {
		this.numComponents = numComponents;
	}

	@Override
	public double getMaxValue(HyperspectralImageData img) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMinValue(HyperspectralImageData img) {
		throw new UnsupportedOperationException();
	}

}
