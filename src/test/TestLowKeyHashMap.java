package test;

import org.junit.Test;

import com.jypec.util.datastructures.LowKeyHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for the {@link LowKeyHashMap} class
 * @author Daniel
 */
public class TestLowKeyHashMap {
	
	/**
	 * Test the recovery of keys present
	 */
	@Test
	public void testExactRecovery() {
		LowKeyHashMap<Integer, Integer> lkhm = new LowKeyHashMap<Integer, Integer>();
		lkhm.put(0, 1);
		lkhm.put(1, 2);
		lkhm.put(2, 3);
		assertEquals(Integer.valueOf(1), lkhm.get(0));
		assertEquals(Integer.valueOf(2), lkhm.get(1));
		assertEquals(Integer.valueOf(3), lkhm.get(2));
	}
	
	
	/**
	 * Test the recovery of keys lower than the given one, which is not present
	 */
	@Test
	public void testLowerRecovery() {
		LowKeyHashMap<Integer, Integer> lkhm = new LowKeyHashMap<Integer, Integer>();
		lkhm.put(0, 1);
		lkhm.put(8, 9);
		assertEquals(Integer.valueOf(9), lkhm.get(100));
		assertEquals(Integer.valueOf(1), lkhm.get(4));
	}
	
	
	/**
	 * Test the null result of looking for a key that does not exist, and a lower
	 * one is not present
	 */
	@Test
	public void testNullRecovery() {
		LowKeyHashMap<Integer, Integer> lkhm = new LowKeyHashMap<Integer, Integer>();
		assertNull(lkhm.get(5));
		lkhm.put(20, 20);
		assertNull(lkhm.get(5));
		assertEquals(Integer.valueOf(20), lkhm.get(20));
	}
	
	
}
