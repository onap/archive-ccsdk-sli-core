package org.onap.ccsdk.sli.core.api;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.DuplicateValueException;

public class DuplicatevalueExceptionTest {
    @Test
    public void DuplicateValueExceptionTest() {
        assertNotNull(new DuplicateValueException());

    }

    @Test
    public void DuplicateValueExceptionTestString() {
        assertNotNull(new DuplicateValueException("JUnit Test"));

    }

    @Test
    public void DuplicateValueExceptionTestStringThrowable() {
        assertNotNull(new DuplicateValueException("JUnit Test", new Exception("JUnit Test")));

    }
}
