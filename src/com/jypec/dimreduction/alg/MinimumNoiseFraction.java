package com.jypec.dimreduction.alg;

import java.io.IOException;

import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.DimensionalityReduction;
import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageHeaderData;
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
	
	
	/**
	 * Build a MNF algorithm
	 */
	public MinimumNoiseFraction() {
		super(DimensionalityReductionAlgorithm.DRA_MNF);
	}

	@Override
	public void train(HyperspectralImageData source) {
		//find out noise
		
		//find out data
		
		//calculate noise and data covariance matrices
		
		//solve the eigenvalue problem
		
		//keep the interesting eigenvectors
		
		//TODO
	}

	@Override
	public double[][][] reduce(HyperspectralImageData source) {
		//project using A
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void boost(double[][][] source, HyperspectralImageData dst) {
		//unproject using A
		
		// TODO Auto-generated method stub
		
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
