package com.jypec.arithco.predict;

import com.jypec.arithco.ArithmeticCoder;
import com.jypec.arithco.ArithmeticDecoder;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * Wrapper for the {@link ArithmeticCoder} which provides
 * prediction functionality by adding an extra symbol meaning
 * "success in predicting"
 * @author Daniel
 *
 */
public class PredictiveArithmeticCodec {

	private PredictiveFunction pf;
	
	/**
	 * Build a predictive arithmetic coder based on the given predictive function
	 * @param pf
	 */
	public PredictiveArithmeticCodec(PredictiveFunction pf) {
		this.pf = pf;
	}
	
	
	/**
	 * Code the given data into the given stream
	 * @param data the data to be coded
	 * @param numberOfChars the number of different values present in data, 
	 * 		which must range from 0 to numberOfChars - 1
	 * @param bos the output stream
	 */
	public void code(int[] data, int numberOfChars, BitOutputStream bos) {
		//build an arith coder with one more char for encoding right prediction
		ArithmeticCoder ac = new ArithmeticCoder(32, numberOfChars + 1, numberOfChars * 256); //experimental value that works ok
		ac.initialize();
		pf.resetStatistics();
		
		for (int i = 0; i < data.length; i++) {
			if (this.pf.predict(data, i)) { //code success
				ac.code(numberOfChars, bos);
			} else {						//code fail (raw value)
				ac.code(data[i], bos);
			}
		}
		
		ac.finishCoding(bos);
	}
	
	/**
	 * Decode the input stream into the original data
	 * @param numberOfChars number of different values present in the original raw data 
	 * @param bis the stream from where to read 
	 * @return the decoded data
	 */
	public int[] decode(int numberOfChars, BitInputStream bis) {
		//build an arith decoder and decode the raw values
		ArithmeticDecoder ad = new ArithmeticDecoder(32, numberOfChars + 1, numberOfChars * 256);
		int[] data = ad.decode(bis);
		pf.resetStatistics();
		
		for (int i = 0; i < data.length; i++) {
			if (data[i] == numberOfChars) { //if the value was predicted
				pf.predict(data, i); //this is returning false because the prediction doesn't match
									 //the expected value since it is the special value "correct",
									 //but it is needed nonetheless
				//recover prediction
				data[i] = pf.getPrediction();
			}
		}
		return data;
	}
}
