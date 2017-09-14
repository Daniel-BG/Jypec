package com.jypec.util.io.headerio;

import java.util.HashMap;

import com.jypec.util.io.headerio.primitives.ArrayValueCompressorDecompressor;
import com.jypec.util.io.headerio.primitives.ByteValueCompressorDecompressor;
import com.jypec.util.io.headerio.primitives.FloatValueCompressorDecompressor;
import com.jypec.util.io.headerio.primitives.IntegerValueCompressorDecompressor;
import com.jypec.util.io.headerio.primitives.StringValueCompressorDecompressor;
import com.jypec.util.io.headerio.primitives.ValueCompressorDecompressor;

/**
 * Class storing various header constants<br>
 * Check http://harrisgeospatial.com/docs/ENVIHeaderFiles.html for more info
 * <br>
 * <br>
 * Do NOT change the order to ensure backwards compatibility. IF something is no
 * longer use, at most substitute for dummy enum
 * 
 * @author Daniel
 */
public enum HeaderConstants {

	/** Time of picture in ISO-8601 format */
	HEADER_ACQ_TIME,
	/** Array of names of image bands. Format: {B1name, B2name, ...} */
	HEADER_BAND_NAMES,
	/** Number of bands in the image */
	HEADER_BANDS,
	/**
	 * Bad band multiplier for each band. 0 for bad 1 for good typically {BB1,
	 * BB2, ...}
	 */
	HEADER_BBL,
	/** Byte order of the data. 0 for lil endian 1 for big endian */
	HEADER_BYTE_ORDER,
	/**
	 * Array of RGB integers referring to {@link #HEADER_CLASS_NAMES}. Size is 3
	 * * {@link #HEADER_CLASSES}
	 */
	HEADER_CLASS_LOOKUP,
	/** Name of classes {@link #HEADER_CLASSES} */
	HEADER_CLASS_NAMES,
	/** Number of classes, including unclassified */
	HEADER_CLASSES,
	/** Percentage of cloud cover */
	HEADER_CLOUD_COVER,
	/**
	 * Complex function to use for complex data. Values include Real, Imaginary,
	 * Power, Magnitude, and Phase
	 */
	HEADER_COMPLEX_FUNC,
	/** Specifies coordinates. Information between braces */
	HEADER_COORD_SYS_STR,
	/** Gain values for each band in W/(m2 * µm * sr). */
	HEADER_DATA_GAIN_VALUES,
	/** Specifies pixels to be ignored for e.g. feature extraction */
	HEADER_DATA_IGNORE_VAL,
	/** Offset values for each band */
	HEADER_DATA_OFFSET_VALUES,
	/** Array of reflectance gain values */
	HEADER_DATA_REF_GAIN_VALUES,
	/** Array of reflectance offset values */
	HEADER_DATA_REF_OFF_VALUES,
	/**
	 * The data type in the image. Options are <br>
	 * The type of data representation:<br>
	 * 1 = Byte: 8-bit unsigned integer<br>
	 * 2 = Integer: 16-bit signed integer<br>
	 * 3 = Long: 32-bit signed integer<br>
	 * 4 = Floating-point: 32-bit single-precision<br>
	 * 5 = Double-precision: 64-bit double-precision floating-point<br>
	 * 6 = Complex: Real-imaginary pair of single-precision floating-point<br>
	 * 9 = Double-precision complex: Real-imaginary pair of double precision
	 * floating-point<br>
	 * 12 = Unsigned integer: 16-bit<br>
	 * 13 = Unsigned long integer: 32-bit<br>
	 * 14 = 64-bit long integer (signed)<br>
	 * 15 = 64-bit unsigned long integer (unsigned)<br>
	 */
	HEADER_DATA_TYPE,
	/** Array of bands to load by default */
	HEADER_DEFAULT_BANDS,
	/** Stretch to use when displaying the image. Various formats allowed */
	HEADER_DEFAULT_STRETCH,
	/**
	 * Index of the dem band. Only indicate if there is more than one band and
	 * it is not the first one, otherwise not needed as it defaults to Zero
	 */
	HEADER_DEM_BAND,
	/** Path of the DEM band file associated */
	HEADER_DEM_FILE,
	/** String describing the image */
	HEADER_DESCRIPTION,
	/** String specifying file type */
	HEADER_FILE_TYPE,
	/**
	 * Set full-width-half-maximum values for each band if the image, units are
	 * the same as {@link #HEADER_WAVELENGTH_UNITS}
	 */
	HEADER_FWHM,
	/** List of up to 16 values indicating corner coordinates */
	HEADER_GEO_POINTS,
	/** Number of bytes to skip to get from the header to the data */
	HEADER_OFFSET,
	/** BSQ, BIP, BIL */
	HEADER_INTERLEAVE,
	/** Number of lines per band */
	HEADER_LINES,
	/** Geographic information in an array */
	HEADER_MAP_INFO,
	/** Pixel size in meters {x, y} */
	HEADER_PIXEL_SIZE,
	/** Info about a projection */
	HEADER_PROJECTION_INFO,
	/** Define a custom file reader */
	HEADER_READ_PROCEDURES,
	/** Reflectance scaling */
	HEADER_REFL_SCALE_FACTOR,
	/** Rational polinomial coefficient (geospatial information) */
	HEADER_RPC_INFO,
	/** Number of samples in the image per line per band */
	HEADER_SAMPLES,
	/** String with security info */
	HEADER_SECURITY_TAG,
	/** Sensor type */
	HEADER_SENSOR_TYPE,
	/** Solar irradiance per band */
	HEADER_SOLAR_IRRADIANCE,
	/** List of spectra names */
	HEADER_SPECTRA_NAMES,
	/** Degrees of azimuth */
	HEADER_SUN_AZIMUTH,
	/** Degrees of elevation */
	HEADER_SUN_ELEVATION,
	/** Wavelengths of each band */
	HEADER_WAVELENGTH,
	/**
	 * Wavelength units: Micrometers, um, Nanometers, nm, Millimeters, mm,
	 * Centimeters, cm, Meters, m, Wavenumber, Angstroms, GHz, MHz, Index,
	 * Unknown
	 */
	HEADER_WAVELENGTH_UNITS,
	/** Coordinate for upper left subpixel */
	HEADER_X_START,
	/** Coordinate for upper left supixel */
	HEADER_Y_START,
	/** Number of pixels in x and y direction to average in z plot */
	HEADER_Z_PLOT_AVG,
	/** Min and max for z_plot */
	HEADER_Z_PLOT_RANGE,
	/** X and Y axis titles for z_plots */
	HEADER_Z_PLOT_TITLES;
	
	//create reverse dictionary
	private static HashMap<String, HeaderConstants> stringValueTranslator;
	static {
		stringValueTranslator = new HashMap<String, HeaderConstants>();
		for (HeaderConstants hc: HeaderConstants.values()) {
			stringValueTranslator.put(hc.toString(), hc);
		}
	}
	

	@Override
	public String toString() {
		switch (this) {
		case HEADER_ACQ_TIME:
			return "acquisition time";
		case HEADER_BAND_NAMES:
			return "band names";
		case HEADER_BANDS:
			return "bands";
		case HEADER_BBL:
			return "bbl";
		case HEADER_BYTE_ORDER:
			return "byte order";
		case HEADER_CLASS_LOOKUP:
			return "class lookup";
		case HEADER_CLASS_NAMES:
			return "class names";
		case HEADER_CLASSES:
			return "classes";
		case HEADER_CLOUD_COVER:
			return "cloud cover";
		case HEADER_COMPLEX_FUNC:
			return "complex function";
		case HEADER_COORD_SYS_STR:
			return "coordinate system string";
		case HEADER_DATA_GAIN_VALUES:
			return "data gain values";
		case HEADER_DATA_IGNORE_VAL:
			return "data ignore value";
		case HEADER_DATA_OFFSET_VALUES:
			return "data offset values";
		case HEADER_DATA_REF_GAIN_VALUES:
			return "data reflectance gain values";
		case HEADER_DATA_REF_OFF_VALUES:
			return "data reflectance offset values";
		case HEADER_DATA_TYPE:
			return "data type";
		case HEADER_DEFAULT_BANDS:
			return "default bands";
		case HEADER_DEFAULT_STRETCH:
			return "default stretch";
		case HEADER_DEM_BAND:
			return "dem band";
		case HEADER_DEM_FILE:
			return "dem file";
		case HEADER_DESCRIPTION:
			return "description";
		case HEADER_FILE_TYPE:
			return "file type";
		case HEADER_FWHM:
			return "fwhm";
		case HEADER_GEO_POINTS:
			return "geo points";
		case HEADER_OFFSET:
			return "header offset";
		case HEADER_INTERLEAVE:
			return "interleave";
		case HEADER_LINES:
			return "lines";
		case HEADER_MAP_INFO:
			return "map info";
		case HEADER_PIXEL_SIZE:
			return "pixel size";
		case HEADER_PROJECTION_INFO:
			return "projection info";
		case HEADER_READ_PROCEDURES:
			return "read procedures";
		case HEADER_REFL_SCALE_FACTOR:
			return "reflectance scale factor";
		case HEADER_RPC_INFO:
			return "rpc info";
		case HEADER_SAMPLES:
			return "samples";
		case HEADER_SECURITY_TAG:
			return "security tag";
		case HEADER_SENSOR_TYPE:
			return "sensor type";
		case HEADER_SOLAR_IRRADIANCE:
			return "solar irradiance";
		case HEADER_SPECTRA_NAMES:
			return "spectra names";
		case HEADER_SUN_AZIMUTH:
			return "sun azimuth";
		case HEADER_SUN_ELEVATION:
			return "sun elevation";
		case HEADER_WAVELENGTH:
			return "wavelength";
		case HEADER_WAVELENGTH_UNITS:
			return "wavelength units";
		case HEADER_X_START:
			return "x start";
		case HEADER_Y_START:
			return "y start";
		case HEADER_Z_PLOT_AVG:
			return "z plot average";
		case HEADER_Z_PLOT_RANGE:
			return "z plot range";
		case HEADER_Z_PLOT_TITLES:
			return "z plot titles";
		default:
			throw new UnsupportedOperationException("This data type is not implemented. Implement it!");
		}
	}
	
	
	/**
	 * @param s
	 * @return the HeaderConstant which corresponds to the given string
	 */
	public static HeaderConstants fromString(String s) {
		return HeaderConstants.stringValueTranslator.get(s);
	}

	/**
	 * @return the byte code of this header constant for use in compressed headers
	 */
	public byte getCode() {
		return (byte) this.ordinal();
	}
	

	/**
	 * @return the compressor decompressor for this header constant. a new one is returned each time
	 * to ensure no overwriting
	 */
	public ValueCompressorDecompressor getValueComDec() {
		switch(this) {
		/* Integer values */
		case HEADER_BANDS:
		case HEADER_CLASSES:
		case HEADER_DEFAULT_BANDS:
		case HEADER_DEM_BAND:
		case HEADER_OFFSET:
		case HEADER_LINES:
		case HEADER_SAMPLES:
			return new IntegerValueCompressorDecompressor();
		/* Float values */
		case HEADER_CLOUD_COVER:
		case HEADER_REFL_SCALE_FACTOR:
		case HEADER_SUN_AZIMUTH:
		case HEADER_SUN_ELEVATION:
		case HEADER_X_START:
		case HEADER_Y_START:
			return new FloatValueCompressorDecompressor();
		/* Byte values */
		case HEADER_BYTE_ORDER:
		case HEADER_DATA_TYPE:
			return new ByteValueCompressorDecompressor();
		/* Array of integers */
		case HEADER_DATA_IGNORE_VAL:
		case HEADER_Z_PLOT_AVG:
			return new ArrayValueCompressorDecompressor(new IntegerValueCompressorDecompressor());
		/* Array of floats */
		case HEADER_BBL:
		case HEADER_DATA_GAIN_VALUES:
		case HEADER_DATA_OFFSET_VALUES:
		case HEADER_DATA_REF_GAIN_VALUES:
		case HEADER_DATA_REF_OFF_VALUES:
		case HEADER_FWHM:
		case HEADER_GEO_POINTS:
		case HEADER_PIXEL_SIZE:
		case HEADER_WAVELENGTH:
		case HEADER_Z_PLOT_RANGE:
			return new ArrayValueCompressorDecompressor(new FloatValueCompressorDecompressor());
		/* Array of bytes */
		case HEADER_CLASS_LOOKUP:
			return new ArrayValueCompressorDecompressor(new ByteValueCompressorDecompressor());
		/* Array of strings */
		case HEADER_BAND_NAMES:
		case HEADER_CLASS_NAMES:
		case HEADER_SPECTRA_NAMES:
		case HEADER_Z_PLOT_TITLES:
			return new ArrayValueCompressorDecompressor(new StringValueCompressorDecompressor());
		/* Strings or other non easily parseable data */
		case HEADER_ACQ_TIME:		//is a date, would need to be parsed
		case HEADER_COMPLEX_FUNC:	//can be enum'd
		case HEADER_COORD_SYS_STR:	//weird coordinate format
		case HEADER_DEFAULT_STRETCH://can be enum'd
		case HEADER_DEM_FILE:
		case HEADER_DESCRIPTION:
		case HEADER_FILE_TYPE:
		case HEADER_INTERLEAVE:		//can be enum'd
		case HEADER_MAP_INFO:		//list of different types
		case HEADER_PROJECTION_INFO:
		case HEADER_READ_PROCEDURES:
		case HEADER_RPC_INFO:
		case HEADER_SECURITY_TAG:
		case HEADER_SENSOR_TYPE:
		case HEADER_SOLAR_IRRADIANCE:
		case HEADER_WAVELENGTH_UNITS://can be enum'd
			return new StringValueCompressorDecompressor();
		default:
			throw new UnsupportedOperationException("This Header constant is not yet implemented");	
		}
		
	}


}
