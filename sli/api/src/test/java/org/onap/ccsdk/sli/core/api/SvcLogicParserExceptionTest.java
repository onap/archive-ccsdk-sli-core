package org.onap.ccsdk.sli.core.api;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicParserException;

public class SvcLogicParserExceptionTest {

	@Test
	public void SvcLogicParserExceptionTest() {
		assertNotNull(new SvcLogicParserException());
	
	}
	
	@Test
	public void SvcLogicParserExceptionTestString() {
		assertNotNull(new SvcLogicParserException("JUnit Test"));
	
	}
	
	@Test
	public void SvcLogicParserExceptionTestThrowable() {
		assertNotNull(new SvcLogicParserException(new Exception("JUnit Test")));
	
	}
	
	@Test
	public void SvcLogicParserExceptionTestStringThrowable() {
		assertNotNull(new SvcLogicParserException("JUnit Test", new Exception("JUnit Test")));
	
	}


}
