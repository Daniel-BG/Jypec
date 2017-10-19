package com.jypec.wavelet;

import java.io.IOException;

import com.jypec.cli.InputArguments;
import com.jypec.quantization.LogPrequantization;
import com.jypec.quantization.PrequantizationTransformer;
import com.jypec.quantization.SplitLinearSqrtPrequantization;
import com.jypec.quantization.SqrtPrequantization;
import com.jypec.temp.ClassLogger.LoggerParameter;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStreamTree;

/**
 * Transforms previous to the quantization allowing for non-uniform transforms
 * @author Daniel
 */
public class PrequantizationTransform implements BidimensionalWavelet {
	
	private PrequantizationTransformer transformer;
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
		PREQUANT_SPLIT_LINEAR_SQRT
	}
	
	/**
	 * @param transformer the prequantization transformer to use
	 */
	private PrequantizationTransform(PrequantizationTypes type, PrequantizationTransformer transformer) {
		this.transformer = transformer;
		this.type = type;
	}

	@Override
	public void forwardTransform(float[][] s, int width, int height) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				s[i][j] = this.transformer.forward(s[i][j]);
			}
		}
	}

	@Override
	public void reverseTransform(float[][] s, int width, int height) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				s[i][j] = this.transformer.reverse(s[i][j]);
			}
		}
	}
	
	/**
	 * @param bis
	 * @return the {@link PrequantizationTransform} read from the stream, ready to go
	 * @throws IOException
	 */
	public static PrequantizationTransform loadFrom(BitInputStream bis) throws IOException {
		PrequantizationTypes type = PrequantizationTypes.class.cast(bis.readEnum(PrequantizationTypes.class, true));
		switch(type) {
		case PREQUANT_LOG:
			return new PrequantizationTransform(type, new LogPrequantization(bis.readFloat()));
		case PREQUANT_SPLIT_LINEAR_SQRT:
			return new PrequantizationTransform(type, new SplitLinearSqrtPrequantization(bis.readFloat(), bis.readFloat()));
		case PREQUANT_SQRT:
			return new PrequantizationTransform(type, new SqrtPrequantization(bis.readFloat()));
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	public PrequantizationTransform loadFrom(InputArguments ia) {
		
	}
	
	public PrequantizationTransform saveTo(BitOutputStreamTree bost) {
		
	}
}

