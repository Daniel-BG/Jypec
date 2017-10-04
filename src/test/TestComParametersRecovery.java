package test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.jypec.comdec.ComParameters;
import com.jypec.dimreduction.alg.DeletingDimensionalityReduction;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;
import com.jypec.util.datastructures.LowKeyHashMap;

/**
 * @author Daniel
 * Test compressor parameters class
 */
public class TestComParametersRecovery {

	
	/**
	 * Test if the Compressor Parameters object is able to save itself
	 * and reload from a BitStream
	 */
	@Test
	public void testComParametersRecovery() {
		ComParameters cp = new ComParameters();
		ComParameters cpr = new ComParameters();

		Random r = new Random(0);
		
		for (int i = 0; i < 100; i++) {
			BitOutputStreamTree bost = new BitOutputStreamTree(null, false);
			BitInputStream input;
			
			cp.wavePasses = r.nextInt(0x100);
			cp.bits = r.nextInt(0x100);
			LowKeyHashMap<Integer, Integer> hm = new LowKeyHashMap<Integer, Integer>();
			hm.put(0, 20);
			hm.put(5, 6);
			cp.shaveMap = hm;
			cp.dr = new DeletingDimensionalityReduction();
			
			try {
				cp.saveTo(bost);
				bost.paddingFlush();
				input = bost.bis;
				cpr.loadFrom(input);
				
				assertTrue("Compressor parameters do not equal each other", cp.equals(cpr));
				assertTrue("The bitstream still has bits left", input.available() == 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
