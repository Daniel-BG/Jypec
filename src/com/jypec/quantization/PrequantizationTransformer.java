package com.jypec.quantization;

import java.io.IOException;
import com.jypec.cli.InputArguments;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Transforms values ahead of the uniform quantization, to possibly
 * make it non-uniform, depending on the transform used. <br>
 * The contract is that <code>value = reverse(forward(value))</code><br>
 * however <code>value = forward(reverse(value))</code> might not hold
 * @author Daniel
 */
public abstract class PrequantizationTransformer {
	
	private PrequantizationTypes type;
	
	/**
	 * Lists implementations of {@link PrequantizationTransformer}
	 * @author Daniel
	 */
	public enum PrequantizationTypes {
		/** {@link LogPrequantization}*/
		PREQUANT_LOG,
		/** {@link SqrtPrequantization} */
		PREQUANT_SQRT,
		/** {@link SplitLinearSqrtPrequantization} */
		PREQUANT_SPLIT_LINEAR_SQRT,
		/** {@link NoPrequantization}*/
		PREQUANT_NONE
	}
	
	/**
	 * @param type the type of this prequantization (used to load/store)
	 */
	public PrequantizationTransformer(PrequantizationTypes type) {
		this.type = type;
	}
	
	/**
	 * @param input the value to be transformed
	 * @return the transformed input
	 */
	public abstract float forward(float input);
	
	/**
	 * @param input the transformed value
	 * @return the original value
	 */
	public abstract float reverse(float input);
	
	/**
	 * Train this {@link PrequantizationTransformer} so it can analyze the data 
	 * prior to transforming it with {@link #forwardTransform(float[][], int, int)}
	 * @param s
	 * @param width
	 * @param height
	 */
	public abstract void train(float[][] s);
	
	/**
	 * Prequantize the given matrix
	 * @param s
	 * @param width
	 * @param height
	 */
	public void forwardTransform(float[][] s, int width, int height) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				s[i][j] = this.forward(s[i][j]);
			}
		}
	}

	/**
	 * Undo prequantization of the given matrix
	 * @param s
	 * @param width
	 * @param height
	 */
	public void reverseTransform(float[][] s, int width, int height) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				s[i][j] = this.reverse(s[i][j]);
			}
		}
	}
	
	/**
	 * @param bost
	 * @throws IOException
	 */
	public void saveTo(BitOutputStreamTree bost) throws IOException {
		bost.addChild("code").writeByte((byte) this.type.ordinal());
		this.doSaveTo(bost.addChild("data"));
	}
	
	/**
	 * Save to the given BitOutputStreamTree
	 * @param bost
	 * @throws IOException if an I/O error occurs
	 */
	public abstract void doSaveTo(BitOutputStreamTree bost) throws IOException;
	
	/**
	 * @param bis where to read from
	 * @return the {@link PrequantizationTransformer} read from the stream
	 * @throws IOException if an I/O exception occurs
	 */
	public static PrequantizationTransformer loadFrom(BitInputStream bis) throws IOException {
		PrequantizationTypes type = PrequantizationTypes.class.cast(bis.readEnum(PrequantizationTypes.class, true));
		switch(type) {
		case PREQUANT_LOG:
			return new LogPrequantization(bis.readFloat());
		case PREQUANT_SPLIT_LINEAR_SQRT:
			return new SplitLinearSqrtPrequantization(bis.readFloat(), bis.readFloat());
		case PREQUANT_SQRT:
			return new SqrtPrequantization(bis.readFloat());
		case PREQUANT_NONE:
			return new NonePrequantization();
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * @param args
	 * @return the {@link PrequantizationTransformer} specified in <code>args</code>
	 */
	public static PrequantizationTransformer loadFrom(InputArguments args) {
		//TODO this.type = ia.something
		if (args.requestPrequantization) {
			if (args.prequantizationArgs == null || args.prequantizationArgs.length < 1) {
				throw new IllegalArgumentException("Need at least the name of the prequantization algorithm");
			}
			
			switch(args.prequantizationArgs[0].toLowerCase()) {
			case "log":
				return new LogPrequantization();
			case "sqrt":
				return new SqrtPrequantization();
			case "split":
				return new SplitLinearSqrtPrequantization();
			case "none":
				return new NonePrequantization();
			default:
				throw new UnsupportedOperationException("The algorithm: " + args.prequantizationArgs[0] + " requested is not available");
			}
		}
		
		return new NonePrequantization();
	}
		
}
