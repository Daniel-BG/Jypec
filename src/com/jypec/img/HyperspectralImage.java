package com.jypec.img;

/**
 * Class that stores the hyperspectral image data as well
 * as the metadata
 * @author Daniel
 */
public class HyperspectralImage {

	private HyperspectralImageData data;
	private ImageHeaderData header;
	
	
	/**
	 * Build this image with the given data and header.
	 * Consistency checks are not done for data metadata and header data 
	 * @param data
	 * @param header
	 */
	public HyperspectralImage(HyperspectralImageData data, ImageHeaderData header) {
		this.data = data;
		this.header = header;
	}

	/**
	 * @return the image header
	 */
	public ImageHeaderData getHeader() {
		return header;
	}
	
	/**
	 * @return the image data
	 */
	public HyperspectralImageData getData() {
		return data;
	}

	/**
	 * Resize the image to the given size
	 * @param bands
	 * @param lines
	 * @param samples
	 */
	public void resize(int bands, int lines, int samples) {
		this.data = this.data.resize(bands, lines, samples);
		this.header.replace(HeaderConstants.HEADER_BANDS, bands);
		this.header.replace(HeaderConstants.HEADER_LINES, lines);
		this.header.replace(HeaderConstants.HEADER_SAMPLES, samples);
	}
}
