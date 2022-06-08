package com.jypec.img;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;

/**
 * Stores image header data
 * @author Daniel
 */
public class ImageHeaderData {
	
	private boolean wasCompressed = false;
	private ArrayListMultimap<HeaderConstants, Object> data;
	
	/**
	 * Create a new header data
	 */
	public ImageHeaderData() {
		this.data = ArrayListMultimap.create();
	}

	/**
	 * @return true if this header was compressed in its input file
	 */
	public boolean wasCompressed() {
		return wasCompressed;
	}

	/**
	 * @param wasCompressed pass as true to indicate that this header was compressed 
	 * in the input file
	 */
	public void setWasCompressed(boolean wasCompressed) {
		this.wasCompressed = wasCompressed;
	}

	/**
	 * Removes all HeaderConstant-value pairs
	 */
	public void clear() {
		this.data.clear();
	}

	/**
	 * Adds a HeaderConstant-value pair. Will throw an exception
	 * if the added key allows only one object
	 * @param key
	 * @param value
	 */
	public void put(HeaderConstants key, Object value) {
		if (this.data.containsKey(key) && !key.allowDuplicates()) {
			throw new IllegalStateException("Cannot have two pairings for key: " + key);
		}
		this.data.put(key, value);
	}
	
	/**
	 * Remove all values of key, then put (key, value)
	 * @param key
	 * @param value
	 */
	public void replace(HeaderConstants key, Object value) {
		this.data.removeAll(key);
		this.data.put(key, value);
	}

	/**
	 * @return the set of all entries (keys with multiple values
	 * will have multiple entries)
	 */
	public Collection<Entry<HeaderConstants, Object>> entrySet() {
		return this.data.entries();
	}

	/**
	 * @param key the key to look for
	 * @return the value paired with the given key. will throw exception
	 * if the key can be paired with multiple values
	 */
	public Object getOnce(HeaderConstants key) {
		if (key.allowDuplicates()) {
			throw new IllegalArgumentException("The given key cannot allow multiple pairings");
		}
		List<Object> col = this.data.get(key);
		if (col.isEmpty()) {
			return null;
		} else {
			return col.get(0);
		}
	}
	
	/**
	 * @param key
	 * @return a list of all the objects associated with the key
	 */
	public List<Object> getAll(HeaderConstants key) {
		if (!key.allowDuplicates()) {
			throw new IllegalArgumentException("The given key must allow multiple pairings");
		}
		return this.data.get(key);
	}
	
	
	
}
