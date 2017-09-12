package com.jypec.util.io.imagewriting;

import java.nio.ByteBuffer;
import com.jypec.img.HyperspectralImage;
import com.jypec.img.ImageDataType;

/**
 * Factory that gets you your desired ImageWriter object
 * @author Daniel
 *
 */
public class ImageWriterFactory {
	
	/**
	 * Defines the order of the image samples in the image file
	 * @author Daniel
	 */
	public enum ImageOrdering {
		/** Band Sequential. Band -> Line -> Sample */
		BSQ, 
		/** Band Interleaved by Pixel. Line -> Sample -> Band */
		BIP, 
		/** Band Interleaved by Line. Line -> Band -> Sample */
		BIL
	};
	
	/**
	 * Ordering of the bytes in the image file. 
	 * Only applies to byte-width data types
	 * @author Daniel
	 *
	 */
	public enum ByteOrdering {
		/** Big endian. Start with the most significant byte in the lowest memory address */
		BIG_ENDIAN, 
		/** Little endian. Start with the least significant byte in the lowest memory address */
		LITTLE_ENDIAN
	};
	
	
	/**
	 * @param imgOrdering 
	 * @param byteOrdering 
	 * @param type 
	 * @return the imageWriter of your liking
	 */
	public static ImageWriter getWriter(ImageOrdering imgOrdering, ByteOrdering byteOrdering, ImageDataType type) {
		
		switch(imgOrdering) {
		case BIL:
			if (type.getBitDepth() % 8 == 0) {
				return new BILByteImageWriter(byteOrdering, type.getByteDepth());
			} else {
				return new BILBitImageWriter(type.getBitDepth());
			}
		case BIP:
			if (type.getBitDepth() % 8 == 0) {
				return new BIPByteImageWriter(byteOrdering, type.getByteDepth());
			} else {
				return new BIPBitImageWriter(type.getBitDepth());
			}
		case BSQ:
			if (type.getBitDepth() % 8 == 0) {
				return new BSQByteImageWriter(byteOrdering, type.getByteDepth());
			} else {
				return new BSQBitImageWriter(type.getBitDepth());
			}
		}
		
		throw new UnsupportedOperationException("The type of writer you requested is not implemented");
	}
	
	/**
	 * Useful class frame for writing images which type bit depth is a multiple of 8
	 * @author Daniel
	 *  * @see {@link BitImageWriter}
	 */
	private static abstract class ByteImageWriter implements ImageWriter {
		protected ByteOrdering byteOrdering;
		protected int dataBytes;
		
		public ByteImageWriter(ByteOrdering byteOrdering, int dataBytes) {
			this.byteOrdering = byteOrdering;
			this.dataBytes = dataBytes;
		}
	}
	
	private static class BIPByteImageWriter extends ByteImageWriter {
		public BIPByteImageWriter(ByteOrdering byteOrdering, int dataBytes) {
			super(byteOrdering, dataBytes);
		}
		
		@Override
		public void writeToBuffer(HyperspectralImage hi, ByteBuffer bb) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int k = 0; k < hi.getNumberOfSamples(); k++) {
					for (int i = 0; i < hi.getNumberOfBands(); i++) {
						putBytes(hi.getDataAt(i, j, k), this.byteOrdering, this.dataBytes, bb);
					}
				}
			}
		}
	}
	
	private static class BILByteImageWriter extends ByteImageWriter {
		public BILByteImageWriter(ByteOrdering byteOrdering, int dataBytes) {
			super(byteOrdering, dataBytes);
		}
		
		@Override
		public void writeToBuffer(HyperspectralImage hi, ByteBuffer bb) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int i = 0; i < hi.getNumberOfBands(); i++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						putBytes(hi.getDataAt(i, j, k), this.byteOrdering, this.dataBytes, bb);
					}
				}
			}
		}
	}
	
	private static class BSQByteImageWriter extends ByteImageWriter {
		public BSQByteImageWriter(ByteOrdering byteOrdering, int dataBytes) {
			super(byteOrdering, dataBytes);
		}
		
		@Override
		public void writeToBuffer(HyperspectralImage hi, ByteBuffer bb) {
			for (int i = 0; i < hi.getNumberOfBands(); i++) {
				for (int j = 0; j < hi.getNumberOfLines(); j++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						putBytes(hi.getDataAt(i, j, k), this.byteOrdering, this.dataBytes, bb);
					}
				}
			}
		}
	}
	
	/**
	 * Useful class frame for implementing image writers which bit depth
	 * is NOT a multiple of 8 
	 * @author Daniel
	 * @see {@link ByteImageWriter}
	 */
	private static abstract class BitImageWriter implements ImageWriter {
		
		protected int dataBits;
		protected int dataMask;
		protected int acc;
		protected int accBits;
		
		public BitImageWriter(int dataBits) {
			if (dataBits < 1 || dataBits > 25) { //25+7=32 more wont fit in an integer
				throw new IllegalArgumentException("Cannot work with that size");
			}
			this.dataBits = dataBits;
			this.dataMask = (-1) >>> (32 - this.dataBits);
		}
	}
	
	private static class BIPBitImageWriter extends BitImageWriter {
		public BIPBitImageWriter(int dataBits) {
			super(dataBits);
		}
		
		@Override
		public void writeToBuffer(HyperspectralImage hi, ByteBuffer bb) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int k = 0; k < hi.getNumberOfSamples(); k++) {
					for (int i = 0; i < hi.getNumberOfBands(); i++) {
						putBits(this, hi.getDataAt(i, j, k), bb);
					}
				}
			}
			emptyBuffer(this, bb);
		}
	}

	
	private static class BILBitImageWriter extends BitImageWriter {
		public BILBitImageWriter(int dataBits) {
			super(dataBits);
		}
		
		@Override
		public void writeToBuffer(HyperspectralImage hi, ByteBuffer bb) {
			for (int j = 0; j < hi.getNumberOfLines(); j++) {
				for (int i = 0; i < hi.getNumberOfBands(); i++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						putBits(this, hi.getDataAt(i, j, k), bb);
					}
				}
			}
			emptyBuffer(this, bb);
		}
	}
	
	private static class BSQBitImageWriter extends BitImageWriter {
		
		public BSQBitImageWriter(int dataBits) {
			super(dataBits);
		}
		
		@Override
		public void writeToBuffer(HyperspectralImage hi, ByteBuffer bb) {
			for (int i = 0; i < hi.getNumberOfBands(); i++) {
				for (int j = 0; j < hi.getNumberOfLines(); j++) {
					for (int k = 0; k < hi.getNumberOfSamples(); k++) {
						putBits(this, hi.getDataAt(i, j, k), bb);
					}
				}
			}
			emptyBuffer(this, bb);
		}
	}

	/**
	 * Puts the specified number of bytes from the value into the buffer, taking into account 
	 * the ordering requested
	 * @param val
	 * @param byteOrdering
	 * @param dataBytes
	 * @param bb
	 */
	private static void putBytes(int val, ByteOrdering byteOrdering, int dataBytes, ByteBuffer bb) {
		if (byteOrdering == ByteOrdering.LITTLE_ENDIAN) {
			val = flipBytes(val, dataBytes);
		} 
		putRightBytes(val, dataBytes, bb);
	}
	
	/**
	 * Empties the specified number of bits from the accumulator
	 * into the buffer, filling with zeroes if necessary
	 * @param acc
	 * @param accBits
	 * @param bb
	 */
	private static void emptyBuffer(BitImageWriter biw, ByteBuffer bb) {
		if (biw.accBits > 0) {
			putLeftBytes(biw.acc, (biw.accBits + 7) % 8, bb);
		}
		biw.acc = 0;
		biw.accBits = 0;
	}
	
	/**
	 * Take the bits from the accumulator (has accBits in it), append the bits 
	 * from the value (has dataBits in it), and drop any fully completed bytes into 
	 * the given buffer. The resulting accumulator is returned. The number of bits 
	 * left in the accumulator is equal to (accBits + dataBits) & 0x7, since any full
	 * bytes are sent to the buffer. The number of bytes that the accumulator has to be
	 * shifted right by is, for the same reason, (accBits + dataBits) & 0xf8; 
	 * All of this values are update in the given bitImageWriter
	 * @param biw
	 * @param val
	 * @param bb
	 */
	private static void putBits(BitImageWriter biw, int val, ByteBuffer bb) {
		biw.acc |= (val & biw.dataMask) << (32 - biw.dataBits - biw.accBits);
		biw.accBits += biw.dataBits;
		int accBytes = biw.accBits >> 3;
		if (accBytes > 0) {
			putLeftBytes(biw.acc, accBytes, bb);
		}
		int bitsLost = (biw.accBits + biw.dataBits) & 0xf8;
		biw.acc <<= bitsLost;
		biw.accBits -= bitsLost;
	}

	/**
	 * Put the leftmost bytes in the buffer (from left to right)
	 * @param val where to take the bytes from
	 * @param bytes number of bytes to take 
	 * @param bb where to put the bytes
	 * @see {@link #putRightBytes(int, int, ByteBuffer)}
	 */
	private static void putLeftBytes(int val, int bytes, ByteBuffer bb) {
		switch (bytes) {
		case 1:
			bb.put((byte) (val >> 24));
			break;
		case 2:
			bb.putShort((short) (val >> 16));
			break;
		case 3:
			bb.put((byte) (val >> 24));
			bb.putShort((short) (val >> 16));
			break;
		case 4:
			bb.putInt(val);
			break;
		}
	}
	
	/**
	 * Put the rightmost bytes in the buffer (from left to right)
	 * @param val where to take the bytes from
	 * @param bytes number of bytes to take 
	 * @param bb where to put the bytes
	 * @see {@link #putLeftBytes(int, int, ByteBuffer)}
	 */
	private static void putRightBytes(int val, int bytes, ByteBuffer bb) {
		switch (bytes) {
		case 1:
			bb.put((byte) val);
			break;
		case 2:
			bb.putShort((short) val);
			break;
		case 3:
			bb.put((byte) (val >> 16));
			bb.putShort((short) val);
			break;
		case 4:
			bb.putInt(val);
			break;
		}
	}
	
	/**
	 * flip the given number of bytes (basically change from lil endian to big endian
	 * and vice versa
	 * @param source integer to be flipped
	 * @param bytes number of bytes to be flipped
	 * @return the flipped integer
	 */
	private static int flipBytes(int source, int bytes) {
		switch(bytes) {
		case 1:
			return source;
		case 2:
			return ((source >> 8) & 0xff) | ((source & 0xff) << 8);
		case 3:
			return (source & 0xff00) | ((source >> 16) & 0xff) | ((source & 0xff) << 16);
		case 4:
			return ((source & 0xff) << 24) | ((source & 0xff00) << 8) | ((source & 0xff0000) >> 8) | ((source >> 24) & 0xff);
		}
		throw new IllegalArgumentException("Cannot work with that size");
	}

}
