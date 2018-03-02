package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class DBLIBResourceProviderTest {
	
	static DBLIBResourceProvider provider;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		provider = new DBLIBResourceProvider();
	}

	@Test
	public void testDBLIBResourceProvider() {
		assertNotNull(provider);
	}

	@Test
	public void testGetProperties() {
		assertNotNull(provider.getProperties());
	}

	@Test
	public void testDeterminePropertiesFile() {
		assertNotNull(provider.determinePropertiesFile(provider));
	}

}
