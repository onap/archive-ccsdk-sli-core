package org.onap.ccsdk.sli.core.api;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.exceptions.DuplicateValueException;

public class DuplicatevalueExceptionTest {
    @Test
    public void duplicateValueExceptionTest() {
        assertNotNull(new DuplicateValueException());
    }

    @Test
    public void duplicateValueExceptionTestString() {
        assertNotNull(new DuplicateValueException("JUnit Test"));
    }

    @Test
    public void duplicateValueExceptionTestStringThrowable() {
        assertNotNull(new DuplicateValueException("JUnit Test", new Exception("JUnit Test")));
    }
}
