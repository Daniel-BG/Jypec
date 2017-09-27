package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * @author Daniel
 * Perform all tests for this project, ensuring correct behaviour of the tested elements
 */
@RunWith(Suite.class)
@SuiteClasses({
	TestEBCodec.class,
	TestWaveletTransform.class,
	TestQuantizer.class,
	TestBitStreamReaderWriter.class,
	TestComParametersRecovery.class,
	TestPCARecovery.class,
	TestLowKeyHashMap.class
})


public class AllTests {

	
	
}
