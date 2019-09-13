package org.onap.ccsdk.sli.core.api;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.ConfigurationException;

public class ConfigurationExceptionTest {
    @Test
    public void ConfigurationExceptionTest() {
        assertNotNull(new ConfigurationException());

    }

    @Test
    public void ConfigurationExceptionTestString() {
        assertNotNull(new ConfigurationException("JUnit Test"));

    }

    @Test
    public void ConfigurationExceptionTestStringThrowable() {
        assertNotNull(new ConfigurationException("JUnit Test", new Exception("JUnit Test")));

    }
}
