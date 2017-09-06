package test;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.jypec.comdec.ComParameters;
import com.jypec.util.bits.BitStream;
import com.jypec.util.bits.BitStreamDataReaderWriter;
import com.jypec.util.bits.FIFOBitStream;

/**
 * @author Daniel
 * Test compressor parameters class
 */
public class ComParametersRecoveryTest {

	
	/**
	 * Test if the Compressor Parameters object is able to save itself
	 * and reload from a BitStream
	 */
	@Test
	public void testComParametersRecovery() {
		ComParameters cp = new ComParameters();
		ComParameters cpr = new ComParameters();
		BitStream bs = new FIFOBitStream();
		BitStreamDataReaderWriter bw = new BitStreamDataReaderWriter();
		bw.setStream(bs);
		Random r = new Random(0);
		
		for (int i = 0; i < 100; i++) {
			cp.bands = r.nextInt(0x10000);
			cp.lines = r.nextInt(0x10000);
			cp.samples = r.nextInt(0x10000);
			cp.redBitDepth = r.nextInt(0x100);
			cp.srcBitDepth = r.nextInt(0x100);
			cp.srcSigned = r.nextBoolean();
			cp.wavePasses = r.nextInt(0x100);
			cp.newMaxVal = r.nextDouble();
			
			cp.saveTo(bw);
			cpr.loadFrom(bw);
			
			assertTrue("Compressor parameters do not equal each other", cp.equals(cpr));
			assertTrue("The bitstream still has bits left", bs.getNumberOfBits() == 0);
		}
		
	}
}
