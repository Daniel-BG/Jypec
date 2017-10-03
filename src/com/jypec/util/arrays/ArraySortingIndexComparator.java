package com.jypec.util.arrays;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Class that allows the sorting of an integer array which
 * represents indices for another comparable array which
 * we do not want in principle to sort
 * @author Daniel. Modified from <a href="https://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-after-sorting">here</a>
 *
 * @param <T> type of the source array of which we want to find
 * the sorted indices
 */
public class ArraySortingIndexComparator<T extends Comparable<T>> implements Comparator<Integer> {

	private T[] array;
	private boolean descending;
	
	/**
	 * Create the comparator, which will be based on the given array
	 * @param array
	 * @param descending if true the order is descending, otherwise ascending
	 */
	public ArraySortingIndexComparator(T[] array, boolean descending) {
		this.array = array;
		this.descending = descending;
	}
	
	/**
	 * @return the index array corresponding to the given array when constructing
	 */
	public Integer[] createIndexArray() {
		Integer[] indices = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			indices[i] = i;
		}
		return indices;
	}
	
	/**
	 * @return the array indicating to which position the original 
	 * objects should move to in order for them to be sorted
	 */
	public Integer[] createSortingArray() {
		Integer[] indices = createIndexArray();
		Arrays.sort(indices, this);
		return indices;
	}

	@Override
	public int compare(Integer index1, Integer index2) {
		if (descending) {
			return array[index2].compareTo(array[index1]);
		} else {
			return array[index1].compareTo(array[index2]);
		}
		
	}
	
	
	
	/**
	 * In case this class wants to be accessed in astatic way
	 * @param baseArray 
	 * @param descending if true the order is descending, otherwise ascending
	 * @return d
	 */
	public static<TT extends Comparable<TT>> Integer[] createSortingArray(TT[] baseArray, boolean descending) {
		ArraySortingIndexComparator<TT> asic = new ArraySortingIndexComparator<TT>(baseArray, descending);
		return asic.createSortingArray();
	}

}