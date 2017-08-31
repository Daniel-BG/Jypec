package test;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.jypec.util.BitStream;
import com.jypec.util.FIFOBitStream;
import com.jypec.util.io.BitStreamDataReaderWriter;

/**
 * @author Daniel
 * Test BitStreamDataReaderWriter
 */
public class TestBitStreamReaderWriter {

	private int testSampleSize = 100;
	
	/** Test if doubles are written and read by BitStreamDataReaderWriter */
	@Test
	public void testDoubles() {
		BitStream b = new FIFOBitStream();
		BitStreamDataReaderWriter rw = new BitStreamDataReaderWriter();
		rw.setStream(b);
		Random r = new Random();
		double[] data = new double[this.testSampleSize];
		double[] defData = {0d, 1d, -1d, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.MIN_NORMAL};

		for (int i = 0; i < this.testSampleSize; i++) {
			if (i < defData.length)
				data[i] = defData[i];
			else
				data[i] = r.nextDouble();
		}
		
		for (int i = 0; i < this.testSampleSize; i++) {
			rw.writeDouble(data[i]);
		}
		
		for (int i = 0; i < this.testSampleSize; i++) {
			Double d = rw.readDouble();
			assertTrue("Failed when recovering: " + data[i] + " " + d, Double.compare(d, data[i]) == 0);
		}
	}
	
	/** Test if floats are written and read by BitStreamDataReaderWriter */
	@Test
	public void testFloats() {
		BitStream b = new FIFOBitStream();
		BitStreamDataReaderWriter rw = new BitStreamDataReaderWriter();
		rw.setStream(b);
		Random r = new Random();
		float[] data = new float[this.testSampleSize];
		float[] defData = {0f, 1f, -1f, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN,
				Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.MIN_NORMAL};

		for (int i = 0; i < this.testSampleSize; i++) {
			if (i < defData.length)
				data[i] = defData[i];
			else
				data[i] = r.nextFloat();
		}
		
		for (int i = 0; i < this.testSampleSize; i++) {
			rw.writeFloat(data[i]);
		}
		
		for (int i = 0; i < this.testSampleSize; i++) {
			Float f = rw.readFloat();
			assertTrue("Failed when recovering: " + data[i] + " " + f, Float.compare(f, data[i]) == 0);
		}
	}
	
	
	/** Test if integers are written and read by BitStreamDataReaderWriter */
	@Test
	public void testInts() {
		BitStream b = new FIFOBitStream();
		BitStreamDataReaderWriter rw = new BitStreamDataReaderWriter();
		rw.setStream(b);
		Random r = new Random();
		int[] data = new int[this.testSampleSize];
		int[] defData = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};

		for (int i = 0; i < this.testSampleSize; i++) {
			if (i < defData.length)
				data[i] = defData[i];
			else
				data[i] = r.nextInt();
		}
		
		for (int i = 0; i < this.testSampleSize; i++) {
			rw.writeInt(data[i]);
		}
		
		for (int i = 0; i < this.testSampleSize; i++) {
			assertTrue("Failed when recovering: " + data[i], rw.readInt() == data[i]);
		}
	}
	
	
}
