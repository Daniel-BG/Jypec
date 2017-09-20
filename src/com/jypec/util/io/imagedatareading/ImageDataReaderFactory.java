package com.jypec.util.io.imagedatareading;

import java.nio.ByteBuffer;

import com.jypec.img.HyperspectralImageData;
import com.jypec.img.ImageDataType;
import com.jypec.util.io.IOUtilities;
import com.jypec.util.io.IODataTypes.ByteOrdering;
import com.jypec.util.io.IODataTypes.ImageOrdering;

/**
 * Hyperspectral image reading (only raw data, not headers)
 * @author Daniel
 */
public class ImageDataReaderFactory {

	
	/**
	 * @param imgOrdering 
	 * @param byteOrdering 
	 * @param type 
	 * @return the imageWriter of your liking
	 */
	public static ImageDataReader getReader(ImageOrdering imgOrdering, ByteOrdering byteOrdering, ImageDataType type) {
		
		switch(imgOrdering) {
		case BIL:
			if (type.getBitDepth() % 8 == 0) {
				return new BILByteImageReader(byteOrdering, type.getByteDepth());
			} else {
				return new BILBitImageReader(type.getBitDepth());
			}
		case BIP:
			if (type.getBitDepth() % 8 == 0) {
				return new BIPByteImageReader(byteOrdering, type.getByteDepth());
			} else {
				return new BIPBitImageReader(type.getBitDepth());
			}
		case BSQ:
			if (type.getBitDepth() % 8 == 0) {
				return new BSQByteImageReader(byteOrdering, type.getByteDepth());
			} else {
				return new BSQBitImageReader(type.getBitDepth());
			}
		}
		
		throw new UnsupportedOperationException("The type of writer you requested is not implemented");
	}
	
	
	/**
	 * Useful class frame for writing images which type bit depth is a multiple of 8
	 * @author Daniel
	 *  * @see {@link BitImageReader}
	 */
	private static abstract class ByteImageReader implements ImageDataReader {
		protected ByteOrdering byteOrdering;
		protected int dataBytes;
		
		public ByteImageReader(ByteOrdering byteOrdering, int dataBytes) {
			this.byteOrdering = byteOrdering;
			this.dataBytes = dataBytes;
		}
	}
	
	private static class BIPByteImageReader extends ByteImageReader {
		public BIPByteImageReader(ByteOrdering byteOrdering, int dataBytes) {
			super(byteOrdering, dataBytes);
		}

		@Override
		public void readFromBuffer(ByteBuffer bb, HyperspectralImageData hi) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int k = 0; k < hi.getNumberOfSamples(); k++) {
					for (int i = 0; i < hi.getNumberOfBands(); i++) {
						hi.setDataAt(IOUtilities.getBytes(this.byteOrdering, this.dataBytes, bb), i, j, k);
					}
				}
			}
		}
	}
	
	private static class BILByteImageReader extends ByteImageReader {
		public BILByteImageReader(ByteOrdering byteOrdering, int dataBytes) {
			super(byteOrdering, dataBytes);
		}

		@Override
		public void readFromBuffer(ByteBuffer bb, HyperspectralImageData hi) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int i = 0; i < hi.getNumberOfBands(); i++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						hi.setDataAt(IOUtilities.getBytes(this.byteOrdering, this.dataBytes, bb), i, j, k);
					}
				}
			}
		}
	}
	
	private static class BSQByteImageReader extends ByteImageReader {
		public BSQByteImageReader(ByteOrdering byteOrdering, int dataBytes) {
			super(byteOrdering, dataBytes);
		}

		@Override
		public void readFromBuffer(ByteBuffer bb, HyperspectralImageData hi) {
			for (int i = 0; i < hi.getNumberOfBands(); i++) {
				for (int j = 0; j < hi.getNumberOfLines(); j++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						hi.setDataAt(IOUtilities.getBytes(this.byteOrdering, this.dataBytes, bb), i, j, k);
					}
				}
			}
		}
	}
	
	
	/**
	 * Useful class frame for implementing image readers which bit depth
	 * is NOT a multiple of 8 
	 * @author Daniel
	 * @see {@link ByteImageReader}
	 */
	private static abstract class BitImageReader implements ImageDataReader {
		
		protected int dataBits;
		protected int dataMask;
		protected int acc;
		protected int accBitsLeft;
		
		public BitImageReader(int dataBits) {
			if (dataBits < 1 || dataBits > 25) { //25+7=32 more wont fit in an integer
				throw new IllegalArgumentException("Cannot work with that size");
			}
			this.dataBits = dataBits;
			this.dataMask = (-1) >>> (32 - this.dataBits);
			this.accBitsLeft = 32;
		}
	}
	
	private static class BIPBitImageReader extends BitImageReader {
		public BIPBitImageReader(int dataBits) {
			super(dataBits);
		}
		
		@Override
		public void readFromBuffer(ByteBuffer bb, HyperspectralImageData hi) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int k = 0; k < hi.getNumberOfSamples(); k++) {
					for (int i = 0; i < hi.getNumberOfBands(); i++) {
						hi.setDataAt(getBits(this, bb), i, j, k);
					}
				}
			}
		}
	}
	
	private static class BILBitImageReader extends BitImageReader {
		public BILBitImageReader(int dataBits) {
			super(dataBits);
		}
		
		@Override
		public void readFromBuffer(ByteBuffer bb, HyperspectralImageData hi) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int i = 0; i < hi.getNumberOfBands(); i++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						hi.setDataAt(getBits(this, bb), i, j, k);
					}
				}
			}
		}
	}
	
	private static class BSQBitImageReader extends BitImageReader {
		public BSQBitImageReader(int dataBits) {
			super(dataBits);
		}
		
		@Override
		public void readFromBuffer(ByteBuffer bb, HyperspectralImageData hi) {
			for (int i = 0; i < hi.getNumberOfBands(); i++) {
				for (int j = 0; j < hi.getNumberOfLines(); j++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						hi.setDataAt(getBits(this, bb), i, j, k);
					}
				}
			}
		}
	}
	
	
	/**
	 * Gets the next set of bits from the bytebuffer and returns it.
	 * This is limited by implementation to reading a max of 25 bit
	 * words. If more are needed the implementation has to change, though
	 * right now (c.2017) i don't know of any hyperspectral images with
	 * bit depths that high.
	 * @param bir
	 * @param bb
	 * @return the newly read sample. bir's accumulator is updated so that
	 * future reads remain consistent
	 */
	private static int getBits(BitImageReader bir, ByteBuffer bb) {
		int toRead = Math.min(4 - (bir.accBitsLeft / 8), bb.remaining());
		bir.accBitsLeft -= toRead << 3;
		int read = IOUtilities.getLeftBytes(toRead, bb);
		bir.acc |= read >>> (32 - bir.accBitsLeft);
		int result = bir.acc >>> (32 - bir.dataBits);
		bir.acc <<= bir.dataBits;
		bir.accBitsLeft += bir.dataBits;
		return result & bir.dataMask;
	}

}
