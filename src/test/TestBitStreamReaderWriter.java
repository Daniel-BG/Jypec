package test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * @author Daniel
 * Test BitStreamDataReaderWriter
 */
public class TestBitStreamReaderWriter {

	private int testSampleSize = 100;
	
	/** Test if floats are written and read by BitStreamDataReaderWriter */
	@Test
	public void testfloats() {
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(bais);
		BitInputStream input;

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
		
		try {
			for (int i = 0; i < this.testSampleSize; i++) {
				output.writeFloat(data[i]);
			}
			
			input = new BitInputStream(new ByteArrayInputStream(bais.toByteArray()));
			
			for (int i = 0; i < this.testSampleSize; i++) {
				float d = input.readFloat();
				assertTrue("Failed when recovering: " + data[i] + " " + d, Float.compare(d, data[i]) == 0);
			}
			output.close();
			input.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/** Test if floats are written and read by BitStreamDataReaderWriter */
	@Test
	public void testFloats() {
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(bais);
		BitInputStream input;
		
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
		
		try {
			for (int i = 0; i < this.testSampleSize; i++) {
				output.writeFloat(data[i]);
			}
			
			input = new BitInputStream(new ByteArrayInputStream(bais.toByteArray()));
			
			for (int i = 0; i < this.testSampleSize; i++) {
				Float f = input.readFloat();
				assertTrue("Failed when recovering: " + data[i] + " " + f, Float.compare(f, data[i]) == 0);
			}
			input.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/** Test if integers are written and read by BitStreamDataReaderWriter */
	@Test
	public void testInts() {
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(bais);
		BitInputStream input;
		
		Random r = new Random();
		int[] data = new int[this.testSampleSize];
		int[] defData = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};

		for (int i = 0; i < this.testSampleSize; i++) {
			if (i < defData.length)
				data[i] = defData[i];
			else
				data[i] = r.nextInt();
		}
		
		try {
			for (int i = 0; i < this.testSampleSize; i++) {
				output.writeInt(data[i]);
			}
			
			input = new BitInputStream(new ByteArrayInputStream(bais.toByteArray()));
			
			for (int i = 0; i < this.testSampleSize; i++) {
				int rec = input.readInt();
				assertTrue("Failed when recovering: " + data[i] + ", got: " + rec, rec == data[i]);
			}
			input.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test that multiple integer bit depths are correctly saved
	 */
	@Test
	public void testDifferentIntBitDepths() {

		
		Random r = new Random();
		
		for (int depth = 1; depth <= 32; depth++) {
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			BitOutputStream output = new BitOutputStream(bais);
			BitInputStream input;
			
			int[] data = new int[this.testSampleSize];
			int[] defData = {0, 1, 0xffffffff >>> (32 - depth), depth > 1 ? 0xffffffff >>> (33 - depth) : 0, 0x1 << (depth - 1)};

			for (int i = 0; i < this.testSampleSize; i++) {
				if (i < defData.length)
					data[i] = defData[i];
				else
					data[i] = r.nextInt() & (0xffffffff >>> (32 - depth));
			}
			
			try {
				for (int i = 0; i < this.testSampleSize; i++) {
					output.writeNBitNumber(data[i], depth);
				}

				output.paddingFlush();
				input = new BitInputStream(new ByteArrayInputStream(bais.toByteArray()));
				
				for (int i = 0; i < this.testSampleSize; i++) {
					int rec = input.readNBitNumber(depth);
					assertTrue("Failed when recovering: " + data[i] + " at bit depth: " + depth + " got value: " + rec, rec == data[i]);
				}
				input.close();
				output.close();
			} catch (IOException e) {
				assertTrue("Exception was thrown", false);
				e.printStackTrace();
			}
		}
		

	}
	
}
