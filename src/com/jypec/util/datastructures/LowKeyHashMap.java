package com.jypec.util.datastructures;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Acts as a HashMap but when asked for the value of a key, 
 * if the key is not present, then the value of the previous
 * key stored (according to the natural ordering of <b>K</b>)
 * is returned. <br>
 * Example: If it has the mapping 2->3 and 6->10, a call to
 * {@link #get(Comparable)} for 5 will return 3, since the mapping 2->3 exists
 * @author Daniel
 * @param <K> key type
 * @param <V> value type
 */
public class LowKeyHashMap<K extends Comparable<K>, V> {
	private HashMap<K, V> innerMap;
	private SortedSet<K> keys;


	/**
	 *Default constructor
	 */
	public LowKeyHashMap() {
		this.innerMap = new HashMap<K, V>();
		this.keys = new TreeSet<K>();
	}
	
	/**
	 * Put the specified key-value pair in the mapping.
	 * If the key is already present, the value will 
	 * overwrite the previous
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		this.innerMap.put(key, value);
		this.keys.add(key);
	}
	
	/**
	 * Get the value associated with the given key.
	 * Note that the key might not be present, and
	 * the value from the closest previous key is returned.
	 * @param key
	 * @return the value tied to the given key, or null if not present
	 */
	public V get(K key) {
		K trueKey = this.closestKey(key);
		return this.innerMap.get(trueKey);
	}
	
	/**
	 * @param key
	 * @return the closest lower key stored to the given one, according
	 * to the ordering of <b>K</b>
	 */
	public K closestKey(K key) {
		if (this.keys.contains(key))
			return key;
		SortedSet<K> t = this.keys.headSet(key);
		return t.isEmpty() ? null : t.last();
	}

	/**
	 * @return the size of the underlying {@link HashMap}
	 */
	public int size() {
		return this.innerMap.size();
	}

	/**
	 * @return the entry set of the inner map. Note that, while
	 * {@link #get(Comparable)} might return a value for a key <b>K</b>,
	 * it might not be present on the entry set.
	 */
	public Set<Entry<K, V>> entrySet() {
		return this.innerMap.entrySet();
	}

	/**
	 * @param key
	 * @return true if {@link #get(Comparable)} will return not null for the given key
	 */
	public boolean hasMappingForKey(K key) {
		return this.closestKey(key) != null;
	}

}
