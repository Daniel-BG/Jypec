package com.jypec;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Daniel
 *
 */
public class TestClass extends InputStream {

	@Override
	public int read() throws IOException {
		return 123;
	}

}
