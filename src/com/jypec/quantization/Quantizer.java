package com.jypec.quantization;

/**
 * This class quantizes samples (from doubles to integers in sign-magnitude form)
 * And dequantizes samples.
 * 
 * Made following chapter 10.5 of David Taubman and Michael Marcellin's "Jpeg2000 Image Compression Fundamentals"
 * @author Daniel
 *
 */
public class Quantizer {
	
	/** exclusive limit */
	private static final int MAX_EXPONENT = 0x1<<5;
	/** exclusive limit */
	private static final int MAX_MANTISSA = 0x1<<11;
	/** inclusive limit */
	private static final int MAX_GUARD = 7;
	
	/** mask used for dequantization */
	private int signMask;
	/** Quantizer configuration values */
	private int exponent, guard;
	/** Guard used to ensure samples stay in bounds */
	private double lowerGuard, upperGuard;
	/** Quantity that decides the size of the intervals that map to a single integer */
	private double delta;
	/** Used to change the input sample's range to the interval [-1/2, 1/2] before applying the quantizer */
	private double sampleLowerLimit, sampleIntervalLength;
	/** Offset used when dequantizing samples */
	private double reconstructionOffset;
	
	
	
	/**
	 * Create a quantizer
	 * @param exponent basically the number of bits that the quantized output will use, minus one
	 * @param mantissa adjust the interval of quantization with decimal values
	 * @param guard number of guard bits in case samples exceed the limits
	 * @param sampleLowerLimit lower limit of the quantized samples
	 * @param sampleUpperLimit upper limit of the quantized samples
	 * @param reconstructionOffset offset used when reconstructing the values. Usually 0.5 is used to round to the nearest integer, but other values to take
	 * into account the distribution of the differences to the nearest integer might be used such as 0.375
	 */
	public Quantizer(int exponent, int mantissa, int guard, double sampleLowerLimit, double sampleUpperLimit, double reconstructionOffset) {
		//validity checks
		if (exponent < 0 || exponent >= MAX_EXPONENT) {
			throw new IllegalArgumentException("Exponent not in the allowed range");
		}
		if (mantissa < 0 || mantissa >= MAX_MANTISSA) {
			throw new IllegalArgumentException("Mantissa not in the allowed range");
		}
		if (guard < 0 || guard > MAX_GUARD) {
			throw new IllegalArgumentException("Guard not in the allowed range");
		}
		if (sampleLowerLimit >= sampleUpperLimit) {
			throw new IllegalArgumentException("Limits must define a non-empty interval");
		}
		if (reconstructionOffset < 0.0 || reconstructionOffset > 1.0) {
			throw new IllegalArgumentException("Reconstruction offset not in the allowed range");
		}
		//assignments
		this.exponent = exponent;
		this.guard = guard;
		this.signMask = 0x1 << this.getNecessaryBitPlanes();
		if (this.guard == 0) {
			this.lowerGuard = -0.5;
			this.upperGuard = 0.5;
		} else {
			this.upperGuard = (double) (0x1 << this.guard);
			this.lowerGuard = -this.upperGuard;
		}
		this.sampleLowerLimit = sampleLowerLimit;
		this.sampleIntervalLength = sampleUpperLimit - sampleLowerLimit;
		this.reconstructionOffset = reconstructionOffset;
		//set up delta
		long inverseExpFactor = 1l << exponent;
		double expFactor = 1.0 / ((double) inverseExpFactor);
		double trueMantissa = (double) mantissa / (double) MAX_MANTISSA;
		this.delta = expFactor * (1.0 + trueMantissa);
	}
	
	/**
	 * Quantize a sample
	 * @param input sample to be quantized
	 * @param normalizeRange indicates if the input should be normalized (if true, the input is 
	 * normalized to the interval [-0.5, 0.5] using the quantizer setup values
	 * @param canBeNegative indicates if the sample can take negative values, in which case the normlization
	 * process skips centering around zero
	 * @return the quantized value after normalization, see {@link #quantize(double)} for more information
	 */
	public int normalizeAndQuantize(double input) {
		//normalize
		input -= this.sampleLowerLimit;
		input /= this.sampleIntervalLength;
		input -= 0.5;
		//quantize
		return this.quantize(input);
	}
	
	/**
	 * @param input value assumed to be in the interval [-1/2, 1/2] representing the given sample
	 * @return the quantized value in sign-magnitude format according to this quantizer's setup. 
	 * {@link #getNecessaryBitPlanes()} returns the number of magnitude bits that this value uses 
	 */
	private int quantize(double input) {
		//clamp to the guard bit interval
		input = Math.max(Math.min(input, this.upperGuard), this.lowerGuard);
		//get the sign before butchering the input
		int sign = input >= 0.0 ? 0 : 1;
		//round the input up
		input = Math.abs(input);
		input /= this.delta;
		int transformedInput = (int) Math.floor(input);
		//join sign and magnitude together
		return transformedInput | (sign << this.getNecessaryBitPlanes());
	}
	
	/**
	 * Dequantizes the input into an approximation of the original value
	 * @param input the quantized value
	 * @return the unquantized value
	 */
	private double deQuantize(int input) {
		//base case
		if (input == 0) {
			return 0;
		}
		//reconstruct
		int sign = input & this.signMask;
		input &= ~this.signMask;
		double result = (double) input + this.reconstructionOffset;
		result *= this.delta;
		if (sign != 0) {
			result = -result;
		}
		return result;
	}
	
	/**
	 *  
	 * @param input
	 * @return the dequantized value using {@link #deQuantize(int)} then denormalized to this quantizer's interval
	 */
	public double deQuantizeAndDenormalize(int input) {
		double dequantized = this.deQuantize(input);
		dequantized += 0.5;
		dequantized *= this.sampleIntervalLength;
		dequantized += this.sampleLowerLimit;
		return dequantized;
	}
	
	/**
	 * @return the number of MAGNITUDE bits this quantizer uses when returning values
	 */
	public int getNecessaryBitPlanes() {
		return Math.max(0, this.exponent + this.guard - 1);
	}
	
}
