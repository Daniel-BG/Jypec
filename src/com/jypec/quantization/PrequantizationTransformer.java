package com.jypec.quantization;

/**
 * Transforms values ahead of the uniform quantization, to possibly
 * make it non-uniform, depending on the transform used. <br>
 * The contract is that <code>value = reverse(forward(value))</code><br>
 * however <code>value = forward(reverse(value))</code> might not hold
 * @author Daniel
 */
public interface PrequantizationTransformer {
	
	/**
	 * @param input the value to be transformed
	 * @return the transformed input
	 */
	public float forward(float input);
	
	/**
	 * @param input the transformed value
	 * @return the original value
	 */
	public float reverse(float input);
}
