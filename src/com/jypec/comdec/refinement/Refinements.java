package com.jypec.comdec.refinement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ejml.data.FMatrixRMaj;

import com.jypec.util.Pair;

/**
 * General class for refinements to the compression flow
 * @author Daniel
 *
 */
public class Refinements {
	
	private static float[] nonOutlierRange = new float[2];

	/**
	 * @param waveForm
	 * @param percentOutliers
	 * @return a list of top <code>percentOutliers</code> in the given matrix.
	 * These are the highest or lowest values
	 */
	public static List<Pair<Float, Pair<Integer, Integer>>> findOutliers(FMatrixRMaj waveForm, double percentOutliers) {
		List<Pair<Float, Pair<Integer, Integer>>> list = Refinements.toSortedByValuePositionalArray(waveForm);
		List<Pair<Float, Pair<Integer, Integer>>> res = new ArrayList<Pair<Float, Pair<Integer, Integer>>>();
		int lowIndex = 0, highIndex = list.size() - 1;
		double count = 0, totalCount = list.size();
		while (count / totalCount < percentOutliers) {
			float lowVal = list.get(lowIndex).first();
			float lowNext = list.get(lowIndex + 1).first();
			float highVal = list.get(highIndex).first();
			float highPrev = list.get(highIndex - 1).first();
			
			float lowDiff = lowNext - lowVal;
			float highDiff = highVal - highPrev;
			
			if (lowDiff > highDiff) {
				res.add(list.get(lowIndex));
				lowIndex++;
				nonOutlierRange[0] = lowNext;
				nonOutlierRange[1] = highVal;
			} else {
				res.add(list.get(highIndex));
				highIndex--;
				nonOutlierRange[0] = lowVal;
				nonOutlierRange[1] = highPrev;
			}
			
			count++;
		}
		
		return res;
	}
	
	/**
	 * Call after {@link #findOutliers(FMatrixRmaj, double)}. 
	 * @return the range [min, max] of the non-outlier part of the 
	 * waveform sent to {@link #findOutliers(FMatrixRmaj, double)}
	 */
	public static float[] getNonOutlierRange() {
		return Refinements.nonOutlierRange;
	}

	
	private static List<Pair<Float, Pair<Integer, Integer>>> toSortedByValuePositionalArray(FMatrixRMaj waveForm) {
		List<Pair<Float, Pair<Integer, Integer>>> list = new ArrayList<Pair<Float, Pair<Integer, Integer>>>();
		
		for (int i = 0; i < waveForm.getNumRows(); i++) {
			for (int j = 0; j < waveForm.getNumCols(); j++) {
				list.add(new Pair<Float, Pair<Integer, Integer>>(waveForm.get(i, j), new Pair<Integer, Integer>(i, j)));
			}
		}
		
		Collections.sort(list, new Comparator<Pair<Float, Pair<Integer, Integer>>>() {
			@Override
			public int compare(Pair<Float, Pair<Integer, Integer>> o1, Pair<Float, Pair<Integer, Integer>> o2) {
				return Float.compare(o1.first(), o2.first());
			}
		});
		
		return list;
	}

	/**
	 * Clamp any values of the waveForm that fall outside the given [min, max] range 
	 * to that range
	 * @param waveForm
	 * @param range
	 */
	public static void clamp(FMatrixRMaj waveForm, float[] range) {
		for (int i = 0; i < waveForm.getNumRows(); i++) {
			for (int j = 0; j < waveForm.getNumCols(); j++) {
				if (waveForm.unsafe_get(i, j) < range[0]) {
					waveForm.unsafe_set(i, j, range[0]);
				} else if (waveForm.unsafe_get(i, j) > range[1]) {
					waveForm.unsafe_set(i, j, range[1]);
				}
			}
		}
	}

	/**
	 * Sets the outliers value from <code>outliers</code> in the given matrix
	 * @param outliers
	 * @param waveForm
	 */
	public static void addOutliersBack(List<Pair<Float, Pair<Integer, Integer>>> outliers, FMatrixRMaj waveForm) {
		for (Pair<Float, Pair<Integer, Integer>> p: outliers) {
			waveForm.unsafe_set(p.second().first(), p.second().second(), p.first());
		}
	}
}
